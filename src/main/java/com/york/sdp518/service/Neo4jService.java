package com.york.sdp518.service;

import java.util.Optional;

public interface Neo4jService<T> {
    Iterable<T> findAll();

    Optional<T> find(String id);

    void delete(String id);

    T createOrUpdate(T object);
}