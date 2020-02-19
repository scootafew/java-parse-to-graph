package com.york.sdp518.visitors;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.Resolvable;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.york.sdp518.domain.Method;

import java.util.Optional;
import java.util.Set;

public class MethodVisitor<T extends Resolvable<ResolvedMethodDeclaration>> extends VoidVisitorAdapter<Set<Method>> {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";

//    @Override
//    public void visit(MethodDeclaration md, Set<Method> methods) {
//        super.visit(md, methods);
//        try {
//            ResolvedMethodDeclaration rmd = md.resolve();
//            Method m = new Method(rmd.getQualifiedSignature(), rmd.getName());
//
//            methods.add(m);
//        } catch (Exception e) {
//            System.out.println(ANSI_RED + md.getNameAsString() + " : ERROR - " + e.getMessage() + ANSI_RESET);
//        }
//    }

    protected Optional<ResolvedMethodDeclaration> resolve(T node) {
        try {
            return Optional.of(node.resolve());
        } catch (Exception e) {
            System.out.println(ANSI_RED + node.toString() + " : ERROR - " + e.getMessage() + ANSI_RESET);
        }
        return Optional.empty();
    }

    protected Method transformToMethod(ResolvedMethodDeclaration dec) {
        try {
            return new Method(dec.getQualifiedSignature(), dec.getName());
        } catch (Exception e) {
            System.out.println(ANSI_RED + dec.toString() + " : ERROR - " + e.getMessage() + ANSI_RESET);
        }
        return new Method("unknown.package." + dec.getName(), dec.getName());
    }
}
