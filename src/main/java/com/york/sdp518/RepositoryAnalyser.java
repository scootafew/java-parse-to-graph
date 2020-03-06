package com.york.sdp518;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.domain.Repository;
import com.york.sdp518.exception.JavaParseToGraphException;
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
        // Git clone
        File cloneDestination = vcsClient.clone(uri);
        Path projectDirectory = Paths.get(cloneDestination.toURI()).normalize();

        String version = checkVersionAlignment(projectDirectory);

        Repository repository = new Repository(uri, Utils.repoNameFromURI(uri));

        // Process with spoon
        try {
            SpoonProcessor processor = new SpoonProcessor();
            processor.process(projectDirectory, version);
            Set<Artifact> artifacts = processor.getProcessedArtifacts();

            repository.addAllArtifacts(artifacts);
            Session session = Neo4jSessionFactory.getInstance().getNeo4jSession();
            session.save(repository);
        } finally {
            Neo4jSessionFactory.getInstance().close();
        }
    }

    private String checkVersionAlignment(Path projectPath) throws JavaParseToGraphException {
        File pomFile = projectPath.resolve("pom.xml").toFile();
        PomModelUtils pomModel = new PomModelUtils(pomFile);

        String groupId = pomModel.getGroupId();
        String artifactId = pomModel.getArtifactId();

        String localProjectVersion = mavenPluginService.getProjectVersion(pomFile);
        String latestVersion = mavenMetadataService.getLatestVersion(groupId, artifactId);

        logger.info("Local project version is: {}, latest version is {}", localProjectVersion, latestVersion);

        if (!localProjectVersion.equals(latestVersion)) {
            mavenPluginService.setVersion(pomFile, latestVersion);
            return latestVersion;
        }
        return localProjectVersion;
    }
}
