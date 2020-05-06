package com.york.sdp518.util;

import com.york.sdp518.exception.DependencyManagementServiceException;
import com.york.sdp518.exception.PomFileException;
import com.york.sdp518.service.impl.MavenDependencyManagementService;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.MavenLauncher;
import spoon.reflect.CtModel;
import spoon.support.compiler.SpoonPom;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class SpoonedMavenProject extends MavenProject {

    private static final Logger logger = LoggerFactory.getLogger(SpoonedMavenProject.class);

    private MavenLauncher launcher;
    private SpoonPom rootSpoonPom;
    private boolean classpathBuiltSuccessfully;

    private MavenDependencyManagementService depManagementService; // TODO Move service out
    private Set<Dependency> dependencies = new HashSet<>();

    public SpoonedMavenProject(Path projectDirectory,
                               MavenDependencyManagementService depManagementService) throws PomFileException {
        super(projectDirectory);
        this.depManagementService = depManagementService;
        this.launcher = new MavenLauncher(projectDirectory.toString(), MavenLauncher.SOURCE_TYPE.APP_SOURCE);
        this.launcher.getEnvironment().setComplianceLevel(11);  // Max currently supported by Spoon
        this.rootSpoonPom = launcher.getPomFile();
        this.classpathBuiltSuccessfully = checkClasspathBuiltSuccessfully();
    }

    @Override
    Stream<Model> getModels() {
        return getModelsFromPom(rootSpoonPom);
    }

    private Stream<Model> getModelsFromPom(SpoonPom spoonPom) {
        return this.getAllModulesStream(spoonPom).map(SpoonPom::getModel);
    }

    private Stream<SpoonPom> getAllModulesStream(SpoonPom spoonPom) {
        if (spoonPom.getModules().isEmpty()) {
            return Stream.of(spoonPom);
        }
        return Stream.concat(
                Stream.of(spoonPom),
                spoonPom.getModules().stream().flatMap(this::getAllModulesStream)
        );
    }

    /**
     * Expect temp classpath files to have been generated for all modules
     * @return whether the classpath was built successfully
     */
    private boolean checkClasspathBuiltSuccessfully() {
        List<File> tempFiles = launcher.getPomFile().getClasspathTmpFiles("spoon.classpath-app.tmp");
        List<File> tempAppFiles = launcher.getPomFile().getClasspathTmpFiles("spoon.classpath.tmp");

        int tempFileCount = tempFiles.size() + tempAppFiles.size();
        long moduleCount = getModels().count();
        logger.info("{} temp files generated for {} modules", tempFileCount, moduleCount);
        return tempFileCount == moduleCount;
    }

    public boolean classpathNotBuiltSuccessfully() {
        return !classpathBuiltSuccessfully;
    }

    public void rebuildClasspath() {
        this.launcher.rebuildClasspath();
        this.classpathBuiltSuccessfully = checkClasspathBuiltSuccessfully();
    }

    public CtModel getSpoonModel() {
        logger.info("Building model...");
        this.launcher.buildModel();
        return launcher.getModel();
    }

    @Override
    public Stream<Dependency> getDependencies() {
        if (dependencies.isEmpty()) {
            dependencies.addAll(getAllModulesStream(rootSpoonPom)
                    .map(SpoonPom::toFile)
                    .flatMap(this::getDependencies)
                    .collect(Collectors.toSet())
            );
        }
        return dependencies.stream();
    }

    private Stream<Dependency> getDependencies(File pomFile) {
        try {
            return depManagementService.getDependencies(pomFile).stream();
        } catch (DependencyManagementServiceException e) {
            logger.warn("Could not get dependencies for POM at {}", pomFile);
            return Stream.empty();
        }
    }
}
