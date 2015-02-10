package org.springframework.data.neo4j.ogm.integration.hierarchy.domain.annotated;

import org.springframework.data.neo4j.ogm.annotation.NodeEntity;
import org.springframework.data.neo4j.ogm.integration.hierarchy.domain.plain.PlainConcreteParent;

@NodeEntity
public class AnnotatedChildWithPlainConcreteParent extends PlainConcreteParent {
}
