package com.york.sdp518.util;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.exception.PomFileException;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PomModel {

    private Model model;
    private String projectVersion;

    public PomModel(File pomFile) throws PomFileException {
        this.model = createModelFromFile(pomFile);
    }

    public PomModel(Model model) {
        this.model = model;
    }

    public PomModel(Model model, String projectVersion) {
        this.model = model;
        this.projectVersion = projectVersion;
    }

    public Model createModelFromFile(File pomFile) throws PomFileException {
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        try (FileReader reader = new FileReader(pomFile)) {
            return pomReader.read(reader);
        } catch (XmlPullParserException | IOException e) {
            throw new PomFileException("Error reading POM file at path " + pomFile.getPath());
        }
    }

    public String getFullyQualifiedName() {
        return String.join(":", getGroupId(), getArtifactId(), getVersion());
    }

    public String getGroupId() {
        return model.getGroupId() != null ? model.getGroupId() : model.getParent().getGroupId();
    }

    public String getArtifactId() {
        return model.getArtifactId();
    }

    public String getVersion() {
        if (projectVersion != null) {
            return projectVersion;
        }
        return model.getVersion() != null ? model.getVersion() : model.getParent().getVersion();
    }

    public String getSourceDirectory() {
        if (model.getBuild() != null) {
            return model.getBuild().getSourceDirectory();
        }
        return null;
    }

    public Packaging getPackaging() {
        if (model.getPackaging() != null) {
            return Packaging.fromString(model.getPackaging());
        }
        return Packaging.JAR;
    }

    // TODO Each model should inherit properties from its parent
    public Properties getProperties() {
        return model.getProperties();
    }

    public Artifact asArtifact() {
        String fqn = String.join(":", getGroupId(), getArtifactId(), getVersion());
        return new Artifact(fqn, getArtifactId());
    }
}
