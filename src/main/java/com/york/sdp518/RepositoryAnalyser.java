package com.york.sdp518;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.domain.Repository;
import com.york.sdp518.exception.JavaParseToGraphException;
import com.york.sdp518.exception.MavenMetadataException;
import com.york.sdp518.processor.SpoonProcessor;
import com.york.sdp518.service.VCSClient;
import com.york.sdp518.service.impl.MavenMetadataService;
import com.york.sdp518.service.impl.MavenPluginService;
import com.york.sdp518.util.PomModelUtils;
import com.york.sdp518.util.Utils;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        try {
            Session neo4jSession = Neo4jSessionFactory.getInstance().getNeo4jSession();
            Repository repo = neo4jSession.load(Repository.class, uri);
            if (repo == null) {
                cloneAndProcess(uri);
            } else {
                logger.info("Repository has already been processed, exiting...");
                // TODO Throw new AlreadyProcessedException ? Probably not as not an error state but should we measure
            }
        } finally {
            Neo4jSessionFactory.getInstance().close();
        }

    }

    private void cloneAndProcess(String uri) throws JavaParseToGraphException {
        // Git clone
        File cloneDestination = vcsClient.clone(uri);
        Path projectDirectory = Paths.get(cloneDestination.toURI()).normalize();

        Repository repository = new Repository(uri, Utils.repoNameFromURI(uri));
        String version = checkVersionAlignment(projectDirectory);

        // Process with spoon
        SpoonProcessor processor = new SpoonProcessor();
        processor.process(projectDirectory, version);
        Set<Artifact> artifacts = processor.getProcessedArtifacts();

        repository.addAllArtifacts(artifacts);
        Session session = Neo4jSessionFactory.getInstance().getNeo4jSession();
        session.save(repository);
    }

    private String checkVersionAlignment(Path projectPath) throws JavaParseToGraphException {
        File pomFile = projectPath.resolve("pom.xml").toFile();
        PomModelUtils pomModel = new PomModelUtils(pomFile);

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
