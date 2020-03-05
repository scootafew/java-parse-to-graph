package com.york.sdp518;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.exception.JavaParseToGraphException;
import com.york.sdp518.service.impl.GitVCSClient;

import java.io.File;
import java.io.PrintStream;

public class Main {

    public static void main(String[] args) {
        String uri = args[0];
        try {
            if (uri.endsWith(".git")) {
                RepositoryAnalyser repositoryAnalyser = new RepositoryAnalyser(new GitVCSClient());
                repositoryAnalyser.analyseRepository(uri);
            } else {
                ArtifactAnalyser artifactAnalyser = new ArtifactAnalyser();
                artifactAnalyser.analyseArtifact(uri);
            }
        } catch (JavaParseToGraphException e) {
//            e.printStackTrace();
            System.err.println(e.getMessage() + ": " + e.getCause().getMessage());
            System.exit(e.getCode().getCode());
        } catch (Exception e) {
            String message = e.getMessage() + (e.getCause() != null ? ": " + e.getCause().getMessage() : "");
            System.err.println(message);
            System.exit(1);
        }
    }

    private static void redirectStdOut() throws Exception {
        // Creating a File object that represents the disk file.
        PrintStream o = new PrintStream(new File("log.txt"));

        // Store current System.out before assigning a new value
        PrintStream console = System.out;

        // Assign o to output stream
        System.setOut(o);
    }
}
