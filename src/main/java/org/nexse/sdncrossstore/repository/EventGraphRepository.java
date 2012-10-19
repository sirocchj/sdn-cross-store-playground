package org.nexse.sdncrossstore.repository;

import org.nexse.sdncrossstore.domain.Event;
import org.springframework.data.neo4j.repository.GraphRepository;

interface EventGraphRepository extends GraphRepository<Event> {}