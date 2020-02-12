package com.york.sdp518;

import org.neo4j.ogm.config.ClasspathConfigurationSource;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

public class Neo4jSessionFactory {
    private static Configuration config = new Configuration.Builder()
            .uri(System.getenv("NEO4J_URI"))
            .verifyConnection(true)
            .build();
    private static SessionFactory sessionFactory = new SessionFactory(config, "com.york.sdp518.domain");
    private static Neo4jSessionFactory factory = new Neo4jSessionFactory();

    public static Neo4jSessionFactory getInstance() {
        return factory;
    }

    private Neo4jSessionFactory() {
    }

    public Session getNeo4jSession() {
        return sessionFactory.openSession();
    }
}
