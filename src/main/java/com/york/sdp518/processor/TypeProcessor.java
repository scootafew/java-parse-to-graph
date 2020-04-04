package com.york.sdp518.processor;

import com.york.sdp518.Neo4jSessionFactory;
import com.york.sdp518.domain.Call;
import com.york.sdp518.domain.Type;
import com.york.sdp518.domain.Method;
import com.york.sdp518.domain.Package;
import com.york.sdp518.service.Neo4jService;
import com.york.sdp518.service.impl.MethodNeo4jService;
import com.york.sdp518.spoonvisitors.OutsideMethodVisitor;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import spoon.reflect.declaration.CtType;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TypeProcessor {

    private static final Logger logger = LoggerFactory.getLogger(TypeProcessor.class);

    private Neo4jService<Method> methodService;
    private MethodProcessor methodProcessor;

    boolean useOldMethod = false;

    public TypeProcessor(MethodProcessor methodProcessor, Neo4jService<Method> methodService) {
        this.methodProcessor = methodProcessor;
        this.methodService = methodService;
    }

    public void processTypes(Collection<CtType<?>> types) {
        // list all classes
        for(CtType<?> ctType : types) {
            logger.debug("\nClass: {}", ctType.getQualifiedName());

            Type type = new Type(ctType.getQualifiedName(), ctType.getSimpleName());

            // First create declared methods
            Collection<Method> declaredMethods = ctType.getMethods().stream()
                    .map(methodProcessor::processMethodDeclaration)
                    .collect(Collectors.toSet());
            type.addAllDeclaredMethods(declaredMethods);

            // TODO should be one atomic transaction?
            Session session = Neo4jSessionFactory.getInstance().getNeo4jSession();
            Package currentPackage = session.load(Package.class, ctType.getPackage().getQualifiedName());
            currentPackage.addClass(type);
            session.save(currentPackage);

            // Process class level method calls
            OutsideMethodVisitor classLevelVisitor = new OutsideMethodVisitor(type);
            ctType.accept(classLevelVisitor);

            Collection<Call> calls = classLevelVisitor.getCalledMethods();
            calls.forEach(c -> methodService.find(c.getEndMethod().getFullyQualifiedName()).ifPresent(c::setEndMethod));
            type.addAllCalledMethods(calls);

            // Two methods trading off (memory? and) processing time
            if (useOldMethod) {
                // reads from db, saves each declaration after processing
                ctType.getMethods().forEach(methodProcessor::processMethodCalls);
            } else {
                // reads declaration from memory, bulk saves once all processed
                Set<Method> updatedDeclarations = ctType.getMethods().stream()
                        .map(method -> methodProcessor.processMethodCalls2(type, method))
                        .collect(Collectors.toSet());
                type.addAllDeclaredMethods(updatedDeclarations);
                session.save(type);
            }


        }
    }
}
