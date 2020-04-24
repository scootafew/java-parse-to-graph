package com.york.sdp518;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.domain.ProcessingState;
import com.york.sdp518.exception.BuildClasspathException;
import com.york.sdp518.exception.JavaParseToGraphException;
import com.york.sdp518.processor.SpoonProcessor;
import com.york.sdp518.service.impl.MavenDependencyManagementService;
import com.york.sdp518.service.impl.Neo4jServiceFactory;
import com.york.sdp518.service.impl.ProcessableNeo4jService;
import com.york.sdp518.util.SpoonedArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class ArtifactAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactAnalyser.class);

    private MavenDependencyManagementService mavenService;
    private SpoonProcessor spoonProcessor;
    private ProcessableNeo4jService<Artifact> neo4jService;

    @Autowired
    public ArtifactAnalyser(MavenDependencyManagementService mavenService, SpoonProcessor spoonProcessor,
                            Neo4jServiceFactory neo4jServiceFactory) {
        this.mavenService = mavenService;
        this.spoonProcessor = spoonProcessor;
        this.neo4jService = neo4jServiceFactory.getServiceForProcessableEntity(Artifact.class);
    }

    public void analyseArtifact(String artifactFqn) throws JavaParseToGraphException {
        analyseArtifact(new Artifact(artifactFqn));
    }

    public void analyseArtifact(Artifact artifact) throws JavaParseToGraphException {
        logger.info("Processing artifact {}", artifact.getFullyQualifiedName());
        // Check if artifact has already been processed
        neo4jService.tryToBeginProcessing(artifact);

        // if no AlreadyProcessedException thrown, continue
        try {
            processArtifact(artifact);
            artifact.setProcessingState(ProcessingState.COMPLETED);
        } catch (Exception e) {
            artifact.setProcessingState(ProcessingState.FAILED);
            throw e;
        } finally {
            neo4jService.createOrUpdate(artifact);
        }
    }

    private void processArtifact(Artifact artifact) throws JavaParseToGraphException {
        Path sourcesPath = mavenService.getSources(artifact);

        SpoonedArtifact spoonedArtifact = new SpoonedArtifact(sourcesPath, artifact, mavenService);
        if (spoonedArtifact.classpathNotBuiltSuccessfully()) {
            logger.error("Could not build classpath, exiting...");
            throw new BuildClasspathException("Could not build classpath for artifact");
        }

        // print found dependencies
        spoonedArtifact.printArtifact();
        spoonedArtifact.printDependencies();

        // Process with spoon
        spoonProcessor.process(spoonedArtifact);
    }

}
