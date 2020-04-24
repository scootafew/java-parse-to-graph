package com.york.sdp518.service.impl;

import com.york.sdp518.Neo4jSessionFactory;
import com.york.sdp518.domain.Entity;
import com.york.sdp518.service.Neo4jService;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;

import java.util.Optional;

public abstract class GenericNeo4jService<T extends Entity> implements Neo4jService<T> {
    private static final int DEPTH_LIST = 0;
    private static final int DEPTH_ENTITY = 1;

    protected Session neo4jSession;

    protected GenericNeo4jService(Neo4jSessionFactory neo4jSessionFactory) {
        neo4jSession = neo4jSessionFactory.getNeo4jSession();
    }

    @Override
    public Iterable<T> findAll() {
        return neo4jSession.loadAll(getEntityType(), DEPTH_LIST);
    }

    @Override
    public Optional<T> find(String id) {
        return Optional.ofNullable(neo4jSession.load(getEntityType(), id, DEPTH_ENTITY));
    }

    @Override
    public void delete(String id) {
        neo4jSession.delete(neo4jSession.load(getEntityType(), id));
    }

    @Override
    public void createOrUpdate(T entity) {
        neo4jSession.save(entity);
    }

    @Override
    public void createOrUpdate(T entity, int depth) {
        neo4jSession.save(entity, depth);
    }

    @Override
    public Transaction beginTransaction() {
        return neo4jSession.beginTransaction();
    }

    abstract Class<T> getEntityType();
}
