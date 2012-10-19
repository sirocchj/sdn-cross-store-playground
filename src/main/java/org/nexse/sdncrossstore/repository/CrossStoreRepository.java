package org.nexse.sdncrossstore.repository;

import org.nexse.sdncrossstore.domain.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.conversion.Result;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.Map;

import static org.neo4j.helpers.collection.MapUtil.map;

@Repository
public class CrossStoreRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private Neo4jTemplate neo4jTemplate;

    @Transactional
    public Event persist(Event event) {
        entityManager.persist(event);
        return neo4jTemplate.save(event);
    }

    public Result<Map<String, Object>> findPath(Long startIdNode, Long endIdNode) {
        return neo4jTemplate.query(
                "start n=node({startNodeId}), m=node({endNodeId}) match p=n-[r:LINKED_TO]->m return nodes(p), r",
                map("startNodeId", startIdNode, "endNodeId", endIdNode)
        );
    }

}