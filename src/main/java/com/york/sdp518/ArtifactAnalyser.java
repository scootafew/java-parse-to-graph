package com.york.sdp518;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.exception.AlreadyProcessedException;
import com.york.sdp518.exception.BuildClasspathException;
import com.york.sdp518.exception.JavaParseToGraphException;
import com.york.sdp518.exception.PomFileException;
import com.york.sdp518.processor.SpoonProcessor;
import com.york.sdp518.service.impl.MavenPluginService;
import com.york.sdp518.util.SpoonedArtifact;
import com.york.sdp518.util.PomModel;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ArtifactAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactAnalyser.class);

    private MavenPluginService mavenService;
    private SpoonProcessor spoonProcessor;

    public ArtifactAnalyser() {
        this.mavenService = new MavenPluginService();
        this.spoonProcessor = new SpoonProcessor();
    }

    public Artifact analyseArtifact(String artifactFqn) throws JavaParseToGraphException {
        logger.info("Processing artifact {}", artifactFqn);
        // Check if repository has already been processed
        Session neo4jSession = Neo4jSessionFactory.getInstance().getNeo4jSession();
        Artifact artifact = neo4jSession.load(Artifact.class, artifactFqn);
        if (artifact == null) {
            artifact = processArtifact(artifactFqn);
        } else {
            throw new AlreadyProcessedException("Artifact has already been processed");
        }
        return artifact;
    }

    private Artifact processArtifact(String artifactFqn) throws JavaParseToGraphException {
        Path sourcesPath = getSources(artifactFqn);

        SpoonedArtifact spoonedArtifact = new SpoonedArtifact(sourcesPath);
        if (spoonedArtifact.classpathNotBuiltSuccessfully()) {
            logger.error("Could not build classpath, exiting...");
            throw new BuildClasspathException("Could not build classpath for artifact");
        }

        // Process with spoon
        spoonProcessor.process(spoonedArtifact);

        Artifact processedArtifact = spoonedArtifact.getArtifact();
        Neo4jSessionFactory.getInstance().getNeo4jSession().save(spoonedArtifact.getArtifact());

        return processedArtifact;
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
//        mavenService.setVersion(destinationPath.resolve("pom.xml").toFile(), version);
        return destinationPath;
    }

    private void refactorToExpectedStructure(Path destinationPath, Path pomPath, Path jarPath) {
        try {
            // Rename pom
            Path pom = destinationPath.resolve(pomPath);
            Files.move(pom, pom.resolveSibling("pom.xml"));

            // Read source directory structure from POM
            Path sourceDirectoryPath = getSourceDirectoryPath(destinationPath.resolve("pom.xml").toFile());
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

    private Path getSourceDirectoryPath(File pomFile) throws PomFileException {
        PomModel pomModel = new PomModel(pomFile);
        if (pomModel.getSourceDirectory() != null) {
            return Paths.get(pomModel.getSourceDirectory());
        }
        return Paths.get("src/main/java");
    }

    private String getArtifactIdFromArtifact(String artifact) {
        return artifact.split(":")[1];
    }

    private String getVersionFromArtifact(String artifact) {
        return artifact.split(":")[2];
    }
}
