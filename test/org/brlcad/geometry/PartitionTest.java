/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.geometry;

import java.util.BitSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Ray;
import org.brlcad.numerics.Tolerance;
import org.brlcad.numerics.Vector3;
import org.brlcad.preppedGeometry.PreppedCombination;
import org.brlcad.spacePartition.RayData;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jra
 */
public class PartitionTest {

    public PartitionTest() {}

    @Test
    public void intersectTest() {
        // make some Partitions to intersect
        PreppedCombination reg1 = new MockPreppedCombination();
        PreppedCombination reg2 = new MockPreppedCombination();
        PreppedCombination reg3 = new MockPreppedCombination();
        PreppedCombination reg4 = new MockPreppedCombination();
        TreeSet<Partition> segs1 = new TreeSet<Partition>();
        TreeSet<Partition> segs2 = new TreeSet<Partition>();
        Point start = new Point( 1, 2, 3 );
        Vector3 dir = new Vector3( 0, 1, 0 );
        Ray ray = new Ray( start, dir );
        BitSet solidBits = new BitSet();
        BitSet regionBits = new BitSet();
        RayData rayData = new RayData( start, 0.0, solidBits, regionBits, new Tolerance(0.005, 0.995), ray);
        double dist = 1.0;
        Point hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        Vector3 norm = Vector3.negate(dir);
        Hit inHit = new Hit(dist, hitPoint, norm, 1, rayData);
        dist = 2.0;
        hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        norm = new Vector3(dir);
        Hit outHit = new Hit(dist, hitPoint, norm, 1, rayData);

        Segment seg = new Segment(inHit, outHit);
        Partition part = new Partition(seg, reg1.getName(), rayData);
        segs1.add(part);

        dist = 3.0;
        hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        norm = Vector3.negate(dir);
        inHit = new Hit(dist, hitPoint, norm, 1, rayData);
        dist = 4.0;
        hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        norm = new Vector3(dir);
        outHit = new Hit(dist, hitPoint, norm, 1, rayData);

        seg = new Segment(inHit, outHit);
        part = new Partition(seg, reg2.getName(), rayData);
        segs1.add(part);

        dist = 1.25;
        hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        norm = Vector3.negate(dir);
        inHit = new Hit(dist, hitPoint, norm, 1, rayData);
        dist = 1.75;
        hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        norm = new Vector3(dir);
        outHit = new Hit(dist, hitPoint, norm, 1, rayData);

        seg = new Segment(inHit, outHit);
        part = new Partition(seg, reg3.getName(), rayData);
        segs2.add(part);

        dist = 2.5;
        hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        norm = Vector3.negate(dir);
        inHit = new Hit(dist, hitPoint, norm, 1, rayData);
        dist = 3.5;
        hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        norm = new Vector3(dir);
        outHit = new Hit(dist, hitPoint, norm, 1, rayData);

        seg = new Segment(inHit, outHit);
        part = new Partition(seg, reg4.getName(), rayData);
        segs2.add(part);

        SortedSet<Partition> result = Partition.intersect(null, null);
        assertNull("Result should be null", result);

        result = Partition.intersect(segs1, null);
        assertNull("Result should be null", result);

        result = Partition.intersect(null, segs1);
        assertNull("Result should be null", result);

        result = Partition.intersect(segs1, segs2);
        assertNotNull("Result should not be null", result);
        assertTrue( "Result should have 2 Segments", result.size() == 2);
        assertTrue( "First Segment should start at 1.25, but was " + result.first().getInHit().getHit_dist()
                , result.first().getInHit().getHit_dist() == 1.25);
        assertTrue( "First Segment should end at 1.75, but was " + result.first().getOutHit().getHit_dist(),
                result.first().getOutHit().getHit_dist() == 1.75);
        assertTrue( "Second Segment should start at 3.0, but was " + result.last().getInHit().getHit_dist(),
                result.last().getInHit().getHit_dist() == 3.0);
        assertTrue( "Second Segment should end at 3.5, but was " + result.last().getOutHit().getHit_dist(),
                result.last().getOutHit().getHit_dist() == 3.5);
    }

