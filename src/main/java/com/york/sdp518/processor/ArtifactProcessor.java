package com.york.sdp518.processor;

import com.york.sdp518.domain.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import spoon.support.compiler.SpoonPom;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArtifactProcessor {

    public Set<Artifact> processModel(SpoonPom pom) {
        Set<Model> models = getModels(pom).collect(Collectors.toSet());
        models.forEach(this::printDependencies);

        Set<Artifact> artifacts = models.stream()
                .map(this::getArtifact)
                .collect(Collectors.toSet());
        artifacts.forEach(this::printArtifact);
        return artifacts;
    }

    private Stream<Model> getModels(SpoonPom spoonPom) {
        if (spoonPom.getModules().isEmpty()) {
            return Stream.of(spoonPom.getModel());
        }
        return Stream.concat(
                Stream.of((spoonPom.getModel())),
                spoonPom.getModules().stream().flatMap(this::getModels)
        );
    }

    private Artifact getArtifact(Model model) {
        String groupId = getGroupId(model);
        String version = model.getVersion() != null ? model.getVersion() : model.getParent().getVersion();
        String fqn = String.join(":", groupId, model.getArtifactId(), version);
        return new Artifact(fqn, model.getArtifactId());
    }

    private void printDependencies(Model model) {
        List<String> excludedScopes = Arrays.asList("test", "system");
        model.getDependencies().stream()
                .filter(dependency -> !excludedScopes.contains(dependency.getScope()))
                .forEach(this::printDependency);
    }

    private void printDependency(Dependency dep) {
        String fqn = String.join(":", dep.getGroupId(), dep.getArtifactId(), dep.getVersion());
        System.out.println("Found maven dependency: " + fqn);
    }

    private void printArtifact(Artifact artifact) {
        System.out.println("Found maven artifact: " + artifact.getFullyQualifiedName());
    }

    private String getGroupId(Model model) {
        return model.getGroupId() != null ? model.getGroupId() : model.getParent().getGroupId();
    }
}
