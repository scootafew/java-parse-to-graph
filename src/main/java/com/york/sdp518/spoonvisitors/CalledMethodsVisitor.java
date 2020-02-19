package com.york.sdp518.spoonvisitors;

import com.york.sdp518.domain.Method;
import spoon.reflect.code.CtExecutableReferenceExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.CtScanner;

import java.util.HashSet;
import java.util.Set;

public class CalledMethodsVisitor extends CtScanner {

    private Set<Method> calledMethods;

    public CalledMethodsVisitor() {
        calledMethods = new HashSet<>();
    }

    @Override
    public <T> void visitCtExecutableReference(CtExecutableReference<T> reference) {
        System.out.println("ExecutableReference: " + getQualifiedSignature(reference));
        calledMethods.add(createMethod(reference));
        super.visitCtExecutableReference(reference);
    }

    @Override
    public <T, E extends CtExpression<?>> void visitCtExecutableReferenceExpression(CtExecutableReferenceExpression<T, E> expression) {
        System.out.println("ExecutableReferenceExpression: " + getQualifiedSignature(expression.getExecutable()));
        calledMethods.add(createMethod(expression.getExecutable()));
        super.visitCtExecutableReferenceExpression(expression);
    }

    public Set<Method> getCalledMethods() {
        return calledMethods;
    }

    private <T> String getQualifiedSignature(CtExecutableReference<T> reference) {
        String qualifier = reference.getDeclaringType().getQualifiedName();
        String signature = reference.getSignature();

        if (signature.startsWith(qualifier)) {
            return signature;
        }
        return qualifier + "." + signature;
    }

    private <T> Method createMethod(CtExecutableReference<T> reference) {
        return new Method(getQualifiedSignature(reference), reference.getSimpleName());
    }
}
