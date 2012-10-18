package org.nexse.sdncrossstore.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nexse.sdncrossstore.domain.Node;
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
public class NodeRepositoryTest {

    private final transient Logger log = LoggerFactory.getLogger(getClass());

    protected Long userId;

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private Neo4jTemplate template;

    @PersistenceContext
    protected EntityManager em;

    @PersistenceUnit
    protected EntityManagerFactory emf;

    @BeforeTransaction
    public void setUpBeforeTransaction() {
        EntityManager setUpEm = emf.createEntityManager();
        EntityTransaction setUpTx = setUpEm.getTransaction();
        setUpTx.begin();
        Node node = new Node();
        node.setName("Cippa");
        setUpEm.persist(node);
        setUpEm.flush();
        node.persist();
        this.userId = node.getId();
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
        EntityManager tearDownEm = emf.createEntityManager();
        EntityTransaction tearDownTx = tearDownEm.getTransaction();
        tearDownTx.begin();
        Node node = tearDownEm.find(Node.class, this.userId);
        tearDownEm.remove(node);
        tearDownEm.flush();
        tearDownTx.commit();
    }

    @Transactional
    @Test
    public void testPersist() throws Exception {
        Node newNode = new Node();
        newNode.setName("Lippa");
        nodeRepository.persist(newNode);
        Node newNode1 = new Node();
        newNode1.setName("Stoka");
        nodeRepository.persist(newNode1);
        em.flush();
        List<Node> nodes = em.createQuery("from Node", Node.class).getResultList();
        Node lippa = em.createQuery("from Node where name = :name", Node.class)
                .setParameter("name", "Lippa")
                .setMaxResults(1)
                .getSingleResult();
        assertEquals("should have found three entries", 3, nodes.size());
        log.info("The two entries have the following data: {}", nodes);
        assertEquals("should have found the correct entry", "Lippa", lippa.getName());
        log.info("Lippa is: {}", lippa);
        assertTrue("nodeId (NEO4J) should be present", lippa.getNodeId() != null);
        log.info("NEO4J nodeId for {} is: {}", lippa, lippa.getNodeId());
        Iterable<String> lippaProperties = lippa.getPersistentState().getPropertyKeys();
        for (String key: lippaProperties) {
            log.info("Lippa contains property '{}': {}", key, lippa.getPersistentState().getProperty(key));
        }
        BigInteger lippaPresumedId = (BigInteger) em.createNativeQuery("select id_num from nodes_tb where name_txt = 'Lippa'")
                .setMaxResults(1).getSingleResult();
        log.info("HSQL contains: {}", lippaPresumedId);
        assertEquals("Lippa id should match what RDBMS is saying", lippa.getId().longValue(), lippaPresumedId.longValue());
        Node persistedNode = nodeRepository.findByName(newNode.getName());
        assertEquals("should have the correct value", newNode.getName(), persistedNode.getName());
    }

}