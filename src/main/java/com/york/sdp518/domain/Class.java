package com.york.sdp518.domain;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class Class extends Type {

    @Relationship(type = "EXTENDS")
    Type superType = null;

    @Relationship(type = "IMPLEMENTS")
    Set<Interface> interfaces = new HashSet<>();

    boolean isAbstract = false;

    public Class() {
        super();
    }

    public Class(String fullyQualifiedName, String name) {
        super(fullyQualifiedName, name);
    }

    public void setSuperType(Type superType) {
        this.superType = superType;
    }

    public void addAllInterfaces(Collection<Interface> interfaces) {
        this.interfaces.addAll(interfaces);
    }

    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }
}
