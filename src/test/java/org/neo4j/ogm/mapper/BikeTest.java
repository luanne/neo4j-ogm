package org.neo4j.ogm.mapper;

import org.junit.Test;
import org.neo4j.ogm.CypherQueryProxy;
import org.neo4j.ogm.mapper.cypher.CypherQuery;
import org.neo4j.ogm.mapper.domain.bike.Bike;
import org.neo4j.ogm.mapper.domain.bike.Wheel;
import org.neo4j.ogm.mapper.model.BikeRequest;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.util.Collection;

import static org.junit.Assert.*;

public class BikeTest {

    @Test
    public void testDeserialiseBikeModel() throws Exception {

        CypherQuery queryProxy = new CypherQueryProxy();
        queryProxy.setRequest(new BikeRequest());

        SessionFactory sessionFactory = new SessionFactory(queryProxy, "org.neo4j.ogm.mapper.domain.bike");
        Session session = sessionFactory.openSession();

        long now = -System.currentTimeMillis();
        Collection<Bike> bikes = session.load(Bike.class);
        System.out.println("deserialised in " + (now + System.currentTimeMillis()) + " milliseconds");

        assertFalse(bikes.isEmpty());
        Bike bike = bikes.iterator().next();

        assertNotNull(bike);
        assertEquals(15, (long) bike.getId());
        assertEquals(2, bike.getColours().length);

        // check the frame
        assertEquals(18, (long) bike.getFrame().getId());
        assertEquals(27, (int) bike.getFrame().getSize());

        // check the saddle
        assertEquals(19, (long) bike.getSaddle().getId());
        assertEquals(42.99, bike.getSaddle().getPrice(), 0.00);
        assertEquals("plastic", bike.getSaddle().getMaterial());

        // check the wheels
        assertEquals(2, bike.getWheels().size());
        for (Wheel wheel : bike.getWheels()) {
            if (wheel.getId().equals(16L)) {
                assertEquals(3, (int) wheel.getSpokes());
            }
            if (wheel.getId().equals(17L)) {
                assertEquals(5, (int) wheel.getSpokes());
            }
        }
    }

}
