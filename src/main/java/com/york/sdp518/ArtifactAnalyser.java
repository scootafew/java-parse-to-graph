package com.york.sdp518;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.domain.ProcessingState;
import com.york.sdp518.exception.BuildClasspathException;
import com.york.sdp518.exception.JavaParseToGraphException;
import com.york.sdp518.exception.PomFileException;
import com.york.sdp518.processor.SpoonProcessor;
import com.york.sdp518.service.impl.MavenPluginService;
import com.york.sdp518.service.impl.Neo4jServiceUtils;
import com.york.sdp518.util.SpoonedArtifact;
import com.york.sdp518.util.PomModel;
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

    public void analyseArtifact(String artifactFqn) throws JavaParseToGraphException {
        analyseArtifact(new Artifact(artifactFqn));
    }

    public void analyseArtifact(Artifact artifact) throws JavaParseToGraphException {
        logger.info("Processing artifact {}", artifact.getFullyQualifiedName());
        // Check if repository has already been processed
        Neo4jServiceUtils neo4jService = new Neo4jServiceUtils();
        neo4jService.tryToBeginProcessing(Artifact.class, artifact);

        // if no AlreadyProcessedException thrown, continue
        try {
            processArtifact(artifact);
            artifact.setProcessingState(ProcessingState.COMPLETED);
        } catch (Exception e) {
            artifact.setProcessingState(ProcessingState.FAILED);
            throw e;
        } finally {
            Neo4jSessionFactory.getInstance().getNeo4jSession().save(artifact);
        }
    }

    private void processArtifact(Artifact artifact) throws JavaParseToGraphException {
        Path sourcesPath = getSources(artifact);

        SpoonedArtifact spoonedArtifact = new SpoonedArtifact(sourcesPath, artifact);
        if (spoonedArtifact.classpathNotBuiltSuccessfully()) {
            logger.error("Could not build classpath, exiting...");
            throw new BuildClasspathException("Could not build classpath for artifact");
        }

        // Process with spoon
        spoonProcessor.process(spoonedArtifact);
    }

    private Path getSources(Artifact artifact) throws JavaParseToGraphException {
        String artifactId = artifact.getArtifactId();
        String version = artifact.getVersion();

        // Download sources from Maven
        File destination = new File("../artifacts/" + artifactId);
        Path destinationPath = Paths.get(destination.toURI()).normalize();
        mavenService.downloadAndCopyArtifactResources(artifact.getFullyQualifiedName(), destinationPath);

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

}
