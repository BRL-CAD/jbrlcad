package org.brlcad.numerics;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import org.junit.Assert;

/**
 * Test framework for the BoundingBox class
 *
 * @author Erik Greenwald
 *         <p/>
 *         $Header: /cvs/MUVES3/muves/modules/support/test/mil.army.muves/math/BoundingBoxTest.java,v 1.3 2007/02/16 15:00:35 jfowler Exp $
 */

public class BoundingBoxTest {

    public BoundingBoxTest() {
    }

    @Test
    public void testSetGet() {
        Point min = new Point(1.0, 2.0, 3.0);
        Point max = new Point(7.0, 8.0, 9.0);
        BoundingBox bb = new BoundingBox(min, max);

        assertEquals(min, bb.getMin());
        assertEquals(max, bb.getMax());
        Point v = new Point(4.0, 5.0, 6.0);

        bb.setMin(v);
        assertEquals(v, bb.getMin());
        v = new Point(10.0, 11.0, 12.0);
        bb.setMax(v);
        assertEquals(v, bb.getMax());
        return;
    }

    @Test
    public void testBackwardsCreation() {
        Point a = new Point(1.0, 2.0, 3.0);
        Point b = new Point(7.0, 8.0, 9.0);
        BoundingBox bb;

        // forward
        bb = new BoundingBox(a, b);
        assertEquals(a, bb.getMin());
        assertEquals(b, bb.getMax());

        // backward
        bb = new BoundingBox(b, a);
        assertEquals(a, bb.getMin());
        assertEquals(b, bb.getMax());

        // some swapped
        bb = new BoundingBox(new Point(7, 2, 3), new Point(1, 8, 9));
        assertEquals(a, bb.getMin());
        assertEquals(b, bb.getMax());

        // some more swapped
        bb = new BoundingBox(new Point(7, 8, 3), new Point(1, 2, 9));
        assertEquals(a, bb.getMin());
        assertEquals(b, bb.getMax());
    }

    @Test
    public void testBound() {
        BoundingBox bb =
                new BoundingBox(new Point(1, 2, 3), new Point(7, 8, 9));
        assertEquals(true, bb.bound(new Point(4, 3, 6)));
        assertEquals(false, bb.bound(new Point(0, 3, 6)));
        assertEquals(false, bb.bound(new Point(4, 1, 6)));
        assertEquals(false, bb.bound(new Point(4, 9, 6)));
        assertEquals(false, bb.bound(new Point(4, 5, 60)));
    }

    @Test
    public void testExtents() {
        BoundingBox bb = new BoundingBox(new Point(1.5, 2.5, 3.5), new Point(6.5, 8.5, 10.5));

        // Check extents along X-axis
        List<Double> exts = bb.getExtentsInDirection(new Vector3(1.0, 0.0, 0.0));
        double min = exts.get(0);
        double max = exts.get(1);
        double tol = 0.000001;
        assertTrue("Checking minimum extent along x-Axis", min - 1.5 < tol && min - 1.5 > -tol);
        assertTrue("Checking maximum extent along x-Axis", max - 6.5 < tol && max - 6.5 > -tol);

        // Check extents along Y-axis
        exts = bb.getExtentsInDirection(new Vector3(0.0, 1.0, 0.0));
        min = exts.get(0);
        max = exts.get(1);
        assertTrue("Checking minimum extent along y-Axis", min - 2.5 < tol && min - 2.5 > -tol);
        assertTrue("Checking maximum extent along y-Axis", max - 8.5 < tol && max - 8.5 > -tol);

        // Check extents along Z-axis
        exts = bb.getExtentsInDirection(new Vector3(0.0, 0.0, 1.0));
        min = exts.get(0);
        max = exts.get(1);
        assertTrue("Checking minimum extent along z-Axis", min - 3.5 < tol && min - 3.5 > -tol);
        assertTrue("Checking maximum extent along z-Axis", max - 10.5 < tol && max - 10.5 > -tol);

        // Try an off-axis direction
        Vector3 dir = new Vector3(1.0, 1.0, 0.0);
        dir.normalize();
        exts = bb.getExtentsInDirection(dir);
        min = exts.get(0);
        max = exts.get(1);
        double expectedMin = 2.82842712474619;
        double expectedMax = 10.606601717798213;
        assertTrue("Checking minimum extent along off-Axis",
                min - expectedMin < tol && min - expectedMin > -tol);
        assertTrue("Checking maximum extent along off-Axis",
                max - expectedMax < tol && max - expectedMax > -tol);

    }

