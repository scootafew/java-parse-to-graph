package com.york.sdp518.processor;

import com.york.sdp518.util.SpoonedMavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import spoon.reflect.CtModel;

@Component
public class SpoonProcessor {

    private static final Logger logger = LoggerFactory.getLogger(SpoonProcessor.class);

    private PackageProcessor packageProcessor;

    @Autowired
    public SpoonProcessor(PackageProcessor packageProcessor) {
        this.packageProcessor = packageProcessor;
    }

    public void process(SpoonedMavenProject mavenProject) {
        logger.info("Processing project in directory {}", mavenProject.getProjectDirectory());
        CtModel model = mavenProject.getSpoonModel();
        logger.info("Processing model...");
        packageProcessor.processPackages(model.getAllPackages());
    }

}
