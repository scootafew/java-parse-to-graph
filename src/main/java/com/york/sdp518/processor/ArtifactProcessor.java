package com.york.sdp518.processor;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.util.PomModel;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import spoon.support.compiler.SpoonPom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArtifactProcessor {

    private String projectVersion;

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public Set<Artifact> processModel(SpoonPom pom) {
        Set<Model> models = getModels(pom).collect(Collectors.toSet());
        models.forEach(this::printDependencies);

        Set<Artifact> artifacts = models.stream()
                .map(this::getArtifact)
                .collect(Collectors.toSet());
        artifacts.forEach(this::printArtifact);
        return artifacts;
    }

    public Stream<Model> getModels(SpoonPom spoonPom) {
        if (spoonPom.getModules().isEmpty()) {
            return Stream.of(spoonPom.getModel());
        }
        return Stream.concat(
                Stream.of((spoonPom.getModel())),
                spoonPom.getModules().stream().flatMap(this::getModels)
        );
    }

    private Artifact getArtifact(Model model) {
        PomModel pom = new PomModel(model);
        String fqn = String.join(":", pom.getGroupId(), pom.getArtifactId(), pom.getVersion());
        return new Artifact(fqn, model.getArtifactId());
    }

    /**
     * Prints each dependency as found in either dependencies or dependencyManagement sections of POM
     * Excludes test scope and dependencies without explicit version
     * @param model
     */
    private void printDependencies(Model model) {
        List<String> excludedScopes = Arrays.asList("test", "system");
        List<Dependency> dependencies = new ArrayList<>();
        if (model.getDependencyManagement() != null) {
            dependencies.addAll(model.getDependencyManagement().getDependencies());
        }
        dependencies.addAll(model.getDependencies());
        dependencies.stream()
                .filter(d -> !excludedScopes.contains(d.getScope()) && d.getVersion() != null)
                .forEach(this::printDependency);
    }

    private void printDependency(Dependency dep) {
        String version = dep.getVersion();
        if (version.startsWith("${") && version.endsWith("}")) {
            if (version.equals("${project.version}")) {
                version = projectVersion;
            } else {
                return;
            }
        }
        String fqn = String.join(":", dep.getGroupId(), dep.getArtifactId(), version);
        System.out.println("Found maven dependency: " + fqn);
    }

    private void printArtifact(Artifact artifact) {
        System.out.println("Found maven artifact: " + artifact.getFullyQualifiedName());
    }

}
