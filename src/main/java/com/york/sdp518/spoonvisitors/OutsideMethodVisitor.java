package com.york.sdp518.spoonvisitors;

import com.york.sdp518.domain.Type;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;

public class OutsideMethodVisitor extends CalledMethodsVisitor<Type> {

    public OutsideMethodVisitor(Type clazz) {
        super(clazz);
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
