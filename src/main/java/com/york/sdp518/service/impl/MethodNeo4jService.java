package com.york.sdp518.service.impl;

import com.york.sdp518.domain.Method;
import org.springframework.stereotype.Service;

@Service
public class MethodNeo4jService extends GenericNeo4jService<Method> {

    @Override
    Class<Method> getEntityType() {
        return Method.class;
    }
}
