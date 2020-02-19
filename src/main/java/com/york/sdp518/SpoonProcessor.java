package com.york.sdp518;

import com.york.sdp518.domain.Class;
import com.york.sdp518.domain.Method;
import com.york.sdp518.domain.Package;
import com.york.sdp518.spoonvisitors.CalledMethodsVisitor;
import com.york.sdp518.spoonvisitors.OutsideMethodVisitor;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;
import spoon.MavenLauncher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;

import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

public class SpoonProcessor {

    private Path projectPath;

    public SpoonProcessor(Path projectPath) {
        this.projectPath = projectPath;
    }

    public void run() {
        MavenLauncher launcher = new MavenLauncher(projectPath.toString(), MavenLauncher.SOURCE_TYPE.APP_SOURCE, true);
        launcher.buildModel();
        CtModel model = launcher.getModel();
        System.out.println(model);

        processPackages(model.getAllPackages());
    }

    private void processPackages(Collection<CtPackage> packages) {
        // list all packages
        for(CtPackage p : packages) {
            if (!p.isUnnamedPackage()) {
                System.out.println("\n====================================================");
                System.out.println("Package: " + p.getQualifiedName());
                createPackageIfNotExists(p.getQualifiedName());
                processTypes(p.getTypes());
            }

        }
    }

    private void processTypes(Collection<CtType<?>> types) {
        // list all classes
        for(CtType<?> type : types) {
            System.out.println("\nClass: " + type.getQualifiedName());

            Class clazz = new Class(type.getQualifiedName(), type.getSimpleName());

            OutsideMethodVisitor classLevelVisitor = new OutsideMethodVisitor();
            type.accept(classLevelVisitor);
            clazz.addAllCalledMethods(classLevelVisitor.getCalledMethods());

            Collection<Method> declaredMethods = type.getMethods().stream()
                    .map(this::processMethod)
                    .collect(Collectors.toSet());
            clazz.addAllDeclaredMethods(declaredMethods);

            Session session = Neo4jSessionFactory.getInstance().getNeo4jSession();
            Package currentPackage = session.load(Package.class, type.getPackage().getQualifiedName());
            currentPackage.addClass(clazz);
            session.save(currentPackage);
        }
    }

    private Method processMethod(CtMethod<?> method) {
        // list method
        System.out.println("Method: " + buildFullyQualifiedSignature(method));

        Method declaredMethod = new Method(buildFullyQualifiedSignature(method), method.getSimpleName());

        CalledMethodsVisitor calledMethodsVisitor = new CalledMethodsVisitor();
        method.accept(calledMethodsVisitor);
        declaredMethod.addAllMethodCalls(calledMethodsVisitor.getCalledMethods());

        return declaredMethod;
    }

    private String buildFullyQualifiedSignature(CtMethod<?> method) {
        return method.getDeclaringType().getQualifiedName() + "." + method.getSignature();
    }

    // New method following package structure more efficiently
    private void createPackageIfNotExists(String qualifiedPackageName) {
        Session session = Neo4jSessionFactory.getInstance().getNeo4jSession();
        try (Transaction tx = session.beginTransaction()) {
            Package currentPackage = session.load(Package.class, qualifiedPackageName);

            if (currentPackage == null) {
                String parentPackageQualifiedName = getParentPackageQualifiedName(qualifiedPackageName);
                String currentPackageName = getCurrentPackageQualifiedName(qualifiedPackageName);
                currentPackage = new Package(qualifiedPackageName, currentPackageName);
                Package packageForCreation = getParentOrCreate(parentPackageQualifiedName, currentPackage, session);

                session.save(packageForCreation);
                tx.commit();
            }

        }
    }

    private Package getParentOrCreate(String qualifiedPackageName, Package childPackage, Session session) {
        if (qualifiedPackageName.isEmpty()) {
            return childPackage;
        }

        Package currentPackage = session.load(Package.class, qualifiedPackageName);
        if (currentPackage != null) {
            currentPackage.getPackages().add(childPackage);
            return currentPackage;
        } else {
            String parentPackageQualifiedName = getParentPackageQualifiedName(qualifiedPackageName);
            String currentPackageName = getCurrentPackageQualifiedName(qualifiedPackageName);
            currentPackage = new Package(qualifiedPackageName, currentPackageName);
            currentPackage.getPackages().add(childPackage);
            return getParentOrCreate(parentPackageQualifiedName, currentPackage, session);
        }
    }

    private String getParentPackageQualifiedName(String qualifiedName) {
        return qualifiedName.contains(".") ? StringUtils.substringBeforeLast(qualifiedName, ".") : "";
    }

    private String getCurrentPackageQualifiedName(String qualifiedName) {
        return qualifiedName.contains(".") ? StringUtils.substringAfterLast(qualifiedName, ".") : qualifiedName;
    }


}
