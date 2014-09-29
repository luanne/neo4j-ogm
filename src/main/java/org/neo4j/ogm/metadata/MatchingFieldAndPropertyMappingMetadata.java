package org.neo4j.ogm.metadata;

import org.graphaware.graphmodel.Property;

/**
 * Super-simple implementation of {@link MappingMetadata} that blindly assumes the instance variable name will always match the
 * name of the property read from the graph.
 */
public class MatchingFieldAndPropertyMappingMetadata implements MappingMetadata {

    @Override
    public PersistentField lookUpPersistentFieldForProperty(Property<?, ?> property) {
        return new RegularPersistentField(String.valueOf(property.getKey()));
    }

}