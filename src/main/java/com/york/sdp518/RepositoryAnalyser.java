package com.york.sdp518;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.domain.ProcessingState;
import com.york.sdp518.domain.Repository;
import com.york.sdp518.exception.AlreadyProcessedException;
import com.york.sdp518.exception.DependencyManagementServiceException;
import com.york.sdp518.exception.JavaParseToGraphException;
import com.york.sdp518.processor.SpoonProcessor;
import com.york.sdp518.service.VCSClient;
import com.york.sdp518.service.impl.MavenDependencyManagementService;
import com.york.sdp518.service.impl.Neo4jServiceFactory;
import com.york.sdp518.service.impl.ProcessableNeo4jService;
import com.york.sdp518.util.OutputUtils;
import com.york.sdp518.util.SpoonedRepository;
import com.york.sdp518.util.Packaging;
import com.york.sdp518.util.PomModel;
import com.york.sdp518.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RepositoryAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryAnalyser.class);

    private VCSClient vcsClient;
    private MavenDependencyManagementService mavenService;
    private ArtifactAnalyser artifactAnalyser;
    private SpoonProcessor spoonProcessor;
    private ProcessableNeo4jService<Repository> neo4jService;

    @Autowired
    public RepositoryAnalyser(VCSClient vcsClient, MavenDependencyManagementService mavenService,
                              ArtifactAnalyser artifactAnalyser, SpoonProcessor spoonProcessor,
                              Neo4jServiceFactory neo4jServiceFactory) {
        this.vcsClient = vcsClient;
        this.mavenService = mavenService;
        this.artifactAnalyser = artifactAnalyser;
        this.spoonProcessor = spoonProcessor;
        this.neo4jService = neo4jServiceFactory.getServiceForProcessableEntity(Repository.class);
    }

    public void analyseRepository(String uri) throws JavaParseToGraphException {
        // Check if repository has already been processed
        Repository repo = neo4jService.tryToBeginProcessing(new Repository(uri));

        // if no AlreadyProcessedException thrown, continue
        try {
            cloneAndProcess(repo);
            repo.setProcessingState(ProcessingState.COMPLETED);
        } catch (Exception e) {
            repo.setProcessingState(ProcessingState.FAILED);
            throw e;
        } finally {
            neo4jService.createOrUpdate(repo);
        }
    }

    private void cloneAndProcess(Repository repository) throws JavaParseToGraphException {
        // Git clone
        File cloneDestination = vcsClient.clone(repository.getUrl());
        Path projectDirectory = Paths.get(cloneDestination.toURI()).normalize();

        // Expect path to root POM for all projects found in repository (likely one, maybe more)
        Collection<Path> directoriesWithPom = Utils.getDirectoriesWithPom(projectDirectory);

        for (Path path : directoriesWithPom) {
            SpoonedRepository spoonedRepository = new SpoonedRepository(path, repository.getUrl(), mavenService);
            PomModel pom = spoonedRepository.getRootPom();

            if (mavenService.isPublishedArtifact(pom.getGroupId(), pom.getArtifactId())) {
                processAsLibrary(spoonedRepository, repository);
            } else {
                processAsRepository(spoonedRepository, repository);
            }
        }

    }

    private void processAsRepository(SpoonedRepository spoonedRepository, Repository repository) {
        logger.info("Processing project {} as repository", spoonedRepository.getProjectName());

        // print discovered dependencies (not artifacts as repository is not a library therefore is not reused)
        spoonedRepository.printDependencies();

        // Process with spoon
        spoonProcessor.process(spoonedRepository);

        spoonedRepository.getAllModules().stream()
                .filter(pomModel -> !pomModel.getPackaging().equals(Packaging.POM))
                .map(PomModel::asArtifact)
                .forEach(artifact -> {
                    artifact.setProcessingState(ProcessingState.COMPLETED);
                    repository.addArtifact(artifact);
                });
    }

    private void processAsLibrary(SpoonedRepository spoonedRepository, Repository repository) {
        logger.info("Processing project {} as library", spoonedRepository.getProjectName());

        Set<Artifact> artifactsToProcess = spoonedRepository.getAllModules(Collections.singletonList(Packaging.JAR))
                .stream()
                .map(this::getArtifactToProcess)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        artifactsToProcess.forEach(OutputUtils::printArtifact); // output all so other instances can contribute if free

        artifactsToProcess.stream()
                .map(this::processArtifact)
                .forEach(repository::addArtifact);
    }

    private Optional<Artifact> getArtifactToProcess(PomModel pomModel) {
        try {
            String version = mavenService.getLatestVersion(pomModel.getGroupId(), pomModel.getArtifactId());
            String fqn = String.join(":", pomModel.getGroupId(), pomModel.getArtifactId(), version);

            return Optional.of(new Artifact(fqn));
        } catch (DependencyManagementServiceException e) {
            logger.info("No published artifact found for {}, skipping...", pomModel.getArtifactId());
        }
        return Optional.empty();
    }

    private Artifact processArtifact(Artifact artifact) {
        try {
            artifactAnalyser.analyseArtifact(artifact);
        } catch (AlreadyProcessedException e) {
            logger.info("Artifact {} has already been processed, skipping...", artifact.getArtifactId());
        } catch (JavaParseToGraphException e) {
            logger.warn("Processing failed for artifact {}", artifact.getArtifactId());
        }
        return artifact;
    }

}
