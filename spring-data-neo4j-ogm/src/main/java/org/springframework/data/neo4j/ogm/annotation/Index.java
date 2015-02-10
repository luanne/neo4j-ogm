package org.springframework.data.neo4j.ogm.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Inherited
public @interface Index {

    static final String CLASS = "org.springframework.data.neo4j.ogm.annotation.Index";
    static final String UNIQUE = "unique";

    boolean unique() default false;

}
