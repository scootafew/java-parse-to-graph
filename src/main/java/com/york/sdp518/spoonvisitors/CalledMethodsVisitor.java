package com.york.sdp518.spoonvisitors;

import com.york.sdp518.domain.Call;
import com.york.sdp518.domain.Entity;
import com.york.sdp518.domain.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CalledMethodsVisitor<T extends Entity> extends CtScanner {

    private static final Logger logger = LoggerFactory.getLogger(CalledMethodsVisitor.class);

    private T callingEntity;
    private Map<String, Call> calledMethods;

    public CalledMethodsVisitor(T callingEntity) {
        this.callingEntity = callingEntity;
        calledMethods = new HashMap<>();
    }

    @Override
    public <K> void visitCtExecutableReference(CtExecutableReference<K> reference) {
        String fullyQualifiedSignature = getQualifiedSignature(reference);
        logger.debug("Method Call: {}", fullyQualifiedSignature);
        Call call = calledMethods.getOrDefault(fullyQualifiedSignature, new Call(callingEntity, createMethod(reference)));
        call.addLineNumber(getLineNumber(reference));
        calledMethods.put(fullyQualifiedSignature, call);
        super.visitCtExecutableReference(reference);
    }

    public Collection<Call> getCalledMethods() {
        return calledMethods.values();
    }

    private <K> String getQualifiedSignature(CtExecutableReference<K> reference) {
        CtTypeReference<?> dt = reference.getDeclaringType();
        if (dt != null) {
            String qualifier = reference.getDeclaringType().getQualifiedName();
            String signature = reference.getSignature();

            if (signature.startsWith(qualifier)) {
                return signature;
            }
            return qualifier + "." + signature;
        } else {
            logger.warn("No declaring type found for node {}", reference);
            logger.warn("Using signature {} instead", reference.getSignature());
        }
        return reference.getSignature();
    }

    private <K> Method createMethod(CtExecutableReference<K> reference) {
        return new Method(getQualifiedSignature(reference), reference.getSimpleName());
    }

    // TODO Verify behaviour
    private int getLineNumber(CtElement reference) {
        if (reference.getPosition().isValidPosition()) {
            return reference.getPosition().getLine();
        } else {
            return getLineNumber(reference.getParent());
        }
    }
}
