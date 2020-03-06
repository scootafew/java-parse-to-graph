package com.york.sdp518.util;

import com.york.sdp518.exception.PomFileException;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class PomModelUtils {

    private Model model;

    public PomModelUtils(File pomFile) throws PomFileException {
        this.model = createModelFromFile(pomFile);
    }

    public PomModelUtils(Model model) {
        this.model = model;
    }

    public Model createModelFromFile(File pomFile) throws PomFileException {
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        try (FileReader reader = new FileReader(pomFile)) {
            return pomReader.read(reader);
        } catch (XmlPullParserException | IOException e) {
            throw new PomFileException("Error reading POM file at path " + pomFile.getPath());
        }
    }

    public String getGroupId() {
        return model.getGroupId() != null ? model.getGroupId() : model.getParent().getGroupId();
    }

    public String getArtifactId() {
        return model.getArtifactId();
    }

    public String getVersion() {
        return model.getVersion() != null ? model.getVersion() : model.getParent().getVersion();
    }

    public Build getBuild() {
        return model.getBuild();
    }
}
