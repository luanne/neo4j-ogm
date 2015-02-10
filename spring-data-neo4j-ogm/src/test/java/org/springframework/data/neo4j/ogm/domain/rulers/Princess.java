package org.springframework.data.neo4j.ogm.domain.rulers;

public class Princess extends Daughter {

    @Override
    public boolean isCommoner() {
        return false;
    }
}
