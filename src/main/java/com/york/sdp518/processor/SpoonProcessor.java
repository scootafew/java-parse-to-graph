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

import java.nio.file.Path;
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

    public void process(Path projectPath, String type) throws JavaParseToGraphException {
        logger.info("Processing project in directory {}", projectPath);
        MavenLauncher launcher = new MavenLauncher(projectPath.toString(), MavenLauncher.SOURCE_TYPE.APP_SOURCE);

        if (classpathNotBuiltSuccessfully(launcher) && type.equals("repository")) { // TODO fix once PR merged
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

    private boolean classpathNotBuiltSuccessfully(MavenLauncher launcher) {
        return launcher.getEnvironment().getSourceClasspath().length == 0;
    }

    private String getGroupId(Model model) {
        return model.getGroupId() != null ? model.getGroupId() : model.getParent().getGroupId();
    }


}
