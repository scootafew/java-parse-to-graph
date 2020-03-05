package com.york.sdp518;

import com.york.sdp518.exception.JavaParseToGraphException;
import com.york.sdp518.processor.SpoonProcessor;
import com.york.sdp518.service.impl.MavenPluginService;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ArtifactAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactAnalyser.class);

    MavenPluginService mavenService;

    public ArtifactAnalyser() {
        this.mavenService = new MavenPluginService();
    }

    public void analyseArtifact(String artifact) throws JavaParseToGraphException {
        try {
            Path sourcesPath = getSources(artifact);

            // Process with spoon
            SpoonProcessor processor = new SpoonProcessor();
            processor.process(sourcesPath, "artifact");
        } finally {
            Neo4jSessionFactory.getInstance().close();
        }
    }

    private Path getSources(String artifact) throws JavaParseToGraphException {
        String[] artifactName = artifact.split(":");
        String artifactId = artifactName[1];
        String version = artifactName[2];

        // Download sources from Maven
        File destination = new File("../artifacts/" + artifactId);
        Path destinationPath = Paths.get(destination.toURI()).normalize();
        mavenService.downloadAndCopyArtifactResources(artifact, destinationPath);

        String pomFile = String.format("%s-%s.pom", artifactId, version);
        String sourcesFile = String.format("%s-%s-sources.jar", artifactId, version);
        Path pomPath = destinationPath.resolve(pomFile);
        Path jarPath = destinationPath.resolve(sourcesFile);

        refactorToExpectedStructure(destinationPath, pomPath, jarPath);
        return destinationPath;
    }

    private void refactorToExpectedStructure(Path destinationPath, Path pomPath, Path jarPath) {
        try {
            // Rename pom
            Path pom = destinationPath.resolve(pomPath);
            Files.move(pom, pom.resolveSibling("pom.xml"));

            // Read source directory structure from POM
            String sourceDirectoryPath = getSourceDirectoryPath(destinationPath.resolve("pom.xml").toFile());
            File sourceDirectory = destinationPath.resolve(sourceDirectoryPath).toFile();
            sourceDirectory.mkdirs(); // create source directory structure

            // Unpack jar
            ProcessBuilder processBuilder = new ProcessBuilder("jar", "-xf", jarPath.toString());
            processBuilder.directory(sourceDirectory);
            Process process = processBuilder.start();
            process.waitFor(); // wait for process to finish

            // Delete jar
            Files.delete(jarPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getSourceDirectoryPath(File pomFile) throws IOException, XmlPullParserException {
        // Read source directory structure from POM
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        String sourceDirectory;
        try (FileReader reader = new FileReader(pomFile)) {
            sourceDirectory = pomReader.read(reader).getBuild().getSourceDirectory();
        } catch (FileNotFoundException e) {
            throw new IOException("Pom does not exist.");
        }
        return sourceDirectory != null ? sourceDirectory : "src/main/java";
    }
}
