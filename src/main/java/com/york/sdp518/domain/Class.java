package com.york.sdp518.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

@NodeEntity
public class Class extends Entity {

    @Relationship(type = "DECLARES")
    Set<Method> declaredMethods;

    @Relationship(type = "CALLS")
    Set<Method> calledMethods;

    public Class() {

    }

    public Class(String fullyQualifiedName, String name) {
        setFullyQualifiedName(fullyQualifiedName);
        setName(name);
    }

    public void setDeclaredMethods(Set<Method> declaredMethods) {
        this.declaredMethods = declaredMethods;
    }

    public void setCalledMethods(Set<Method> calledMethods) {
        this.calledMethods = calledMethods;
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
