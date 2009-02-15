package org.brlcad.numerics;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test framework for the Ray class
 *
 * @author Erik Greenwald
 *         <p/>
 *         $Header: /cvs/MUVES3/muves/modules/support/test/mil.army.muves/math/RayTest.java,v 1.2 2007/02/16 15:00:35 jfowler Exp $
 */

public class RayTest {

    public RayTest() {
    }

    @Test
    public void testStart() {
        Point p = new Point(1, 2, 3);
        Ray r = new Ray(p, new Vector3(4, 5, 6));

        assertEquals(p, r.getStart());
        r = new Ray(new Point(1, 2, 2), new Vector3(4, 5, 6));
        assertEquals(false, r.getStart().isEqual(p));
        return;
    }

    @Test
    public void testDirection() {
        Vector3 d = new Vector3(4, 5, 6);
        Ray r = new Ray(new Point(1, 2, 3), d);

        assertEquals(d, r.getDirection());
        r = new Ray(new Point(1, 2, 3), new Vector3(4, 5, 5));
        assertEquals(false, r.getDirection().isEqual(d));
        return;
    }

    @Test
    public void testEquality() {
        Ray r1 = new Ray(new Point(1, 2, 3), new Vector3(4, 5, 6));
        Ray r2 = new Ray(new Point(1, 2, 3), new Vector3(4, 5, 6));
        Ray r3 = new Ray(new Point(1, 2, 2), new Vector3(4, 5, 6));
        Ray r4 = new Ray(new Point(1, 2, 3), new Vector3(4, 5, 5));
        assertEquals(true, r1.isEqual(r2));
        assertEquals(true, Ray.isEqual(r1, r2));
        assertEquals(false, r1.isEqual(r3));
        assertEquals(false, Ray.isEqual(r1, r3));
        assertEquals(false, r1.isEqual(r4));
        assertEquals(false, Ray.isEqual(r1, r4));
    }
}
