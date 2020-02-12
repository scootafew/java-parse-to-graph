package com.york.sdp518.domain;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

@NodeEntity
public class Method extends Entity {

    @Relationship(type = "DECLARES", direction = Relationship.INCOMING)
    Class clazz;

    @Relationship(type = "CALLS")
    Set<Method> methodCalls;

    public Method() {

    }

    public Method(String fullyQualifiedSignature, String name) {
        setFullyQualifiedName(fullyQualifiedSignature);
        setName(name);
    }
}
