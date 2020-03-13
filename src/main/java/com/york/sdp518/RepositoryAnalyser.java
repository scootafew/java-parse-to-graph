package com.york.sdp518;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.domain.Repository;
import com.york.sdp518.exception.JavaParseToGraphException;
import com.york.sdp518.exception.MavenMetadataException;
import com.york.sdp518.processor.SpoonProcessor;
import com.york.sdp518.service.VCSClient;
import com.york.sdp518.service.impl.MavenMetadataService;
import com.york.sdp518.service.impl.MavenPluginService;
import com.york.sdp518.util.MavenProject;
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

    public RepositoryAnalyser(VCSClient vcsClient) {
        this.vcsClient = vcsClient;
        this.mavenPluginService = new MavenPluginService();
        this.mavenMetadataService = new MavenMetadataService();
    }

    public void analyseRepository(String uri) throws JavaParseToGraphException {
        // TODO Check version as well, might want to use flag instead and create repo first in db to account for partial parsing
        // Check if repository has already been processed
        Session neo4jSession = Neo4jSessionFactory.getInstance().getNeo4jSession();
        Repository repo = neo4jSession.load(Repository.class, uri);
        if (repo == null) {
            cloneAndProcess(uri);
        } else {
            logger.info("Repository has already been processed, exiting...");
            // TODO Throw new AlreadyProcessedException ? Probably not as not an error state but should we measure
        }

    }

    private void cloneAndProcess(String remoteUrl) throws JavaParseToGraphException {
        // Git clone
        File cloneDestination = vcsClient.clone(remoteUrl);
        Path projectDirectory = Paths.get(cloneDestination.toURI()).normalize();

//        Path pathToPomDirectory = getPomDirectory(projectDirectory, projectDirectory.resolve(pathToPom).getParent());

        Collection<Path> directoriesWithPom = Utils.getDirectoriesWithPom(projectDirectory);

        for (Path path : directoriesWithPom) {
            MavenProject mavenProject = new MavenProject(path, remoteUrl);
            PomModel pom = mavenProject.getRootPom();

            if (mavenMetadataService.isPublishedArtifact(pom.getGroupId(), pom.getArtifactId())) {
                processAsLibrary(mavenProject);
            } else {
                processAsRepository(mavenProject);
            }
        }

//        MavenProject mavenProject = new MavenProject(pathToPomDirectory, remoteUrl);
//        PomModel pom = mavenProject.getRootPom();
//
//        if (mavenMetadataService.isPublishedArtifact(pom.getGroupId(), pom.getArtifactId())) {
//            processAsLibrary(mavenProject);
//        } else {
//            processAsRepository(mavenProject);
//        }
    }

    private void processAsRepository(MavenProject mavenProject) throws JavaParseToGraphException {
        logger.info("Processing project {} as repository", mavenProject.getProjectName());
        if (!mavenProject.classpathBuiltSuccessfully()) {
            mavenPluginService.cleanInstall(mavenProject.getRootPomFile(), false);
            // TODO Rebuild classpath here if passing through to SpoonProcessor
        }

        Repository repository = new Repository(mavenProject.getRemoteUrl(), mavenProject.getProjectName());

        // Process with spoon
        SpoonProcessor processor = new SpoonProcessor();
        processor.process(mavenProject.getProjectDirectory(), mavenProject.getRootPom().getVersion()); // TODO may directly pass in MavenProject
        Set<Artifact> artifacts = processor.getProcessedArtifacts();

        repository.addAllArtifacts(artifacts);
        Neo4jSessionFactory.getInstance().getNeo4jSession().save(repository);
    }

    private void processAsLibrary(MavenProject mavenProject) throws JavaParseToGraphException {
        logger.info("Processing project {} as library", mavenProject.getProjectName());

        ArtifactAnalyser artifactProcessor = new ArtifactAnalyser();
        Repository repository = new Repository(mavenProject.getRemoteUrl(), mavenProject.getProjectName());
        Set<PomModel> jarPackagedArtifacts = mavenProject.getAllModules(Collections.singletonList(Packaging.JAR));
        for (PomModel artifact : jarPackagedArtifacts) {
            try {
                String version = mavenMetadataService.getLatestVersion(artifact.getGroupId(), artifact.getArtifactId());
                String fqn = String.join(":", artifact.getGroupId(), artifact.getArtifactId(), version);
                artifactProcessor.analyseArtifact(fqn);

                Artifact processedArtifact = new Artifact(fqn, artifact.getArtifactId());
                repository.addAllArtifacts(Collections.singleton(processedArtifact));
            } catch (MavenMetadataException e) {
                logger.info("No published artifact found for {}, skipping...", artifact.getArtifactId());
            }
        }
        Neo4jSessionFactory.getInstance().getNeo4jSession().save(repository);
    }

    /**
     * Starting with candidate pom path, navigates up directory structure until it finds a directory without a POM
     * or that is the top directory of the project
     * @param projectDirectory
     * @param pathToCandidatePom
     * @return
     */
    private Path getPomDirectory(Path projectDirectory, Path pathToCandidatePom) {
        if (pathToCandidatePom.equals(projectDirectory)) {
            return pathToCandidatePom;
        }
        if (pathToCandidatePom.resolveSibling("pom.xml").toFile().exists()) {
           return getPomDirectory(projectDirectory, pathToCandidatePom.getParent());
        } else {
            return pathToCandidatePom;
        }
    }

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
