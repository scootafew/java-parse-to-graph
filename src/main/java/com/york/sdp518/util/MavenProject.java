package com.york.sdp518.util;

import org.apache.maven.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.MavenLauncher;
import spoon.support.compiler.SpoonPom;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MavenProject {

    private static final Logger logger = LoggerFactory.getLogger(MavenProject.class);

    private Path projectDirectory;
    private String remoteUrl;
    private MavenLauncher launcher;
    private boolean classpathBuiltSuccessfully;

    public MavenProject(Path projectDirectory, String remoteUrl) {
        this.projectDirectory = projectDirectory;
        this.remoteUrl = remoteUrl;
        this.launcher = new MavenLauncher(projectDirectory.toString(), MavenLauncher.SOURCE_TYPE.APP_SOURCE);
        this.classpathBuiltSuccessfully = checkClasspathBuiltSuccessfully();
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public String getProjectName() {
        return Utils.repoNameFromURI(remoteUrl);
    }

    public File getRootPomFile() {
        return launcher.getPomFile().toFile();
    }

    // TODO Maybe compute at instantiation
    public PomModel getRootPom() {
        return new PomModel(launcher.getPomFile().getModel());
    }

    public Path getProjectDirectory() {
      return projectDirectory;
    }

    public Set<PomModel> getAllModules() {
        return getAllModulesStream().collect(Collectors.toSet());
    }

    public Set<PomModel> getAllModules(Collection<Packaging> includedPackagingTypes) {
        return getAllModulesStream()
                .filter(pom -> includedPackagingTypes.contains(pom.getPackaging()))
                .collect(Collectors.toSet());
    }

    // TODO Maybe compute at instantiation
    private Stream<PomModel> getAllModulesStream() {
        return getModels(launcher.getPomFile())
                .map(PomModel::new);
    }

    private Stream<Model> getModels(SpoonPom spoonPom) {
        if (spoonPom.getModules().isEmpty()) {
            return Stream.of(spoonPom.getModel());
        }
        return Stream.concat(
                Stream.of((spoonPom.getModel())),
                spoonPom.getModules().stream().flatMap(this::getModels)
        );
    }

    /**
     * Expect temp classpath files to have been generated for all modules
     * @return whether the classpath was built successfully
     */
    public boolean checkClasspathBuiltSuccessfully() {
        List<File> tempFiles = launcher.getPomFile().getClasspathTmpFiles("spoon.classpath-app.tmp");
        List<File> tempAppFiles = launcher.getPomFile().getClasspathTmpFiles("spoon.classpath.tmp");

        int tempFileCount = tempFiles.size() + tempAppFiles.size();
        long moduleCount = getModels(launcher.getPomFile()).count();
        logger.info("{} temp files generated for {} modules", tempFileCount, moduleCount);
        return tempFileCount != moduleCount;
    }

    public boolean classpathBuiltSuccessfully() {
        return classpathBuiltSuccessfully;
    }
}