    @Test
    public void testIntersect() {
        BoundingBox bb = new BoundingBox( new Point( -1, -2, -3 ), new Point( 5, 6, 7 ) );
        
        // a Ray that intersects
        Ray r = new Ray( new Point( -10, 2, 2 ), new Vector3( 1, 0, 0 ));
        double[] hits = null;
        long startTime = System.currentTimeMillis();
        for (int i=0 ; i<10000 ; i++) {
            hits = bb.intersect(r);
        }
        System.out.println( "************intersect time = " + (System.currentTimeMillis() - startTime));
        assertEquals( "first hit", 9.0, hits[0], 0.00000001 );
        assertEquals( "second hit", 15.0, hits[1], 0.00000001 );
        
        // a Ray that misses
        r = new Ray( new Point( -10, 2, 2 ), new Vector3( 0, 0, 1 ));
        hits = bb.intersect(r);
        assertNull( "should not intersect", hits );
        
        // a Ray that intersects the corners
        Vector3 dir = Vector3.minus( bb.getMax(), bb.getMin() );
        dir.normalize();
        Point start = new Point( bb.getMin() );
        start.join( -5.0, dir );
        r = new Ray( start, dir );
        hits = bb.intersect(r);
        assertEquals( "first hit", 5.0, hits[0], 0.00000001 );
        assertEquals( "second hit", 19.142135623730947, hits[1], 0.00000001 );
    }

    @Test
    public void testIsect2() {
        BoundingBox bb = new BoundingBox( new Point( -1, -2, -3 ), new Point( 5, 6, 7 ) );

        // a Ray that intersects
        Ray r = new Ray(new Point(-10, 2, 2), new Vector3(1, 0, 0));
        double[] hits = null;
        long startTime = System.currentTimeMillis();
        for (int i=0 ; i<10000 ; i++) {
            hits = bb.isect2(r);
        }
        System.out.println( "************isect time = " + (System.currentTimeMillis() - startTime));
        assertEquals( "first hit", 9.0, hits[0], 0.00000001 );
        assertEquals( "second hit", 15.0, hits[1], 0.00000001 );

        // a Ray that misses
        r = new Ray( new Point( -10, 2, 2 ), new Vector3( 0, 0, 1 ));
        hits = bb.isect2(r);
        assertNull( "should not intersect", hits );

        // a Ray that intersects the corners
        Vector3 dir = Vector3.minus( bb.getMax(), bb.getMin() );
        dir.normalize();
        Point start = new Point( bb.getMin() );
        start.join( -5.0, dir );
        r = new Ray( start, dir );
        hits = bb.isect2(r);
        assertEquals( "first hit", 5.0, hits[0], 0.00000001 );
        assertEquals( "second hit", 19.142135623730947, hits[1], 0.00000001 );
    }
    
    @Test
    public void testFlatBoxIntersect() {
        BoundingBox bb = new BoundingBox( new Point( 0, 0, 0 ), new Point( 6, 6, 0 ) );
        // a Ray that intersects
        Ray r = new Ray(new Point(3, 3, -10), new Vector3(0, 0, 1));
        //Should intersect the Flatbox
        Assert.assertTrue(bb.doesIntersect(r));
    }

