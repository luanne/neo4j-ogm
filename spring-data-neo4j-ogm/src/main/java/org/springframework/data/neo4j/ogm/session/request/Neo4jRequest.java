package org.springframework.data.neo4j.ogm.session.request;

import org.springframework.data.neo4j.ogm.session.response.Neo4jResponse;

public interface Neo4jRequest<T> {

    Neo4jResponse<T> execute(String url, String jsonStatements);

}
