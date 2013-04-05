package org.brlcad.numerics;

import java.util.logging.Logger;
import org.jscience.physics.amount.Amount;
import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * Test framework for the Vector3 class
 *
 * @author Erik Greenwald
 *         <p/>
 *         $Header: /cvs/MUVES3/muves/modules/support/test/mil.army.muves/math/Vector3Test.java,v 1.7.6.1 2008/08/13 15:01:27 jra Exp $
 */

public class Vector3Test {

    double tolerance = 0.0000001;
    double mag123 = 3.74165738677394138558;
    private Logger logger;

    public Vector3Test() {
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    /* TODO: only tests phi really... */
    @Test
    public void testInitAngle() {
        Vector3 v;

        v = Vector3.fromThetaAndPhi(java.lang.Math.PI / 2.0,
                0 * java.lang.Math.PI / 4.0);
        assertEquals(true, v.isEqualDir(new Vector3(1, 0, 0)));

        v = Vector3.fromThetaAndPhi(java.lang.Math.PI / 2.0,
                1 * java.lang.Math.PI / 4.0);
        Vector3 expectedDir = new Vector3(1, 1, 0);
        expectedDir.normalize();
        assertEquals("Direction should be " + expectedDir + ", but was " + v.toString(), true, v.isEqualDir(new Vector3(1, 1, 0)));

        v = Vector3.fromThetaAndPhi(java.lang.Math.PI / 2.0,
                2 * java.lang.Math.PI / 4.0);
        assertEquals(true, v.isEqualDir(new Vector3(0, 1, 0)));

        v = Vector3.fromThetaAndPhi(java.lang.Math.PI / 2.0,
                3 * java.lang.Math.PI / 4.0);
        assertEquals(true, v.isEqualDir(new Vector3(-1, 1, 0)));

        v = Vector3.fromThetaAndPhi(java.lang.Math.PI / 2.0,
                4 * java.lang.Math.PI / 4.0);
        assertEquals(true, v.isEqualDir(new Vector3(-1, 0, 0)));

        v = Vector3.fromThetaAndPhi(java.lang.Math.PI / 2.0,
                5 * java.lang.Math.PI / 4.0);
        assertEquals(true, v.isEqualDir(new Vector3(-1, -1, 0)));

        v = Vector3.fromThetaAndPhi(java.lang.Math.PI / 2.0,
                6 * java.lang.Math.PI / 4.0);
        assertEquals(true, v.isEqualDir(new Vector3(0, -1, 0)));

        v = Vector3.fromThetaAndPhi(java.lang.Math.PI / 2.0,
                7 * java.lang.Math.PI / 4.0);
        assertEquals(true, v.isEqualDir(new Vector3(1, -1, 0)));

        v = Vector3.fromAzimuthAndElevation((Amount<Angle>) (Amount.valueOf(90.0, NonSI.DEGREE_ANGLE)),
                (Amount<Angle>) (Amount.valueOf(45.0, NonSI.DEGREE_ANGLE)));
        Vector3 expected = new Vector3(0.0, -0.7071067811865475, -0.7071067811865476);
        assertTrue("constructor using az/el Should be " + expected + ", but was " + v,
                v.isEqual(expected));
        
        v = Vector3.fromAzimuthAndElevation((Amount<Angle>)Amount.valueOf(90.0, NonSI.DEGREE_ANGLE), (Amount<Angle>)Amount.valueOf(0, NonSI.DEGREE_ANGLE));
        System.out.println( "vector from 90,0 is " + v);
        System.out.println( "az,el from " + v + " is " + Vector3.getAzimuth(v).to(NonSI.DEGREE_ANGLE) + "," + Vector3.getElevation(v).to(NonSI.DEGREE_ANGLE));
    }

    @Test
    public void testTolerance() {
        Vector3 v = new Vector3();
        double t;

        t = v.getTolerance();
        if (t > 0.0001) {
            fail("Default tolerance is too darn high.");
        }
        v.setTolerance(1.2345);
        if ((1.2345 - v.getTolerance()) > tolerance) {
            fail("Unable to set tolerance; expected 1.2345 but got " +
                    v.getTolerance());
        }
    }

    @Test
    public void testAngleBetween() {
        Vector3 v1 = new Vector3(1, 0, 0);
        Vector3 v2 = new Vector3(0, 1, 0);
        double r = v1.angleBetween(v2);

        assertEquals(java.lang.Math.PI / 2.0, r, tolerance);
        assertEquals(java.lang.Math.PI / 2.0, Vector3.angleBetween(v1, v2),
                tolerance);
    }

    @Test
    public void testCloneConstruct() {
        Vector3 v = new Vector3(1, 2, 3);
        Vector3 nv = new Vector3(v);

        assertEquals("(1.0, 2.0, 3.0)", nv.toString());
        assertEquals(mag123, nv.magnitude(), tolerance);
    }

    @Test
    public void testCosOfAngleBetween() {
        Vector3 v1 = new Vector3(1, 0, 0);
        Vector3 v2 = new Vector3(0, 1, 0);

        assertEquals(java.lang.Math.cos(v1.angleBetween(v2)),
                Vector3.cosOfAngleBetween(v1, v2), tolerance);
    }

    @Test
    public void testCross() {
        Vector3 v = new Vector3(1, 0, 0);
        Vector3 v2 = new Vector3(0, 1, 0);
        Vector3 vr = new Vector3(0, 0, 1);

        assertEquals(vr.isEqual(v.crossProduct(v2)), true);
    }

    @Test
    public void testDot() {
        Vector3 v = new Vector3(1, 2, 3);
        Vector3 v2 = new Vector3(1, 2, 3);
        double d;

        assertEquals(14.0, v.dotProduct(v2), tolerance);
        d = Vector3.dotProduct(new Vector3(3, 1, 1),
                new Vector3(-2, 1, 1));
        assertEquals(-4.0, d, tolerance);
    }

    @Test
    public void testEq() {
        Vector3 v1 = new Vector3(1, 2, 3);
        Vector3 v2 = new Vector3(1, 2, 3);
        Vector3 v3 = new Vector3(2, 4, 6);
        Vector3 v4 = new Vector3(1, 4, 6);

        assertEquals(true, v1.isEqual(v2));
        assertEquals(true, Vector3.isEqual(v1, v2));
        assertEquals(false, v1.isEqual(v3));
        assertEquals(false, Vector3.isEqual(v1, v3));
        assertEquals(true, v1.isEqualDir(v2));
        assertEquals(true, Vector3.isEqualDir(v1, v2));
        assertEquals(true, v1.isEqualDir(v3));
        assertEquals(true, Vector3.isEqualDir(v1, v3));
        assertEquals(false, v1.isEqualDir(v4));
        assertEquals(false, Vector3.isEqualDir(v1, v4));
    }

    @Test
    public void testGetCosTheta() {
        Vector3 v = new Vector3(1, 1, 1);

        assertEquals(1.0 / java.lang.Math.sqrt(3), v.getCosTheta(),
                tolerance);
    }

    @Test
    public void testGetPhi() {
        Vector3 v;

        v = new Vector3(0, 0, 1);
        assertEquals(0.0, v.getPhi(), tolerance);
        assertEquals(0.0, Vector3.getPhi(v), tolerance);

        v = new Vector3(1, 0, 0);
        assertEquals(0 * java.lang.Math.PI / 4.0, v.getPhi(), tolerance);
        v = new Vector3(1, 1, 0);
        assertEquals(1 * java.lang.Math.PI / 4.0, v.getPhi(), tolerance);
        v = new Vector3(0, 1, 0);
        assertEquals(2 * java.lang.Math.PI / 4.0, v.getPhi(), tolerance);
        v = new Vector3(-1, 1, 0);
        assertEquals(3 * java.lang.Math.PI / 4.0, v.getPhi(), tolerance);
        v = new Vector3(-1, 0, 0);
        assertEquals(4 * java.lang.Math.PI / 4.0, v.getPhi(), tolerance);
        v = new Vector3(-1, -1, 0);
        assertEquals(5 * java.lang.Math.PI / 4.0, v.getPhi(), tolerance);
        v = new Vector3(0, -1, 0);
        assertEquals(6 * java.lang.Math.PI / 4.0, v.getPhi(), tolerance);
        v = new Vector3(1, -1, 0);
        assertEquals(7 * java.lang.Math.PI / 4.0, v.getPhi(), tolerance);

        assertEquals(0 * java.lang.Math.PI / 4.0,
                Vector3.getPhi(new Vector3(1, 0, 0)), tolerance);
        assertEquals(1 * java.lang.Math.PI / 4.0,
                Vector3.getPhi(new Vector3(1, 1, 0)), tolerance);
        assertEquals(2 * java.lang.Math.PI / 4.0,
                Vector3.getPhi(new Vector3(0, 1, 0)), tolerance);
        assertEquals(3 * java.lang.Math.PI / 4.0,
                Vector3.getPhi(new Vector3(-1, 1, 0)), tolerance);
        assertEquals(4 * java.lang.Math.PI / 4.0,
                Vector3.getPhi(new Vector3(-1, 0, 0)), tolerance);
        assertEquals(5 * java.lang.Math.PI / 4.0,
                Vector3.getPhi(new Vector3(-1, -1, 0)), tolerance);
        assertEquals(6 * java.lang.Math.PI / 4.0,
                Vector3.getPhi(new Vector3(0, -1, 0)), tolerance);
        assertEquals(7 * java.lang.Math.PI / 4.0,
                Vector3.getPhi(new Vector3(1, -1, 0)), tolerance);
    }

    @Test
    public void testGetTheta() {
        Vector3 v = new Vector3(0, 0, 1);

        assertEquals(0 * java.lang.Math.PI / 4.0, v.getTheta(), tolerance);
        v = new Vector3(0, 1, 1);
        assertEquals(1 * java.lang.Math.PI / 4.0, v.getTheta(), tolerance);
        v = new Vector3(0, 1, 0);
        assertEquals(2 * java.lang.Math.PI / 4.0, v.getTheta(), tolerance);
        v = new Vector3(0, 1, -1);
        assertEquals(3 * java.lang.Math.PI / 4.0, v.getTheta(), tolerance);
        v = new Vector3(0, 0, -1);
        assertEquals(4 * java.lang.Math.PI / 4.0, v.getTheta(), tolerance);
        v = new Vector3(0, -1, -1);
        assertEquals(3 * java.lang.Math.PI / 4.0, v.getTheta(), tolerance);
        v = new Vector3(0, -1, 0);
        assertEquals(2 * java.lang.Math.PI / 4.0, v.getTheta(), tolerance);
        v = new Vector3(0, -1, 1);
        assertEquals(1 * java.lang.Math.PI / 4.0, v.getTheta(), tolerance);

        assertEquals(0 * java.lang.Math.PI / 4.0,
                Vector3.getTheta(new Vector3(0, 0, 1)), tolerance);
        assertEquals(1 * java.lang.Math.PI / 4.0,
                Vector3.getTheta(new Vector3(1, 0, 1)), tolerance);
        assertEquals(2 * java.lang.Math.PI / 4.0,
                Vector3.getTheta(new Vector3(1, 0, 0)), tolerance);
        assertEquals(3 * java.lang.Math.PI / 4.0,
                Vector3.getTheta(new Vector3(1, 0, -1)), tolerance);
        assertEquals(4 * java.lang.Math.PI / 4.0,
                Vector3.getTheta(new Vector3(0, 0, -1)), tolerance);
        assertEquals(3 * java.lang.Math.PI / 4.0,
                Vector3.getTheta(new Vector3(-1, 0, -1)), tolerance);
        assertEquals(2 * java.lang.Math.PI / 4.0,
                Vector3.getTheta(new Vector3(-1, 0, 0)), tolerance);
        assertEquals(1 * java.lang.Math.PI / 4.0,
                Vector3.getTheta(new Vector3(-1, 0, 1)), tolerance);
    }

    @Test
    public void testGetters() {
        Vector3 v = new Vector3(1, 2, 3);

        assertEquals(1.0, v.getX(), tolerance);
        assertEquals(2.0, v.getY(), tolerance);
        assertEquals(3.0, v.getZ(), tolerance);
        assertEquals("(1.0, 2.0, 3.0)", v.toString());
        assertEquals(mag123, v.magnitude(), tolerance);
    }

    @Test
    public void testIsNonZero() {
        Vector3 v = new Vector3(0, 0, 0);

        assertEquals(false, v.isNonZero());
        v = new Vector3(1, 1, 0);
        assertEquals(true, v.isNonZero());
    }


    @Test
    public void testMinus() {
        Vector3 v = new Vector3(1, 2, 3);
        Vector3 res = new Vector3(0, 0, 0);

        v.minus(v);
        assertEquals(true, v.isEqual(res));
        Vector3 v2 = new Vector3(5, 4, 3);

        v = new Vector3(1, 2, 3);
        res = Vector3.minus(v2, v);
        assertEquals(true, res.isEqual(new Vector3(4, 2, 0)));
    }

    @Test
    public void testNegate() {
        Vector3 v = new Vector3(1, 2, 3);

        v.negate();
        assertEquals(true, v.isEqual(new Vector3(-1, -2, -3)));
        v = Vector3.negate(new Vector3(-2, 1, -6));
        assertEquals(true, v.isEqual(new Vector3(2, -1, 6)));
    }

    @Test
    public void testPlus() {
        Vector3 v = new Vector3(1, 2, 3);

        v.plus(v);
        assertEquals(true, v.isEqual(new Vector3(2, 4, 6)));
        v = Vector3.plus(v, new Vector3(4, 3, 2));
        assertEquals(true, v.isEqual(new Vector3(6, 7, 8)));
    }

    @Test
    public void testString() {
        Vector3 v = new Vector3(1, 2, 3);

        assertEquals("(1.0, 2.0, 3.0)", v.toString());
        assertEquals(mag123, v.magnitude(), tolerance);
    }

    @Test
    public void testThetaPhi00() {
        Vector3 v = Vector3.fromThetaAndPhi(0, 0);

        assertEquals("(0.0, 0.0, 1.0)", v.toString());
        assertEquals(1.0, v.magnitude(), tolerance);
    }

    @Test
    public void testUpdate() {
        Vector3 v = new Vector3(0, 1, 0);

        v.update(9, 8, 7);
        assertEquals(true, v.isEqual(new Vector3(9, 8, 7)));
        v.update(new Vector3(3, 4, 5));
        assertEquals(true, v.isEqual(new Vector3(3, 4, 5)));
    }

    @Test
    public void testVectorBetween() {
        double v = java.lang.Math.sin(java.lang.Math.PI / 4.0);
        Vector3 v1 = new Vector3(1, 0, 0);
        Vector3 v2 = new Vector3(0, 1, 0);
        Vector3 vr = v1.vectorBetween(v2);

        assertEquals(true, vr.isEqual(new Vector3(v, v, 0)));
        vr = Vector3.vectorBetween(v1, v2);
        assertEquals(true, vr.isEqual(new Vector3(v, v, 0)));
    }

    @Test
    public void testRotate() {
        Vector3 v, u;

        v = new Vector3(1, 0, 0);
        u = new Vector3(0, 1, 0);
        v.rotate(u, 90);
        assertEquals(true, v.isEqual(new Vector3(0, 0, -1)));
    }

    @Test
    public void testOrtho() {
        Vector3 orig;
        Vector3 ortho;

        orig = new Vector3(123.0, 0.0, 0.0);
        ortho = Vector3.orthogonalVector(orig);
        if (Math.abs(Vector3.dotProduct(orig, ortho)) > 0.000001) {
            fail("Failed to create vector orthogonal to " + orig);
        }

        orig = new Vector3(0.0, 0.123, 0.0);
        ortho = Vector3.orthogonalVector(orig);
        if (Math.abs(Vector3.dotProduct(orig, ortho)) > 0.000001) {
            fail("Failed to create vector orthogonal to " + orig);
        }

        orig = new Vector3(0.0, 0.0, -0.0002);
        ortho = Vector3.orthogonalVector(orig);
        if (Math.abs(Vector3.dotProduct(orig, ortho)) > 0.000001) {
            fail("Failed to create vector orthogonal to " + orig);
        }

        orig = new Vector3(123.0, -4656.0, 789.0);
        ortho = Vector3.orthogonalVector(orig);
        if (Math.abs(Vector3.dotProduct(orig, ortho)) > 0.000001) {
            fail("Failed to create vector orthogonal to " + orig + ", ortho = " + ortho + ", dot = " + Vector3.dotProduct(orig, ortho));
        }

        orig = new Vector3(0.0, 0.0, 0.0);
        ortho = Vector3.orthogonalVector(orig);
        if (!ortho.isEqual(new Vector3(0.0, 0.0, 0.0))) {
            fail("Failed to create zero length vector");
        }
    }
}
