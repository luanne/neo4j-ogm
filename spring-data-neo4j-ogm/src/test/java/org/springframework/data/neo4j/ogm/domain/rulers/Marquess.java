package org.springframework.data.neo4j.ogm.domain.rulers;

public class Marquess extends Nobleman {
    @Override
    public String rulesOver() {
        return "March";
    }
}
