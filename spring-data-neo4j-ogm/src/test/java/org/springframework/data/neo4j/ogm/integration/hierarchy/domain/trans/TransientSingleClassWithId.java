package org.springframework.data.neo4j.ogm.integration.hierarchy.domain.trans;

import org.springframework.data.neo4j.ogm.annotation.Transient;

@Transient
public class TransientSingleClassWithId {

    private Long id;  //shouldn't really need an ID if it is transient
}
