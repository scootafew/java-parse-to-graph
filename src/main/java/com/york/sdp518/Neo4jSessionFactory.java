package com.york.sdp518;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

public class Neo4jSessionFactory {

    private SessionFactory sessionFactory;

    Neo4jSessionFactory(Configuration configuration) {
        this.sessionFactory = new SessionFactory(configuration, "com.york.sdp518.domain");
    }

    public Session getNeo4jSession() {
        return sessionFactory.openSession();
    }

    void close() {
        sessionFactory.close();
    }
}
