package com.york.sdp518.processor;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.exception.BuildClasspathException;
import com.york.sdp518.exception.JavaParseToGraphException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.MavenLauncher;
import spoon.reflect.CtModel;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class SpoonProcessor {

    private static final Logger logger = LoggerFactory.getLogger(SpoonProcessor.class);
    private Set<Artifact> processedArtifacts;

    private ArtifactProcessor artifactProcessor;
    private PackageProcessor packageProcessor;

    public SpoonProcessor() {
        this.artifactProcessor = new ArtifactProcessor();
        this.packageProcessor = new PackageProcessor();
    }

    public void process(Path projectPath, String version) throws JavaParseToGraphException {
        logger.info("Processing project in directory {}", projectPath);

        artifactProcessor.setProjectVersion(version);

        MavenLauncher launcher = new MavenLauncher(projectPath.toString(), MavenLauncher.SOURCE_TYPE.APP_SOURCE, true);
        launcher.getEnvironment().setComplianceLevel(11); // Max currently supported by Spoon
//        launcher.getEnvironment().setLevel(Level.INFO.name());

        if (classpathNotBuiltSuccessfully(launcher)) {
            logger.error("Could not build classpath, exiting...");
            throw new BuildClasspathException("Could not build classpath for repository");
        }

        logger.info("Building model...");
        launcher.buildModel();

        logger.info("Processing model...");
        processedArtifacts = artifactProcessor.processModel(launcher.getPomFile()); // TODO remove as unneeded?

        CtModel model = launcher.getModel();
        packageProcessor.processPackages(model.getAllPackages());
    }

    public Set<Artifact> getProcessedArtifacts() {
        return processedArtifacts;
    }

    // TODO Method now in MavenProject
    /**
     * Expect temp classpath files to have been generated for all modules
     * @param launcher the launcher with which Maven was invoked
     * @return whether the classpath was built successfully
     */
    private boolean classpathNotBuiltSuccessfully(MavenLauncher launcher) {
        List<File> tempFiles = launcher.getPomFile().getClasspathTmpFiles("spoon.classpath-app.tmp");
        List<File> tempAppFiles = launcher.getPomFile().getClasspathTmpFiles("spoon.classpath.tmp");

        int tempFileCount = tempFiles.size() + tempAppFiles.size();
        long moduleCount = artifactProcessor.getModels(launcher.getPomFile()).count();
        logger.info("{} temp files generated for {} modules", tempFileCount, moduleCount);
        return tempFileCount != moduleCount;
    }


}
