package com.york.sdp518.processor;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.exception.BuildClasspathException;
import com.york.sdp518.exception.JavaParseToGraphException;
import com.york.sdp518.service.impl.MavenMetadataService;
import com.york.sdp518.service.MetadataService;
import com.york.sdp518.service.impl.MavenPluginService;
import org.apache.maven.model.Model;
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

    public void process(Path projectPath) throws JavaParseToGraphException {
        logger.info("Processing project in directory {}", projectPath);
        MavenLauncher launcher = new MavenLauncher(projectPath.toString(), MavenLauncher.SOURCE_TYPE.APP_SOURCE);

        if (classpathNotBuiltSuccessfully(launcher)) {
            logger.warn("Could not build classpath, trying to retrieve latest version...");
            attemptToBuildClasspath(launcher);
        }

        logger.info("Building model...");
        launcher.buildModel();

        logger.info("Processing model...");
        processedArtifacts = artifactProcessor.processModel(launcher.getPomFile());

        CtModel model = launcher.getModel();
        System.out.println(model.getAllPackages().size());
        packageProcessor.processPackages(model.getAllPackages());
    }

    public Set<Artifact> getProcessedArtifacts() {
        return processedArtifacts;
    }

    private void attemptToBuildClasspath(MavenLauncher launcher) throws JavaParseToGraphException {
        // if can't build classpath, try setting version to latest version
        String groupId = getGroupId(launcher.getPomFile().getModel());
        String artifactId = launcher.getPomFile().getModel().getArtifactId();

        MetadataService metadataService = new MavenMetadataService();
        String latestVersion = metadataService.getLatestVersion(groupId, artifactId);

        MavenPluginService pluginService = new MavenPluginService();
        pluginService.setVersion(launcher.getPomFile().toFile(), latestVersion);

        launcher.rebuildClasspath();

        if (classpathNotBuiltSuccessfully(launcher)) {
            logger.error("Still could not build classpath, exiting...");
            throw new BuildClasspathException("Could not build classpath for repository");
        }
    }

    /**
     * Expect temp classpath files to have been generated for all modules
     * @param launcher
     * @return
     */
    private boolean classpathNotBuiltSuccessfully(MavenLauncher launcher) {
        List<File> tempFiles = launcher.getPomFile().getClasspathTmpFiles("spoon.classpath-app.tmp");
        List<File> tempAppFiles = launcher.getPomFile().getClasspathTmpFiles("spoon.classpath.tmp");

        int tempFileCount = tempFiles.size() + tempAppFiles.size();
        long moduleCount = artifactProcessor.getModels(launcher.getPomFile()).count();
        logger.info("{} temp files generated for {} modules", tempFileCount, moduleCount);
        return tempFileCount != moduleCount;
    }

    private String getGroupId(Model model) {
        return model.getGroupId() != null ? model.getGroupId() : model.getParent().getGroupId();
    }


}