    @Test
    public void testUnion() {
        // make some Partitions to union
        PreppedCombination reg1 = new MockPreppedCombination();
        PreppedCombination reg2 = new MockPreppedCombination();
        PreppedCombination reg3 = new MockPreppedCombination();
        PreppedCombination reg4 = new MockPreppedCombination();
        TreeSet<Partition> segs1 = new TreeSet<Partition>();
        TreeSet<Partition> segs2 = new TreeSet<Partition>();
        Point start = new Point( 1, 2, 3 );
        Vector3 dir = new Vector3( 0, 1, 0 );
        Ray ray = new Ray( start, dir );
        BitSet solidBits = new BitSet();
        BitSet regionBits = new BitSet();
        RayData rayData = new RayData( start, 0.0, solidBits, regionBits, new Tolerance(0.005, 0.995), ray);
        double dist = 1.0;
        Point hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        Vector3 norm = Vector3.negate(dir);
        Hit inHit = new Hit(dist, hitPoint, norm, 1, rayData);
        dist = 2.0;
        hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        norm = new Vector3(dir);
        Hit outHit = new Hit(dist, hitPoint, norm, 1, rayData);

        Segment seg = new Segment(inHit, outHit);
        Partition part = new Partition(seg, reg1.getName(), rayData);
        segs1.add(part);

        dist = 3.0;
        hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        norm = Vector3.negate(dir);
        inHit = new Hit(dist, hitPoint, norm, 1, rayData);
        dist = 4.0;
        hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        norm = new Vector3(dir);
        outHit = new Hit(dist, hitPoint, norm, 1, rayData);

        seg = new Segment(inHit, outHit);
        part = new Partition(seg, reg2.getName(), rayData);
        segs1.add(part);

        dist = 1.25;
        hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        norm = Vector3.negate(dir);
        inHit = new Hit(dist, hitPoint, norm, 1, rayData);
        dist = 1.75;
        hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        norm = new Vector3(dir);
        outHit = new Hit(dist, hitPoint, norm, 1, rayData);

        seg = new Segment(inHit, outHit);
        part = new Partition(seg, reg3.getName(), rayData);
        segs2.add(part);

        dist = 2.5;
        hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        norm = Vector3.negate(dir);
        inHit = new Hit(dist, hitPoint, norm, 1, rayData);
        dist = 3.5;
        hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        norm = new Vector3(dir);
        outHit = new Hit(dist, hitPoint, norm, 1, rayData);

        seg = new Segment(inHit, outHit);
        part = new Partition(seg, reg4.getName(), rayData);
        segs2.add(part);

        SortedSet<Partition> result = Partition.subtract(null, null);
        assertNull("Result should be null", result);

        result = Partition.subtract(segs1, null);
        assertNotNull("Result should not be null", result);
        assertTrue("Result should equal input", this.partitionListsAreEqual(result, segs1));

        result = Partition.subtract(null, segs1);
        assertNull("Result should be null", result);

        result = Partition.subtract(segs1, segs2);
        assertNotNull("Result should not be null", result);
        assertTrue( "Result should have 3 Segments", result.size() == 3);
        Iterator<Partition> iter = result.iterator();
        part = iter.next();
        assertTrue( "First Segment should start at 1.0, but was " + part.getInHit().getHit_dist()
                , part.getInHit().getHit_dist() == 1.0);
        assertTrue( "First Segment should end at 1.25, but was " + part.getOutHit().getHit_dist(),
                part.getOutHit().getHit_dist() == 1.25);
        part = iter.next();
        assertTrue( "Second Segment should start at 1.75, but was " + part.getInHit().getHit_dist(),
                part.getInHit().getHit_dist() == 1.75);
        assertTrue( "Second Segment should end at 2.0, but was " + part.getOutHit().getHit_dist(),
                part.getOutHit().getHit_dist() == 2.0);
        part = iter.next();
        assertTrue( "Third Segment should start at 3.5, but was " + part.getInHit().getHit_dist(),
                part.getInHit().getHit_dist() == 3.5);
        assertTrue( "Third Segment should end at 4.0, but was " + part.getOutHit().getHit_dist(),
                part.getOutHit().getHit_dist() == 4.0);
    }

