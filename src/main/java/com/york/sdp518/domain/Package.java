package com.york.sdp518.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@NodeEntity
public class Package extends Entity {

    @Relationship(type = "CONTAINS")
    Set<Package> packages;

    @Relationship(type = "CONTAINS")
    Set<Class> classes;

    public Package() {
        this.packages = new HashSet<>();
        this.classes = new HashSet<>();
    }

    public Package(String fullyQualifiedName, String name) {
        this();
        setFullyQualifiedName(fullyQualifiedName);
        setName(name);
    }

    public void addPackage(Package p) {
        this.packages.add(p);
    }

    public Set<Package> getPackages() {
        return packages;
    }

    public void addClass(Class c) {
        this.classes.add(c);
    }

    public Set<Class> getClasses() {
        return classes;
    }

    public Optional<Package> getSubPackage(String fqn) {
        return this.packages.stream()
                .filter(p -> p.getName().equals(fqn))
                .findFirst();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Package aPackage = (Package) o;

        return new EqualsBuilder()
                .append(packages, aPackage.packages)
                .append(classes, aPackage.classes)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(packages)
                .append(classes)
                .toHashCode();
    }
}
