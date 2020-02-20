package com.york.sdp518;

import com.york.sdp518.service.VCSClient;
import com.york.sdp518.service.impl.GitVCSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String uri = args[0];
        try {
//            redirectStdOut();
            doSpoon(uri);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Neo4jSessionFactory.getInstance().close();
        }
    }

    private static void doSpoon(String uri) throws Exception {
        VCSClient gitClient = new GitVCSClient();

        // Git clone
        URI destination = gitClient.clone(uri);
        Path projectPath = Paths.get(destination).normalize();

        logger.info("Cloned project to {}", projectPath.toString());

        SpoonProcessor processor = new SpoonProcessor(projectPath);
        processor.run();
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
