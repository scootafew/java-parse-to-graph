//package com.york.sdp518.domain;
//
//import org.apache.commons.lang3.builder.EqualsBuilder;
//import org.apache.commons.lang3.builder.HashCodeBuilder;
//import org.neo4j.ogm.annotation.EndNode;
//import org.neo4j.ogm.annotation.GeneratedValue;
//import org.neo4j.ogm.annotation.Id;
//import org.neo4j.ogm.annotation.RelationshipEntity;
//import org.neo4j.ogm.annotation.StartNode;
//
//import java.util.Collection;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.Set;
//
//@RelationshipEntity(type = "CALLS")
//public class Call {
//
//    @Id
//    @GeneratedValue
//    private Long callId;
//
//    @StartNode
//    Entity startEntity;
//
//    @EndNode
//    Method endMethod;
//
//    Set<Integer> lineNumbers = new HashSet<>();
//
//    public Call() {
//
//    }
//
//    public Call(Entity start, Method end) {
//        this.startEntity = start;
//        this.endMethod = end;
//    }
//
//    public void addLineNumber(int lineNumber) {
//        this.lineNumbers.addAll(Collections.singleton(lineNumber));
//    }
//
//    public void addLineNumbers(Collection<Integer> lineNumberCollection) {
//        this.lineNumbers.addAll(lineNumberCollection);
//    }
//
//    public void setEndMethod(Method endMethod) {
//        this.endMethod = endMethod;
//    }
//
//    public Method getEndMethod() {
//        return endMethod;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//
//        if (o == null || getClass() != o.getClass()) return false;
//
//        Call call = (Call) o;
//
//        return new EqualsBuilder()
//                .append(lineNumbers, call.lineNumbers)
//                .append(startEntity, call.startEntity)
//                .append(endMethod, call.endMethod)
//                .isEquals();
//    }
//
//    @Override
//    public int hashCode() {
//        return new HashCodeBuilder(17, 37)
//                .append(startEntity)
//                .append(endMethod)
//                .append(lineNumbers)
//                .toHashCode();
//    }
//}
