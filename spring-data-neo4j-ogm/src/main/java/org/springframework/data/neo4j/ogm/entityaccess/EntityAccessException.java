package org.springframework.data.neo4j.ogm.entityaccess;

public class EntityAccessException extends RuntimeException {

    public EntityAccessException(String msg, Exception cause) {
        super(msg, cause);
    }
}
