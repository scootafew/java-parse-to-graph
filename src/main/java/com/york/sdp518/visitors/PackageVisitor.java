package com.york.sdp518.visitors;

import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.york.sdp518.domain.Package;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class PackageVisitor extends VoidVisitorAdapter<Set<Package>> {

    VoidVisitorAdapter<Set<Package>> nameVisitor = new VoidVisitorAdapter<Set<Package>>() {
        @Override
        public void visit(Name name, Set<Package> packages) {
            super.visit(name, packages);
//            System.out.println(name.asString());
//            System.out.println(name.getIdentifier());
            addNestedPackages(packages, new ArrayList<>(Arrays.asList(name.asString().split("\\."))));
        }

        private Set<Package> addNestedPackages(Set<Package> packages, List<String> name) {
            String current = name.remove(0);
            Package currentPackage = packages.stream()
                    .filter(p -> p.getName().equals(current))
                    .findFirst()
                    .orElseGet(() -> {
                        Package newPackage = new Package(current, current);
                        packages.add(newPackage);
                        return newPackage;
                    });
            if (name.isEmpty()) {
                return packages;
            }
            return addNestedPackages(currentPackage.getPackages(), name);
        }


    };

    @Override
    public void visit(PackageDeclaration pd, Set<Package> packages) {
        super.visit(pd, packages);
        nameVisitor.visit(pd, packages);
    }


}
