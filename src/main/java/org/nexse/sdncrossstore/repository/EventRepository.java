package org.nexse.sdncrossstore.repository;

import org.nexse.sdncrossstore.domain.Event;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class EventRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Event findByName(String name) {
        if (name == null) return null;
        List<Event> events = entityManager.createQuery("from Event where name = :name", Event.class)
                .setParameter("name", name)
                .getResultList();
        if (events.size() > 0) {
            final Event event = events.get(0);
            if (event != null) {
                event.persist();
            }
            return event;
        }
        return null;
    }

    @Transactional
    public void persist(Event event) {
        this.entityManager.persist(event);
        this.entityManager.flush();
        event.persist();
    }

}