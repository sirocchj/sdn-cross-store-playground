package org.nexse.sdncrossstore.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nexse.sdncrossstore.domain.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.cross_store.support.node.CrossStoreNodeEntityState;
import org.springframework.data.neo4j.support.typerepresentation.AbstractIndexingTypeRepresentationStrategy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.math.BigInteger;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext
public class GraphNodeRepositoryTest {

    private final transient Logger log = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private GraphNodeRepository graphNodeRepository;

    @Transactional
    @Test
    public void testPersist() {
        final Node node = new Node();
        node.setName("Some node");
        entityManager.persist(node);
        final Node persistedNode = graphNodeRepository.save(node);
        assertEquals("the names should be equal", node.getName(), persistedNode.getName());
        final BigInteger jpaNodeId = (BigInteger) entityManager
                .createNativeQuery("select id_num from nodes_tb where name_txt = :name")
                .setParameter("name", "Some node")
                .setMaxResults(1)
                .getSingleResult();
        assertEquals("node JPA persisted and direct JDBC query should yield same node id (JPA)",
                node.getId().longValue(), jpaNodeId.longValue());
        final Long graphId = persistedNode.getNodeId();
        final String persistentNodeType = (String)persistedNode.getPersistentState()
                .getProperty(AbstractIndexingTypeRepresentationStrategy.TYPE_PROPERTY_NAME);
        final Long foreignId = (Long)persistedNode.getPersistentState()
                .getProperty(CrossStoreNodeEntityState.FOREIGN_ID);
        assertTrue("neo4j node graph id should be present", graphId != null);
        assertEquals("neo4j node graph should contain correct type", Node.class.getName(), persistentNodeType);
        assertEquals("graph node should link to correct tuple in RDBMS", node.getId(), foreignId);
    }

    @Transactional
    @Test
    public void testHowPersist() {
        final Node node = new Node();
        node.setName("Some other node");
        entityManager.persist(node);
        final Node persistedNode = graphNodeRepository.save(node);
        final Node jpaRetrievedNode = entityManager.createQuery("from Node where name = :name", Node.class)
                .setParameter("name", "Some other node")
                .setMaxResults(1)
                .getSingleResult();
        assertTrue("neo4j state should be retrieved when JPA query is run", jpaRetrievedNode.getPersistentState() != null);
        final Long graphId = jpaRetrievedNode.getNodeId();
        final String persistentNodeType = (String)jpaRetrievedNode.getPersistentState()
                .getProperty(AbstractIndexingTypeRepresentationStrategy.TYPE_PROPERTY_NAME);
        final Long foreignId = (Long)jpaRetrievedNode.getPersistentState()
                .getProperty(CrossStoreNodeEntityState.FOREIGN_ID);
        assertTrue("neo4j node graph id should be present", graphId != null);
        assertEquals("neo4j node graph should contain correct type", Node.class.getName(), persistentNodeType);
        assertEquals("graph node should link to correct tuple in RDBMS", node.getId(), foreignId);
    }

}