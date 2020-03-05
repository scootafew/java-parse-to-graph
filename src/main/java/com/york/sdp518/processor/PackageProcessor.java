package com.york.sdp518.processor;

import com.york.sdp518.Neo4jSessionFactory;
import com.york.sdp518.domain.Package;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtPackage;

import java.util.Collection;

public class PackageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PackageProcessor.class);

    private TypeProcessor typeProcessor;

    public PackageProcessor() {
        this.typeProcessor = new TypeProcessor();
    }

    public void processPackages(Collection<CtPackage> packages) {
        // list all packages
        for(CtPackage p : packages) {
            if (!p.isUnnamedPackage()) {
                logger.debug("\n====================================================");
                logger.debug("Package: {}", p.getQualifiedName());
                createPackageIfNotExists(p.getQualifiedName());
                typeProcessor.processTypes(p.getTypes());
            }

        }
    }

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
}
