package com.york.sdp518.service;

import org.neo4j.ogm.transaction.Transaction;

import java.util.Optional;

public interface Neo4jService<T> {
    Iterable<T> findAll();

    Optional<T> find(String id);

    void delete(String id);

    void createOrUpdate(T object);

    void createOrUpdate(T object, int depth);

    Transaction beginTransaction();
}