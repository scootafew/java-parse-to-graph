package com.york.sdp518.util;

import com.york.sdp518.exception.PomFileException;
import org.apache.maven.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.MavenLauncher;
import spoon.reflect.CtModel;
import spoon.support.compiler.SpoonPom;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public abstract class SpoonedMavenProject extends MavenProject {

    private static final Logger logger = LoggerFactory.getLogger(SpoonedMavenProject.class);

    private MavenLauncher launcher;
    private SpoonPom rootSpoonPom;
    private boolean classpathBuiltSuccessfully;

    public SpoonedMavenProject(Path projectDirectory) throws PomFileException {
        super(projectDirectory);
        this.launcher = new MavenLauncher(projectDirectory.toString(), MavenLauncher.SOURCE_TYPE.APP_SOURCE);
        this.launcher.getEnvironment().setComplianceLevel(11);  // Max currently supported by Spoon
        // launcher.getEnvironment().setLevel(Level.INFO.name());
        this.rootSpoonPom = launcher.getPomFile();
        this.classpathBuiltSuccessfully = checkClasspathBuiltSuccessfully();
    }

    @Override
    Stream<Model> getModels() {
        return getModelsFromPom(rootSpoonPom);
    }

    private Stream<Model> getModelsFromPom(SpoonPom spoonPom) {
        if (spoonPom.getModules().isEmpty()) {
            return Stream.of(spoonPom.getModel());
        }
        return Stream.concat(
                Stream.of((spoonPom.getModel())),
                spoonPom.getModules().stream().flatMap(this::getModelsFromPom)
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
}
