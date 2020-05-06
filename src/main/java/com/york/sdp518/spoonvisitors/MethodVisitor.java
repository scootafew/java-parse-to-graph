package com.york.sdp518.spoonvisitors;

import com.york.sdp518.domain.Constructor;
import com.york.sdp518.domain.Method;
import com.york.sdp518.service.Neo4jService;
import com.york.sdp518.service.impl.Neo4jServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;

import java.util.HashMap;
import java.util.Map;

public class MethodVisitor extends CtScanner {

    private static final Logger logger = LoggerFactory.getLogger(MethodVisitor.class);

    Neo4jServiceFactory neo4jServiceFactory;
    Neo4jService<Method> methodService;
    Neo4jService<Constructor> constructorService;

    /**
     * This is used to share state between visitors. For example, a method declaration may have been discovered, but
     * not yet persisted in the database. Therefore when a method call to it is discovered the process should be
     * check if method exists in DB else -> check if method exists in cache else -> create method
     * This is implemented in:
     * @see #getOrCreateMethod(spoon.reflect.reference.CtExecutableReference)
     * @see #getOrCreateConstructor(spoon.reflect.reference.CtExecutableReference)
     */
    Map<String, Method> methodCache;

    MethodVisitor(Neo4jServiceFactory neo4jServiceFactory) {
        this(new HashMap<>(), neo4jServiceFactory);
    }

    MethodVisitor(Map<String, Method> methodCache, Neo4jServiceFactory neo4jServiceFactory) {
        this.methodCache = methodCache;
        this.neo4jServiceFactory = neo4jServiceFactory;
        this.methodService = neo4jServiceFactory.getServiceForClass(Method.class);
        this.constructorService = neo4jServiceFactory.getServiceForClass(Constructor.class);
    }

    String buildFullyQualifiedSignature(CtMethod<?> method) {
        return method.getDeclaringType().getQualifiedName() + "." + method.getSignature();
    }

    <K> String getQualifiedSignature(CtExecutableReference<K> reference) {
        CtTypeReference<?> dt = reference.getDeclaringType();
        if (dt != null) {
            String qualifier = reference.getDeclaringType().getQualifiedName();
            String signature = reference.getSignature();

            if (signature.startsWith(qualifier)) {
                return signature;
            }
            return qualifier + "." + signature;
        } else {
            logger.warn("No declaring type found for node {}", reference);
            logger.warn("Using signature {} instead", reference.getSignature());
        }
        return reference.getSignature();
    }

    <K> Method getOrCreateMethod(CtExecutableReference<K> reference) {
        if (reference.isConstructor()) {
            return getOrCreateConstructor(reference);
        }
        String fullyQualifiedSignature = getQualifiedSignature(reference);
        // first search found but not saved to db, then db, if not found then create
        return methodCache.getOrDefault(fullyQualifiedSignature,
                methodService.find(fullyQualifiedSignature)
                        .orElse(new Method(fullyQualifiedSignature, reference.getSimpleName())));
    }

    <K> Constructor getOrCreateConstructor(CtExecutableReference<K> reference) {
        String fullyQualifiedSignature = getQualifiedSignature(reference);
        // first search found but not saved to db, then db, if not found then create
        return (Constructor) methodCache.getOrDefault(fullyQualifiedSignature,
                constructorService.find(fullyQualifiedSignature)
                        .orElse(new Constructor(fullyQualifiedSignature, reference.getSimpleName())));
    }
}
