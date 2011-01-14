package org.brlcad.numerics;

import org.jscience.physics.amount.Amount;
import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test framework for the Matrix class
 *
 * @author Erik Greenwald
 *         <p/>
 *         $Header: /cvs/MUVES3/muves/modules/support/test/mil.army.muves/math/MatrixTest.java,v 1.5 2007/10/17 17:44:33 jra Exp $
 */

public class MatrixTest {

    public MatrixTest() {
    }

    @Test
    public void testCreate() {
        Matrix m1 = new Matrix(4, 4);

        assertEquals
                ("[ 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 ]",
                        m1.toString());
    }

    @Test
    public void testCreatep() {
        Point v1 = new Point(1.0, 2.0, 3.0);

        Matrix m1 = new Matrix(v1);

        assertEquals("[ 1.0 2.0 3.0 1.0 ]", m1.toString());
    }

    @Test
    public void testUnit() {
        Matrix m2 = new Matrix(4, 4);

        m2.unit();
        assertEquals
                ("[ 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 ]",
                        m2.toString());
    }

    @Test
    public void testAdd() {
        Matrix m1 = new Matrix(4, 4);
        Matrix m2 = new Matrix(4, 4);

        m2.unit();
        m1.add(m2);

        assertEquals
                ("[ 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 ]",
                        m1.toString());
    }

    @Test
    public void testSub() {
        Matrix m1 = new Matrix(4, 4);
        Matrix m2 = new Matrix(4, 4);

        m2.unit();
        m1.sub(m2);

        assertEquals
                ("[ -1.0 0.0 0.0 0.0 0.0 -1.0 0.0 0.0 0.0 0.0 -1.0 0.0 0.0 0.0 0.0 -1.0 ]",
                        m1.toString());
    }

    @Test
    public void testMultm() {
        Matrix m1 = new Matrix(4, 4);
        Matrix m2 = new Matrix(4, 4);

        m1.unit();
        m2.unit();
        m1.mult(m2);
        assertEquals
                ("[ 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 ]",
                        m1.toString());
        m2.mult(m1);
        assertEquals
                ("[ 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 ]",
                        m1.toString());
        
        Matrix m3 = new Matrix(4, 2);
        m3.unit();
        m1.mult(m3);
        assertEquals( "result matrix should have 4 rows", 4, m1.getRows());
        assertEquals( "result matrix should have 2 columns", 2, m1.getColumns());
        
        Matrix m4 = new Matrix( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
        Matrix m5 = new Matrix( 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26);
        m4.mult(m5);
        assertEquals
                ("[ 190.0 200.0 210.0 220.0 462.0 488.0 514.0 540.0 734.0 776.0 818.0 860.0 1006.0 1064.0 1122.0 1180.0 ]",
                        m4.toString());
    }


    @Test
    public void testMultp() {
        Point p1 = new Point(1.0, 2.0, 3.0);
        //Point p2 = new Point(2, 2, 3);

        Matrix m1 = new Matrix(2, 0, 0, 0,
                               0, 1, 0, 0,
                               0, 0, 1, 0,
                               0, 0, 0, 1);
        m1.mult(p1);
        assertEquals("(2.0, 2.0, 3.0)", p1.toString());
    }

    @Test
    public void testPrettyString() {
        Matrix m1 = new Matrix(4, 4);

        assertEquals
                ("[ 0.0  0.0  0.0  0.0 ]\n[ 0.0  0.0  0.0  0.0 ]\n[ 0.0  0.0  0.0  0.0 ]\n[ 0.0  0.0  0.0  0.0 ]\n",
                        m1.toPrettyString());

        m1.unit();
        assertEquals
                ("[ 1.0  0.0  0.0  0.0 ]\n[ 0.0  1.0  0.0  0.0 ]\n[ 0.0  0.0  1.0  0.0 ]\n[ 0.0  0.0  0.0  1.0 ]\n",
                        m1.toPrettyString());

        m1 = new Matrix(1, 0, 0, 0,
                        0, 1, 0, 0,
                        0, 0, 1, 0,
                        0, 0, 3, 1);
        assertEquals
                ("[ 1.0  0.0  0.0  0.0 ]\n[ 0.0  1.0  0.0  0.0 ]\n[ 0.0  0.0  1.0  0.0 ]\n[ 0.0  0.0  3.0  1.0 ]\n",
                        m1.toPrettyString());
    }

    @Test
    public void testRollYawPitch() {
        Matrix m = new Matrix(Amount.valueOf(0, Angle.UNIT), Amount.valueOf(0, Angle.UNIT), Amount.valueOf(0, Angle.UNIT), new Point(0.0, 0.0, 0.0));
        Matrix unit = new Matrix(4, 4);
        unit.unit();
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                assertEquals("row " + row + " col " + col + " not equal",
                        unit.get(row, col), m.get(row, col), 0.00001);
            }
        }

