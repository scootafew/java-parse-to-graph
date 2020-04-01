package com.york.sdp518.service.impl;

import com.york.sdp518.Neo4jSessionFactory;
import com.york.sdp518.domain.ProcessableEntity;
import com.york.sdp518.domain.ProcessingState;
import com.york.sdp518.exception.AlreadyProcessedException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;

public class Neo4jServiceUtils {

    Session neo4jSession = Neo4jSessionFactory.getInstance().getNeo4jSession();

    public <K extends ProcessableEntity> K tryToBeginProcessing(Class<K> clazz, K instance) throws AlreadyProcessedException {
        K item;
        try (Transaction tx = neo4jSession.beginTransaction()) {
            item = neo4jSession.load(clazz, instance.getFullyQualifiedName());

            if (item == null) {
                item = instance;
            }

            if (item.getProcessingState().equals(ProcessingState.NOT_PROCESSED)) {
                item.setProcessingState(ProcessingState.IN_PROGRESS);
                neo4jSession.save(item);
                tx.commit();
            } else {
                tx.rollback();
                throw new AlreadyProcessedException(clazz.getSimpleName() + " has already been processed");
            }
        }
        return item;
    }

}
