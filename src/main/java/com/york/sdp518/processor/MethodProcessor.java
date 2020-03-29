package com.york.sdp518.processor;

import com.york.sdp518.Neo4jSessionFactory;
import com.york.sdp518.domain.Class;
import com.york.sdp518.domain.Method;
import com.york.sdp518.service.Neo4jService;
import com.york.sdp518.spoonvisitors.CalledMethodsVisitor;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;

import java.util.Set;
import java.util.stream.Collectors;

public class MethodProcessor {

    private static final Logger logger = LoggerFactory.getLogger(MethodProcessor.class);

    private Neo4jService<Method> methodService;

    public MethodProcessor(Neo4jService<Method> methodService) {
        this.methodService = methodService;
    }

    // TODO Check if this includes constructors
    public Method processMethodDeclaration(CtMethod<?> method) {
        // list method
        String fullyQualifiedSignature = buildFullyQualifiedSignature(method);
        logger.debug("Method: {}", fullyQualifiedSignature);

        Method declaredMethod = methodService.find(fullyQualifiedSignature)
                .orElse(new Method(fullyQualifiedSignature, method.getSimpleName()));
        declaredMethod.setDiscovered();
        declaredMethod.setLineNumber(method.getPosition().getLine());
        System.out.println("\u001B[31m" + "Method: " + declaredMethod.getFullyQualifiedName() + ", Line: " + method.getPosition().getLine() +  "\u001B[0m");

        return declaredMethod;
    }

    public void processMethodCalls(CtMethod<?> method) {
        String fullyQualifiedSignature = buildFullyQualifiedSignature(method);
        Method declaredMethod = methodService.find(fullyQualifiedSignature)
                .orElse(new Method(fullyQualifiedSignature, method.getSimpleName()));

//        System.out.println("PROCESSING_CALL: " + fullyQualifiedSignature);

        CalledMethodsVisitor calledMethodsVisitor = new CalledMethodsVisitor();
        method.accept(calledMethodsVisitor);

        Session session = Neo4jSessionFactory.getInstance().getNeo4jSession();
        Set<Method> calledMethodsFromDB = calledMethodsVisitor.getCalledMethods().stream()
                .map(m -> methodService.find(m.getFullyQualifiedName()).orElse(m))
                .collect(Collectors.toSet());

        declaredMethod.addAllMethodCalls(calledMethodsFromDB);
        session.save(declaredMethod);
    }

    public Method processMethodCalls2(Class clazz, CtMethod<?> method) {
        String fullyQualifiedSignature = buildFullyQualifiedSignature(method);
//        Method declaredMethod = methodService.find(fullyQualifiedSignature)
//                .orElse(new Method(fullyQualifiedSignature, method.getSimpleName()));
        Method declaredMethod = clazz.getDeclaredMethod(fullyQualifiedSignature);

        System.out.println("PROCESSING_CALL: " + fullyQualifiedSignature);

        CalledMethodsVisitor calledMethodsVisitor = new CalledMethodsVisitor();
        method.accept(calledMethodsVisitor);

        Set<Method> calledMethodsFromDB = calledMethodsVisitor.getCalledMethods().stream()
                .map(m -> methodService.find(m.getFullyQualifiedName()).orElse(m))
                .collect(Collectors.toSet());

        declaredMethod.addAllMethodCalls(calledMethodsFromDB);
        return declaredMethod;
    }

    private String buildFullyQualifiedSignature(CtMethod<?> method) {
        return method.getDeclaringType().getQualifiedName() + "." + method.getSignature();
    }
}
