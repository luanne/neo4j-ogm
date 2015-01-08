package org.neo4j.ogm.integration;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.bike.Bike;
import org.neo4j.ogm.domain.bike.Saddle;
import org.neo4j.ogm.domain.bike.Wheel;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.SessionFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

/**
 *  Temporary playground for the full cycle.
 */
public class EndToEndTest extends IntegrationTest {

    private static SessionFactory sessionFactory;

    @BeforeClass
    public static void init() throws IOException {
        setUp();
        sessionFactory = new SessionFactory("org.neo4j.ogm.domain.bike");
        session = sessionFactory.openSession("http://localhost:" + neoPort);
    }

    @Test
    public void canSimpleQueryDatabase() {
        Saddle expected = new Saddle();
        expected.setPrice(29.95);
        expected.setMaterial("Leather");
        session.save(expected);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("material", "Leather");
        Saddle actual = session.queryForObject(Saddle.class, "MATCH (saddle:Saddle{material: {material}}) RETURN saddle", parameters);

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getMaterial(), actual.getMaterial());
    }

    @Test
    public void canComplexQueryDatabase() {
        Saddle saddle = new Saddle();
        saddle.setPrice(29.95);
        saddle.setMaterial("Leather");
        Wheel frontWheel = new Wheel();
        Wheel backWheel = new Wheel();
        Bike bike = new Bike();
        bike.setBrand("Huffy");
        bike.setWheels(Arrays.asList(frontWheel, backWheel));
        bike.setSaddle(saddle);

        session.save(bike);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("brand", "Huffy");
        Bike actual = session.queryForObject(Bike.class, "MATCH (bike:Bike{brand:{brand}})-[rels]-() RETURN bike, COLLECT(DISTINCT rels) as rels", parameters);

        assertEquals(bike.getId(), actual.getId());
        assertEquals(bike.getBrand(), actual.getBrand());
        assertEquals(bike.getWheels().size(), actual.getWheels().size());
        assertNotNull(actual.getSaddle());
    }

    @Test
    public void canSaveNewObjectTreeToDatabase() {

        Wheel frontWheel = new Wheel();
        Wheel backWheel = new Wheel();
        Bike bike = new Bike();

        // TODO: can't persist the 1-side of an object relationship...
        //bike.setFrame(new Frame());
        //bike.setSaddle(new Saddle());
        bike.setWheels(Arrays.asList(frontWheel, backWheel));

        assertNull(frontWheel.getId());
        assertNull(backWheel.getId());
        assertNull(bike.getId());
        //assertNull(bike.getFrame().getId());
        //assertNull(bike.getSaddle().getId());

        session.save(bike);

        assertNotNull(frontWheel.getId());
        assertNotNull(backWheel.getId());
        assertNotNull(bike.getId());
        //assertNotNull(bike.getFrame().getId());
        //assertNotNull(bike.getSaddle().getId());

    }

    @Test
    public void tourDeFrance() {

        //session = sessionFactory.openSession("http://localhost:7474");

        long now = -System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {

            Wheel frontWheel = new Wheel();
            Wheel backWheel = new Wheel();
            Bike bike = new Bike();

            // TODO: can't persist the 1-side of an object relationship...
            //bike.setFrame(new Frame());
            //bike.setSaddle(new Saddle());
            bike.setWheels(Arrays.asList(frontWheel, backWheel));

            session.save(bike);
        }
        now += System.currentTimeMillis();

        Neo4jSession defaultSession = (Neo4jSession) session;
        System.out.println("Number of separate requests: 1000");
        System.out.println("Number of threads: 1");
        System.out.println("Number of new objects to create per request: 3");
        System.out.println("Number of relationships to create per request: 2");
        System.out.println("Average number of requests per second to HTTP TX endpoint (avg. throughput) : " + (int) (1000000.0 / now));
    }

    @Test
    public void multiThreadedTourDeFrance() throws InterruptedException {

        //session = sessionFactory.openSession("http://localhost:7474");


        final int NUM_THREADS=4;
        final int NUM_INSERTS = 1000 / NUM_THREADS;

        List<Thread> threads = new ArrayList<>();

        long now = -System.currentTimeMillis();

        for (int i = 0; i < NUM_THREADS; i++) {
            Thread thread = new Thread( new Runnable() {

                @Override
                public void run() {
                    for (int i = 0; i < NUM_INSERTS; i++) {

                        Wheel frontWheel = new Wheel();
                        Wheel backWheel = new Wheel();
                        Bike bike = new Bike();

                        // TODO: can't persist the 1-side of an object relationship...
                        //bike.setFrame(new Frame());
                        //bike.setSaddle(new Saddle());
                        bike.setWheels(Arrays.asList(frontWheel, backWheel));

                        session.save(bike);
                    }
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (int i = 0; i < NUM_THREADS; i++) {
            threads.get(i).join();
        }
        now += System.currentTimeMillis();

        Neo4jSession defaultSession = (Neo4jSession) session;
        System.out.println("Number of separate requests: 1000");
        System.out.println("Number of threads: " + NUM_THREADS);
        System.out.println("Number of new objects to create per request: 3");
        System.out.println("Number of relationships to create per request: 2");
        System.out.println("Average number of requests per second to HTTP TX endpoint (avg. throughput) : " + (int) (1000000.0 / now));


    }
}
