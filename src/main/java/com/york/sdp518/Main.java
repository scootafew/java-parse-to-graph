package com.york.sdp518;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        VCSClient gitClient = new GitVCSClient();

        try {
//            Git clone
            URI destination = gitClient.clone("https://github.com/rtyley/small-test-repo.git");
            Path projectPath = Paths.get(destination);

//            Maven get dependencies
//            Invoker invoker = new DefaultInvoker();
//            invoker.setMavenHome(new File("C:/Program Files/apache-maven-3.6.2"));
//
//            InvocationRequest request = new DefaultInvocationRequest();
//            request.setPomFile(projectPath.resolve("pom.xml").toFile());
//            request.setGoals(Collections.singletonList("dependency:copy-dependencies"));
//            invoker.execute(request);

//            Analyse
            JavaProjectProcessor projectProcessor = new JavaProjectProcessor(projectPath);
            projectProcessor.printMethods();
//        } catch (VCSClientException | MavenInvocationException e) {
        } catch (VCSClientException e) {
            logger.error(e.getMessage());
        }

    }
}
