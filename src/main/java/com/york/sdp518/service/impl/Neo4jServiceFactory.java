package com.york.sdp518.service.impl;

import com.york.sdp518.domain.Entity;
import com.york.sdp518.domain.Method;
import com.york.sdp518.service.Neo4jService;
import org.springframework.stereotype.Service;

@Service
public class Neo4jServiceFactory {

    public <T extends Entity> Neo4jService<T> getServiceForClass(Class<T> clazz) {
        return new GenericNeo4jService<T>() {
            @Override
            Class<T> getEntityType() {
                return clazz;
            }
        };
    }

}
