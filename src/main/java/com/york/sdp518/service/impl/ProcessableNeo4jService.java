package com.york.sdp518.service.impl;

import com.york.sdp518.Neo4jSessionFactory;
import com.york.sdp518.domain.ProcessableEntity;
import com.york.sdp518.domain.ProcessingState;
import com.york.sdp518.exception.AlreadyProcessedException;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Scanner;

public abstract class ProcessableNeo4jService<T extends ProcessableEntity> extends GenericNeo4jService<T> {

    private static final Logger logger = LoggerFactory.getLogger(ProcessableNeo4jService.class);

    protected ProcessableNeo4jService(Neo4jSessionFactory neo4jSessionFactory) {
        super(neo4jSessionFactory);
    }

    public T tryToBeginProcessing(T instance) throws AlreadyProcessedException {
        T item;
        try (Transaction tx = neo4jSession.beginTransaction()) {
            Optional<T> result = find(instance.getFullyQualifiedName());

            if (result.isPresent()) {
                item = result.get();
            } else {
                logger.info("Artifact not found in database, creating...");
                item = instance;
            }

            if (item.getProcessingState().equals(ProcessingState.NOT_PROCESSED)) {
                item.setProcessingState(ProcessingState.IN_PROGRESS);
//                promptEnterKey();
                neo4jSession.save(item);
                tx.commit();
            } else {
                tx.rollback();
                throw new AlreadyProcessedException(instance.getClass().getSimpleName() + " has already been processed");
            }
        }
        return item;
    }

    public void promptEnterKey(){
        System.out.println("Press \"ENTER\" to continue...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }

}
