package com.york.sdp518;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.domain.Repository;
import com.york.sdp518.exception.JavaParseToGraphException;
import com.york.sdp518.processor.SpoonProcessor;
import com.york.sdp518.service.VCSClient;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class RepositoryAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryAnalyser.class);

    VCSClient vcsClient;

    public RepositoryAnalyser(VCSClient vcsClient) {
        this.vcsClient = vcsClient;
    }

    public void analyseRepository(String uri) throws JavaParseToGraphException {
        // Git clone
        File destination = vcsClient.clone(uri);
        Path projectDirectory = Paths.get(destination.toURI()).normalize();

        Repository repository = new Repository(uri, Utils.repoNameFromURI(uri));

        // Process with spoon
        try {
            SpoonProcessor processor = new SpoonProcessor();
            processor.process(projectDirectory);
            Set<Artifact> artifacts = processor.getProcessedArtifacts();

            repository.addAllArtifacts(artifacts);
            Session session = Neo4jSessionFactory.getInstance().getNeo4jSession();
            session.save(repository);
        } finally {
            Neo4jSessionFactory.getInstance().close();
        }

    }
}
