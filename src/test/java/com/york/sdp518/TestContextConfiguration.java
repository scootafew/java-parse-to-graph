package com.york.sdp518;

import com.york.sdp518.service.MavenInvoker;
import org.mockito.Mock;
import org.neo4j.ogm.config.Configuration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.Neo4jContainer;

@TestConfiguration
@Import(Main.class)
public class TestContextConfiguration {

//    static final Neo4jContainer<?> NEO4J_CONTAINER;
//
//    static {
//        NEO4J_CONTAINER = new Neo4jContainer<>();
//        NEO4J_CONTAINER.start();
//    }

//    @Bean
//    Neo4jContainer<?> getNeo4jContainer() {
//        return new Neo4jContainer<>();
//    }
//
//    @Bean
//    Configuration getNeo4jConfiguration(Neo4jContainer<?> neo4jContainer) {
//        return new Configuration.Builder()
//                .uri(neo4jContainer.getBoltUrl())
//                .credentials("neo4j", neo4jContainer.getAdminPassword())
//                .build();
//    }

//    @Bean
//    MavenInvoker getMavenInvoker() {
//        return new MavenInvoker();
//    }

    @MockBean
    MavenInvoker mavenInvoker;

    @MockBean
    Neo4jSessionFactory neo4jSessionFactory;

    @MockBean
    Configuration configuration;

}
