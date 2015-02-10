package org.springframework.data.neo4j.ogm.domain.rulers;

public class Viscount extends Nobleman {
    @Override
    public String rulesOver() {
        return "District";
    }
}
