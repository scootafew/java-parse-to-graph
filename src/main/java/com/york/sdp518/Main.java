package com.york.sdp518;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String MAVEN_HOME = "MAVEN_HOME";

    public static void main(String[] args) {
        String uri = "https://github.com/scootafew/ast.git";
        try {
//            doProcessing(args[0], args[1]);
//            redirectStdOut();
            doSpoon(args[0]);
        } catch (Exception e) {
            e.printStackTrace();
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

    private static void doSpoon(String uri) throws Exception {
        VCSClient gitClient = new GitVCSClient();

        // Git clone
        URI destination = gitClient.clone(uri);
        Path projectPath = Paths.get(destination);

        SpoonProcessor processor = new SpoonProcessor(projectPath);
        processor.run();
    }

    private static void doProcessing(String uri, String version) throws Exception {
        VCSClient gitClient = new GitVCSClient();

        // Git clone
        URI destination = gitClient.clone(uri);
        Path projectPath = Paths.get(destination);

        // Maven get dependencies
        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(System.getProperty(MAVEN_HOME)));

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(projectPath.resolve("pom.xml").toFile());
        request.setGoals(Collections.singletonList("versions:set -DprocessAllModules=true -DnewVersion=" + version));
        InvocationResult invocationResult = invoker.execute(request);
        if (invocationResult.getExitCode() != 0) {
            String msg = "Maven invocation exception";
            if (invocationResult.getExecutionException() != null) {
                msg = invocationResult.getExecutionException().getMessage();
            }
            throw new Exception(msg);
        }

        InvocationRequest invocationRequest = new DefaultInvocationRequest();
        invocationRequest.setPomFile(projectPath.resolve("pom.xml").toFile());
        invocationRequest.setGoals(Collections.singletonList("dependency:copy-dependencies -DoutputDirectory=\"target/dependency\""));
        InvocationResult result = invoker.execute(invocationRequest);
        if (result.getExitCode() != 0) {
            String msg = "Maven invocation exception";
            if (result.getExecutionException() != null) {
                msg = result.getExecutionException().getMessage();
            }
            throw new Exception(msg);
        }

        // Analyse
        JavaProjectProcessor projectProcessor = new JavaProjectProcessor(projectPath);
        projectProcessor.printMethods();
    }
}
