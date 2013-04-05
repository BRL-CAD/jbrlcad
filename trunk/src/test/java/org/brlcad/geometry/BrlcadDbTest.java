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
 * Test cases related to BrlcadDb.java
 * @author jra, rmendes
 */
public class BrlcadDbTest {
    public BrlcadDbTest() {}

    @Test
    public void testGetTopLevelObjects() {
        try {
            BrlcadDb db = new BrlcadDb("src/test/resources/ktank.g");
            List<String> tlos = db.getTopLevelObjects();
            assertTrue( "expected 5 top level objects, but found " + tlos.size(), 5 == tlos.size());
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * Test handling when the tree references a combination that does not exist.
     *  
     * In such cases, the error should be logged(notifying the user), and the non-existent object should be skipped
     * This is the behavior when using mged in Brlcad.
     * 
     * @throws FileNotFoundException
     * @throws DbException
     * @throws IOException  
     */
    @Test
    public void testHandlingWhenNonExistentObjectIsRefencedInATree() throws FileNotFoundException, DbException, IOException {
            BrlcadDb db = new BrlcadDb("src/test/resources/geomFileContainingBadReference.g");
            List<String> tlos = db.getTopLevelObjects();
            assertTrue( "expected 1 top level objects, but found " + tlos.size(), 1 == tlos.size());
    }    
    
}
