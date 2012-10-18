package org.nexse.sdncrossstore.domain;

import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import javax.persistence.*;

@Entity
@Table(name = "nodes_tb")
@NodeEntity(partial = true)
public class Node {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_num")
    private Long id;

    @Version
    @Column(name = "ver_num")
    private Integer version;

    @Column(name = "name_txt")
    private String name;

    @RelatedTo(type = "LINKED-TO")
    private Node peerNode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Node getPeerNode() {
        return peerNode;
    }

    public void setPeerNode(Node peerNode) {
        this.peerNode = peerNode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Node");
        sb.append("{id=").append(id);
        sb.append(", version=").append(version);
        sb.append(", name='").append(name).append('\'');
        sb.append(", peerNode=").append(peerNode);
        sb.append('}');
        return sb.toString();
    }

}