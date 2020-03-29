package com.york.sdp518.processor;

import com.york.sdp518.Neo4jSessionFactory;
import com.york.sdp518.domain.Class;
import com.york.sdp518.domain.Method;
import com.york.sdp518.domain.Package;
import com.york.sdp518.service.Neo4jService;
import com.york.sdp518.service.impl.MethodNeo4jService;
import com.york.sdp518.spoonvisitors.OutsideMethodVisitor;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class TypeProcessor {

    private static final Logger logger = LoggerFactory.getLogger(TypeProcessor.class);

    private Neo4jService<Method> methodService = new MethodNeo4jService();
    private MethodProcessor methodProcessor;

    boolean useOldMethod = false;

    public TypeProcessor() {
        this.methodProcessor = new MethodProcessor(methodService);
    }

    public void processTypes(Collection<CtType<?>> types) {
        // list all classes
        for(CtType<?> type : types) {
            logger.debug("\nClass: {}", type.getQualifiedName());

            Class clazz = new Class(type.getQualifiedName(), type.getSimpleName());

            // First create declared methods
            Collection<Method> declaredMethods = type.getMethods().stream()
                    .map(methodProcessor::processMethodDeclaration)
                    .collect(Collectors.toSet());
            clazz.addAllDeclaredMethods(declaredMethods);

            // TODO should be one atomic transaction?
            Session session = Neo4jSessionFactory.getInstance().getNeo4jSession();
            Package currentPackage = session.load(Package.class, type.getPackage().getQualifiedName());
            currentPackage.addClass(clazz);
            session.save(currentPackage);

            // Process class level method calls
            OutsideMethodVisitor classLevelVisitor = new OutsideMethodVisitor();
            type.accept(classLevelVisitor);
            Set<Method> calledMethodsFromDB = classLevelVisitor.getCalledMethods().stream()
                    .map(m -> methodService.find(m.getFullyQualifiedName()).orElse(m))
                    .collect(Collectors.toSet());
            clazz.addAllCalledMethods(calledMethodsFromDB);

            // Two methods trading off (memory? and) processing time
            if (useOldMethod) {
                // reads from db, saves each declaration after processing
                type.getMethods().forEach(methodProcessor::processMethodCalls);
            } else {
                // reads declaration from memory, bulk saves once all processed
                Set<Method> updatedDeclarations = type.getMethods().stream()
                        .map(method -> methodProcessor.processMethodCalls2(clazz, method))
                        .collect(Collectors.toSet());
                clazz.addAllDeclaredMethods(updatedDeclarations);
                session.save(clazz);
            }


        }
    }
}
