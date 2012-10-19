package org.nexse.sdncrossstore.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nexse.sdncrossstore.domain.Event;
import org.nexse.sdncrossstore.domain.EventRelationshipTypes;
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
import java.util.Arrays;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext
public class EventGraphRepositoryTest {

    private final transient Logger log = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EventGraphRepository eventGraphRepository;

    @Autowired
    private CrossStoreRepository crossStoreRepository;

    @Transactional
    @Test
    public void testPersist() {
        final Event event = new Event().withName("Some event");
        entityManager.persist(event);
        final Event persistedEvent = eventGraphRepository.save(event);
        assertEquals("the names should be equal", event.getName(), persistedEvent.getName());
        final BigInteger jpaNodeId = (BigInteger) entityManager
                .createNativeQuery("select id_num from events_tb where name_txt = :name")
                .setParameter("name", "Some event")
                .setMaxResults(1)
                .getSingleResult();
        assertEquals("event JPA persisted and direct JDBC query should yield same event id (JPA)",
                event.getId().longValue(), jpaNodeId.longValue());
        final Long graphId = persistedEvent.getNodeId();
        final String persistentNodeType = (String) persistedEvent.getPersistentState()
                .getProperty(AbstractIndexingTypeRepresentationStrategy.TYPE_PROPERTY_NAME);
        final Long foreignId = (Long) persistedEvent.getPersistentState()
                .getProperty(CrossStoreNodeEntityState.FOREIGN_ID);
        assertTrue("neo4j event graph id should be present", graphId != null);
        assertEquals("neo4j event graph should contain correct type", Event.class.getName(), persistentNodeType);
        assertEquals("graph event should link to correct tuple in RDBMS", event.getId(), foreignId);
    }

    @Transactional
    @Test
    public void testHowPersist() {
        final Event event = new Event().withName("Some other event");
        entityManager.persist(event);
        final Event persistedEvent = eventGraphRepository.save(event);
        final Event jpaRetrievedEvent = entityManager.createQuery("from Event where name = :name", Event.class)
                .setParameter("name", "Some other event")
                .setMaxResults(1)
                .getSingleResult();
        assertTrue("neo4j state should be retrieved when JPA query is run", jpaRetrievedEvent.getPersistentState() != null);
        final Long graphId = jpaRetrievedEvent.getNodeId();
        final String persistentNodeType = (String) jpaRetrievedEvent.getPersistentState()
                .getProperty(AbstractIndexingTypeRepresentationStrategy.TYPE_PROPERTY_NAME);
        final Long foreignId = (Long) jpaRetrievedEvent.getPersistentState()
                .getProperty(CrossStoreNodeEntityState.FOREIGN_ID);
        assertTrue("neo4j event graph id should be present", graphId != null);
        assertEquals("neo4j event graph should contain correct type", Event.class.getName(), persistentNodeType);
        assertEquals("graph event should link to correct tuple in RDBMS", event.getId(), foreignId);
    }

    @Transactional
    @Test
    public void testPersistTwoNodes() {
        final Event eventA = new Event().withName("node A").withSomeProp("property on node A");
        final Event eventB = new Event().withName("node B").withSomeProp("property on node B");
        eventA.setPeerEvent(eventB);
        entityManager.persist(eventA);
        entityManager.persist(eventB);
        final Iterable<Event> persistedNodes = eventGraphRepository.save(Arrays.asList(eventA, eventB));
        assertTrue("eventA has relationship LINKED_TO",
                eventA.getPersistentState().hasRelationship(EventRelationshipTypes.LINKED_TO));
        for (Map<String, Object> node : crossStoreRepository.findPath(eventA.getNodeId(), eventB.getNodeId())) {
            log.info("PATH CONTAINS Event: {}", node);
        }
    }

}