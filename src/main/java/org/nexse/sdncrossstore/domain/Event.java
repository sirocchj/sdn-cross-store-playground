package org.nexse.sdncrossstore.domain;

import org.springframework.data.neo4j.annotation.GraphProperty;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import javax.persistence.*;

@Entity
@Table(name = "events_tb")
@NodeEntity(partial = true)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_num")
    private Long id;

    @Version
    @Column(name = "ver_num")
    private Integer version;

    @Column(name = "name_txt")
    private String name;

    @GraphProperty
    private String someProp;

    @RelatedTo(type = "LINKED_TO")
    private Event peerEvent;

    public Long getId() {
        return id;
    }

    public Integer getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSomeProp() {
        return someProp;
    }

    public void setSomeProp(String someProp) {
        this.someProp = someProp;
    }

    public Event getPeerEvent() {
        return peerEvent;
    }

    public void setPeerEvent(Event peerEvent) {
        this.peerEvent = peerEvent;
    }

    public Event withName(String name) {
        setName(name);
        return this;
    }

    public Event withSomeProp(String someProp) {
        setSomeProp(someProp);
        return this;
    }

    public Event withPeerEvent(Event peerEvent) {
        setPeerEvent(peerEvent);
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Event");
        sb.append("{id=").append(id);
        sb.append(", version=").append(version);
        sb.append(", name='").append(name).append('\'');
        sb.append(", someProp='").append(someProp).append('\'');
        sb.append(", peerEvent=").append(peerEvent);
        sb.append('}');
        return sb.toString();
    }

}