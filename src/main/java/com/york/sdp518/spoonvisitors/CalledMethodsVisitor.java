package com.york.sdp518.spoonvisitors;

import com.york.sdp518.domain.Method;
import com.york.sdp518.service.impl.Neo4jServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.reference.CtExecutableReference;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CalledMethodsVisitor extends MethodVisitor {

    private static final Logger logger = LoggerFactory.getLogger(CalledMethodsVisitor.class);

    private Map<String, Method> calledMethods;

    public CalledMethodsVisitor(Map<String, Method> declarations, Neo4jServiceFactory neo4jServiceFactory) {
        super(declarations, neo4jServiceFactory);
        calledMethods = new HashMap<>();
    }

    @Override
    public <K> void visitCtExecutableReference(CtExecutableReference<K> reference) {
        String fullyQualifiedSignature = getQualifiedSignature(reference);
        logger.debug("Method Call: {}", fullyQualifiedSignature);
        Method method = getOrCreateMethod(reference);
        calledMethods.put(fullyQualifiedSignature, method);
        super.visitCtExecutableReference(reference);
    }

    public Collection<Method> getCalledMethods() {
        return calledMethods.values();
    }

    public Map<String, Method> getCalledMethodsMap() {
        return calledMethods;
    }

}
