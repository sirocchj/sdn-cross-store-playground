package org.nexse.sdncrossstore.repository;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nexse.sdncrossstore.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.neo4j.support.node.Neo4jHelper;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext
@Ignore
public class EventRepositoryTest {

    private final transient Logger log = LoggerFactory.getLogger(getClass());

    protected Long userId;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private Neo4jTemplate template;

    @PersistenceContext
    protected EntityManager em;

    @PersistenceUnit
    protected EntityManagerFactory emf;

    @BeforeTransaction
    public void setUpBeforeTransaction() {
        final EntityManager setUpEm = emf.createEntityManager();
        final EntityTransaction setUpTx = setUpEm.getTransaction();
        setUpTx.begin();
        final Event event = new Event().withName("Cippa");
        setUpEm.persist(event);
        setUpEm.flush();
        event.persist();
        this.userId = event.getId();
        setUpTx.commit();
    }

    @Before
    public void setUp() throws Exception {
        //em = emf.createEntityManager();
    }

    @Transactional
    @BeforeTransaction
    public void cleanDb() {
        Neo4jHelper.cleanDb(template);
    }

    @AfterTransaction
    public void tearDown() {
        final EntityManager tearDownEm = emf.createEntityManager();
        final EntityTransaction tearDownTx = tearDownEm.getTransaction();
        tearDownTx.begin();
        final Event event = tearDownEm.find(Event.class, this.userId);
        tearDownEm.remove(event);
        tearDownEm.flush();
        tearDownTx.commit();
    }

    @Transactional
    @Test
    public void testPersist() throws Exception {
        final Event newEvent = new Event().withName("Lippa");
        final Event newEvent1 = new Event().withName("Stoka");
        eventRepository.persist(newEvent);
        eventRepository.persist(newEvent1);
        em.flush();
        final List<Event> events = em.createQuery("from Event", Event.class).getResultList();
        final Event lippa = em.createQuery("from Event where name = :name", Event.class)
                .setParameter("name", "Lippa")
                .setMaxResults(1)
                .getSingleResult();
        assertEquals("should have found three entries", 3, events.size());
        log.info("The two entries have the following data: {}", events);
        assertEquals("should have found the correct entry", "Lippa", lippa.getName());
        log.info("Lippa is: {}", lippa);
        assertTrue("nodeId (NEO4J) should be present", lippa.getNodeId() != null);
        log.info("NEO4J nodeId for {} is: {}", lippa, lippa.getNodeId());
        for (final String key : lippa.getPersistentState().getPropertyKeys()) {
            log.info("Lippa contains property '{}': {}", key, lippa.getPersistentState().getProperty(key));
        }
        final BigInteger lippaPresumedId = (BigInteger) em.createNativeQuery("select id_num from nodes_tb where name_txt = 'Lippa'")
                .setMaxResults(1).getSingleResult();
        log.info("HSQL contains: {}", lippaPresumedId);
        assertEquals("Lippa id should match what RDBMS is saying", lippa.getId().longValue(), lippaPresumedId.longValue());
        final Event persistedEvent = eventRepository.findByName(newEvent.getName());
        assertEquals("should have the correct value", newEvent.getName(), persistedEvent.getName());
    }

}