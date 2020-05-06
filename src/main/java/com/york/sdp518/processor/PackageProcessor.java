package com.york.sdp518.processor;

import com.york.sdp518.domain.Package;
import com.york.sdp518.service.Neo4jService;
import com.york.sdp518.service.impl.Neo4jServiceFactory;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import spoon.reflect.declaration.CtPackage;

import java.util.Collection;
import java.util.Optional;

@Component
public class PackageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PackageProcessor.class);

    private TypeProcessor typeProcessor;
    private Neo4jService<Package> neo4jService;

    @Autowired
    public PackageProcessor(TypeProcessor typeProcessor, Neo4jServiceFactory neo4jServiceFactory) {
        this.typeProcessor = typeProcessor;
        this.neo4jService = neo4jServiceFactory.getServiceForClass(Package.class);
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
        try (Transaction tx = neo4jService.beginTransaction()) {
            Package pack = getPackageToCreate(qualifiedPackageName);
            logger.debug("Created package {} in database", pack.getFullyQualifiedName());
            tx.commit();
        }
    }

    private Package getPackageToCreate(String qualifiedPackageName) {
        Optional<Package> currentPackage = neo4jService.find(qualifiedPackageName);
        if (currentPackage.isPresent()) {
            return currentPackage.get(); // Package already exists (therefore all parents do too)
        } else {
            String parentPackageQualifiedName = getParentPackageQualifiedName(qualifiedPackageName);
            String currentPackageName = getCurrentPackageQualifiedName(qualifiedPackageName);
            Package newPackage = new Package(qualifiedPackageName, currentPackageName);
            if (parentPackageQualifiedName.isEmpty()) {
                neo4jService.createOrUpdate(newPackage); // just create as has no parent
            } else {
                Package parent = getPackageToCreate(parentPackageQualifiedName);
                parent.addPackage(newPackage);
                neo4jService.createOrUpdate(parent);
            }
            return newPackage;
        }
    }

    private String getParentPackageQualifiedName(String qualifiedName) {
        return qualifiedName.contains(".") ? StringUtils.substringBeforeLast(qualifiedName, ".") : "";
    }

    private String getCurrentPackageQualifiedName(String qualifiedName) {
        return qualifiedName.contains(".") ? StringUtils.substringAfterLast(qualifiedName, ".") : qualifiedName;
    }
}
