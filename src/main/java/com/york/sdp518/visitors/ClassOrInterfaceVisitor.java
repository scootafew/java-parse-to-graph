package com.york.sdp518.visitors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.york.sdp518.domain.Class;
import com.york.sdp518.domain.Method;
import com.york.sdp518.domain.Package;

import java.util.HashSet;
import java.util.Set;

public class ClassOrInterfaceVisitor extends VoidVisitorAdapter<Package> {

    private VoidVisitor<Set<Method>> methodDeclarationVisitor = new MethodDeclarationVisitor();
    private VoidVisitor<Set<Method>> methodCallVisitor = new MethodCallVisitor();
    private VoidVisitor<Set<Method>> methodReferenceVisitor = new MethodReferenceVisitor();

    @Override
    public void visit(ClassOrInterfaceDeclaration classDeclaration, Package p) {
        super.visit(classDeclaration, p);

        String fqn = classDeclaration.getFullyQualifiedName()
                .orElseGet(() -> String.join(".", p.getFullyQualifiedName(), classDeclaration.getNameAsString()));
        Class c = new Class(fqn, classDeclaration.getNameAsString());

        Set<Method> declaredMethods = new HashSet<>();
        methodDeclarationVisitor.visit(classDeclaration, declaredMethods);

        Set<Method> calledMethods = new HashSet<>();
        methodCallVisitor.visit(classDeclaration, calledMethods);
        methodReferenceVisitor.visit(classDeclaration, calledMethods);

        c.setDeclaredMethods(declaredMethods);
        c.setCalledMethods(calledMethods);

        p.addClass(c);
    }
}
