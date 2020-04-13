package com.york.sdp518.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class Method extends Annotatable {

    @Relationship(type = "CALLS")
    Set<Method> methodCalls = new HashSet<>();

    boolean declarationDiscovered = false;

    int lineNumber;

    public Method() { }

    public Method(String fullyQualifiedSignature, String name) {
        this();
        setFullyQualifiedName(fullyQualifiedSignature);
        setName(name);
    }

    public void addAllMethodCalls(Collection<Method> methods) {
        methodCalls.addAll(methods);
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setDiscovered() {
        this.declarationDiscovered = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Method method = (Method) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(57, 11)
                .appendSuper(super.hashCode())
                .toHashCode();
    }
}
