/*
 * Copyright (c) 2014-2015 "GraphAware"
 *
 * GraphAware Ltd
 *
 * This file is part of Neo4j-OGM.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.neo4j.ogm.cypher.compiler;

import org.neo4j.ogm.cypher.statement.ParameterisedStatement;

import java.util.List;
import java.util.Map;

/**
 * Defines a simple API for building up Cypher queries programmatically.
 */
public interface CypherCompiler {

    /**
     * Defines a new relationship between the specified start node to end node with the given relationship type and direction
     *
     * @deprecated Setting relationships should now be done through the {@link RelationshipBuilder}
     * @param startNode The {@link NodeBuilder} representation of the relationship start node
     * @param relationshipType The type of relationship to create between the nodes
     * @param relationshipProperties The (optional) {@code Map} containing the properties of the relationship
     * @param endNode The {@link NodeBuilder} representation of the relationship end node
     */
    @Deprecated
    void relate(String startNode, String relationshipType, Map<String, Object> relationshipProperties, String endNode);

    /**
     * Defines a relationship deletion between the specified start node to end node with the given relationship type and direction.
     *
     * @param startNode The {@link NodeBuilder} representation of the relationship start node
     * @param relationshipType The type of relationship between the nodes to delete
     * @param endNode The {@link NodeBuilder} representation of the relationship end node
     */
    void unrelate(String startNode, String relationshipType, String endNode);

    /**
     * Returns {@link NodeBuilder} that represents a new node to be created in the database.
     *
     * @return A {@link NodeBuilder} representing a new node
     */
    NodeBuilder newNode();

    /**
     * Returns a {@link NodeBuilder} that represents a node that already exists in the database and matches the given ID.
     *
     * @param existingNodeId The ID of the node in the database
     * @return A {@link NodeBuilder} representing the node in the database that corresponds to the given ID
     */
    NodeBuilder existingNode(Long existingNodeId);

    /**
     * Returns a {@link RelationshipBuilder} to use for constructing Cypher for writing a new relationship to the database.
     *
     * @return A new {@link RelationshipBuilder}
     */
    RelationshipBuilder newRelationship();

    /**
     * Returns a {@link RelationshipBuilder} to use for constructing Cypher to update an existing relationship in the database
     * that possesses the given ID.
     *
     * @param existingRelationshipId The ID of the relationship in the database, which shouldn't be <code>null</code>
     * @return A new {@link RelationshipBuilder} bound to the identified relationship
     */
    RelationshipBuilder existingRelationship(Long existingRelationshipId);

    /**
     * Retrieves the Cypher queries that have been built up through this {@link CypherCompiler}.
     * <p>
     * Please node that there is no requirement that implementations of {@link CypherCompiler} provide an idempotent or
     * even deterministic implementation of this method.  Therefore, it's recommended to only call this method once
     * after all query building has been completed.
     * </p>
     *
     * @return A {@link List} of Cypher queries to be executed or an empty list if there aren't any, never <code>null</code>
     */
    List<ParameterisedStatement> getStatements();

    /**
     * Returns an unused relationship's reference to the ref pool
     *
     * This is to ensure that references are only created when needed
     *
     * @param relationshipBuilder
     */
    void release(RelationshipBuilder relationshipBuilder);

    String nextIdentifier();

    /**
     * Returns this compiler's context
     * @return the current compiler context
     */
    CypherContext context();

    /**
     * Compiles the current request and returns the compile context, which
     * includes all the statements to be executed and related information
     * @return the current compiler context
     */
    CypherContext compile();
}
