package com.york.sdp518.service.impl;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class MavenPluginService {

    private static final String MAVEN_HOME = "M2_HOME";

    private Invoker invoker;

    public MavenPluginService() {
        invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(System.getProperty(MAVEN_HOME)));
    }

    public void setVersion(File pomFile, String version) throws Exception {
        List<String> goal = Collections.singletonList("versions:set -DprocessAllModules=true -DnewVersion=" + version);
        executeGoals(pomFile, goal);
    }

    private void executeGoals(File pomFile, List<String> goals) throws Exception {
        // Maven get dependencies
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(pomFile);
        request.setGoals(goals);
        InvocationResult invocationResult = invoker.execute(request);
        if (invocationResult.getExitCode() != 0) {
            String msg = "Maven invocation exception";
            if (invocationResult.getExecutionException() != null) {
                msg = invocationResult.getExecutionException().getMessage();
            }
            throw new Exception(msg);
        }
    }
}
