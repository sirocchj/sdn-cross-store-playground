package org.nexse.sdncrossstore.repository;

import org.nexse.sdncrossstore.domain.Node;
import org.springframework.data.neo4j.repository.GraphRepository;

interface GraphNodeRepository extends GraphRepository<Node> {}