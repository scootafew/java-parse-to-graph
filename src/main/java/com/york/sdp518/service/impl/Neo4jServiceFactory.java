package com.york.sdp518.service.impl;

import com.york.sdp518.Neo4jSessionFactory;
import com.york.sdp518.domain.Entity;
import com.york.sdp518.domain.ProcessableEntity;
import com.york.sdp518.service.Neo4jService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Neo4jServiceFactory {

    private Neo4jSessionFactory neo4jSessionFactory;

    @Autowired
    public Neo4jServiceFactory(Neo4jSessionFactory neo4jSessionFactory) {
        this.neo4jSessionFactory = neo4jSessionFactory;
    }

    public <T extends Entity> Neo4jService<T> getServiceForClass(Class<T> clazz) {
        return new GenericNeo4jService<T>(neo4jSessionFactory) {
            @Override
            Class<T> getEntityType() {
                return clazz;
            }
        };
    }

    public <K extends ProcessableEntity> ProcessableNeo4jService<K> getServiceForProcessableEntity(Class<K> clazz) {
        return new ProcessableNeo4jService<K>(neo4jSessionFactory) {
            @Override
            Class<K> getEntityType() {
                return clazz;
            }
        };
    }

}
