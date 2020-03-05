package com.york.sdp518.processor;

import com.york.sdp518.Neo4jSessionFactory;
import com.york.sdp518.domain.Class;
import com.york.sdp518.domain.Method;
import com.york.sdp518.domain.Package;
import com.york.sdp518.spoonvisitors.OutsideMethodVisitor;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.util.Collection;
import java.util.stream.Collectors;

public class TypeProcessor {

    private static final Logger logger = LoggerFactory.getLogger(TypeProcessor.class);

    private MethodProcessor methodProcessor;

    public TypeProcessor() {
        this.methodProcessor = new MethodProcessor();
    }

    public void processTypes(Collection<CtType<?>> types) {
        // list all classes
        for(CtType<?> type : types) {
            logger.debug("\nClass: {}", type.getQualifiedName());

            Class clazz = new Class(type.getQualifiedName(), type.getSimpleName());

            OutsideMethodVisitor classLevelVisitor = new OutsideMethodVisitor();
            type.accept(classLevelVisitor);
            clazz.addAllCalledMethods(classLevelVisitor.getCalledMethods());

            Collection<Method> declaredMethods = type.getMethods().stream()
                    .map(methodProcessor::processMethod)
                    .collect(Collectors.toSet());
            clazz.addAllDeclaredMethods(declaredMethods);

            Session session = Neo4jSessionFactory.getInstance().getNeo4jSession();
            Package currentPackage = session.load(Package.class, type.getPackage().getQualifiedName());
            currentPackage.addClass(clazz);
            session.save(currentPackage);
        }
    }
}