        m = new Matrix((Amount<Angle>) Amount.valueOf(Math.PI / 6.0, SI.RADIAN),
                Amount.valueOf(0, Angle.UNIT), Amount.valueOf(0, Angle.UNIT), new Point(0.0, 0.0, 0.0));

        Vector3 v = new Vector3(1.0, 0.0, 0.0);
        m.mult(v);
        assertTrue("rotated X-dir in yaw failed", v.isEqual(new Vector3(0.8660254037844387, 0.5, 0.0)));
        v = new Vector3(0.0, 1.0, 0.0);
        m.mult(v);
        assertTrue("rotated Y-dir in yaw failed", v.isEqual(new Vector3(-0.5, 0.8660254037844387, 0.0)));
        v = new Vector3(0.0, 0.0, 1.0);
        m.mult(v);
        assertTrue("rotated Z-dir in yaw failed", v.isEqual(new Vector3(0.0, 0.0, 1.0)));

        m = new Matrix(Amount.valueOf(0, Angle.UNIT), (Amount<Angle>) Amount.valueOf(Math.PI / 6.0, SI.RADIAN),
                Amount.valueOf(0, Angle.UNIT), new Point(0.0, 0.0, 0.0));

        v = new Vector3(1.0, 0.0, 0.0);
        m.mult(v);
        assertTrue("rotated X-dir in pitch failed", v.isEqual(new Vector3(0.8660254037844387, 0.0, -0.5)));
        v = new Vector3(0.0, 1.0, 0.0);
        m.mult(v);
        assertTrue("rotated Y-dir in pitch failed", v.isEqual(new Vector3(0.0, 1.0, 0.0)));
        v = new Vector3(0.0, 0.0, 1.0);
        m.mult(v);
        assertTrue("rotated Z-dir in pitch failed", v.isEqual(new Vector3(0.5, 0.0, 0.8660254037844387)));

        m = new Matrix(Amount.valueOf(0, Angle.UNIT), Amount.valueOf(0, Angle.UNIT), (Amount<Angle>) Amount.valueOf(Math.PI / 6.0, SI.RADIAN),
                new Point(0.0, 0.0, 0.0));

        v = new Vector3(1.0, 0.0, 0.0);
        m.mult(v);
        assertTrue("rotated X-dir in roll failed", v.isEqual(new Vector3(1.0, 0.0, 0.0)));
        v = new Vector3(0.0, 1.0, 0.0);
        m.mult(v);
        assertTrue("rotated Y-dir in roll failed", v.isEqual(new Vector3(0.0, 0.8660254037844387, 0.5)));
        v = new Vector3(0.0, 0.0, 1.0);
        m.mult(v);
        assertTrue("rotated Z-dir in roll failed", v.isEqual(new Vector3(0.0, -0.5, 0.8660254037844387)));

