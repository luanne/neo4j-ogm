package org.springframework.data.neo4j.ogm.integration.hierarchy.domain.annotated;

import org.springframework.data.neo4j.ogm.annotation.NodeEntity;

@NodeEntity(label = "Parent")
public class AnnotatedConcreteNamedParent {

    private Long id;
}
