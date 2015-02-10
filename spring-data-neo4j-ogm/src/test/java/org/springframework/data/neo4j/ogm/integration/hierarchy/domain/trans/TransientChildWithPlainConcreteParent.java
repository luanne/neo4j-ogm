package org.springframework.data.neo4j.ogm.integration.hierarchy.domain.trans;

import org.springframework.data.neo4j.ogm.annotation.Transient;
import org.springframework.data.neo4j.ogm.integration.hierarchy.domain.plain.PlainConcreteParent;

@Transient
public class TransientChildWithPlainConcreteParent extends PlainConcreteParent {
}
