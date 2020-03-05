package com.york.sdp518.service.impl;

import com.york.sdp518.Utils;
import com.york.sdp518.exception.VCSClientException;
import com.york.sdp518.service.VCSClient;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;

public class GitVCSClient implements VCSClient {

    private static final Logger logger = LoggerFactory.getLogger(GitVCSClient.class);

    // TODO Remove - only needed for cloning private repo in test
//    private static final String GIT_USERNAME = Utils.getPropertyOrEnv("GIT_USERNAME", true);
//    private static final String GIT_PASSWORD = Utils.getPropertyOrEnv("GIT_PASSWORD", true);

    // Monitor to print command progress to System.out
    private TextProgressMonitor consoleProgressMonitor = new TextProgressMonitor(new PrintWriter(System.out));

    public File clone(String uri) throws VCSClientException {
        File destination = new File("../clones/" + Utils.repoNameFromURI(uri));
        logger.info(">>> Cloning repository into directory \"{}\"", destination.getPath());

        if (Utils.isNonEmptyDirectory(destination)) {
            logger.info("Directory already exists and is non-empty");
            return destination;
        }

        try {
            Git.cloneRepository().setProgressMonitor(consoleProgressMonitor)
                    .setDirectory(destination)
                    .setURI(uri)
//                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(GIT_USERNAME, GIT_PASSWORD))
                    .call();

            URI projectPath = destination.toURI();
            logger.info("Cloned project to {}", projectPath);

            return destination;
        } catch (GitAPIException e) {
            throw new VCSClientException("Error while cloning git repository", e);
        }
    }
}
