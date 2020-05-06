package com.york.sdp518.processor;

import com.york.sdp518.domain.Annotation;
import com.york.sdp518.domain.Class;
import com.york.sdp518.domain.Enum;
import com.york.sdp518.domain.Interface;
import com.york.sdp518.domain.Type;
import com.york.sdp518.domain.Method;
import com.york.sdp518.domain.Package;
import com.york.sdp518.service.Neo4jService;
import com.york.sdp518.service.impl.Neo4jServiceFactory;
import com.york.sdp518.spoonvisitors.DeclaredMethodVisitor;
import com.york.sdp518.spoonvisitors.ClassLevelMethodCallVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeInformation;
import spoon.reflect.declaration.ModifierKind;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

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

            Type type = createType(ctType);

            Set<String> modifiers = ctType.getModifiers().stream().map(ModifierKind::toString).collect(Collectors.toSet());
            type.addAllModifiers(modifiers);
            type.addAllAnnotations(getAnnotations(ctType));

            // Process declarations (includes method calls in declarations)
            visitTypeContents(ctType, type);

            // TODO should be one atomic transaction?
            Neo4jService<Package> packageService = neo4jServiceFactory.getServiceForClass(Package.class);
            packageService.find(ctType.getPackage().getQualifiedName()).ifPresent(currentPackage -> {
                currentPackage.addClass(type);
                packageService.createOrUpdate(currentPackage);
            });


            // Process class level method calls
            ClassLevelMethodCallVisitor classLevelVisitor = new ClassLevelMethodCallVisitor(type.getDeclaredMethodMap(), neo4jServiceFactory);
            ctType.accept(classLevelVisitor);
            Collection<Method> calls = classLevelVisitor.getCalledMethods();
            type.addAllCalledMethods(calls);

            neo4jServiceFactory.getServiceForClass(Type.class).createOrUpdate(type);
        }
    }

    private Type createType(CtType<?> ctType) {
        Type type;
        if (ctType.isClass()) {
            type = createClass(ctType);
        } else if (ctType.isInterface()) {
            type = createInterface(ctType);
        } else if (ctType.isEnum()) {
            type = new Enum(ctType.getQualifiedName(), ctType.getSimpleName());
        } else if (ctType.isAnnotationType()) {
            type = new Annotation(ctType.getQualifiedName(), ctType.getSimpleName());
        } else {
            logger.warn("Unknown type {}, processing as generic type", ctType.getQualifiedName());
            type = new Type(ctType.getQualifiedName(), ctType.getSimpleName());
        }

        return type;
    }

    private void visitTypeContents(CtType<?> ctType, Type type) {
        DeclaredMethodVisitor declaredMethodVisitor = new DeclaredMethodVisitor(neo4jServiceFactory);
        ctType.accept(declaredMethodVisitor);
        type.addAllDeclaredMethods(declaredMethodVisitor.getDeclarations());
    }

    private Collection<Annotation> getAnnotations(CtElement element) {
        Neo4jService<Annotation> annotationService = neo4jServiceFactory.getServiceForClass(Annotation.class);
        return element.getAnnotations().stream()
                .map(CtAnnotation::getAnnotationType)
                .map(ctTypeReference -> {
                    String fqn = ctTypeReference.getQualifiedName();
                    return annotationService.find(fqn).orElse(new Annotation(fqn,  ctTypeReference.getSimpleName()));
                })
                .collect(Collectors.toSet());
    }

    private Collection<Interface> getSuperInterfaces(CtTypeInformation typeInformation) {
        Neo4jService<Interface> interfaceService = neo4jServiceFactory.getServiceForClass(Interface.class);
        return typeInformation.getSuperInterfaces().stream()
                .filter(CtTypeInformation::isInterface)
                .map(ctTypeReference -> {
                    String fqn = ctTypeReference.getQualifiedName();
                    return interfaceService.find(fqn).orElse(new Interface(fqn,  ctTypeReference.getSimpleName()));
                })
                .collect(Collectors.toSet());
    }

    private Class createClass(CtType<?> ctType) {
        Class clazz = new Class(ctType.getQualifiedName(), ctType.getSimpleName());
        clazz.setAbstract(ctType.isAbstract());
        clazz.addAllInterfaces(getSuperInterfaces(ctType));
        if (ctType.getSuperclass() != null && ctType.getSuperclass().getTypeDeclaration() != null) {
            clazz.setSuperType(createType(ctType.getSuperclass().getTypeDeclaration()));
        }
        return clazz;
    }

    private Interface createInterface(CtType<?> ctType) {
        Interface intrface = new Interface(ctType.getQualifiedName(), ctType.getSimpleName());
        intrface.addAllInterfaces(getSuperInterfaces(ctType));
        return intrface;
    }
}
