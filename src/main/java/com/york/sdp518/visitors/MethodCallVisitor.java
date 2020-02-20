package com.york.sdp518.visitors;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.york.sdp518.domain.Method;

import java.util.Set;

@Deprecated
public class MethodCallVisitor extends MethodVisitor<MethodCallExpr> {

    @Override
    public void visit(MethodCallExpr methodCallExpr, Set<Method> methods) {
        super.visit(methodCallExpr, methods);
        resolve(methodCallExpr).ifPresent(resolvedMethodDeclaration -> {
            Method method = transformToMethod(resolvedMethodDeclaration);
            methods.add(method);
        });

    }
}
