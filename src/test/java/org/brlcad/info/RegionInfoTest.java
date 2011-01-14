/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.info;

import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jra
 */
public class RegionInfoTest {

    public RegionInfoTest() {}

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

}
