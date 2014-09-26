package org.neo4j.ogm.mapper;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.junit.Test;
import org.neo4j.ogm.mapper.domain.bike.Bike;
import org.neo4j.ogm.mapper.domain.bike.Wheel;
import org.neo4j.ogm.mapper.model.BikeModel;
import org.neo4j.ogm.strategy.simple.SimpleMappingStrategy;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

public class BikeTest2 {

    @Test
    public void testDefaultBikeMapping() throws Exception {

        GraphModel graphModel = BikeModel.load();

        Bike bike = (Bike) SimpleMappingStrategy.forType(Bike.class).mapToObject(graphModel);

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