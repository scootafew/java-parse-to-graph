package com.york.sdp518.service.impl;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.util.PomModel;
import com.york.sdp518.util.Utils;
import com.york.sdp518.exception.MavenPluginInvocationException;
import com.york.sdp518.exception.PomFileException;
import com.york.sdp518.service.MavenInvoker;
import org.apache.maven.model.Dependency;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MavenPluginService {

    private MavenInvoker invoker;

    public MavenPluginService(MavenInvoker mavenInvoker) {
        invoker = mavenInvoker;
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

    private void getDependencies(File pomFile, File outputFile) throws MavenPluginInvocationException {
        List<String> goal = Collections.singletonList("dependency:list");
        Properties properties = new Properties();
        properties.setProperty("excludeTransitive", "true");
        properties.setProperty("includeScope", "runtime");
        properties.setProperty("includeScope", "compile");
        properties.setProperty("outputFile", outputFile.toString());
        invoker.executeGoals(goal, properties, pomFile);
    }

    public Path getSources(Artifact artifact) throws MavenPluginInvocationException {
        // Download sources from Maven
        File destination = new File("../artifacts/maven/" + artifact.getArtifactId());
        Path destPath = Paths.get(destination.toURI()).normalize();

        downloadArtifactSources(artifact.getFullyQualifiedName());
        copyArtifactPom(artifact.getFullyQualifiedName(), destPath);
        copyArtifactSources(artifact.getFullyQualifiedName(), destPath);

        String pomFile = String.format("%s-%s.pom", artifact.getArtifactId(), artifact.getVersion());
        String sourcesFile = String.format("%s-%s-sources.jar", artifact.getArtifactId(), artifact.getVersion());
        Path pomPath = destPath.resolve(pomFile);
        Path jarPath = destPath.resolve(sourcesFile);

        refactorToExpectedStructure(destPath, pomPath, jarPath);
        return destPath;
    }

    private void downloadArtifactSources(String artifact) throws MavenPluginInvocationException {
        List<String> goal = Collections.singletonList("dependency:get");
        Properties properties = new Properties();
        properties.setProperty("artifact", artifact + ":jar:sources");

        invoker.executeGoals(goal, properties);
    }

    private void copyArtifactPom(String artifact, Path dest) throws MavenPluginInvocationException {
        List<String> goal = Collections.singletonList("dependency:copy");
        Properties properties = new Properties();
        properties.setProperty("artifact", artifact + ":pom");
        properties.setProperty("outputDirectory", dest.toString());

        invoker.executeGoals(goal, properties);
    }

    private void copyArtifactSources(String artifact, Path dest) throws MavenPluginInvocationException {
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

    private void refactorToExpectedStructure(Path destinationPath, Path pomPath, Path jarPath) {
        try {
            // Rename pom
            Path pom = destinationPath.resolve(pomPath);
            Files.move(pom, pom.resolveSibling("pom.xml"));

            // Read source directory structure from POM
            Path sourceDirectoryPath = getSourceDirectoryPath(destinationPath.resolve("pom.xml").toFile());
            File sourceDirectory = destinationPath.resolve(sourceDirectoryPath).toFile();
            sourceDirectory.mkdirs(); // create source directory structure

            // Unpack jar
            ProcessBuilder processBuilder = new ProcessBuilder("jar", "-xf", jarPath.toString());
            processBuilder.directory(sourceDirectory);
            Process process = processBuilder.start();
            process.waitFor(); // wait for process to finish

            // Delete jar
            Files.delete(jarPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Path getSourceDirectoryPath(File pomFile) throws PomFileException {
        PomModel pomModel = new PomModel(pomFile);
        if (pomModel.getSourceDirectory() != null) {
            return Paths.get(pomModel.getSourceDirectory());
        }
        return Paths.get("src/main/java");
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
