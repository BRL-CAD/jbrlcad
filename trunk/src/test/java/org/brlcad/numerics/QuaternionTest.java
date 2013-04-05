package org.brlcad.numerics;

import org.jscience.physics.amount.Amount;
import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.junit.Test;
import static org.junit.Assert.*;


public class QuaternionTest {

    // Removed to work with Agitator, JF, 1/31/2006
    //public QuaternionTest(String name, boolean tp) {
    //    super(name, tp);
    //}

    public QuaternionTest() {
    }

    @Test
    public void testBasic() {
        Quaternion q = new Quaternion();
        //assertEquals("q[0]", 0.0, q.q[0]);
        //assertEquals("q[1]", 0.0, q.q[1]);
        //assertEquals("q[2]", 0.0, q.q[2]);
        //assertEquals("q[3]", 1.0, q.q[3]);
        assertTrue("q0", 0.0 == q.getQ0());
        assertTrue("q1", 0.0 == q.getQ1());
        assertTrue("q2", 0.0 == q.getQ2());
        assertTrue("q3", 1.0 == q.getQ3());

        q = new Quaternion((Amount<Angle>) Amount.valueOf(Math.PI / 2.0, SI.RADIAN), Amount.valueOf(0, Angle.UNIT), Amount.valueOf(0, Angle.UNIT));
        //assertEquals("q[0]", 0.0, q.q[0]);
        //assertEquals("q[1]", 0.0, q.q[1]);
        //assertEquals("q[2]", 0.70711, q.q[2], 0.00001);
        //assertEquals("q[3]", 0.70711, q.q[3], 0.00001);
        assertTrue("q0", 0.0 == q.getQ0());
        assertTrue("q1", 0.0 == q.getQ1());
        assertEquals("q2", 0.70711, q.getQ2(), 0.00001);
        assertEquals("q3", 0.70711, q.getQ3(), 0.00001);
        Vector3 v = q.mult(new Vector3(1.0, 0.0, 0.0));
        v.setTolerance(0.0000001);
        assertTrue("rotated vector", v.isEqual(new Vector3(0.0, 1.0, 0.0)));

        q = new Quaternion(Amount.valueOf(0, Angle.UNIT), (Amount<Angle>) Amount.valueOf(Math.PI / 3.0, SI.RADIAN), Amount.valueOf(0, Angle.UNIT));
        //assertEquals("q[0]", 0.0, q.q[0], 0.00001);
        //assertEquals("q[1]", 0.5, q.q[1], 0.00001);
        //assertEquals("q[2]", 0.0, q.q[2], 0.00001);
        //assertEquals("q[3]", 0.86603, q.q[3], 0.00001);
        assertEquals("q0", 0.0, q.getQ0(), 0.00001);
        assertEquals("q1", 0.5, q.getQ1(), 0.00001);
        assertEquals("q2", 0.0, q.getQ2(), 0.00001);
        assertEquals("q3", 0.86603, q.getQ3(), 0.00001);
        v = q.mult(new Vector3(1.0, 0.0, 0.0));
        v.setTolerance(0.0000001);
        assertTrue("rotated vector", v.isEqual(new Vector3(0.5, 0.0, -0.866025403)));

        q = new Quaternion(Amount.valueOf(0, Angle.UNIT), Amount.valueOf(0, Angle.UNIT), (Amount<Angle>) Amount.valueOf(Math.PI / 6.0, SI.RADIAN));
        //assertEquals("q[0]", 0.25881904510252074, q.q[0], 0.00001);
        //assertEquals("q[1]", 0.0, q.q[1], 0.00001);
        //assertEquals("q[2]", 0.0, q.q[2], 0.00001);
        //assertEquals("q[3]", 0.9659258262890683, q.q[3], 0.00001);
        assertEquals("q0", 0.25881904510252074, q.getQ0(), 0.00001);
        assertEquals("q1", 0.0, q.getQ1(), 0.00001);
        assertEquals("q2", 0.0, q.getQ2(), 0.00001);
        assertEquals("q3", 0.9659258262890683, q.getQ3(), 0.00001);
        v = q.mult(new Vector3(0.0, 0.0, 1.0));
        v.setTolerance(0.0000001);
        assertTrue("rotated vector", v.isEqual(new Vector3(0.0, -0.5, 0.866025403)));

        q = new Quaternion((Amount<Angle>) Amount.valueOf(10.0, NonSI.DEGREE_ANGLE),
                (Amount<Angle>) Amount.valueOf(20.0, NonSI.DEGREE_ANGLE),
                (Amount<Angle>) Amount.valueOf(30.0, NonSI.DEGREE_ANGLE));
        Matrix m = new Matrix((Amount<Angle>) Amount.valueOf(10.0, NonSI.DEGREE_ANGLE),
                (Amount<Angle>) Amount.valueOf(20.0, NonSI.DEGREE_ANGLE),
                (Amount<Angle>) Amount.valueOf(30.0, NonSI.DEGREE_ANGLE),
                new Point(0.0, 0.0, 0.0));
        //assertEquals("q[0]", 0.2392983377447303, q.q[0], 0.00001);
        //assertEquals("q[1]", 0.18930785741199999, q.q[1], 0.00001);
        //assertEquals("q[2]", 0.03813457647485015, q.q[2], 0.00001);
        //assertEquals("q[3]", 0.9515485246437885, q.q[3], 0.00001);
        assertEquals("q0", 0.2392983377447303, q.getQ0(), 0.00001);
        assertEquals("q1", 0.18930785741199999, q.getQ1(), 0.00001);
        assertEquals("q2", 0.03813457647485015, q.getQ2(), 0.00001);
        assertEquals("q3", 0.9515485246437885, q.getQ3(), 0.00001);
        v = new Vector3(1.0, 0.0, 0.0);
        m.mult(v);
        v.setTolerance(0.0000001);
        Vector3 v1 = q.mult(new Vector3(1.0, 0.0, 0.0));
        v1.setTolerance(0.0000001);
        assertTrue("vector rotated by Quaternoin and Matrix do not agree", v1.isEqual(v));
    }
}

