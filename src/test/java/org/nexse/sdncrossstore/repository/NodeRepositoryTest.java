package org.nexse.sdncrossstore.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nexse.sdncrossstore.domain.Node;
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
import java.util.List;

import static junit.framework.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext
public class NodeRepositoryTest {

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
        em.flush();
        List<Node> nodes = em.createQuery("from Node where name = :name", Node.class)
                .setParameter("name", "Lippa")
                .getResultList();
        assertEquals("should have found the entry", 1, nodes.size());
        assertEquals("should have found the correct entry", "Lippa", nodes.get(0).getName());
        Node persistedNode = nodeRepository.findByName(newNode.getName());
        assertEquals("should have the correct value", newNode.getName(), persistedNode.getName());
    }

}