package org.brlcad.numerics;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test framework for the Point class
 *
 * @author Erik Greenwald
 *         <p/>
 *         $Header: /cvs/MUVES3/muves/modules/support/test/mil.army.muves/math/PointTest.java,v 1.2 2007/02/01 18:27:20 jfowler Exp $
 */

public class PointTest {

    double tolerance = Vector3.INITIAL_TOLERANCE;

    public PointTest() {
    }

    @Test
    public void testCreate() {
        Point p = new Point(1, 2, 3);
        Point p2 = new Point(p);
        Point p3 = new Point();

        assertEquals(1.0, p.getX(), tolerance);
        assertEquals(2.0, p.getY(), tolerance);
        assertEquals(3.0, p.getZ(), tolerance);
        assertEquals(1.0, p2.getX(), tolerance);
        assertEquals(2.0, p2.getY(), tolerance);
        assertEquals(3.0, p2.getZ(), tolerance);
        assertEquals(0.0, p3.getX(), tolerance);
        assertEquals(0.0, p3.getY(), tolerance);
        assertEquals(0.0, p3.getZ(), tolerance);
        return;
    }

    @Test
    public void testSet() {
        Point p = new Point(1, 2, 3);

        p.setX(4.0);
        p.setY(5.0);
        p.setZ(6.0);
        assertEquals(4.0, p.getX(), tolerance);
        assertEquals(5.0, p.getY(), tolerance);
        assertEquals(6.0, p.getZ(), tolerance);
    }

    @Test
    public void testDist() {
        Point p = new Point(4, 5, 6);
        Point q = new Point(5, 6, 7);

        assertEquals(Math.sqrt(3.0), q.dist(p), tolerance);
    }


    @Test
    public void testString() {
        Point p = new Point(1, 2, 3);

        assertEquals("(1.0, 2.0, 3.0)", p.toString());
    }

    @Test
    public void testEquals() {
        Point p1 = new Point(1, 2, 3);
        Point p2 = new Point(1, 2, 3);
        Point p3 = new Point(3, 2, 3);
        String cow = "moo";

        assertEquals(true, p1.equals(p2));
        assertEquals(false, p1.equals(p3));
        assertEquals(false, p1.equals(cow));
    }

    @Test
    public void testIsEqual() {
        Point p1 = new Point(1, 2, 3);
        Point p2 = new Point(1, 2, 3);
        Point p3 = new Point(3, 2, 3);

        assertEquals(true, p1.isEqual(p2));
        assertEquals(false, p1.isEqual(p3));
        assertEquals(true, Point.isEqual(p1, p2));
        assertEquals(false, Point.isEqual(p1, p3));
    }

    @Test
    public void testScale() {
        Point p = new Point(1, 2, 3);

        p.scale(2);
        assertEquals(2.0, p.getX(), tolerance);
        assertEquals(4.0, p.getY(), tolerance);
        assertEquals(6.0, p.getZ(), tolerance);

        p.scale(-0.5);
        assertEquals(-1.0, p.getX(), tolerance);
        assertEquals(-2.0, p.getY(), tolerance);
        assertEquals(-3.0, p.getZ(), tolerance);
    }

    @Test
    public void testJoin() {
        Point p = new Point(1, 2, 3);
        Vector3 v = new Vector3(5, 4, 3);

        p.join(1.2, v);
        assertEquals(7.0, p.getX(), tolerance);
        assertEquals(6.8, p.getY(), tolerance);
        assertEquals(6.6, p.getZ(), tolerance);
    }

    @Test
    public void testTolerance() {
        Point p = new Point(0, 0, 0);

        assertEquals(Vector3.INITIAL_TOLERANCE, p.getTolerance(), Vector3.INITIAL_TOLERANCE);
        p.setTolerance(3.14159);
        assertEquals(3.14159, p.getTolerance(), Vector3.INITIAL_TOLERANCE);
    }

    @Test
    public void testTranslate() {
        Point p = new Point(0, 0, 0);
        Vector3 v = new Vector3(4, 5, 6);

        p.translate(v);
        assertEquals(4.0, p.getX(), tolerance);
        assertEquals(5.0, p.getY(), tolerance);
        assertEquals(6.0, p.getZ(), tolerance);

        v = new Vector3(-3, -2, -1);
        p.translate(v);
        assertEquals(1.0, p.getX(), tolerance);
        assertEquals(3.0, p.getY(), tolerance);
        assertEquals(5.0, p.getZ(), tolerance);
    }
}
