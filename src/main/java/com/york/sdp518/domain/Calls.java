package com.york.sdp518.domain;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import java.util.Set;

@RelationshipEntity(type = "CALLS")
public class Calls {

    @StartNode
    Method startMethod;

    @EndNode
    Method endMethod;

    Set<Integer> lineNumbers;
}
