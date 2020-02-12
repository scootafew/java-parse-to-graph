package com.york.sdp518.domain;

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
}
