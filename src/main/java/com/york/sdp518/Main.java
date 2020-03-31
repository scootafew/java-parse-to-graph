package com.york.sdp518;

import com.york.sdp518.exception.AlreadyProcessedException;
import com.york.sdp518.exception.JavaParseToGraphException;
import com.york.sdp518.service.impl.GitVCSClient;

import java.io.File;
import java.io.PrintStream;

public class Main {

    private static final int NORMAL_EXIT_CODE = 0;
    private static final int ERROR_EXIT_CODE = 1;

    public static void main(String[] args) {
        String uri = args[0];
        try {
            doAnalysis(uri);
            exit("Processed successfully", NORMAL_EXIT_CODE);
        } catch (AlreadyProcessedException e) {
            exit(e.getMessage(), NORMAL_EXIT_CODE);
        } catch (JavaParseToGraphException e) {
            e.printStackTrace();
            String message = e.getMessage() + (e.getCause() != null ? ": " + e.getCause() : "");
            exit(message, e.getCode().getCode());
        } catch (Exception e) {
            e.printStackTrace();
            // Message might not exist (in case of java.lang.NullPointerException)
            String message = e.getMessage() + (e.getCause() != null ? ": " + e.getCause() : "");
            exit(message, ERROR_EXIT_CODE);
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

    private static void exit(String message, int code) {
        System.out.println("Exit message: " + message);
        System.exit(code);
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
