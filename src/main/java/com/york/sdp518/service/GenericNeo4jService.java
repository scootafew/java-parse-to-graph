package com.york.sdp518.service;

import com.york.sdp518.Neo4jSessionFactory;
import com.york.sdp518.domain.Entity;
import org.neo4j.ogm.session.Session;

public abstract class GenericNeo4jService<T extends Entity> implements Neo4jService<T> {
    private static final int DEPTH_LIST = 0
    private static final int DEPTH_ENTITY = 1
    protected Session session = Neo4jSessionFactory.getInstance().getNeo4jSession();

    @Override
    public Iterable<T> findAll() {
        return session.loadAll(getEntityType(), DEPTH_LIST);
    }

    @Override
    public T find(String id) {
        return session.load(getEntityType(), id, DEPTH_ENTITY);
    }

    @Override
    public void delete(String id) {
        session.delete(session.load(getEntityType(), id));
    }

    @Override
    public T createOrUpdate(T entity) {
        session.save(entity, DEPTH_ENTITY);
        return find(entity.getFullyQualifiedName());
    }

    abstract Class<T> getEntityType();
}
