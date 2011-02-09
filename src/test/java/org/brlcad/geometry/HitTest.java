/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brlcad.geometry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.BitSet;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Ray;
import org.brlcad.numerics.Tolerance;
import org.brlcad.numerics.Vector3;
import org.brlcad.spacePartition.RayData;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jra
 */
public class HitTest {

    @Test
    public void testSerialization() {
        try {
            ObjectOutputStream oos = null;
            ObjectInputStream ois = null;
            String prim1 = "prim1";
            double d = 1.0;
            int numSolids = 237;
            BitSet solids = new BitSet(numSolids);
            int numRegions = 123;
            BitSet regions = new BitSet(numRegions);
            double tol_dist = 0.005;
            double perp = 0.995;
            Tolerance tol = new Tolerance(tol_dist, perp);
            Point start = new Point(1, 2, 3);
            Vector3 dir = new Vector3(1, 0, 0);
            Ray r1 = new Ray(start, dir);
            RayData rd1 = new RayData(start, d, solids, regions, tol, r1);
            double indist = 100.0;
            Point inPt = new Point(start);
            inPt.join(indist, dir);
            Vector3 innorm = Vector3.negate(dir);
            Hit hit1 = new Hit(indist, inPt, innorm, 1, rd1, prim1);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(hit1);
            oos.close();
            baos.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ois = new ObjectInputStream(bais);
            Hit hit2 = (Hit) ois.readObject();
            assertTrue("Hit was changed by serialization", hit1.equals(hit2));
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }

    }
}
