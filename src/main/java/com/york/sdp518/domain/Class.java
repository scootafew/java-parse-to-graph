package com.york.sdp518.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class Class extends Entity {

    @Relationship(type = "DECLARES")
    Set<Method> declaredMethods;

    @Relationship(type = "CALLS")
    Set<Method> calledMethods;

    public Class() {
        declaredMethods = new HashSet<>();
        calledMethods = new HashSet<>();
    }

    public Class(String fullyQualifiedName, String name) {
        this();
        setFullyQualifiedName(fullyQualifiedName);
        setName(name);
    }

    public void addAllDeclaredMethods(Collection<Method> methods) {
        declaredMethods.addAll(methods);
    }

    public void addAllCalledMethods(Collection<Method> methods) {
        calledMethods.addAll(methods);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Class aClass = (Class) o;

        return new EqualsBuilder()
                .append(declaredMethods, aClass.declaredMethods)
                .append(calledMethods, aClass.calledMethods)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(declaredMethods)
                .append(calledMethods)
                .toHashCode();
    }
}
