package com.york.sdp518;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String MAVEN_HOME = "MAVEN_HOME";

    public static void main(String[] args) {
        doProcessing();
//        Session session = Neo4jSessionFactory.getInstance().getNeo4jSession();
//        Package pack = new Package("com", "com");
//        Package pack2 = new Package("com.york", "york");
//        pack.addPackage(pack2);
//        session.save(pack);
        Neo4jSessionFactory.getInstance().close();
    }

    private static void doProcessing() {
        VCSClient gitClient = new GitVCSClient();

        try {
//            Git clone
            URI destination = gitClient.clone("https://github.com/scootafew/ast.git");
            Path projectPath = Paths.get(destination);

//            Maven get dependencies
//            Invoker invoker = new DefaultInvoker();
//            invoker.setMavenHome(new File(System.getenv(MAVEN_HOME)));
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
