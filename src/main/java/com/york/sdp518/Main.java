package com.york.sdp518;

import com.york.sdp518.exception.JavaParseToGraphException;
import com.york.sdp518.exception.NormalExitException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.PrintStream;

@SpringBootApplication
public class Main implements CommandLineRunner  {

    private static final int NORMAL_EXIT_CODE = 0;
    private static final int ERROR_EXIT_CODE = 1;

    private RepositoryAnalyser repositoryAnalyser;
    private ArtifactAnalyser artifactAnalyser;
    private Neo4jSessionFactory neo4jSessionFactory;

    public Main(RepositoryAnalyser repositoryAnalyser, ArtifactAnalyser artifactAnalyser,
                Neo4jSessionFactory neo4jSessionFactory) {
        this.repositoryAnalyser = repositoryAnalyser;
        this.artifactAnalyser = artifactAnalyser;
        this.neo4jSessionFactory = neo4jSessionFactory;
    }


    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        String uri = args[0];
        try {
            doAnalysis(uri);
            exit("Processed successfully", NORMAL_EXIT_CODE);
        } catch (NormalExitException e) {
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

    private void doAnalysis(String uri) throws JavaParseToGraphException {
        try {
            if (uri.endsWith(".git")) {
                repositoryAnalyser.analyseRepository(uri);
            } else if (uri.contains(":")) {
                artifactAnalyser.analyseArtifact(uri);
            } else {
                throw new UnsupportedOperationException("Currently can only process Git repositories or Maven artifacts");
            }
        } finally {
            neo4jSessionFactory.close();
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
