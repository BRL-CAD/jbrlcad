package org.brlcad.numerics;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test framework for the Plane3D class
 *
 * @author Erik Greenwald
 *         <p/>
 *         $Header: /cvs/MUVES3/muves/modules/support/test/mil.army.muves/math/Plane3DTest.java,v 1.4 2007/02/16 15:00:35 jfowler Exp $
 */

public class Plane3DTest {

    public Plane3DTest() {
    }

    @Test
    public void testBasic() {
        // use constructor for plane at origin with given normal
        Vector3 n = new Vector3(1.0, 0.0, 0.0);
        Plane3D pl = new Plane3D(n);

        // a point above
        Point p = new Point(3.0, 1.0, 2.0);

        assertEquals(true, pl.above(p));
        assertEquals(false, pl.below(p));
        assertEquals(false, pl.liesIn(p));
        // a point below
        p = new Point(-3.0, 1.0, 2.0);
        assertEquals(false, pl.above(p));
        assertEquals(true, pl.below(p));
        assertEquals(false, pl.liesIn(p));
        // a point in the plane
        p = new Point(0.0, 1.0, 2.0);
        assertEquals(false, pl.above(p));
        assertEquals(false, pl.below(p));
        assertEquals(true, pl.liesIn(p));

        // use constructor with normal and point
        p = new Point(10.0, 3.0, 4.0);
        pl = new Plane3D(n, p);
        // make sure the supplied normal is in the plane
        assertEquals(false, pl.above(p));
        assertEquals(false, pl.below(p));
        assertEquals(true, pl.liesIn(p));
        // a point above
        p = new Point(13.0, 1.0, 2.0);
        assertEquals(true, pl.above(p));
        assertEquals(false, pl.below(p));
        assertEquals(false, pl.liesIn(p));
        // a point below
        p = new Point(3.0, 1.0, 2.0);
        assertEquals(false, pl.above(p));
        assertEquals(true, pl.below(p));
        assertEquals(false, pl.liesIn(p));
        // a point in the plane
        p = new Point(10.0, 1.0, 2.0);
        assertEquals(false, pl.above(p));
        assertEquals(false, pl.below(p));
        assertEquals(true, pl.liesIn(p));

        // use constuctor from three points
        // this builds a plane at y=3 with normal pointing in negative y direction
        Point p1 = new Point(1.0, 3.0, 5.0);
        Point p2 = new Point(-5.0, 3.0, 20.0);
        Point p3 = new Point(-10.0, 3.0, -9.0);

        pl = new Plane3D(p1, p2, p3);
        // a point above
        p = new Point(13.0, 0.0, 2.0);
        assertEquals(true, pl.above(p));
        assertEquals(false, pl.below(p));
        assertEquals(false, pl.liesIn(p));
        // a point below
        p = new Point(3.0, 5.0, 2.0);
        assertEquals(false, pl.above(p));
        assertEquals(true, pl.below(p));
        assertEquals(false, pl.liesIn(p));
        p = new Point(10.0, 3.0, 2.0);
        // a point in the plane
        assertEquals(false, pl.above(p));
        assertEquals(false, pl.below(p));
        assertEquals(true, pl.liesIn(p));

        Vector3 norm = pl.getNormal();

        assertEquals(0.0, norm.getX(), 0.0);
        assertEquals(-1.0, norm.getY(), 0.0);
        assertEquals(0.0, norm.getZ(), 0.0);

        return;
    }

    @Test
    public void testToString() {
        Vector3 n = new Vector3(1.0, 0.0, 0.0);
        Plane3D pl = new Plane3D(n);

        assertEquals("(1.0, 0.0, 0.0, 0.0)", pl.toString());
    }

    @Test
    public void testIntersectPlanes() {
        Plane3D p1 = new Plane3D( new Vector3( 1, 0, 0 ), new Point( 3, 4, 5 ) );
        Plane3D p2 = new Plane3D( new Vector3( 0, 1, 0 ), new Point( 6, 7, 8 ) );
        Point start = new Point();
        Vector3 dir = new Vector3();
        Ray inter;
        
        inter = p1.intersect( p2 );
        
        assertNotNull( "should intersect", inter );
        assertEquals( "start point", new Point( 3, 7, 0 ), inter.getStart() );
        assertEquals( "direction", new Vector3( 0, 0, 1 ), inter.getDirection() );
        
        p2 = new Plane3D( new Vector3( 1, 0, 0 ), new Point( 6, 7, 8 ) );
        inter = p1.intersect( p2 );
        assertNull( "should not intersect", inter );
        
    }

    @Test
    public void testIntersectRay() {
        Plane3D pl = new Plane3D( new Vector3( 1, 0, 0 ), new Point( 3, 4, 5 ) );
        Ray r = new Ray( new Point( 0, 0, 0 ), new Vector3( 1, 0, 0 ) );
        double dist = pl.intersect(r);
        assertEquals( "distance to intersection", 3.0, dist, 0.0000000001 );
        
        r = new Ray( new Point( 0, 0, 0 ), new Vector3( 0, 1, 0 ) );
        dist = pl.intersect(r);
        assertTrue( "should be infinite", Double.POSITIVE_INFINITY == dist );
        
        r = new Ray( new Point( 0, 0, 0 ), new Vector3( 1.5, 0, 0 ) );
        dist = pl.intersect(r);
        assertEquals( "distance to intersection", 2.0, dist, 0.0000000001 );
        
        pl = new Plane3D( new Vector3( 2, 0, 0 ), new Point( 3, 4, 5 ) );
        dist = pl.intersect(r);
        assertEquals( "distance to intersection", 2.0, dist, 0.0000000001 );
        
        Vector3 dir = new Vector3( 1, 1, 0 );
        dir.normalize();
        r = new Ray( new Point( 0, 0, 0 ), dir );
        dist = pl.intersect(r);
        assertEquals( "distance to intersection", 4.242640687119286, dist, 0.0000000001 );
    }

    @Test
    public void testDistanceTo() {
        // test the point used to define the plane
        Plane3D pl = new Plane3D( new Vector3( 1, 0, 0 ), new Point( 3, 4, 5 ) );
        Point p = new Point( 3, 4, 5 );
        double dist = pl.distanceTo(p);
        assertEquals( "distance to plane", 0.0, dist, 0.0000000001 );
        
        // test another point in the plane
        p = new Point( 3, 500, -2345 );
        dist = pl.distanceTo(p);
        assertEquals( "distance to plane", 0.0, dist, 0.0000000001 );
        
        // test a point above the plane
        p = new Point( 5, 500, -2345 );
        dist = pl.distanceTo(p);
        assertEquals( "distance to plane", 2.0, dist, 0.0000000001 );
        
        // test a point below the plane
        p = new Point( -5, 500, -2345 );
        dist = pl.distanceTo(p);
        assertEquals( "distance to plane", -8.0, dist, 0.0000000001 );
        
        // test the effect of a non-unit normal vector
        pl = new Plane3D( new Vector3( 2, 0, 0 ), new Point( 3, 4, 5 ) );
        p = new Point( 5, 500, -2345 );
        dist = pl.distanceTo(p);
        assertEquals( "distance to plane", 4.0, dist, 0.0000000001 );
        
    }
}
