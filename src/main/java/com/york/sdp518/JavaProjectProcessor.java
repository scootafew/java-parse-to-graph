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
import com.york.sdp518.domain.Class;
import com.york.sdp518.domain.Package;
import com.york.sdp518.visitors.ClassOrInterfaceVisitor;
import com.york.sdp518.visitors.MethodCallVisitor;
import com.york.sdp518.visitors.MethodDeclarationVisitor;
import com.york.sdp518.visitors.MethodReferenceVisitor;
import com.york.sdp518.visitors.PackageVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class JavaProjectProcessor {

    private static final Logger logger = LoggerFactory.getLogger(JavaProjectProcessor.class);

    private Path projectPath;
    private ProjectRoot projectRoot;
    private VoidVisitor<?> methodNameVisitor = new MethodDeclarationVisitor();
    private VoidVisitor<?> methodCallReporter = new MethodCallVisitor();
    private VoidVisitor<?> methodReferenceReporter = new MethodReferenceVisitor();
    private VoidVisitor<List<String>> importVisitor = new ImportCounter();

    private VoidVisitor<Set<Package>> packageDeclarationVisitor = new PackageVisitor();
    private VoidVisitor<Package> classOrInterfaceVisitor = new ClassOrInterfaceVisitor();

    public JavaProjectProcessor(Path projectPath) {
        this.projectPath = projectPath;
        this.projectRoot = new SymbolSolverCollectionStrategy().collect(projectPath);
    }

    public void printMethods() {
        this.projectRoot.getSourceRoots().forEach(this::processSourceRoot);
    }

    private void processSourceRoot(SourceRoot sourceRoot) {
        ParserConfiguration config = getParserConfiguration();
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
        System.out.println(compilationUnit);
        Set<Package> packages = new HashSet<>();
        packageDeclarationVisitor.visit(compilationUnit, packages);
        Set<Class> classes = new HashSet<>();
        classOrInterfaceVisitor.visit(compilationUnit, packages.stream().findAny().get());

        System.out.println(packages);

//        System.out.println(Strings.repeat("=", 30) + "\n");
//        printSectionTitle("Primary Types:");
//        printSectionTitle("Method Names:");
//        methodNameVisitor.visit(compilationUnit, null);
//        printSectionTitle("Method Calls:");
//        methodCallReporter.visit(compilationUnit, null);
//        printSectionTitle("Method References:");
//        methodReferenceReporter.visit(compilationUnit, null);
//
//        List<String> imports = new ArrayList<>();
//        importVisitor.visit(compilationUnit, imports);
//        printSectionTitle("Imports (" + imports.size() + "):");
//        imports.forEach(System.out::println);
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

    private Optional<JarTypeSolver> getJarTypeSolver(Path path) {
        try {
            return Optional.of(new JarTypeSolver(path));
        } catch (IOException e) {
            logger.warn("Could not create TypeSolver for jar at path {}", path);
        }
        return Optional.empty();
    }
}
