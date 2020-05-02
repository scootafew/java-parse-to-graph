package com.york.sdp518.service;

import com.york.sdp518.exception.MavenPluginInvocationException;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Properties;

public class MavenInvoker extends DefaultInvoker {

    private static final Logger logger = LoggerFactory.getLogger(MavenInvoker.class);

    public void executeGoals(List<String> goals, Properties properties, File pomFile) throws MavenPluginInvocationException {
        executeGoals(goals, properties, pomFile, null);
    }

    public void executeGoals(List<String> goals, Properties properties) throws MavenPluginInvocationException {
        executeGoals(goals, properties, null, null);
    }

    public void executeGoals(List<String> goals,
                             @Nullable Properties properties,
                             @Nullable File pomFile,
                             @Nullable InvocationOutputHandler outputHandler) throws MavenPluginInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        if (pomFile != null) {
            request.setPomFile(pomFile);
        }
        if (properties != null) {
            request.setProperties(properties);
        }
        request.setBatchMode(true);
        request.setGoals(goals);

        invoke(request, outputHandler);
    }

    private void invoke(InvocationRequest request, @Nullable InvocationOutputHandler outHandler) throws MavenPluginInvocationException {
        if (outHandler == null) {
            outHandler = logger::debug;
        }
        setOutputHandler(outHandler);
        try {
            InvocationResult invocationResult = execute(request);
            if (invocationResult.getExitCode() != 0) {
                String msg = "Maven invocation exception when executing goal(s): " + request.getGoals().toString();
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
