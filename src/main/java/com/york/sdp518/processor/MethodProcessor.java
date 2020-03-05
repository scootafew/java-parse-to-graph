package com.york.sdp518.processor;

import com.york.sdp518.domain.Method;
import com.york.sdp518.spoonvisitors.CalledMethodsVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;

public class MethodProcessor {

    private static final Logger logger = LoggerFactory.getLogger(MethodProcessor.class);

    public MethodProcessor() {

    }

    // TODO Check if this includes constructors
    public Method processMethod(CtMethod<?> method) {
        // list method
        String fullyQualifiedSignature = buildFullyQualifiedSignature(method);
        logger.debug("Method: {}", fullyQualifiedSignature);

        Method declaredMethod = new Method(fullyQualifiedSignature, method.getSimpleName());

        CalledMethodsVisitor calledMethodsVisitor = new CalledMethodsVisitor();
        method.accept(calledMethodsVisitor);
        declaredMethod.addAllMethodCalls(calledMethodsVisitor.getCalledMethods());

        return declaredMethod;
    }

    private String buildFullyQualifiedSignature(CtMethod<?> method) {
        return method.getDeclaringType().getQualifiedName() + "." + method.getSignature();
    }
}
