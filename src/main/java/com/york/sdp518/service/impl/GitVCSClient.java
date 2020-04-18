package com.york.sdp518.service.impl;

import com.york.sdp518.util.Utils;
import com.york.sdp518.exception.VCSClientException;
import com.york.sdp518.service.VCSClient;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class GitVCSClient implements VCSClient {

    private static final Logger logger = LoggerFactory.getLogger(GitVCSClient.class);

    private static final String CLONE_PATH = "../clones/github";

    // TODO Remove - only needed for cloning private repo in test
//    private static final String GIT_USERNAME = Utils.getPropertyOrEnv("GIT_USERNAME", true);
//    private static final String GIT_PASSWORD = Utils.getPropertyOrEnv("GIT_PASSWORD", true);

    // Monitor to print command progress to System.out
    private TextProgressMonitor consoleProgressMonitor = new TextProgressMonitor(new PrintWriter(System.out));

    public File clone(String uri) throws VCSClientException {
        Path destinationPath = Paths.get(CLONE_PATH, Utils.repoFullNameFromURI(uri));
        File destination = new File(destinationPath.toString());

        try {
            if (Utils.isNonEmptyDirectory(destination)) {
                logger.info("Directory already exists and is non-empty, deleting...");
                FileUtils.deleteDirectory(destination);
            }

            logger.info("Cloning repository into directory \"{}\"", destination.getPath());

            Git.cloneRepository().setProgressMonitor(consoleProgressMonitor)
                    .setDirectory(destination)
                    .setURI(uri)
//                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(GIT_USERNAME, GIT_PASSWORD))
                    .call();

            URI projectPath = destination.toURI();
            logger.info("Cloned project to {}", projectPath);

            return destination;
        } catch (IOException e) {
            throw new VCSClientException("Directory already exists and could not be deleted", e);
        } catch (GitAPIException e) {
            throw new VCSClientException("Error while cloning git repository", e);
        }
    }
}
