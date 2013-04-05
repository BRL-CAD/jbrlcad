package org.brlcad.numerics;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    @Test
    public void testSerilization() {
        // before Externalization, serialized size was 338 bytes
        // after externalization, serialized size is 95 bytes
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            Ray r1 = new Ray(new Point(1, 2, 3), new Vector3(4, 5, 6));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(r1);
            oos.close();
            System.out.println( "serialized size = " + baos.size());
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ois = new ObjectInputStream(bais);
            Ray r2 = (Ray) ois.readObject();
            assertTrue("Ray was changed by serialization", r1.isEqual(r2));
        } catch (Exception ex) {
            fail(ex.getMessage());
        } finally {
            try {
                oos.close();
            } catch (IOException ex) {
                Logger.getLogger(RayTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
