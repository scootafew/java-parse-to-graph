package com.york.sdp518;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.domain.Class;
import com.york.sdp518.domain.Method;
import com.york.sdp518.domain.Package;
import com.york.sdp518.domain.Repository;
import com.york.sdp518.exception.BuildClasspathException;
import com.york.sdp518.exception.JavaParseToGraphException;
import com.york.sdp518.service.impl.MavenMetadataService;
import com.york.sdp518.service.MetadataService;
import com.york.sdp518.service.impl.MavenPluginService;
import com.york.sdp518.spoonvisitors.CalledMethodsVisitor;
import com.york.sdp518.spoonvisitors.OutsideMethodVisitor;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.MavenLauncher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.support.compiler.SpoonPom;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpoonProcessor {

    private static final Logger logger = LoggerFactory.getLogger(SpoonProcessor.class);

    public SpoonProcessor() {

    }

    public void process(Repository repository, Path projectPath) throws JavaParseToGraphException {
        logger.info("Processing project in directory {}", projectPath);
        MavenLauncher launcher = new MavenLauncher(projectPath.toString(), MavenLauncher.SOURCE_TYPE.APP_SOURCE);

        if (classpathNotBuiltSuccessfully(launcher)) {
            logger.warn("Could not build classpath, trying to retrieve latest version...");
            attemptToBuildClasspath(launcher);
        }

        logger.info("Building model...");
        launcher.buildModel();

        logger.info("Processing model...");
        processModel(repository, launcher.getPomFile());

        CtModel model = launcher.getModel();
        processPackages(model.getAllPackages());
    }

    private void attemptToBuildClasspath(MavenLauncher launcher) throws JavaParseToGraphException {
        // if can't build classpath, try setting version to latest version
        String groupId = launcher.getPomFile().getModel().getGroupId();
        String artifactId = launcher.getPomFile().getModel().getArtifactId();

        MetadataService metadataService = new MavenMetadataService();
        String latestVersion = metadataService.getLatestVersion(groupId, artifactId);

        MavenPluginService pluginService = new MavenPluginService();
        pluginService.setVersion(launcher.getPomFile().toFile(), latestVersion);

        launcher.rebuildClasspath();

        if (classpathNotBuiltSuccessfully(launcher)) {
            logger.error("Still could not build classpath, exiting...");
            throw new BuildClasspathException("Could not build classpath for repository");
        }
    }

    private boolean classpathNotBuiltSuccessfully(MavenLauncher launcher) {
        return launcher.getEnvironment().getSourceClasspath().length == 0;
    }

    //region Repository-artifact-processing
    private void processModel(Repository repository, SpoonPom pom) {
        Set<Artifact> artifacts = getArtifacts(pom).collect(Collectors.toSet());
        repository.addAllArtifacts(artifacts);

        Session session = Neo4jSessionFactory.getInstance().getNeo4jSession();
        session.save(repository);
    }

    private Stream<Artifact> getArtifacts(SpoonPom spoonPom) {
        if (spoonPom.getModules().isEmpty()) {
            return getArtifact(spoonPom.getModel());
        }
        return Stream.concat(
                getArtifact(spoonPom.getModel()),
                spoonPom.getModules().stream().flatMap(this::getArtifacts)
        );
    }

    private Stream<Artifact> getArtifact(Model model) {
        String groupId = model.getGroupId() != null ? model.getGroupId() : model.getParent().getGroupId();
        String version = model.getVersion() != null ? model.getVersion() : model.getParent().getVersion();
        String fqn = String.join(":", groupId, model.getArtifactId(), version);
        return Stream.of(new Artifact(fqn, model.getArtifactId()));
    }
    //endregion

    private void processPackages(Collection<CtPackage> packages) {
        // list all packages
        for(CtPackage p : packages) {
            if (!p.isUnnamedPackage()) {
                logger.debug("\n====================================================");
                logger.debug("Package: {}", p.getQualifiedName());
                createPackageIfNotExists(p.getQualifiedName());
                processTypes(p.getTypes());
            }

        }
    }

    private void processTypes(Collection<CtType<?>> types) {
        // list all classes
        for(CtType<?> type : types) {
            logger.debug("\nClass: {}", type.getQualifiedName());

            Class clazz = new Class(type.getQualifiedName(), type.getSimpleName());

            OutsideMethodVisitor classLevelVisitor = new OutsideMethodVisitor();
            type.accept(classLevelVisitor);
            clazz.addAllCalledMethods(classLevelVisitor.getCalledMethods());

            Collection<Method> declaredMethods = type.getMethods().stream()
                    .map(this::processMethod)
                    .collect(Collectors.toSet());
            clazz.addAllDeclaredMethods(declaredMethods);

            Session session = Neo4jSessionFactory.getInstance().getNeo4jSession();
            Package currentPackage = session.load(Package.class, type.getPackage().getQualifiedName());
            currentPackage.addClass(clazz);
            session.save(currentPackage);
        }
    }

    // TODO Check if this includes constructors
    private Method processMethod(CtMethod<?> method) {
        // list method
        String fullyQualifiedSignature = buildFullyQualifiedSignature(method);
        logger.debug("Method: {}", fullyQualifiedSignature);

        Method declaredMethod = new Method(fullyQualifiedSignature, method.getSimpleName());

        CalledMethodsVisitor calledMethodsVisitor = new CalledMethodsVisitor();
        method.accept(calledMethodsVisitor);
        declaredMethod.addAllMethodCalls(calledMethodsVisitor.getCalledMethods());

        return declaredMethod;
    }

    private String buildFullyQualifiedSignature(CtMethod<?> method) {
        return method.getDeclaringType().getQualifiedName() + "." + method.getSignature();
    }

    //region Package-processing
    // New method following package structure more efficiently
    private void createPackageIfNotExists(String qualifiedPackageName) {
        Session session = Neo4jSessionFactory.getInstance().getNeo4jSession();
        try (Transaction tx = session.beginTransaction()) {
            Package currentPackage = session.load(Package.class, qualifiedPackageName);

            if (currentPackage == null) {
                String parentPackageQualifiedName = getParentPackageQualifiedName(qualifiedPackageName);
                String currentPackageName = getCurrentPackageQualifiedName(qualifiedPackageName);
                currentPackage = new Package(qualifiedPackageName, currentPackageName);
                Package packageForCreation = getParentOrCreate(parentPackageQualifiedName, currentPackage, session);

                session.save(packageForCreation);
                tx.commit();
            }

        }
    }

    private Package getParentOrCreate(String qualifiedPackageName, Package childPackage, Session session) {
        if (qualifiedPackageName.isEmpty()) {
            return childPackage;
        }

        Package currentPackage = session.load(Package.class, qualifiedPackageName);
        if (currentPackage != null) {
            currentPackage.getPackages().add(childPackage);
            return currentPackage;
        } else {
            String parentPackageQualifiedName = getParentPackageQualifiedName(qualifiedPackageName);
            String currentPackageName = getCurrentPackageQualifiedName(qualifiedPackageName);
            currentPackage = new Package(qualifiedPackageName, currentPackageName);
            currentPackage.getPackages().add(childPackage);
            return getParentOrCreate(parentPackageQualifiedName, currentPackage, session);
        }
    }

    private String getParentPackageQualifiedName(String qualifiedName) {
        return qualifiedName.contains(".") ? StringUtils.substringBeforeLast(qualifiedName, ".") : "";
    }

    private String getCurrentPackageQualifiedName(String qualifiedName) {
        return qualifiedName.contains(".") ? StringUtils.substringAfterLast(qualifiedName, ".") : qualifiedName;
    }
    //endregion


}
