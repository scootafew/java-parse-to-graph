package com.york.sdp518;

import com.york.sdp518.exception.JavaParseToGraphException;
import com.york.sdp518.service.impl.GitVCSClient;

import java.io.File;
import java.io.PrintStream;

public class Main {

    public static void main(String[] args) {
        String uri = args[0];
        try {
            doAnalysis(uri);
        } catch (JavaParseToGraphException e) {
            e.printStackTrace();
            System.err.println(e.getMessage() + (e.getCause() != null ? ": " + e.getCause() : ""));
            System.exit(e.getCode().getCode());
        } catch (Exception e) {
            e.printStackTrace();
            // Message might not exist (in case of java.lang.NullPointerException)
            String message = e.getMessage() + (e.getCause() != null ? ": " + e.getCause() : "");
            System.err.println(message);
            System.exit(1);
        }
    }

    private static void doAnalysis(String uri) throws JavaParseToGraphException {
        try {
            if (uri.endsWith(".git")) {
                RepositoryAnalyser repositoryAnalyser = new RepositoryAnalyser(new GitVCSClient());
                repositoryAnalyser.analyseRepository(uri);
            } else {
                ArtifactAnalyser artifactAnalyser = new ArtifactAnalyser();
                artifactAnalyser.analyseArtifact(uri);
            }
        } finally {
            Neo4jSessionFactory.getInstance().close();
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
