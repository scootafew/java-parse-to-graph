package com.york.sdp518;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.domain.ProcessingState;
import com.york.sdp518.domain.Repository;
import com.york.sdp518.exception.AlreadyProcessedException;
import com.york.sdp518.exception.DependencyManagementServiceException;
import com.york.sdp518.exception.JavaParseToGraphException;
import com.york.sdp518.exception.PartialProcessingFailureException;
import com.york.sdp518.processor.SpoonProcessor;
import com.york.sdp518.service.VCSClient;
import com.york.sdp518.service.impl.MavenDependencyManagementService;
import com.york.sdp518.service.impl.Neo4jServiceUtils;
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

    @Autowired
    public RepositoryAnalyser(VCSClient vcsClient, MavenDependencyManagementService mavenService,
                              ArtifactAnalyser artifactAnalyser, SpoonProcessor spoonProcessor) {
        this.vcsClient = vcsClient;
        this.mavenService = mavenService;
        this.artifactAnalyser = artifactAnalyser;
        this.spoonProcessor = spoonProcessor;
    }

    public void analyseRepository(String uri) throws JavaParseToGraphException {
        // TODO Check version as well, might want to use flag instead and create repo first in db to account for partial parsing
        // Check if repository has already been processed
        Neo4jServiceUtils neo4jService = new Neo4jServiceUtils();
        Repository repo = neo4jService.tryToBeginProcessing(Repository.class, new Repository(uri));

        // if no AlreadyProcessedException thrown, continue
        try {
            cloneAndProcess(repo);
            repo.setProcessingState(ProcessingState.COMPLETED);
        } catch (Exception e) {
            repo.setProcessingState(ProcessingState.FAILED);
            throw e;
        } finally {
            Neo4jSessionFactory.getInstance().getNeo4jSession().save(repo);
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

    private void processAsRepository(SpoonedRepository spoonedRepository, Repository repository) throws JavaParseToGraphException {
        logger.info("Processing project {} as repository", spoonedRepository.getProjectName());
        if (spoonedRepository.classpathNotBuiltSuccessfully()) {
            mavenService.build(spoonedRepository.getRootPomFile(), false);
            spoonedRepository.rebuildClasspath();
        }

        // print discovered artifacts and dependencies
        spoonedRepository.printArtifacts();
        spoonedRepository.printDependencies();

        // Process with spoon
        spoonProcessor.process(spoonedRepository);

        spoonedRepository.getArtifacts().forEach(artifact -> artifact.setProcessingState(ProcessingState.COMPLETED));
        repository.addAllArtifacts(spoonedRepository.getArtifacts());
    }

    private void processAsLibrary(SpoonedRepository spoonedRepository, Repository repository) throws JavaParseToGraphException {
        logger.info("Processing project {} as library", spoonedRepository.getProjectName());

        Set<PomModel> jarPackagedArtifacts = spoonedRepository.getAllModules(Collections.singletonList(Packaging.JAR));

        Set<Artifact> processedArtifacts = jarPackagedArtifacts.stream()
                .map(this::getArtifactToProcess)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::processArtifact)
                .collect(Collectors.toSet());

        processedArtifacts.forEach(repository::addArtifact);
        long failedCount = processedArtifacts.stream()
                .filter(artifact -> artifact.getProcessingState().equals(ProcessingState.FAILED))
                .count();

        if (failedCount > 0) {
            if (failedCount == processedArtifacts.size()) {
                throw new JavaParseToGraphException("Processing failed for all artifacts in repository");
            }
            String message = String.format("Processing failed for %s/%s artifacts in repository",
                    failedCount, processedArtifacts.size());
            throw new PartialProcessingFailureException(message);
        }
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

//    @Deprecated
//    private String checkVersionAlignment(Path projectPath) throws JavaParseToGraphException {
//        File pomFile = projectPath.resolve("pom.xml").toFile();
//        PomModel pomModel = new PomModel(pomFile);
//
//        String groupId = pomModel.getGroupId();
//        String artifactId = pomModel.getArtifactId();
//
//        String localProjectVersion = mavenService.getProjectVersion(pomFile);
//
//        // If project is on maven central, modules may depend on each other so ensure versioning is consistent
//        try {
//            String latestVersion = mavenService.getLatestVersion(groupId, artifactId);
//
//            logger.info("Local project version is: {}, latest version is {}", localProjectVersion, latestVersion);
//
//            if (!localProjectVersion.equals(latestVersion)) {
//                mavenService.setVersion(pomFile, latestVersion);
//                return latestVersion;
//            }
//        } catch (MavenMetadataException e) {
//            logger.info(e.getMessage());
//        }
//
//        return localProjectVersion;
//    }
}
