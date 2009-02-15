/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.preppedGeometry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import org.brlcad.geometry.BrlcadDb;
import org.brlcad.geometry.DbException;
import org.brlcad.geometry.DbExternal;
import org.brlcad.geometry.Hit;
import org.brlcad.geometry.Segment;
import org.brlcad.geometry.Tgc;
import org.brlcad.numerics.BoundingBox;
import org.brlcad.numerics.Matrix;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Ray;
import org.brlcad.numerics.Tolerance;
import org.brlcad.numerics.Vector3;
import org.brlcad.spacePartition.PreppedDb;
import org.brlcad.spacePartition.RayData;
import org.jscience.physics.amount.Amount;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jra
 */
public class PreppedTgcTest {
        double tol = 0.005;

    public PreppedTgcTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testPrep() {
        try {
            Point v = new Point(0.0, 0.0, 0.0);
            Vector3 h = new Vector3(0.0, 0.0, 1000.0);
            Vector3 a = new Vector3(100.0, 0.0, 0.0);
            Vector3 b = new Vector3(0.0, 50.0, 0.0);
            Vector3 c = new Vector3(50.0, 0.0, 0.0);
            Vector3 d = new Vector3(0.0, 100.0, 0.0);
            DbExternal dbExt = new MockDbExternal("tgc", v, h, a, b, c, d);
            Tgc tgc = new Tgc(dbExt);
            MockPreppedDb preppedDb = new MockPreppedDb();
            Matrix matrix = new Matrix(4, 4);
            matrix.unit();
            PreppedCombination reg = new MockPreppedCombination();
            PreppedObject preppedTgc = tgc.prep(reg, preppedDb, matrix);
            BoundingBox expectedBB = new BoundingBox(new Point(-100, -100, 0), new Point(100, 100, 1000));
            Point expectedCenter = new Point(0, 0, 500);
            double expectedBR = expectedBB.getDiameter().magnitude() / 2.0;
            assertTrue("BoundingBox should be " + expectedBB + ", but was " +
                    preppedTgc.getBoundingBox(), expectedBB.equals(preppedTgc.getBoundingBox()));
            assertTrue("Bounding radius should be " + expectedBR + ", but was " +
                    preppedTgc.getBoundingRadius(), expectedBR == preppedTgc.getBoundingRadius());
            assertTrue("Center should be " + expectedCenter + ", but was " +
                    preppedTgc.getCenter(), expectedCenter.equals(preppedTgc.getCenter()));
        } catch (Exception ex) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            ex.printStackTrace(ps);
            ps.flush();
            fail(baos.toString());
        }
    }

    /**
     * Test of shoot method, of class PreppedRec.
     */
    @Test
    public void testShoot() {
        try {
            Point v = new Point(0.0, 0.0, 0.0);
            Vector3 h = new Vector3(0.0, 0.0, 1000.0);
            Vector3 a = new Vector3(100.0, 0.0, 0.0);
            Vector3 b = new Vector3(0.0, 50.0, 0.0);
            Vector3 c = new Vector3(50.0, 0.0, 0.0);
            Vector3 d = new Vector3(0.0, 100.0, 0.0);
            DbExternal dbExt = new MockDbExternal("tgc", v, h, a, b, c, d);
            Tgc tgc = new Tgc(dbExt);
            MockPreppedDb preppedDb = new MockPreppedDb();
            Matrix matrix = new Matrix(4, 4);
            matrix.unit();
            PreppedCombination reg = new MockPreppedCombination();
            PreppedObject preppedTgc = tgc.prep(reg, preppedDb, matrix);

            // First a shot along x-axis
            Ray ray = new Ray(new Point(-1000, 0, 500), new Vector3(1, 0, 0));
            RayData rayData = new RayData(new Point(-1000, 0, 500), 0.0,
                    new BitSet(), new BitSet(), new Tolerance(0.005, 0.995), ray);
            List<Segment> segs = preppedTgc.shoot(ray, rayData);
            assertTrue("expected 1 segment, bit got " + segs.size(), segs.size() == 1);
            Segment seg = segs.get(0);
            // Expected values based on results of rtshot
            Point expectedEntrance = new Point(-75, 0, 500);
            double expectedEntranceDist = 925;
            Vector3 expectedEntranceNorm = new Vector3(-0.998752, 0, 0.0499376);
            expectedEntranceNorm.setTolerance(tol);
            int expectedEntranceSurfno = Tgc.BODY;
            Point expectedExit = new Point(75, 0, 500);
            double expectedExitDist = 1075;
            Vector3 expectedExitNorm = new Vector3(0.998752, 0, 0.0499376);
            expectedExitNorm.setTolerance(tol);
            int expectedExitSurfno = Tgc.BODY;
            assertTrue("expected entrance hit at " + expectedEntrance + ", but got " +
                    seg.getInHit().getHit_pt(), expectedEntrance.equals(seg.getInHit().getHit_pt()));
            assertTrue("expected entrance hit distance is " + expectedEntranceDist + ", bit got " +
                    seg.getInHit().getHit_dist(), expectedEntranceDist == seg.getInHit().getHit_dist());
            assertTrue("expected entrance normal of " + expectedEntranceNorm + ", but got " +
                    seg.getInHit().getHit_normal(), expectedEntranceNorm.equals(seg.getInHit().getHit_normal()));
            assertTrue("expected entrance surface number " + expectedEntranceSurfno + ", but got " +
                    seg.getInHit().getHit_surfno(), expectedEntranceSurfno == seg.getInHit().getHit_surfno());
            assertTrue("expected exit hit at " + expectedExit + ", but got " +
                    seg.getOutHit().getHit_pt(), expectedExit.equals(seg.getOutHit().getHit_pt()));
            assertTrue("expected exit hit distance is " + expectedExitDist + ", bit got " +
                    seg.getOutHit().getHit_dist(), expectedExitDist == seg.getOutHit().getHit_dist());
            assertTrue("expected exit normal of " + expectedExitNorm + ", but got " +
                    seg.getOutHit().getHit_normal(), expectedExitNorm.equals(seg.getOutHit().getHit_normal()));
            assertTrue("expected exit surface number " + expectedExitSurfno + ", but got " +
                    seg.getOutHit().getHit_surfno(), expectedExitSurfno == seg.getOutHit().getHit_surfno());

            // now a shot along z-axis
            ray = new Ray(new Point(0, 0, 1500), new Vector3(0, 0, -1));
            rayData = new RayData(new Point(0, 0, 1500), 0.0,
                    new BitSet(), new BitSet(), new Tolerance(0.005, 0.995), ray);
            segs = preppedTgc.shoot(ray, rayData);
            assertTrue("expected 1 segment, bit got " + segs.size(), segs.size() == 1);
            seg = segs.get(0);
            // Expected values based on results of rtshot
            expectedEntrance = new Point(0, 0, 1000);
            expectedEntrance.setTolerance(tol);
            expectedEntranceDist = 500;
            expectedEntranceNorm = new Vector3(0, 0, 1);
            expectedEntranceNorm.setTolerance(tol);
            expectedEntranceSurfno = Tgc.TOP;
            expectedExit = new Point(0, 0, 0);
            expectedExit.setTolerance(tol);
            expectedExitDist = 1500;
            expectedExitNorm = new Vector3(0, 0, -1);
            expectedExitNorm.setTolerance(tol);
            expectedExitSurfno = Tgc.BOTTOM;
            assertTrue("expected entrance hit at " + expectedEntrance + ", but got " +
                    seg.getInHit().getHit_pt(), expectedEntrance.equals(seg.getInHit().getHit_pt()));
            assertTrue("expected entrance hit distance is " + expectedEntranceDist + ", bit got " +
                    seg.getInHit().getHit_dist(), expectedEntranceDist == seg.getInHit().getHit_dist());
            assertTrue("expected entrance normal of " + expectedEntranceNorm + ", but got " +
                    seg.getInHit().getHit_normal(), expectedEntranceNorm.equals(seg.getInHit().getHit_normal()));
            assertTrue("expected entrance surface number " + expectedEntranceSurfno + ", but got " +
                    seg.getInHit().getHit_surfno(), expectedEntranceSurfno == seg.getInHit().getHit_surfno());
            assertTrue("expected exit hit at " + expectedExit + ", but got " +
                    seg.getOutHit().getHit_pt(), expectedExit.equals(seg.getOutHit().getHit_pt()));
            assertTrue("expected exit hit distance is " + expectedExitDist + ", bit got " +
                    seg.getOutHit().getHit_dist(), expectedExitDist == seg.getOutHit().getHit_dist());
            assertTrue("expected exit normal of " + expectedExitNorm + ", but got " +
                    seg.getOutHit().getHit_normal(), expectedExitNorm.equals(seg.getOutHit().getHit_normal()));
            assertTrue("expected exit surface number " + expectedExitSurfno + ", but got " +
                    seg.getOutHit().getHit_surfno(), expectedExitSurfno == seg.getOutHit().getHit_surfno());

        } catch (Exception ex) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            ex.printStackTrace(ps);
            ps.flush();
            fail(baos.toString());
        }
    }
    /**
     * Test of shoot method, of class PreppedRec.
     */
    @Test
    public void testShootwMatrix() {
        try {
            Point v = new Point(0.0, 0.0, 0.0);
            double height = 1000.0;
            Vector3 h = new Vector3(0.0, 0.0, height);
            Vector3 a = new Vector3(100.0, 0.0, 0.0);
            Vector3 b = new Vector3(0.0, 50.0, 0.0);
            Vector3 c = new Vector3(50.0, 0.0, 0.0);
            Vector3 d = new Vector3(0.0, 100.0, 0.0);
            DbExternal dbExt = new MockDbExternal("tgc", v, h, a, b, c, d);
            Tgc tgc = new Tgc(dbExt);
            MockPreppedDb preppedTgc = new MockPreppedDb();
            Amount<Angle> yaw = Amount.valueOf(0, NonSI.DEGREE_ANGLE);
            Amount<Angle> pitch = Amount.valueOf(30, NonSI.DEGREE_ANGLE);
            Amount<Angle> roll = Amount.valueOf(0, NonSI.DEGREE_ANGLE);
            Point location = new Point( 1234, 3456, 4567 );
            Matrix matrix = new Matrix(yaw, pitch, roll, location);
            PreppedCombination reg = new MockPreppedCombination();
            PreppedObject preppedRec = tgc.prep(reg, preppedTgc, matrix);

            // first a shot along the x-axis
            Point rayStart = new Point(-1000.0, 3456.0, 4817.0);
            Vector3 rayDir = new Vector3(1, 0, 0);
            System.out.println( "rayStart = " + rayStart);
            Ray ray = new Ray(rayStart, rayDir);
            RayData rayData = new RayData(ray.getStart(), 0.0,
                    new BitSet(), new BitSet(), new Tolerance(0.005, 0.995), ray);
            List<Segment> segs = preppedRec.shoot(ray, rayData);
            System.out.println("Segment: " + segs.get(0));
            assertTrue("expected 1 segment, bit got " + segs.size(), segs.size() == 1);
            Segment seg = segs.get(0);

            // expected values based on results from rtshot
            Point expectedEntrance = new Point(1276.6, 3456, 4817);
            expectedEntrance.setTolerance(tol);
            double expectedEntranceDist = 2276.6;
            Vector3 expectedEntranceNorm = new Vector3(-0.839976, 0, 0.542623);
            expectedEntranceNorm.setTolerance(tol);
            int expectedEntranceSurfno = Tgc.BODY;
            Point expectedExit = new Point(1474.37, 3456, 4817);
            expectedExit.setTolerance(tol);
            double expectedExitDist = 2474.37;
            Vector3 expectedExitNorm = new Vector3(0.889914, 0, -0.456129);
            expectedExitNorm.setTolerance(tol);
            int expectedExitSurfno = Tgc.BODY;
            assertTrue("expected entrance hit at " + expectedEntrance + ", but got " +
                    seg.getInHit().getHit_pt(), expectedEntrance.equals(seg.getInHit().getHit_pt()));
            assertTrue("expected entrance hit distance is " + expectedEntranceDist + ", bit got " +
                    seg.getInHit().getHit_dist(),
                    Math.abs(expectedEntranceDist - seg.getInHit().getHit_dist()) < tol);
            assertTrue("expected entrance normal of " + expectedEntranceNorm + ", but got " +
                    seg.getInHit().getHit_normal(), expectedEntranceNorm.equals(seg.getInHit().getHit_normal()));
            assertTrue("expected entrance surface number " + expectedEntranceSurfno + ", but got " +
                    seg.getInHit().getHit_surfno(), expectedEntranceSurfno == seg.getInHit().getHit_surfno());
            assertTrue("expected exit hit at " + expectedExit + ", but got " +
                    seg.getOutHit().getHit_pt(), expectedExit.equals(seg.getOutHit().getHit_pt()));
            assertTrue("expected exit hit distance is " + expectedExitDist + ", bit got " +
                    seg.getOutHit().getHit_dist(),
                    Math.abs(expectedExitDist - seg.getOutHit().getHit_dist()) < tol);
            assertTrue("expected exit normal of " + expectedExitNorm + ", but got " +
                    seg.getOutHit().getHit_normal(), expectedExitNorm.equals(seg.getOutHit().getHit_normal()));
            assertTrue("expected exit surface number " + expectedExitSurfno + ", but got " +
                    seg.getOutHit().getHit_surfno(), expectedExitSurfno == seg.getOutHit().getHit_surfno());

            // now a shot along the rec axis
            rayStart = new Point(2234, 3456, 6299.055);
            rayDir = new Vector3(-500, 0, -866.025);
            rayDir.normalize();
            ray = new Ray(rayStart, rayDir);
            System.out.println( "Ray = " + ray);
            rayData = new RayData(ray.getStart(), 0.0,
                    new BitSet(), new BitSet(), new Tolerance(0.005, 0.995), ray);
            segs = preppedRec.shoot(ray, rayData);
            System.out.println("Segment: " + segs.get(0));
            assertTrue("expected 1 segment, bit got " + segs.size(), segs.size() == 1);
            seg = segs.get(0);

            // expected values based on results from rtshot
            expectedEntrance = new Point(1734, 3456, 5433.03);
            expectedEntrance.setTolerance(tol);
            expectedEntranceDist = 1000;
            expectedEntranceNorm = new Vector3(0.5, 0, 0.866025);
            expectedEntranceNorm.setTolerance(tol);
            expectedEntranceSurfno = Tgc.TOP;
            expectedExit = new Point(1234, 3456, 4567);
            expectedExit.setTolerance(tol);
            expectedExitDist = 2000;
            expectedExitNorm = new Vector3(-0.5, -0, -0.866025);
            expectedExitNorm.setTolerance(tol);
            expectedExitSurfno = Tgc.BOTTOM;
            assertTrue("expected entrance hit at " + expectedEntrance + ", but got " +
                    seg.getInHit().getHit_pt(), expectedEntrance.equals(seg.getInHit().getHit_pt()));
            assertTrue("expected entrance hit distance is " + expectedEntranceDist + ", bit got " +
                    seg.getInHit().getHit_dist(),
                    Math.abs(expectedEntranceDist - seg.getInHit().getHit_dist()) < tol);
            assertTrue("expected entrance normal of " + expectedEntranceNorm + ", but got " +
                    seg.getInHit().getHit_normal(), expectedEntranceNorm.equals(seg.getInHit().getHit_normal()));
            assertTrue("expected entrance surface number " + expectedEntranceSurfno + ", but got " +
                    seg.getInHit().getHit_surfno(), expectedEntranceSurfno == seg.getInHit().getHit_surfno());
            assertTrue("expected exit hit at " + expectedExit + ", but got " +
                    seg.getOutHit().getHit_pt(), expectedExit.equals(seg.getOutHit().getHit_pt()));
            assertTrue("expected exit hit distance is " + expectedExitDist + ", bit got " +
                    seg.getOutHit().getHit_dist(),
                    Math.abs(expectedExitDist - seg.getOutHit().getHit_dist()) < tol);
            assertTrue("expected exit normal of " + expectedExitNorm + ", but got " +
                    seg.getOutHit().getHit_normal(), expectedExitNorm.equals(seg.getOutHit().getHit_normal()));
            assertTrue("expected exit surface number " + expectedExitSurfno + ", but got " +
                    seg.getOutHit().getHit_surfno(), expectedExitSurfno == seg.getOutHit().getHit_surfno());
        } catch (Exception ex) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            ex.printStackTrace(ps);
            ps.flush();
            fail(baos.toString());
        }
    }

    /**
     * Test of makeSegs method, of class PreppedRec.
     */
    @Test
    public void testMakeSegs() {
        try {
            Point hit_pt;
            Vector3 hit_norm;
            int hit_surf;
            double dist;
            Hit hit;

            Point v = new Point(0.0, 0.0, 0.0);
            Vector3 h = new Vector3(0.0, 0.0, 1000.0);
            Vector3 a = new Vector3(100.0, 0.0, 0.0);
            Vector3 b = new Vector3(0.0, 50.0, 0.0);
            Vector3 c = new Vector3(50.0, 0.0, 0.0);
            Vector3 d = new Vector3(0.0, 100.0, 0.0);
            DbExternal dbExt = new MockDbExternal("tgc", v, h, a, b, c, d);
            Tgc tgc = new Tgc(dbExt);
            MockPreppedDb preppedDb = new MockPreppedDb();
            Matrix matrix = new Matrix(4, 4);
            matrix.unit();
            PreppedCombination reg = new MockPreppedCombination();
            PreppedObject preppedTgc = tgc.prep(reg, preppedDb, matrix);
            Set<Hit> hits = new HashSet<Hit>();
            Ray ray = new Ray(new Point(1, 2, 3), new Vector3(1.0, 0.0, 0.0));
            RayData rayData = new RayData(ray.getStart(), 0.0,
                    new BitSet(), new BitSet(), new Tolerance(0.005, 0.995), ray);

            hit_pt = new Point(4, 2, 3);
            hit_norm = new Vector3(1, 0, 0);
            hit_surf = Tgc.BODY;
            dist = hit_pt.dist(ray.getStart());
            hit = new Hit(dist, hit_pt, hit_norm, hit_surf, rayData);
            hits.add(hit);
            Hit expectedExitHit = hit;

            hit_pt = new Point(3, 2, 3);
            hit_norm = new Vector3(-1, 0, 0);
            hit_surf = Tgc.BODY;
            dist = hit_pt.dist(ray.getStart());
            hit = new Hit(dist, hit_pt, hit_norm, hit_surf, rayData);
            hits.add(hit);
            Hit expectedEntranceHit = hit;

            hit_pt = new Point(3, 2, 3);
            hit_norm = new Vector3(-1, 0, 0);
            hit_surf = Tgc.BODY;
            dist = hit_pt.dist(ray.getStart());
            hit = new Hit(dist, hit_pt, hit_norm, hit_surf, rayData);
            hits.add(hit);

            hit_pt = new Point(5, 2, 3);
            hit_norm = new Vector3(1, 0, 0);
            hit_surf = Tgc.BODY;
            dist = hit_pt.dist(ray.getStart());
            hit = new Hit(dist, hit_pt, hit_norm, hit_surf, rayData);
            hits.add(hit);

            List<Segment> segs = preppedTgc.makeSegs(hits, ray, rayData);
            assertTrue( "expected 1 segment, but got " + segs.size(), segs.size() == 1);
            assertTrue( "expected entrance hit: " + expectedEntranceHit + ", but got " +
                    segs.get(0).getInHit(), expectedEntranceHit.equals(segs.get(0).getInHit()));
            assertTrue( "expected exit hit: " + expectedExitHit + ", but got " +
                    segs.get(0).getOutHit(), expectedExitHit.equals(segs.get(0).getOutHit()));
        } catch (Exception ex) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            ex.printStackTrace(ps);
            ps.flush();
            fail(baos.toString());
        }
    }

    private static class MockDbExternal implements DbExternal {

        private String name;
        private byte[] body;
        private ByteBuffer byteBuffer;

        public MockDbExternal(String name, Point v, Vector3 h, Vector3 a, Vector3 b, Vector3 c, Vector3 d) {
            this.name = name;
            this.body = new byte[18 * 8];
            this.byteBuffer = ByteBuffer.wrap(body);
            for (int i = 0; i < 3; i++) {
                this.byteBuffer.putDouble(v.get(i));
            }
            for (int i = 0; i < 3; i++) {
                this.byteBuffer.putDouble(h.get(i));
            }
            for (int i = 0; i < 3; i++) {
                this.byteBuffer.putDouble(a.get(i));
            }
            for (int i = 0; i < 3; i++) {
                this.byteBuffer.putDouble(b.get(i));
            }
            for (int i = 0; i < 3; i++) {
                this.byteBuffer.putDouble(c.get(i));
            }
            for (int i = 0; i < 3; i++) {
                this.byteBuffer.putDouble(d.get(i));
            }
        }

        public String getName() {
            return name;
        }

        public byte getMajorType() {
            return Tgc.majorType;
        }

        public byte getMinorType() {
            return Tgc.minorType;
        }

        public byte[] getBody() {
            return this.body;
        }

        public byte[] getAttributes() {
            return null;
        }
    }

    private static class MockDb extends BrlcadDb {

        @Override
        protected void scan() throws IOException, DbException {
        }
    }

    private static class MockPreppedDb extends PreppedDb {

        @Override
        public void addPreppedObjectToInitialBox(PreppedObject preppedObject) {
        }
    }

    private static class MockPreppedCombination extends PreppedCombination {
    }
}