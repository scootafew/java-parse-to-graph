package com.york.sdp518.spoonvisitors;

import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;

public class OutsideMethodVisitor extends CalledMethodsVisitor {

    @Override
    public <T> void visitCtMethod(CtMethod<T> m) {
//        super.visitCtMethod(m);
        System.out.println("Inside method " + m.getSimpleName());
    }

    @Override
    public <T> void visitCtConstructor(CtConstructor<T> c) {
//        super.visitCtMethod(m);
        System.out.println("Inside constructor " + c.getSimpleName());
    }


}
