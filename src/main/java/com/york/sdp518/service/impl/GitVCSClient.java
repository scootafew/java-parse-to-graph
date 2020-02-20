package com.york.sdp518.service.impl;

import com.york.sdp518.Utils;
import com.york.sdp518.VCSClientException;
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

    // Only needed for cloning private repo in test
    private static final String GIT_USERNAME = System.getProperty("GIT_USERNAME");
    private static final String GIT_PASSWORD = System.getProperty("GIT_PASSWORD");

    // Monitor to print command progress to System.out
    private TextProgressMonitor consoleProgressMonitor = new TextProgressMonitor(new PrintWriter(System.out));

    public URI clone(String uri) throws VCSClientException {
        File destination = new File("../clones/" + Utils.repoNameFromURI(uri));
        logger.info(">>> Cloning repository into directory \"{}\"", destination.getPath());

        if (Utils.isNonEmptyDirectory(destination)) {
            logger.info("Directory already exists and is non-empty");
            return destination.toURI();
        }

        try {
            Git.cloneRepository().setProgressMonitor(consoleProgressMonitor)
                    .setDirectory(destination)
                    .setURI(uri)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(GIT_USERNAME, GIT_PASSWORD))
                    .call();

            return destination.toURI();
        } catch (GitAPIException e) {
            logger.error("Error cloning git repository - {}", e.getMessage());
            throw new VCSClientException("Error while cloning git repository", e.getCause());
        }
    }
}