    @Test
    public void testConeIntersect() {
        // Rays in the -Z direction
        // check a Ray that intersects the BB
        Point min = new Point(1.0, 2.0, 3.0);
        Point max = new Point(7.0, 8.0, 9.0);
        BoundingBox bb = new BoundingBox(min, max);
        Point start = new Point(4.0, 5.0, 10.0);
        Vector3 dir = new Vector3(0,0,-1);
        Ray ray = new Ray(start, dir);
        double halfConeAngle = Math.PI/4.0;
//        System.out.println( "cone: " + ray + ", half cone angle = " + 180.0 * halfConeAngle / Math.PI + " degrees");
        boolean intersects = bb.intersectsCone(ray, Math.cos(halfConeAngle));
        assertTrue("should intersect", intersects);

        // a Ray that misses the BB, but middle of an edge is within cone
        start = new Point(-4.0, 5.0, 10.0);
        dir = new Vector3(0,0,-1);
        ray = new Ray(start, dir);
        halfConeAngle = Math.PI/4.0;
//        System.out.println( "cone: " + ray + ", half cone angle = " + 180.0 * halfConeAngle / Math.PI + " degrees");
        intersects = bb.intersectsCone(ray, Math.cos(halfConeAngle));
        assertTrue("should intersect", intersects);

        // a Ray that misses the BB, but a corner is within cone
        start = new Point(-4.0, 8.0, 9.0);
        dir = new Vector3(0,0,-1);
        ray = new Ray(start, dir);
        halfConeAngle = Math.PI/4.0;
//        System.out.println( "cone: " + ray + ", half cone angle = " + 180.0 * halfConeAngle / Math.PI + " degrees");
        intersects = bb.intersectsCone(ray, Math.cos(halfConeAngle));
        assertTrue("should intersect", intersects);

        // cone that misses
        start = new Point(15.0, 16.0, 10.0);
        dir = new Vector3(0,0,-1);
        ray = new Ray(start, dir);
        halfConeAngle = Math.PI/4.0;
//        System.out.println( "cone: " + ray + ", half cone angle = " + 180.0 * halfConeAngle / Math.PI + " degrees");
        intersects = bb.intersectsCone(ray, Math.cos(halfConeAngle));
        assertFalse("should not intersect", intersects);


        // Rays in the +X direction
        // check a Ray that intersects the BB
        start = new Point(-1.0, 5.0, 6.0);
        dir = new Vector3(1,0,0);
        ray = new Ray(start, dir);
        halfConeAngle = Math.PI/4.0;
//        System.out.println( "cone: " + ray + ", half cone angle = " + 180.0 * halfConeAngle / Math.PI + " degrees");
        intersects = bb.intersectsCone(ray, Math.cos(halfConeAngle));
        assertTrue("should intersect", intersects);

        // a Ray that misses the BB, but middle of an edge is within cone
        start = new Point(-1.0, 12.0, 6.0);
        dir = new Vector3(1,0,0);
        ray = new Ray(start, dir);
        halfConeAngle = Math.PI/4.0;
//        System.out.println( "cone: " + ray + ", half cone angle = " + 180.0 * halfConeAngle / Math.PI + " degrees");
        intersects = bb.intersectsCone(ray, Math.cos(halfConeAngle));
        assertTrue("should intersect", intersects);

        // a Ray that misses the BB, but a corner is within cone
        start = new Point(-1.0, 13.0, 14.0);
        dir = new Vector3(1,0,0);
        ray = new Ray(start, dir);
        halfConeAngle = Math.PI/4.0;
//        System.out.println( "cone: " + ray + ", half cone angle = " + 180.0 * halfConeAngle / Math.PI + " degrees");
        intersects = bb.intersectsCone(ray, Math.cos(halfConeAngle));
        assertTrue("should intersect", intersects);

        // cone that misses
        start = new Point(-1, 16.0, 17.0);
        dir = new Vector3(1,0,0);
        ray = new Ray(start, dir);
        halfConeAngle = Math.PI/4.0;
//        System.out.println( "cone: " + ray + ", half cone angle = " + 180.0 * halfConeAngle / Math.PI + " degrees");
        intersects = bb.intersectsCone(ray, Math.cos(halfConeAngle));
        assertFalse("should not intersect", intersects);

        // a Ray that misses because it is behind BB
//        System.out.println( "***************************");
        start = new Point(8, 5.0, 6.0);
        dir = new Vector3(1,0,0);
        ray = new Ray(start, dir);
        halfConeAngle = Math.PI/4.0;
//        System.out.println( "cone: " + ray + ", half cone angle = " + 180.0 * halfConeAngle / Math.PI + " degrees");
        intersects = bb.intersectsCone(ray, Math.cos(halfConeAngle));
        assertFalse("should not intersect", intersects);

        // a Cone with cone angle > 90 degrees
//        System.out.println( "***************************");
        start = new Point(-20.0, -40.0, 10.0);
        dir = new Vector3(0,0,-1);
        ray = new Ray(start, dir);
        halfConeAngle = 3.0*Math.PI/4.0;
//        System.out.println( "cone: " + ray + ", half cone angle = " + 180.0 * halfConeAngle / Math.PI + " degrees");
        intersects = bb.intersectsCone(ray, Math.cos(halfConeAngle));
        assertTrue("should intersect", intersects);
    }

}
