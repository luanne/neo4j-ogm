package org.springframework.data.neo4j.ogm.cypher.query;

import org.springframework.data.neo4j.ogm.cypher.statement.ParameterisedStatement;

import java.util.Map;

public class RowModelQuery extends ParameterisedStatement {

    public RowModelQuery(String cypher, Map<String, ?> parameters) {
        super(cypher, parameters, "row");
    }
}
