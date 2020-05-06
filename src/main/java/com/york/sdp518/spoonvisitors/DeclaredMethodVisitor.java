package com.york.sdp518.spoonvisitors;

import com.york.sdp518.domain.Annotation;
import com.york.sdp518.domain.Constructor;
import com.york.sdp518.domain.Method;
import com.york.sdp518.service.impl.Neo4jServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.CtVisitable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DeclaredMethodVisitor extends MethodVisitor {

    private static final Logger logger = LoggerFactory.getLogger(DeclaredMethodVisitor.class);

    private Map<String, Method> declarations;

    public DeclaredMethodVisitor(Neo4jServiceFactory neo4jServiceFactory) {
        super(neo4jServiceFactory);
        this.declarations = new HashMap<>();
    }

    public Collection<Method> getDeclarations() {
        return declarations.values();
    }

    @Override
    public <T> void visitCtMethod(CtMethod<T> method) {
        // list method
        String fullyQualifiedSignature = buildFullyQualifiedSignature(method);
        logger.debug("Method: {}", fullyQualifiedSignature);

        Method declaredMethod = declarations.getOrDefault(fullyQualifiedSignature, getOrCreateMethod(method.getReference()));
        declaredMethod.setDiscovered();
        if (method.getPosition().isValidPosition()) {
            declaredMethod.setLineNumber(method.getPosition().getLine());
        }

        // Visit method calls in declaration
        declaredMethod.addAllMethodCalls(getMethodCalls(method));
        declaredMethod.addAllAnnotations(getAnnotations(method));

        declarations.put(fullyQualifiedSignature, declaredMethod);
        super.visitCtMethod(method);
    }


    @Override
    public <T> void visitCtConstructor(CtConstructor<T> constructor) {
        // list constructor
        String fullyQualifiedSignature = getQualifiedSignature(constructor.getReference());
        logger.debug("Constructor: {}", fullyQualifiedSignature);

        Constructor declaredConstructor = (Constructor) declarations.getOrDefault(fullyQualifiedSignature, getOrCreateConstructor(constructor.getReference()));
        declaredConstructor.setDiscovered();

        declaredConstructor.addAllMethodCalls(getMethodCalls(constructor));
        declaredConstructor.addAllAnnotations(getAnnotations(constructor));

        declarations.put(fullyQualifiedSignature, declaredConstructor);
        super.visitCtConstructor(constructor);
    }

    private <K extends CtVisitable> Collection<Method> getMethodCalls(K declaration) {
        CalledMethodsVisitor calledMethodsVisitor = new CalledMethodsVisitor(declarations, neo4jServiceFactory);
        declaration.accept(calledMethodsVisitor);
        methodCache.putAll(calledMethodsVisitor.getCalledMethodsMap());
        return calledMethodsVisitor.getCalledMethods();
    }

    private Collection<Annotation> getAnnotations(CtElement element) {
        return element.getAnnotations().stream()
                .map(CtAnnotation::getAnnotationType)
                .map(ctTypeReference -> new Annotation(ctTypeReference.getQualifiedName(), ctTypeReference.getSimpleName()))
                .collect(Collectors.toSet());
    }

}
