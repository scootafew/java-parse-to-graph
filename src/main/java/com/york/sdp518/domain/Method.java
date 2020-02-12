package com.york.sdp518.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Method method = (Method) o;

        return new EqualsBuilder()
                .append(clazz, method.clazz)
                .append(methodCalls, method.methodCalls)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(clazz)
                .append(methodCalls)
                .toHashCode();
    }
}
