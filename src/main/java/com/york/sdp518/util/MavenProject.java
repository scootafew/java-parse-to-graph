package com.york.sdp518.util;

import com.york.sdp518.exception.PomFileException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class MavenProject {

    private static final Logger logger = LoggerFactory.getLogger(MavenProject.class);

    private Path projectDirectory;
    private PomModel rootPomModel;
    private File rootPomFile;

    public MavenProject(Path projectDirectory) throws PomFileException {
        this.projectDirectory = projectDirectory;
        this.rootPomFile = projectDirectory.resolve("pom.xml").toFile();
        this.rootPomModel = new PomModel(rootPomFile);
    }

    public File getRootPomFile() {
        return rootPomFile;
    }

    public PomModel getRootPom() {
        return rootPomModel;
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

    // TODO Maybe compute at instantiation - future = cannot as method defined in subclass
    private Stream<PomModel> getAllModulesStream() {
        return getModels().map(PomModel::new);
    }

    abstract Stream<Model> getModels();

    abstract Stream<Dependency> getDependencies();

    /**
     * Prints dependencies
     */
    public void printDependencies() {
        getDependencies()
                .distinct()
                .forEach(OutputUtils::printDependency);
    }

}
