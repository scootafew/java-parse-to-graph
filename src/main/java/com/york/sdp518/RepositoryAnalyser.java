package com.york.sdp518;

import com.york.sdp518.domain.Repository;
import com.york.sdp518.exception.JavaParseToGraphException;
import com.york.sdp518.service.VCSClient;
import com.york.sdp518.service.impl.GitVCSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RepositoryAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryAnalyser.class);

    VCSClient vcsClient;

    public RepositoryAnalyser() {
        this.vcsClient = new GitVCSClient();
    }

    public void analyseRepository(String uri) throws JavaParseToGraphException {
        // Git clone
        File destination = vcsClient.clone(uri);
        Path projectDirectory = Paths.get(destination.toURI()).normalize();

        Repository repository = new Repository(uri, Utils.repoNameFromURI(uri));

        // Process with spoon
        SpoonProcessor processor = new SpoonProcessor();
        processor.process(repository, projectDirectory);
    }
}
