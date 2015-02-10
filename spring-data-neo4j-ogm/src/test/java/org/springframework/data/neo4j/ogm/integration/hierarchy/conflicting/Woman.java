package org.springframework.data.neo4j.ogm.integration.hierarchy.conflicting;

import org.springframework.data.neo4j.ogm.annotation.NodeEntity;
import org.springframework.data.neo4j.ogm.integration.hierarchy.domain.people.Person;

@NodeEntity(label = "Female")
public class Woman extends Person {

}
