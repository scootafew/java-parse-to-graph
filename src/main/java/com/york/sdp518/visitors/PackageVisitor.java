package com.york.sdp518.visitors;

import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.Arrays;
import java.util.List;

@Deprecated
public class PackageVisitor extends VoidVisitorAdapter<List<String>> {

    @Override
    public void visit(PackageDeclaration pd, List<String> packageStructure) {
        super.visit(pd, packageStructure);
        List<String> packageList = Arrays.asList(pd.getName().asString().split("\\."));
        packageStructure.addAll(packageList);
    }

}
