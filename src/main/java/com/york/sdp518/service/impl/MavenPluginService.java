package com.york.sdp518.service.impl;

import com.york.sdp518.util.Utils;
import com.york.sdp518.exception.MavenPluginInvocationException;
import com.york.sdp518.exception.PomFileException;
import com.york.sdp518.service.MavenInvoker;
import org.apache.maven.model.Dependency;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class MavenPluginService {

    private static final Logger logger = LoggerFactory.getLogger(MavenPluginService.class);

    private static final String MAVEN_HOME_ENV_VAR = "M2_HOME";
    private static final File MAVEN_HOME = new File(Utils.getPropertyOrEnv(MAVEN_HOME_ENV_VAR, true));

    private MavenInvoker invoker;

    public MavenPluginService() {
        invoker = new MavenInvoker();
        invoker.setMavenHome(MAVEN_HOME);
    }

    public String getProjectVersion(File pomFile) throws PomFileException, MavenPluginInvocationException {
        List<String> goal = Collections.singletonList("help:evaluate -q");
        Properties properties = new Properties();
        properties.setProperty("expression", "project.version");
        properties.setProperty("forceStdout", "true");

        if (pomFile.exists()) {
            VersionOutputHandler outputHandler = new VersionOutputHandler();
            invoker.executeGoals(goal, properties, pomFile, outputHandler);
            return outputHandler.getResult();
        } else {
            throw new PomFileException("Pom file not found at " + pomFile.getPath());
        }
    }

    public void setVersion(File pomFile, String version) throws MavenPluginInvocationException {
        List<String> goal = Collections.singletonList("versions:set");
        Properties properties = new Properties();
        properties.setProperty("processAllModules", "true");
        properties.setProperty("newVersion", version);
        invoker.executeGoals(goal, properties, pomFile);
    }

    public void cleanInstall(File pomFile, boolean runTests) throws MavenPluginInvocationException {
        List<String> goal = Collections.singletonList("clean install");
        Properties properties = new Properties();
        if (!runTests) {
            properties.setProperty("skipTests", "true");
        }
        invoker.executeGoals(goal, properties, pomFile);
    }

    public Set<Dependency> getDependencies(File pomFile) throws MavenPluginInvocationException {
        File outputFile = pomFile.toPath().getParent().resolve("jp2g-dependencies.txt").toFile();
        getDependencies(pomFile, outputFile);

        try {
            List<String> fileLines = Utils.readFileSplitNewLine(outputFile);
            List<String> dependencies = fileLines.subList(2, fileLines.size() - 1);

            return dependencies.stream()
                    .map(String::trim)
                    .map(this::dependencyFromString)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new MavenPluginInvocationException("Error reading dependencies from generated file", e);
        }
    }

    public void getDependencies(File pomFile, File outputFile) throws MavenPluginInvocationException {
        List<String> goal = Collections.singletonList("dependency:list");
        Properties properties = new Properties();
        properties.setProperty("excludeTransitive", "true");
        properties.setProperty("includeScope", "runtime");
        properties.setProperty("includeScope", "compile");
        properties.setProperty("outputFile", outputFile.toString());
        invoker.executeGoals(goal, properties, pomFile);
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

        invoker.executeGoals(goal, properties);
    }

    public void copyArtifactPom(String artifact, Path dest) throws MavenPluginInvocationException {
        List<String> goal = Collections.singletonList("dependency:copy");
        Properties properties = new Properties();
        properties.setProperty("artifact", artifact + ":pom");
        properties.setProperty("outputDirectory", dest.toString());

        invoker.executeGoals(goal, properties);
    }

    public void copyArtifactSources(String artifact, Path dest) throws MavenPluginInvocationException {
        List<String> goal = Collections.singletonList("dependency:copy");
        Properties properties = new Properties();
        properties.setProperty("artifact", artifact + ":jar:sources");
        properties.setProperty("outputDirectory", dest.toString());

        invoker.executeGoals(goal, properties);
    }

    public void buildClasspath(File pomFile) throws MavenPluginInvocationException {
        List<String> goal = Collections.singletonList("dependency:build-classpath");
        Properties properties = new Properties();
        properties.setProperty("includeScope", "runtime");

        invoker.executeGoals(goal, properties);
    }

    private Optional<Dependency> dependencyFromString(String dep) {
        Dependency dependency = new Dependency();
        String[] depComponents = dep.split(":");

        if (depComponents.length > 4) {
            dependency.setGroupId(depComponents[0]);
            dependency.setArtifactId(depComponents[1]);
            dependency.setType(depComponents[2]);
            dependency.setVersion(depComponents[3]);
            dependency.setScope(depComponents[4]);

            return Optional.of(dependency);
        }
        return Optional.empty();
    }

    private static final class VersionOutputHandler implements InvocationOutputHandler {

        private String result;

        @Override
        public void consumeLine(String line) {
            result = line;
        }

        public String getResult() {
            return result;
        }
    }
}
