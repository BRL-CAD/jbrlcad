/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.info;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.brlcad.geometry.BrlcadDb;
import java.util.List;
import java.util.Map;
import org.brlcad.geometry.DbException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases related to RegionInfo.java
 * @author jra, rmendes
 */
public class RegionInfoTest {

    public RegionInfoTest() {}

   @Test
    public void ktankReadAllRegionInfoTest() {
        String[] rootObjects = null;
        RegionInfo ri = new RegionInfo("src/test/resources/ktank.g", rootObjects);
        assertNotNull("RegionInfo Object should not be null", ri);
        Map<Integer,List<String>> idMap = ri.getIdentMap();
        assertNotNull("ident map should not be null", idMap);
        List<String> regions = idMap.get(208);
        assertTrue( "expected one region, but got " + regions.size(), regions.size() == 1);
        String region = regions.get(0);
        String expectedPath = "tank/turret/tur/r19";
        assertTrue("expected " + expectedPath + ", but got " + region, expectedPath.equals(region));
        
        rootObjects = new String[]{};
        ri = new RegionInfo("src/test/resources/ktank.g", rootObjects);
        assertNotNull("RegionInfo Object should not be null", ri);
        idMap = ri.getIdentMap();
        assertNotNull("ident map should not be null", idMap);
        regions = idMap.get(208);
        assertTrue( "expected one region, but got " + regions.size(), regions.size() == 1);
        region = regions.get(0);
        expectedPath = "tank/turret/tur/r19";
        assertTrue("expected " + expectedPath + ", but got " + region, expectedPath.equals(region));
    }

    @Test
    public void ktankTest() {
        String[] rootObjects = {"tank"};
        RegionInfo ri = new RegionInfo("src/test/resources/ktank.g", rootObjects);
        assertNotNull("RegionInfo Object should not be null", ri);
        Map<Integer,List<String>> idMap = ri.getIdentMap();
        assertNotNull("ident map should not be null", idMap);
        List<String> regions = idMap.get(208);
        assertTrue( "expected one region, but got " + regions.size(), regions.size() == 1);
        String region = regions.get(0);
        String expectedPath = "tank/turret/tur/r19";
        assertTrue("expected " + expectedPath + ", but got " + region, expectedPath.equals(region));
    }

    /**
     * Test handling when reading geometry data for a Geometry file that contains a TopLevelObject composed of a single region
     *
     * In such cases, the top-level object should be successfully loaded and the RegionInfo should contain the single region.
     *
     * @throws FileNotFoundException
     * @throws IOException
     * @throws DbException
     */
    @Test
    public void testReadingRegionDataWhenTLOisAregion() throws FileNotFoundException, IOException, DbException {
        //Test that top-level object can be read.
        String filePath = "src/test/resources/plate2_GEOMwithTLOasAregion.g";
        BrlcadDb db = new BrlcadDb(filePath);
        List<String> tlos = db.getTopLevelObjects();
        assertTrue( "expected 1 top level objects, but found " + tlos.size(), 1 == tlos.size());
        assertTrue(tlos.contains("plate.r"));

        //Test reading of region data
        String[] rootObjects = tlos.toArray(new String[0]);
        RegionInfo ri = new RegionInfo(filePath, rootObjects);
        assertNotNull("RegionInfo Object should not be null", ri);

        Map<String, Map<String, String>> regionMap = ri.getRegionMap();
        assertNotNull("ident map should not be null", regionMap);
        assertTrue("expected one region, but got " + regionMap.size(), regionMap.size() == 1);
        assertTrue(regionMap.containsKey("plate.r"));

        Map<Integer,List<String>> idMap = ri.getIdentMap();
        assertNotNull("ident map should not be null", idMap);
        List<String> regions = idMap.get(1000);
        assertTrue( "expected one region, but got " + regions.size(), regions.size() == 1);
        String region = regions.get(0);
        String expectedPath = "plate.r";
        assertTrue("expected " + expectedPath + ", but got " + region, expectedPath.equals(region));
    }
}
