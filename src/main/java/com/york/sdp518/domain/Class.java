package com.york.sdp518.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@NodeEntity
public class Class extends Entity {

    @Transient
    Map<String, Method> declaredMethodMap = new HashMap<>();

    @Relationship(type = "DECLARES")
    Collection<Method> declaredMethods;

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
        methods.forEach(method -> declaredMethodMap.put(method.getFullyQualifiedName(), method));
        declaredMethods = declaredMethodMap.values();
    }

    public void addAllCalledMethods(Collection<Method> methods) {
        calledMethods.addAll(methods);
    }

    public Method getDeclaredMethod(String fqn) {
        return declaredMethodMap.get(fqn);
    }

    public Collection<Method> getDeclaredMethods() {
        return declaredMethods;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Class aClass = (Class) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .toHashCode();
    }
}
