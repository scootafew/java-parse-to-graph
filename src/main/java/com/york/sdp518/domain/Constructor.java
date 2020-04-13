package com.york.sdp518.domain;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class Constructor extends Method {

    public Constructor() {
        super();
    }

    public Constructor(String fullyQualifiedSignature, String name) {
        super(fullyQualifiedSignature, name);
    }
}
