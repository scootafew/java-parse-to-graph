package com.york.sdp518;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.domain.Repository;
import com.york.sdp518.exception.JavaParseToGraphException;
import com.york.sdp518.exception.PomFileException;
import com.york.sdp518.processor.SpoonProcessor;
import com.york.sdp518.service.impl.MavenPluginService;
import com.york.sdp518.util.PomModelUtils;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ArtifactAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactAnalyser.class);

    MavenPluginService mavenService;

    public ArtifactAnalyser() {
        this.mavenService = new MavenPluginService();
    }

    public void analyseArtifact(String artifactFqn) throws JavaParseToGraphException {
        try {
            // Check if repository has already been processed
            Session neo4jSession = Neo4jSessionFactory.getInstance().getNeo4jSession();
            Artifact artifact = neo4jSession.load(Artifact.class, artifactFqn);
            if (artifact == null) {
                Path sourcesPath = getSources(artifactFqn);

                // Process with spoon
                SpoonProcessor processor = new SpoonProcessor();
                processor.process(sourcesPath, getVersionFromArtifact(artifactFqn));
            } else {
                logger.info("Artifact has already been processed, exiting...");
            }

        } finally {
            Neo4jSessionFactory.getInstance().close();
        }
    }

    private Path getSources(String artifact) throws JavaParseToGraphException {
        String artifactId = getArtifactIdFromArtifact(artifact);
        String version = getVersionFromArtifact(artifact);

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

    private String getSourceDirectoryPath(File pomFile) throws PomFileException {
        PomModelUtils pomModel = new PomModelUtils(pomFile);
        return pomModel.getBuild().getSourceDirectory();
    }

    private String getArtifactIdFromArtifact(String artifact) {
        return artifact.split(":")[1];
    }

    private String getVersionFromArtifact(String artifact) {
        return artifact.split(":")[2];
    }
}
