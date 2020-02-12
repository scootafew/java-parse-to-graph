package com.york.sdp518;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

public class ImportCounter extends VoidVisitorAdapter<List<String>> {

    @Override
    public void visit(ImportDeclaration id, List<String> collector) {
        super.visit(id, collector);
        collector.add(id.getNameAsString());
    }
}