        Quaternion q = new Quaternion(Amount.valueOf(0, Angle.UNIT), Amount.valueOf(0, Angle.UNIT), Amount.valueOf(0, Angle.UNIT));
        m = new Matrix(q, new Vector3(0.0, 0.0, 0.0));
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                assertEquals("row " + row + " col " + col + " not equal",
                        unit.get(row, col), m.get(row, col), 0.00001);
            }
        }

        q = new Quaternion((Amount<Angle>) Amount.valueOf(Math.PI / 6.0, SI.RADIAN), Amount.valueOf(0, Angle.UNIT), Amount.valueOf(0, Angle.UNIT));
        m = new Matrix(q, new Vector3(0.0, 0.0, 0.0));
        Matrix m1 = new Matrix((Amount<Angle>) Amount.valueOf(Math.PI / 6.0, SI.RADIAN),
                Amount.valueOf(0, Angle.UNIT), Amount.valueOf(0, Angle.UNIT), new Point(0.0, 0.0, 0.0));
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                assertEquals("row " + row + " col " + col + " not equal",
                        m1.get(row, col), m.get(row, col), 0.00001);
            }
        }

        q = new Quaternion(Amount.valueOf(0, Angle.UNIT), (Amount<Angle>) Amount.valueOf(Math.PI / 6.0, SI.RADIAN), Amount.valueOf(0, Angle.UNIT));
        m = new Matrix(q, new Vector3(0.0, 0.0, 0.0));
        m1 = new Matrix(Amount.valueOf(0, Angle.UNIT), (Amount<Angle>) Amount.valueOf(Math.PI / 6.0, SI.RADIAN),
                Amount.valueOf(0, Angle.UNIT), new Point(0.0, 0.0, 0.0));
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                assertEquals("row " + row + " col " + col + " not equal",
                        m1.get(row, col), m.get(row, col), 0.00001);
            }
        }

        q = new Quaternion(Amount.valueOf(0, Angle.UNIT), Amount.valueOf(0, Angle.UNIT), (Amount<Angle>) Amount.valueOf(Math.PI / 6.0, SI.RADIAN));
        m = new Matrix(q, new Vector3(0.0, 0.0, 0.0));
        m1 = new Matrix(Amount.valueOf(0, Angle.UNIT), Amount.valueOf(0, Angle.UNIT), (Amount<Angle>) Amount.valueOf(Math.PI / 6.0, SI.RADIAN),
                new Point(0.0, 0.0, 0.0));
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                assertEquals("row " + row + " col " + col + " not equal",
                        m1.get(row, col), m.get(row, col), 0.00001);
            }
        }

        q = new Quaternion((Amount<Angle>) Amount.valueOf(10.0, NonSI.DEGREE_ANGLE),
                (Amount<Angle>) Amount.valueOf(20.0, NonSI.DEGREE_ANGLE),
                (Amount<Angle>) Amount.valueOf(30.0, NonSI.DEGREE_ANGLE));
        m = new Matrix(q, new Vector3(12.3, 45.6, 78.9));
        m1 = new Matrix((Amount<Angle>) Amount.valueOf(10.0, NonSI.DEGREE_ANGLE),
                (Amount<Angle>) Amount.valueOf(20.0, NonSI.DEGREE_ANGLE),
                (Amount<Angle>) Amount.valueOf(30.0, NonSI.DEGREE_ANGLE),
                new Point(12.3, 45.6, 78.9));
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                assertEquals("row " + row + " col " + col + " not equal",
                        m1.get(row, col), m.get(row, col), 0.00001);
            }
        }

        Matrix ma = new Matrix((Amount<Angle>) Amount.valueOf(10.0, NonSI.DEGREE_ANGLE),
                Amount.valueOf(0, Angle.UNIT),
                Amount.valueOf(0, Angle.UNIT),
                new Point(0.0, 0.0, 0.0));
        Matrix mb = new Matrix(Amount.valueOf(0, Angle.UNIT),
                (Amount<Angle>) Amount.valueOf(20.0, NonSI.DEGREE_ANGLE),
                Amount.valueOf(0, Angle.UNIT),
                new Point(0.0, 0.0, 0.0));
        Matrix mc = new Matrix(Amount.valueOf(0, Angle.UNIT),
                Amount.valueOf(0, Angle.UNIT),
                (Amount<Angle>) Amount.valueOf(30.0, NonSI.DEGREE_ANGLE),
                new Point(0.0, 0.0, 0.0));
        Matrix mx = new Matrix(Amount.valueOf(0, Angle.UNIT),
                Amount.valueOf(0, Angle.UNIT),
                Amount.valueOf(0, Angle.UNIT),
                new Point(12.3, 45.6, 78.9));

        Matrix mmult = new Matrix(4, 4);
        mmult.unit();
        mmult.mult(mx);
        mmult.mult(ma);
        mmult.mult(mb);
        mmult.mult(mc);
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                assertEquals("row " + row + " col " + col + " not equal",
                        m1.get(row, col), mmult.get(row, col), 0.00001);
            }
        }

        // check that Matrix.inverseYPR actually produces the inverse matrix
        m1 = new Matrix((Amount<Angle>) Amount.valueOf(30.0, NonSI.DEGREE_ANGLE),
                (Amount<Angle>) Amount.valueOf(40.0, NonSI.DEGREE_ANGLE),
                (Amount<Angle>) Amount.valueOf(50.0, NonSI.DEGREE_ANGLE));
        Matrix m2 = Matrix.inverseYPR((Amount<Angle>) Amount.valueOf(30.0, NonSI.DEGREE_ANGLE),
                (Amount<Angle>) Amount.valueOf(40.0, NonSI.DEGREE_ANGLE),
                (Amount<Angle>) Amount.valueOf(50.0, NonSI.DEGREE_ANGLE));

        mmult = new Matrix(4, 4);
        mmult.unit();
        mmult.mult(m1);
        mmult.mult(m2);
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                assertEquals("row " + row + " col " + col + " not equal",
                        unit.get(row, col), mmult.get(row, col), 0.00001);
            }
        }
    }

    @Test
    public void testOperationOrder() {
        Matrix m1 = new Matrix((Amount<Angle>) Amount.valueOf(90.0, NonSI.DEGREE_ANGLE),
                Amount.valueOf(0, Angle.UNIT), Amount.valueOf(0, Angle.UNIT), new Point(0.0, 0.0, 0.0));
        Matrix m2 = new Matrix(Amount.valueOf(0, Angle.UNIT), (Amount<Angle>) Amount.valueOf(45.0, NonSI.DEGREE_ANGLE),
                Amount.valueOf(0, Angle.UNIT), new Point(0.0, 0.0, 0.0));
        Matrix mmult = new Matrix(4, 4);
        mmult.unit();
        mmult.mult(m1);
        mmult.mult(m2);

        Vector3 v = new Vector3(1.0, 0.0, 0.0);
        mmult.mult(v);
        assertTrue("concatenated matrices", v.isEqual(
                new Vector3(0.0, 0.707106781186547, -0.707106781186547)));

        mmult.unit();
        mmult.mult(m2);
        mmult.mult(m1);

        v = new Vector3(1.0, 0.0, 0.0);
        mmult.mult(v);
        assertTrue("concatenated matrices", v.isEqual(new Vector3(0.0, 1.0, 0.0)));

    }

    @Test
    public void testCopyConstructor() {
        Matrix orig = new Matrix((Amount<Angle>) Amount.valueOf(30.0, NonSI.DEGREE_ANGLE),
                (Amount<Angle>) Amount.valueOf(60.0, NonSI.DEGREE_ANGLE),
                (Amount<Angle>) Amount.valueOf(45.0, NonSI.DEGREE_ANGLE),
                new Point(1.0, 2.0, 3.0));

        Matrix copy = new Matrix(orig);

        assertEquals("Copy constructor should create same size Matrix", orig.getRows(), copy.getRows());
        assertEquals("Copy constructor should create same size Matrix", orig.getColumns(), copy.getColumns());

        for (int row = 0; row < orig.getRows(); row++) {
            for (int col = 0; col < orig.getColumns(); col++) {
                assertTrue("Copy constructor should produce duplicate", orig.get(row,col) == copy.get(row,col));
            }
        }
    }
}
