package com.york.sdp518.visitors;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.york.sdp518.domain.Method;

import java.util.Set;

public class MethodDeclarationVisitor extends MethodVisitor<MethodDeclaration> {

    @Override
    public void visit(MethodDeclaration methodDeclaration, Set<Method> methods) {
        super.visit(methodDeclaration, methods);
        resolve(methodDeclaration).ifPresent(resolvedMethodDeclaration -> {
            Method method = transformToMethod(resolvedMethodDeclaration);
            methods.add(method);
        });

    }
}
