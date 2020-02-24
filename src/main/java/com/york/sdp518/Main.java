package com.york.sdp518;

import com.york.sdp518.exception.JavaParseToGraphException;

import java.io.File;
import java.io.PrintStream;

public class Main {

    public static void main(String[] args) {
        String uri = args[0];
        try {
//            redirectStdOut();
            RepositoryAnalyser repositoryAnalyser = new RepositoryAnalyser();
            repositoryAnalyser.analyseRepository(uri);
        } catch (JavaParseToGraphException e) {
            e.printStackTrace();
            System.exit(e.getCode().getCode());
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
