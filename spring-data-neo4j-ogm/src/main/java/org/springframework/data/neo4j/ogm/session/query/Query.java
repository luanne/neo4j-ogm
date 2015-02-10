package org.springframework.data.neo4j.ogm.session.query;

public interface Query<T> extends AutoCloseable {

    Query<T> execute();

    T next();

}
