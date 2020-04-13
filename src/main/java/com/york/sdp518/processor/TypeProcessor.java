package com.york.sdp518.processor;

import com.york.sdp518.Neo4jSessionFactory;
import com.york.sdp518.domain.Type;
import com.york.sdp518.domain.Method;
import com.york.sdp518.domain.Package;
import com.york.sdp518.service.impl.Neo4jServiceFactory;
import com.york.sdp518.spoonvisitors.DeclaredMethodVisitor;
import com.york.sdp518.spoonvisitors.ClassLevelMethodCallVisitor;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import spoon.reflect.declaration.CtType;

import java.util.Collection;

@Service
public class TypeProcessor {

    private static final Logger logger = LoggerFactory.getLogger(TypeProcessor.class);

    private Neo4jServiceFactory neo4jServiceFactory;

    public TypeProcessor(Neo4jServiceFactory neo4jServiceFactory) {
        this.neo4jServiceFactory = neo4jServiceFactory;
    }

    public void processTypes(Collection<CtType<?>> types) {
        // list all classes
        for(CtType<?> ctType : types) {
            logger.debug("\nClass: {}", ctType.getQualifiedName());

            Type type = new Type(ctType.getQualifiedName(), ctType.getSimpleName());

            // Process declarations (includes method calls in declarations)
            DeclaredMethodVisitor declaredMethodVisitor = new DeclaredMethodVisitor(neo4jServiceFactory);
            ctType.accept(declaredMethodVisitor);
            Collection<Method> declarations = declaredMethodVisitor.getDeclarations();
            type.addAllDeclaredMethods(declarations);

            // TODO should be one atomic transaction?
            Session session = Neo4jSessionFactory.getInstance().getNeo4jSession();
            Package currentPackage = session.load(Package.class, ctType.getPackage().getQualifiedName());
            currentPackage.addClass(type);
            session.save(currentPackage);

            // Process class level method calls
            ClassLevelMethodCallVisitor classLevelVisitor = new ClassLevelMethodCallVisitor(type.getDeclaredMethodMap(), neo4jServiceFactory);
            ctType.accept(classLevelVisitor);
            Collection<Method> calls = classLevelVisitor.getCalledMethods();
            type.addAllCalledMethods(calls);

            session.save(type);

        }
    }
}
