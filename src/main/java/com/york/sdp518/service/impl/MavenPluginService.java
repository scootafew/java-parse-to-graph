package com.york.sdp518.service.impl;

import com.york.sdp518.Utils;
import com.york.sdp518.exception.MavenPluginInvocationException;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class MavenPluginService {

    private static final String MAVEN_HOME = "M2_HOME";

    private Invoker invoker;

    public MavenPluginService() {
        invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(Utils.getPropertyOrEnv(MAVEN_HOME, true)));
    }

    public void setVersion(File pomFile, String version) throws MavenPluginInvocationException {
        List<String> goal = Collections.singletonList("versions:set -DprocessAllModules=true -DnewVersion=" + version);
        executeGoals(goal, pomFile);
    }

    public void downloadAndCopyArtifactResources(String artifact, Path dest) throws MavenPluginInvocationException {
        downloadArtifactSources(artifact);
        copyArtifactPom(artifact, dest);
        copyArtifactSources(artifact, dest);
    }

    public void downloadArtifactSources(String artifact) throws MavenPluginInvocationException {
        List<String> goal = Collections.singletonList("dependency:get");
        Properties properties = new Properties();
        properties.setProperty("artifact", artifact + ":jar:sources");

        executeGoals(goal, properties);
    }

    public void copyArtifactPom(String artifact, Path dest) throws MavenPluginInvocationException {
        List<String> goal = Collections.singletonList("dependency:copy");
        Properties properties = new Properties();
        properties.setProperty("artifact", artifact + ":pom");
        properties.setProperty("outputDirectory", dest.toString());

        executeGoals(goal, properties);
    }

    public void copyArtifactSources(String artifact, Path dest) throws MavenPluginInvocationException {
        List<String> goal = Collections.singletonList("dependency:copy");
        Properties properties = new Properties();
        properties.setProperty("artifact", artifact + ":jar:sources");
        properties.setProperty("outputDirectory", dest.toString());

        executeGoals(goal, properties);
    }

    public void buildClasspath(File pomFile) throws MavenPluginInvocationException {
        List<String> goal = Collections.singletonList("dependency:build-classpath");
        Properties properties = new Properties();
        properties.setProperty("includeScope", "runtime");

        executeGoals(goal, properties);
    }

    private void executeGoals(List<String> goals, File pomFile) throws MavenPluginInvocationException {
        executeGoals(goals, null, pomFile);
    }

    private void executeGoals(List<String> goals, Properties properties) throws MavenPluginInvocationException {
        executeGoals(goals, properties,null);
    }

    private void executeGoals(List<String> goals, Properties properties, File pomFile) throws MavenPluginInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        if (pomFile != null) {
            request.setPomFile(pomFile);
        }
        if (properties != null) {
            request.setProperties(properties);
        }
        request.setBatchMode(true);
        request.setGoals(goals);

        invoke(request);
    }

    private void invoke(InvocationRequest request) throws MavenPluginInvocationException {
        try {
            InvocationResult invocationResult = invoker.execute(request);
            if (invocationResult.getExitCode() != 0) {
                String msg = "Maven invocation exception";
                if (invocationResult.getExecutionException() != null) {
                    msg = invocationResult.getExecutionException().getMessage();
                }
                throw new MavenPluginInvocationException(msg);
            }
        } catch (MavenInvocationException e) {
            throw new MavenPluginInvocationException("Error invoking maven plugin goal", e);
        }
    }
}
