/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.cypher.compiler;

/**
 * Manages identifiers used within the scope of a single Cypher query.
 *
 * Two different formatting schemes are used.
 *
 * 1. References to new nodes are identified by a monotonically increasing integer 0, 1, 2...
 * prepended by an underscore, e.g.
 *
 * _0, _1, _2 ...
 *
 * 2. References to existing nodes are identified using the node id prepended by a $, e.g
 *
 * $513400, $9075, $2 ...
 *
 * The use of two separate schemes ensures that the identifiers for new nodes and existing nodes cannot
 * overlap.
 *
 */
class IdentifierManager {

    private static final String NEW_FORMAT = "_%d";
    private static final String EXISTING_FORMAT = "$%d";

    private int idCounter;

    /**
     * Generates the next variable name to use in the context of a Cypher query for creating new objects.
     *
     * @return The next variable name to use of the form _id, never <code>null</code>
     */
    public synchronized String nextIdentifier() {
        return String.format(NEW_FORMAT, this.idCounter++);
    }

    /**
     * Generates a variable name to use in the context of a Cypher query referring to existing objects.
     *
     * @return The variable name to use of the form $id, never <code>null</code>
     */
    public String identifier(Long value) {
        return String.format(EXISTING_FORMAT, value);
    }

    public synchronized void releaseIdentifier() {
        this.idCounter--;
    }
}
