package com.york.sdp518;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.base.Strings;
import com.york.sdp518.domain.Package;
import com.york.sdp518.visitors.ClassOrInterfaceVisitor;
import com.york.sdp518.visitors.PackageVisitor;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class JavaProjectProcessor {

    private static final Logger logger = LoggerFactory.getLogger(JavaProjectProcessor.class);

    private Path projectPath;
    private ProjectRoot projectRoot;

    private VoidVisitor<List<String>> packageDeclarationVisitor = new PackageVisitor();
    private VoidVisitor<Package> classOrInterfaceVisitor = new ClassOrInterfaceVisitor();

    public JavaProjectProcessor(Path projectPath) {
        this.projectPath = projectPath;
        this.projectRoot = new SymbolSolverCollectionStrategy().collect(projectPath);
    }

    public void printMethods() {
        this.projectRoot.getSourceRoots().stream()
                .peek(s -> System.out.println(s.getRoot().toString()))
                .filter(this::isViableRoot)
                .forEach(this::processSourceRoot);
    }

    private boolean isViableRoot(SourceRoot sourceRoot) {
        return sourceRoot.getRoot().toString().endsWith("\\src\\main\\java");
    }

    private void processSourceRoot(SourceRoot sourceRoot) {
        ParserConfiguration config = getParserConfiguration(sourceRoot);
        try {
            sourceRoot.setParserConfiguration(config).tryToParse().stream()
                    .map(ParseResult::getResult)
                    .map(Optional::get)
                    .forEach(this::processCompilationUnit);


        } catch (IOException e) {
            logger.error("IO Exception");
        }
    }

    private void processCompilationUnit(CompilationUnit compilationUnit) {
        compilationUnit.getPrimaryTypeName().ifPresent(System.out::println);
//        System.out.println(compilationUnit);

        String fullyQualifiedName = processPackages(compilationUnit);

        Session session = Neo4jSessionFactory.getInstance().getNeo4jSession();
        Package p = new Package(fullyQualifiedName, fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf(".")));
        classOrInterfaceVisitor.visit(compilationUnit, p);

        Package currentPackage = session.load(Package.class, fullyQualifiedName);
        currentPackage.getClasses().addAll(p.getClasses());
        session.save(currentPackage);
    }

    private String processPackages(CompilationUnit compilationUnit) {
        List<String> packages = new ArrayList<>();
        packageDeclarationVisitor.visit(compilationUnit, packages);

        String fullyQualifiedPackageName = String.join(".", packages);

        if (fullyQualifiedPackageName.isEmpty()) {
            logger.warn("No package declaration found in file " + compilationUnit.getStorage().get().getFileName()
                    + ", possible parsing error");
        } else {
            createPackageIfNotExists(packages);
        }

//        System.out.println(packages);
//        createPackageIfNotExists(packages, null);
        return fullyQualifiedPackageName;
    }

    // TODO Improve to search for packages in reverse order to improve efficiency
    private void createPackageIfNotExists(List<String> hierarchy, String qualifier) {
        if (hierarchy.isEmpty()) return;

        if (hierarchy.size() > 1) {
            String currentName = hierarchy.remove(0);
            String qualifiedName = buildQualifiedName(qualifier, currentName);
            String nextName = hierarchy.get(0);
            String qualifiedNextName = buildQualifiedName(qualifiedName, nextName);

            Session session = Neo4jSessionFactory.getInstance().getNeo4jSession();
            try (Transaction tx = session.beginTransaction()) {
                Package currentPackage = session.load(Package.class, qualifiedName);

                if (currentPackage == null) {
                    currentPackage = new Package(qualifiedName, currentName);
                }

                Optional<Package> subPackage = currentPackage.getPackages().stream()
                        .filter(p -> p.getFullyQualifiedName().equals(qualifiedNextName))
                        .findFirst();
                if (!subPackage.isPresent()) {
                    Package newPackage = new Package(qualifiedNextName, nextName);
                    System.out.println("Adding new package: " + qualifiedNextName);
                    currentPackage.getPackages().add(newPackage);
                }
                session.save(currentPackage);
                tx.commit();
            }
            createPackageIfNotExists(hierarchy, qualifiedName);
        }
    }

    // New method following package structure more efficiently
    private void createPackageIfNotExists(List<String> packageHierarchy) {
        String fullyQualifiedPackageName = buildQualifiedName(packageHierarchy);

        Session session = Neo4jSessionFactory.getInstance().getNeo4jSession();
        try (Transaction tx = session.beginTransaction()) {
            Package currentPackage = session.load(Package.class, fullyQualifiedPackageName);

            if (currentPackage == null) {
                String currentPackageName = packageHierarchy.remove(packageHierarchy.size() - 1);
                Package newPackage = new Package(fullyQualifiedPackageName, currentPackageName);
                Package packageForCreation = getParentOrCreate(packageHierarchy, newPackage, session);

                session.save(packageForCreation);
                tx.commit();
            }

        }
    }

    private Package getParentOrCreate(List<String> packageHierarchy, Package subPackage, Session session) {
        if (packageHierarchy.isEmpty()) {
            return subPackage;
        }

        String fullyQualifiedPackageName = buildQualifiedName(packageHierarchy);
        Package currentPackage = session.load(Package.class, fullyQualifiedPackageName);
        if (currentPackage != null) {
            currentPackage.getPackages().add(subPackage);
            return currentPackage;
        } else {
            String currentPackageName = packageHierarchy.remove(packageHierarchy.size() - 1);
            Package newPackage = new Package(fullyQualifiedPackageName, currentPackageName);
            newPackage.getPackages().add(subPackage);
            return getParentOrCreate(packageHierarchy, newPackage, session);
        }
    }

    private String buildQualifiedName(String qualifier, String name) {
        if (qualifier == null) {
            return name;
        }
        return String.join(".", qualifier, name);
    }

    private String buildQualifiedName(List<String> packageHierarchy) {
        return String.join(".", packageHierarchy);
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

    private void printSectionTitle(String title) {
        System.out.println("\n" + title);
        System.out.println(Strings.repeat("=", title.length()));
    }

    private ParserConfiguration getParserConfiguration() {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_8);
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        typeSolver.setExceptionHandler(CombinedTypeSolver.ExceptionHandlers.IGNORE_ALL);

        projectRoot.getSourceRoots().stream()
                .map(SourceRoot::getRoot)
                .peek(p -> System.out.println(p.toString()))
                .map(JavaParserTypeSolver::new)
                .forEach(typeSolver::add);

        try (Stream<Path> paths = Files.walk(projectPath.resolve("target/dependency"))) {
            paths.filter(p -> p.toString().endsWith(".jar"))
                    .map(this::getJarTypeSolver)
                    .map(Optional::get)
                    .forEach(typeSolver::add);

        } catch (IOException e) {
            e.printStackTrace();
        }

        config.setSymbolResolver(new JavaSymbolSolver(typeSolver));
        return config;
    }

    private ParserConfiguration getParserConfiguration(SourceRoot sourceRoot) {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_8);
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        typeSolver.setExceptionHandler(CombinedTypeSolver.ExceptionHandlers.IGNORE_ALL);

        typeSolver.add(new JavaParserTypeSolver(sourceRoot.getRoot()));

        try (Stream<Path> paths = Files.walk(sourceRoot.getRoot().resolve("../../../target/dependency"))) {
            paths.filter(p -> p.toString().endsWith(".jar"))
                    .map(this::getJarTypeSolver)
                    .map(Optional::get)
                    .forEach(typeSolver::add);

        } catch (IOException e) {
            e.printStackTrace();
        }

        config.setSymbolResolver(new JavaSymbolSolver(typeSolver));
        return config;
    }

    private Optional<JarTypeSolver> getJarTypeSolver(Path path) {
        try {
            return Optional.of(new JarTypeSolver(path));
        } catch (IOException e) {
            logger.warn("Could not create TypeSolver for jar at path {}", path);
        }
        return Optional.empty();
    }
}
