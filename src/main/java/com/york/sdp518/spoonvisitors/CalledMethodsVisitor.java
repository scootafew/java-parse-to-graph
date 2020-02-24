package com.york.sdp518.spoonvisitors;

import com.york.sdp518.domain.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtExecutableReferenceExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;

import java.util.HashSet;
import java.util.Set;

public class CalledMethodsVisitor extends CtScanner {

    private static final Logger logger = LoggerFactory.getLogger(CalledMethodsVisitor.class);
    private Set<Method> calledMethods;

    public CalledMethodsVisitor() {
        calledMethods = new HashSet<>();
    }

    @Override
    public <T> void visitCtExecutableReference(CtExecutableReference<T> reference) {
        logger.debug("Method Call: {}", getQualifiedSignature(reference));
        calledMethods.add(createMethod(reference));
        super.visitCtExecutableReference(reference);
    }

    @Override
    public <T, E extends CtExpression<?>> void visitCtExecutableReferenceExpression(CtExecutableReferenceExpression<T, E> expression) {
        logger.debug("Method Reference: {}", getQualifiedSignature(expression.getExecutable()));
        calledMethods.add(createMethod(expression.getExecutable()));
        super.visitCtExecutableReferenceExpression(expression);
    }

    public Set<Method> getCalledMethods() {
        return calledMethods;
    }

    private <T> String getQualifiedSignature(CtExecutableReference<T> reference) {
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

    private <T> Method createMethod(CtExecutableReference<T> reference) {
        return new Method(getQualifiedSignature(reference), reference.getSimpleName());
    }
}
