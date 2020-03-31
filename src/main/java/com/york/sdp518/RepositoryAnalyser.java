package com.york.sdp518;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.domain.Repository;
import com.york.sdp518.exception.AlreadyProcessedException;
import com.york.sdp518.exception.JavaParseToGraphException;
import com.york.sdp518.exception.MavenMetadataException;
import com.york.sdp518.processor.SpoonProcessor;
import com.york.sdp518.service.VCSClient;
import com.york.sdp518.service.impl.MavenMetadataService;
import com.york.sdp518.service.impl.MavenPluginService;
import com.york.sdp518.util.SpoonedRepository;
import com.york.sdp518.util.Packaging;
import com.york.sdp518.util.PomModel;
import com.york.sdp518.util.Utils;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class RepositoryAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryAnalyser.class);

    private VCSClient vcsClient;
    private MavenPluginService mavenPluginService;
    private MavenMetadataService mavenMetadataService;
    private ArtifactAnalyser artifactAnalyser;

    public RepositoryAnalyser(VCSClient vcsClient) {
        this.vcsClient = vcsClient;
        this.mavenPluginService = new MavenPluginService();
        this.mavenMetadataService = new MavenMetadataService();
        this.artifactAnalyser = new ArtifactAnalyser();
    }

    public void analyseRepository(String uri) throws JavaParseToGraphException {
        // TODO Check version as well, might want to use flag instead and create repo first in db to account for partial parsing
        // Check if repository has already been processed
        Session neo4jSession = Neo4jSessionFactory.getInstance().getNeo4jSession();
        Repository repo = neo4jSession.load(Repository.class, uri);
        if (repo == null) {
            cloneAndProcess(uri);
        } else {
            throw new AlreadyProcessedException("Repository has already been processed");
        }

    }

    private void cloneAndProcess(String remoteUrl) throws JavaParseToGraphException {
        // Git clone
        File cloneDestination = vcsClient.clone(remoteUrl);
        Path projectDirectory = Paths.get(cloneDestination.toURI()).normalize();

//        Path pathToPomDirectory = getPomDirectory(projectDirectory, projectDirectory.resolve(pathToPom).getParent());

        // Expect path to root POM for all projects found in repository (likely one, maybe more)
        Collection<Path> directoriesWithPom = Utils.getDirectoriesWithPom(projectDirectory);

        for (Path path : directoriesWithPom) {
            SpoonedRepository spoonedRepository = new SpoonedRepository(path, remoteUrl);
            PomModel pom = spoonedRepository.getRootPom();

            if (mavenMetadataService.isPublishedArtifact(pom.getGroupId(), pom.getArtifactId())) {
                spoonedRepository.printDependencies(true);
                processAsLibrary(spoonedRepository);
            } else {
                spoonedRepository.printArtifacts();
                spoonedRepository.printDependencies();
                processAsRepository(spoonedRepository);
            }
        }

    }

    private void processAsRepository(SpoonedRepository spoonedRepository) throws JavaParseToGraphException {
        logger.info("Processing project {} as repository", spoonedRepository.getProjectName());
        if (spoonedRepository.classpathNotBuiltSuccessfully()) {
            mavenPluginService.cleanInstall(spoonedRepository.getRootPomFile(), false);
            spoonedRepository.rebuildClasspath();
        }

        Repository repository = new Repository(spoonedRepository.getRemoteUrl(), spoonedRepository.getProjectName());

        // Process with spoon
        SpoonProcessor processor = new SpoonProcessor();
        processor.process(spoonedRepository);

        repository.addAllArtifacts(spoonedRepository.getArtifacts());
        Neo4jSessionFactory.getInstance().getNeo4jSession().save(repository);
    }

    private void processAsLibrary(SpoonedRepository spoonedRepository) throws JavaParseToGraphException {
        logger.info("Processing project {} as library", spoonedRepository.getProjectName());

        Repository repository = new Repository(spoonedRepository.getRemoteUrl(), spoonedRepository.getProjectName());
        Set<PomModel> jarPackagedArtifacts = spoonedRepository.getAllModules(Collections.singletonList(Packaging.JAR));
        for (PomModel artifact : jarPackagedArtifacts) {
            try {
                String version = mavenMetadataService.getLatestVersion(artifact.getGroupId(), artifact.getArtifactId());
                String fqn = String.join(":", artifact.getGroupId(), artifact.getArtifactId(), version);
                Artifact processedArtifact = artifactAnalyser.analyseArtifact(fqn);

                repository.addAllArtifacts(Collections.singleton(processedArtifact));
            } catch (MavenMetadataException e) {
                logger.info("No published artifact found for {}, skipping...", artifact.getArtifactId());
            } catch (AlreadyProcessedException e) {
                logger.info("Artifact {} has already been processed, skipping...", artifact.getArtifactId());
            }
        }
        Neo4jSessionFactory.getInstance().getNeo4jSession().save(repository);
    }

    @Deprecated
    private String checkVersionAlignment(Path projectPath) throws JavaParseToGraphException {
        File pomFile = projectPath.resolve("pom.xml").toFile();
        PomModel pomModel = new PomModel(pomFile);

        String groupId = pomModel.getGroupId();
        String artifactId = pomModel.getArtifactId();

        String localProjectVersion = mavenPluginService.getProjectVersion(pomFile);

        // If project is on maven central, modules may depend on each other so ensure versioning is consistent
        try {
            String latestVersion = mavenMetadataService.getLatestVersion(groupId, artifactId);

            logger.info("Local project version is: {}, latest version is {}", localProjectVersion, latestVersion);

            if (!localProjectVersion.equals(latestVersion)) {
                mavenPluginService.setVersion(pomFile, latestVersion);
                return latestVersion;
            }
        } catch (MavenMetadataException e) {
            logger.info(e.getMessage());
        }

        return localProjectVersion;
    }
}
