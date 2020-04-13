package com.york.sdp518.spoonvisitors;

import com.york.sdp518.domain.Method;
import com.york.sdp518.service.impl.Neo4jServiceFactory;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;

import java.util.Map;

public class ClassLevelMethodCallVisitor extends CalledMethodsVisitor {

    public ClassLevelMethodCallVisitor(Map<String, Method> methodCache, Neo4jServiceFactory methodService) {
        super(methodCache, methodService);
    }

    @Override
    public <T> void visitCtMethod(CtMethod<T> m) {
        // By not calling super we don't visit method calls inside methods, as these are processed with declaration
    }


    @Override
    public <T> void visitCtConstructor(CtConstructor<T> c) {
        // By not calling super we don't visit method calls inside constructors, as these are processed with declaration
    }


}
