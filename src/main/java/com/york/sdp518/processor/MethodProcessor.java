package com.york.sdp518.processor;

import org.springframework.stereotype.Component;

@Deprecated
@Component
public class MethodProcessor {

//    private static final Logger logger = LoggerFactory.getLogger(MethodProcessor.class);
//
//    private MethodNeo4jService neo4jServiceFactory;
//    private Neo4jService<Method> methodService;
//
//    @Autowired
//    public MethodProcessor(MethodNeo4jService neo4jServiceFactory) {
//        this.neo4jServiceFactory = neo4jServiceFactory;
//        this.methodService = neo4jServiceFactory.getServiceForClass(Method.class);
//    }
//
//    // TODO Check if this includes constructors
//    public <K> Method processDeclaredExecutables(CtExecutableReference<K> reference) {
//        // list method
////        String fullyQualifiedSignature = getQualifiedSignature(reference);
//        if (reference.isConstructor()) {
//            return getOrCreateConstructor(reference);
//        } else {
//            return getOrCreateMethod(reference);
//        }
//    }
//
//    // TODO Check if this includes constructors
//    public Method processMethodDeclaration(CtMethod<?> method) {
//        // list method
//        String fullyQualifiedSignature = buildFullyQualifiedSignature(method);
//        logger.debug("Method: {}", fullyQualifiedSignature);
//
//        Method declaredMethod = methodService.find(fullyQualifiedSignature)
//                .orElse(new Method(fullyQualifiedSignature, method.getSimpleName()));
//        declaredMethod.setDiscovered();
//        declaredMethod.setLineNumber(method.getPosition().getLine());
////        System.out.println("\u001B[31m" + "Method: " + declaredMethod.getFullyQualifiedName() + ", Line: " + method.getPosition().getLine() +  "\u001B[0m");
//
//        return declaredMethod;
//    }
//
//    public void processMethodCalls(CtMethod<?> method) {
//        String fullyQualifiedSignature = buildFullyQualifiedSignature(method);
//        Method declaredMethod = methodService.find(fullyQualifiedSignature)
//                .orElse(new Method(fullyQualifiedSignature, method.getSimpleName()));
//
////        System.out.println("PROCESSING_CALL: " + fullyQualifiedSignature);
//
//        CalledMethodsVisitor<Method> calledMethodsVisitor = new CalledMethodsVisitor<>(declaredMethod, neo4jServiceFactory);
//        method.accept(calledMethodsVisitor);
//
//        Session session = Neo4jSessionFactory.getInstance().getNeo4jSession();
//        Collection<Call> calls = calledMethodsVisitor.getCalledMethods();
////        calls.forEach(c -> methodService.find(c.getEndMethod().getFullyQualifiedName()).ifPresent(c::setEndMethod));
//
//        declaredMethod.addAllMethodCalls(calls);
//        session.save(declaredMethod);
//    }
//
//    public Method processMethodCalls2(Type clazz, CtMethod<?> method) {
//        String fullyQualifiedSignature = buildFullyQualifiedSignature(method);
////        Method declaredMethod = methodService.find(fullyQualifiedSignature)
////                .orElse(new Method(fullyQualifiedSignature, method.getSimpleName()));
//        Method declaredMethod = clazz.getDeclaredMethod(fullyQualifiedSignature);
//
////        System.out.println("PROCESSING_CALL: " + fullyQualifiedSignature);
//
//        CalledMethodsVisitor<Method> calledMethodsVisitor = new CalledMethodsVisitor<>(declaredMethod, neo4jServiceFactory);
//        method.accept(calledMethodsVisitor);
//
//        Collection<Call> calls = calledMethodsVisitor.getCalledMethods();
////        calls.forEach(c -> methodService.find(c.getEndMethod().getFullyQualifiedName()).ifPresent(c::setEndMethod));
//
//        declaredMethod.addAllMethodCalls(calls);
//        return declaredMethod;
//    }
//
//    private String buildFullyQualifiedSignature(CtMethod<?> method) {
//        return method.getDeclaringType().getQualifiedName() + "." + method.getSignature();
//    }
//
//    private <K> String getQualifiedSignature(CtExecutableReference<K> reference) {
//        CtTypeReference<?> dt = reference.getDeclaringType();
//        if (dt != null) {
//            String qualifier = reference.getDeclaringType().getQualifiedName();
//            String signature = reference.getSignature();
//
//            if (signature.startsWith(qualifier)) {
//                return signature;
//            }
//            return qualifier + "." + signature;
//        } else {
//            logger.warn("No declaring type found for node {}", reference);
//            logger.warn("Using signature {} instead", reference.getSignature());
//        }
//        return reference.getSignature();
//    }
//
//    private <K> Method getOrCreateMethod(CtExecutableReference<K> reference) {
//        if (reference.isConstructor()) {
//            return getOrCreateConstructor(reference);
//        }
//        String fullyQualifiedSignature = getQualifiedSignature(reference);
//        return methodService.find(fullyQualifiedSignature)
//                .orElse(new Method(fullyQualifiedSignature, reference.getSimpleName()));
//    }
//
//    private <K> Method getOrCreateConstructor(CtExecutableReference<K> reference) {
//        String fullyQualifiedSignature = getQualifiedSignature(reference);
//        return neo4jServiceFactory.getServiceForClass(Constructor.class).find(fullyQualifiedSignature)
//                .orElse(new Constructor(fullyQualifiedSignature, reference.getSimpleName()));
//    }
}