    @Test
    public void testSubtract() {
        // make some Partitions to subtract
        PreppedCombination reg1 = new MockPreppedCombination();
        PreppedCombination reg2 = new MockPreppedCombination();
        PreppedCombination reg3 = new MockPreppedCombination();
        PreppedCombination reg4 = new MockPreppedCombination();
        TreeSet<Partition> segs1 = new TreeSet<Partition>();
        TreeSet<Partition> segs2 = new TreeSet<Partition>();
        Point start = new Point( 1, 2, 3 );
        Vector3 dir = new Vector3( 0, 1, 0 );
        Ray ray = new Ray( start, dir );
        BitSet solidBits = new BitSet();
        BitSet regionBits = new BitSet();
        RayData rayData = new RayData( start, 0.0, solidBits, regionBits, new Tolerance(0.005, 0.995), ray);
        double dist = 1.0;
        Point hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        Vector3 norm = Vector3.negate(dir);
        Hit inHit = new Hit(dist, hitPoint, norm, 1, rayData);
        dist = 2.0;
        hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        norm = new Vector3(dir);
        Hit outHit = new Hit(dist, hitPoint, norm, 1, rayData);

        Segment seg = new Segment(inHit, outHit);
        Partition part = new Partition(seg, reg1.getName(), rayData);
        segs1.add(part);

        dist = 3.0;
        hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        norm = Vector3.negate(dir);
        inHit = new Hit(dist, hitPoint, norm, 1, rayData);
        dist = 4.0;
        hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        norm = new Vector3(dir);
        outHit = new Hit(dist, hitPoint, norm, 1, rayData);

        seg = new Segment(inHit, outHit);
        part = new Partition(seg, reg2.getName(), rayData);
        segs1.add(part);

        dist = 1.25;
        hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        norm = Vector3.negate(dir);
        inHit = new Hit(dist, hitPoint, norm, 1, rayData);
        dist = 1.75;
        hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        norm = new Vector3(dir);
        outHit = new Hit(dist, hitPoint, norm, 1, rayData);

        seg = new Segment(inHit, outHit);
        part = new Partition(seg, reg3.getName(), rayData);
        segs2.add(part);

        dist = 2.5;
        hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        norm = Vector3.negate(dir);
        inHit = new Hit(dist, hitPoint, norm, 1, rayData);
        dist = 3.5;
        hitPoint = new Point(start);
        hitPoint.join(dist, dir);
        norm = new Vector3(dir);
        outHit = new Hit(dist, hitPoint, norm, 1, rayData);

        seg = new Segment(inHit, outHit);
        part = new Partition(seg, reg4.getName(), rayData);
        segs2.add(part);

        SortedSet<Partition> result = Partition.union(null, null);
        assertNull("Result should be null", result);

        result = Partition.union(segs1, null);
        assertNotNull("Result should not be null", result);
        assertTrue("Result should equal input", this.partitionListsAreEqual(result, segs1));

        result = Partition.union(null, segs1);
        assertNotNull("Result should not be null", result);
        assertTrue("Result should equal input", this.partitionListsAreEqual(result, segs1));

        result = Partition.union(segs1, segs2);
        assertNotNull("Result should not be null", result);
        assertTrue( "Result should have 2 Segments", result.size() == 2);
        assertTrue( "First Segment should start at 1.0, but was " + result.first().getInHit().getHit_dist()
                , result.first().getInHit().getHit_dist() == 1.0);
        assertTrue( "First Segment should end at 2.0, but was " + result.first().getOutHit().getHit_dist(),
                result.first().getOutHit().getHit_dist() == 2.0);
        assertTrue( "Second Segment should start at 2.5, but was " + result.last().getInHit().getHit_dist(),
                result.last().getInHit().getHit_dist() == 2.5);
        assertTrue( "Second Segment should end at 4.0, but was " + result.last().getOutHit().getHit_dist(),
                result.last().getOutHit().getHit_dist() == 4.0);
    }

    private boolean partitionListsAreEqual( SortedSet<Partition> parts1, SortedSet<Partition> parts2 ) {
        if( parts1 == null && parts2 == null ) {
            return true;
        }

        if( parts1 == null ) {
            return false;
        }

        if( parts2 == null ) {
            return false;
        }

        if( parts1.size() != parts2.size() ) {
            return false;
        }

        Iterator<Partition> iter1 = parts1.iterator();
        Iterator<Partition> iter2 = parts2.iterator();

        while( iter1.hasNext() && iter2.hasNext() ) {
            Partition part1 = iter1.next();
            Partition part2 = iter2.next();
            if( !part1.equals(part2) ) {
                return false;
            }
        }

        return true;
    }

    private class MockPreppedCombination extends PreppedCombination {

    }
}
