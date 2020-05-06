package com.york.sdp518.processor;

import com.york.sdp518.domain.Package;
import com.york.sdp518.service.Neo4jService;
import com.york.sdp518.service.impl.Neo4jServiceFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import spoon.reflect.CtModelImpl.CtRootPackage;
import spoon.reflect.declaration.CtPackage;
import spoon.support.reflect.declaration.CtClassImpl;
import spoon.support.reflect.declaration.CtPackageImpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(SpringExtension.class)
class PackageProcessorTest {

    @MockBean
    TypeProcessor typeProcessor;
    @MockBean
    Neo4jServiceFactory neo4jServiceFactory;

    @Mock
    Neo4jService<Package> neo4jServiceMock;

    PackageProcessor packageProcessor;

    Collection<CtPackage> packages;

    @BeforeEach
    void setUp() {
        initPackageProcessor();
        createPackages();
    }

    private void initPackageProcessor() {
        given(neo4jServiceFactory.getServiceForClass(Package.class))
                .willReturn(neo4jServiceMock);
        packageProcessor = new PackageProcessor(typeProcessor, neo4jServiceFactory);
    }

    /**
     * Mock classes:
     * org.test.mock.ClassA
     * org.test.mock.core.ClassB
     * org.test.mock.utils.ClassC
     */
    private void createPackages() {
        packages = new HashSet<>();

        CtRootPackage rootPackage = new CtRootPackage();
        rootPackage.setSimpleName(CtPackage.TOP_LEVEL_PACKAGE_NAME);
        packages.add(rootPackage);

        CtPackageImpl org = new CtPackageImpl();
        org.setSimpleName("org");
//        packages.add(org);

        CtPackageImpl test = new CtPackageImpl();
        test.setSimpleName("test");
//        packages.add(test);

        CtPackageImpl mock = new CtPackageImpl();
        mock.setSimpleName("mock");
//        packages.add(mock);

        CtPackageImpl core = new CtPackageImpl();
        core.setSimpleName("core");
//        packages.add(core);

        CtPackageImpl utils = new CtPackageImpl();
        utils.setSimpleName("utils");
//        packages.add(utils);

        core.addType(new CtClassImpl<>());
        utils.addType(new CtClassImpl<>());
        mock.addPackage(core);
        mock.addPackage(utils);
        mock.addType(new CtClassImpl<>());
        test.addPackage(mock);
        org.addPackage(test);
        rootPackage.addPackage(org);
        packages = rootPackage.getPackages();
    }

    @AfterEach
    void tearDown() {
    }

    @Disabled
    @Test
    void processPackages() {
        // given
        willDoNothing().given(typeProcessor).processTypes(any());

        // when
        packageProcessor.processPackages(packages);

        // then
        then(typeProcessor).should(times(3)).processTypes(any());
    }

    @Disabled
    @Test
    void getPackageToCreate() {
        // given
        given(neo4jServiceMock.find(anyString())).willReturn(Optional.empty());
        willDoNothing().given(typeProcessor).processTypes(any());

        // when
//        packageProcessor.getPackageToCreate("com.york.sdp518");
    }
}