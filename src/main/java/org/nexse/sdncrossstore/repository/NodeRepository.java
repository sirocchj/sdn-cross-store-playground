package org.nexse.sdncrossstore.repository;

import org.nexse.sdncrossstore.domain.Node;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class NodeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Node findByName(String name) {
        if (name == null) return null;
        List<Node> nodes = entityManager.createQuery("from Node where name = :name", Node.class)
                .setParameter("name", name)
                .getResultList();
        if (nodes.size() > 0) {
            final Node node = nodes.get(0);
            if (node != null) {
                node.persist();
            }
            return node;
        }
        return null;
    }

    @Transactional
    public void persist(Node node) {
        this.entityManager.persist(node);
        this.entityManager.flush();
        node.persist();
    }

}