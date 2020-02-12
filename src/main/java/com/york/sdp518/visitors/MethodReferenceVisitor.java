package com.york.sdp518.visitors;

import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.york.sdp518.domain.Method;

import java.util.Set;

public class MethodReferenceVisitor extends MethodVisitor<MethodReferenceExpr> {

    @Override
    public void visit(MethodReferenceExpr methodReferenceExpr, Set<Method> methods) {
        super.visit(methodReferenceExpr, methods);
        resolve(methodReferenceExpr).ifPresent(resolvedMethodDeclaration -> {
            Method method = transformToMethod(resolvedMethodDeclaration);
            methods.add(method);
        });
    }
}
