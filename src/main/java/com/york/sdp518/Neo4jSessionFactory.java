package com.york.sdp518;

import com.york.sdp518.util.Utils;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

public class Neo4jSessionFactory {
    private static final String NEO4J_URI = Utils.getPropertyOrEnv("NEO4J_URI", true);
    private static final String NEO4J_USERNAME = Utils.getPropertyOrEnv("NEO4J_USERNAME", true);
    private static final String NEO4J_PASSWORD = Utils.getPropertyOrEnv("NEO4J_PASSWORD", true);

    private static Configuration config = new Configuration.Builder()
            .uri(NEO4J_URI)
            .credentials(NEO4J_USERNAME, NEO4J_PASSWORD)
            .verifyConnection(true)
            .autoIndex("update") // TODO Handle index creation at DB init time
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

    public void close() {
        sessionFactory.close();
    }
}
