package com.york.sdp518.service;

public interface Neo4jService<T> {
    Iterable<T> findAll();

    T find(String id);

    void delete(String id);

    T createOrUpdate(T object);
}