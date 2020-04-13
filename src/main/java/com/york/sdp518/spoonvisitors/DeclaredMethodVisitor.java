package com.york.sdp518.spoonvisitors;

import com.york.sdp518.domain.Constructor;
import com.york.sdp518.domain.Method;
import com.york.sdp518.service.impl.Neo4jServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
        declaredMethod.setLineNumber(method.getPosition().getLine());
        System.out.println("\u001B[31m" + "Method: " + declaredMethod.getFullyQualifiedName() + ", Line: " + method.getPosition().getLine() +  "\u001B[0m");

        CalledMethodsVisitor calledMethodsVisitor = new CalledMethodsVisitor(declarations, neo4jServiceFactory);
        method.accept(calledMethodsVisitor);
        declaredMethod.addAllMethodCalls(calledMethodsVisitor.getCalledMethods());
        methodCache.putAll(calledMethodsVisitor.getCalledMethodsMap());

        System.out.println("\u001B[31m" + "Method: " + declaredMethod.getFullyQualifiedName() + ", Line: " + declaredMethod.getLineNumber() +  "\u001B[0m");

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
//        declaration.getDeclared().setLineNumber(constructor.getPosition().getLine());
//        System.out.println("\u001B[31m" + "Constructor: " + declaredConstructor.getFullyQualifiedName() +  "\u001B[0m");

        CalledMethodsVisitor calledMethodsVisitor = new CalledMethodsVisitor(declarations, neo4jServiceFactory);
        constructor.accept(calledMethodsVisitor);
        declaredConstructor.addAllMethodCalls(calledMethodsVisitor.getCalledMethods());
        methodCache.putAll(calledMethodsVisitor.getCalledMethodsMap());

        declarations.put(fullyQualifiedSignature, declaredConstructor);
        super.visitCtConstructor(constructor);
    }

}