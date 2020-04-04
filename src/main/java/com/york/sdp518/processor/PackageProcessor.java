package com.york.sdp518.processor;

import com.york.sdp518.Neo4jSessionFactory;
import com.york.sdp518.domain.Package;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import spoon.reflect.declaration.CtPackage;

import java.util.Collection;

@Component
public class PackageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PackageProcessor.class);

    private TypeProcessor typeProcessor;

    @Autowired
    public PackageProcessor(TypeProcessor typeProcessor) {
        this.typeProcessor = typeProcessor;
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
            Package packageForCreation = getPackageToCreate(qualifiedPackageName, session);

            // Don't save unnecessarily (if package already existed)
            if (!packageForCreation.getFullyQualifiedName().equals(qualifiedPackageName)) {
                session.save(packageForCreation);
                tx.commit();
            }
        }
    }

    private Package getPackageToCreate(String qualifiedPackageName, Session session) {
        Package currentPackage = session.load(Package.class, qualifiedPackageName);
        if (currentPackage != null) {
            return currentPackage; // Package already exists
        } else {
            String parentPackageQualifiedName = getParentPackageQualifiedName(qualifiedPackageName);
            String currentPackageName = getCurrentPackageQualifiedName(qualifiedPackageName);
            currentPackage = new Package(qualifiedPackageName, currentPackageName);
            if (parentPackageQualifiedName.isEmpty()) {
                return currentPackage; // return new Package as has no parent
            } else {
                Package parent = getPackageToCreate(parentPackageQualifiedName, session);
                parent.addPackage(currentPackage);
                return parent;
            }
        }
    }

    private String getParentPackageQualifiedName(String qualifiedName) {
        return qualifiedName.contains(".") ? StringUtils.substringBeforeLast(qualifiedName, ".") : "";
    }

    private String getCurrentPackageQualifiedName(String qualifiedName) {
        return qualifiedName.contains(".") ? StringUtils.substringAfterLast(qualifiedName, ".") : qualifiedName;
    }
}
