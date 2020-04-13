package com.york.sdp518.domain;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class Interface extends Type {

    @Relationship(type = "IMPLEMENTS")
    Set<Interface> interfaces = new HashSet<>();

    public Interface() {
        super();
    }

    public Interface(String fullyQualifiedName, String name) {
        super(fullyQualifiedName, name);
    }

    public void addAllInterfaces(Collection<Interface> interfaces) {
        this.interfaces.addAll(interfaces);
    }
}
