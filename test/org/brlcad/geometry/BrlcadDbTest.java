/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.geometry;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jra
 */
public class BrlcadDbTest {
    public BrlcadDbTest() {}

    @Test
    public void testGetTopLevelObjects() {
        try {
            BrlcadDb db = new BrlcadDb("test/ktank.g");
            List<String> tlos = db.getTopLevelObjects();
            assertTrue( "expected 5 top level objects, but found " + tlos.size(), 5 == tlos.size());
        } catch (FileNotFoundException ex) {
            fail(ex.getMessage());
        } catch (IOException ex) {
            fail(ex.getMessage());
        } catch (DbException ex) {
            fail(ex.getMessage());
        }
    }

}
