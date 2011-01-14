package org.brlcad.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.brlcad.geometry.BrlcadDb;
import org.brlcad.geometry.Combination;
import org.brlcad.geometry.DbException;
import org.brlcad.geometry.DbExternal;
import org.brlcad.geometry.Operator;
import org.brlcad.geometry.Tree;

/**
 * A class useful for obtaining information about regions in a BRL-CAD ".g" file.
 * 
 */
public class RegionInfo {

    /**
     * Delimiter between parts of the region path name
     */
    private final String delimiter = "/";

    /**
     * Show debugging info
     */
    private boolean debug = false;


    /**
     * A Map that holds a region's attributes as key-value pairs in Map<String, String>;
     * the name of the region is the String part of Map<String, Map>
     */
    private Map<String, Map<String, String>> regionMap = null;

    /**
     * A Map where keys are region idents and the values are lists of region path names
     * for regions with that ident
     */
    private Map<Integer,List<String>> identMap = null;

    /**
     * Constructor
     *
     * @param inFileName The name of a BRL-CAD ".g" file
     * @param rootObjects The top level objects in the ".g" file that are to be processed
     */
    public RegionInfo(String inFileName, String... rootObjects) {
        this.processInput(inFileName, rootObjects);
    }

    /**
     * write all the region info that has been obtained to std out
     */
    public void writeOutput() {

        if( this.getRegionMap() == null ) {
            System.out.println( "No data available, call \"processInput\" method first");
            return;
        }

        System.out.println("Final results:");
        for (Map.Entry<String, Map<String, String>> me : getRegionMap().entrySet()) {
            if ( ! me.getValue().containsKey("aircode")) {
                System.out.print(me.getKey());
                for (Map.Entry<String, String> me_inside : me.getValue().entrySet()) {
                    System.out.print("; " + me_inside.getKey()
                            + ": " + me_inside.getValue());
                }
                System.out.println();
            }
        }

        System.out.println( "IdentMap:");
        for( Map.Entry<Integer,List<String>> me : getIdentMap().entrySet()) {
            System.out.println( "ident " + me.getKey() + ":");
            for( String region : me.getValue() ) {
                System.out.println( "\t" + region);
            }
        }
    }


    /**
     * Open the BRL-CAD db (.g file), get the info about the root object, then
     * start the process of traversing the implied tree of BRL-CAD objects (each
     * of which is a tree in itself)
     * @param inFileName Name of BRL-CAD file to process
     * @param rootObject Name of the top object to process (i.e., root of the tree)
     * @return A Map<String, Map<String, String>>, where the Map<String, String>
     * is a map of each region's attributes including a special attribute which
     * contains the full path name from the top of the hierarchy to the region,
     * and where Map<String, Map> is a map of the region name to it's attributes
     */
    private void processInput(String inFileName, String... rootObjects) {

        this.regionMap = new TreeMap<String, Map<String, String>>();
        this.identMap = new HashMap<Integer, List<String>>();

        try {
            // Open, read, and get pointers to objects in a BRL-CAD file

            BrlcadDb db = new BrlcadDb(inFileName);

            // Get the data for the root object.  If it isn't a BRL-CAD Component,
            // then throw an exception and quit
            for(String objectName : rootObjects) {
                DbExternal rootDbExt = db.getDbExternal(objectName);
                if (rootDbExt == null) {
                    throw new DbException("'" + objectName + "' is not a BRL-CAD top component.");
                }
                if (isDebug()) {
                    printRawInfo("    (pi)", rootDbExt);
                }
                Combination rootCombo = new Combination(rootDbExt);

                // Assume that rootCombo now represents the top of an implied hierarchy
                // (i.e., tree) of smaller trees, each of which contains a combination
                // of combinations and/or components which contain regions.  Recursively
                // process the hierarchy using this "root node" as the start.

                if (isDebug()) {
                    printComboInfo("    (pi->ph)", rootCombo, "");
                }
                processHierarchy(db, rootCombo.getTree(), objectName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Process the hierarchy (i.e., tree) implied by the contents of the trees
     * which are each of the BRL-CAD Combination objects.
     * @param db BRL-CAD database (opened .g file)
     * @param tree Tree (of a Combination object) to process
     * @param parentPath The current region path name from root to this point
     * @param regionMap Map of regions' attributes as built up from processing
     * @throws geometry.DbException Thrown when there's an error in reading the file
     */
    private void processHierarchy(BrlcadDb db, Tree tree, String parentPath) throws DbException {

        // If any parameter is null, return immediately

        if (db == null || tree == null || parentPath == null || getRegionMap() == null) {
            if (isDebug()) System.out.println("    (ph) return immed");
            return;
        }

        // First process the input tree.  Look for two things: (1) regions--save
        // the data in the regionMap for later output; and, (2) leaf nodes that
        // are not regions--the names of these are returned in nameSet.

        Set<String> nameSet = new HashSet<String>();
        if (isDebug()) printTreeInfo("    (ph->pt)", tree, parentPath);
        processTree(db, tree, parentPath, nameSet);

        // Process nameSet to move further down the region path name

        for (String s : nameSet) {
//            if (isDebug()) printRawInfo("    (ph)", db.getDbExternal(s));
            Combination combo = new Combination(db.getDbExternal(s));
            if (isDebug()) printComboInfo("    (ph->ph)", combo, parentPath + delimiter + s);
            processHierarchy(db, combo.getTree(), parentPath + delimiter + s);
        }
    }


    /**
     * Process the contents of a single tree.  If the tree is a leaf and a region,
     * save the region's attributes along with pathname from top to here.  If tree
     * is a leaf but not a region, save the name of the tree to the nameSet for
     * more processing by processHierarchy.  If the tree is not a leaf, then
     * process the left and right branches of the (binary) tree.
     * @param db
     * @param tree
     * @param parentPath
     * @param regionMap
     * @param nameSet
     * @throws geometry.DbException
     */
    private void processTree(BrlcadDb db, Tree tree, String parentPath,
            Set<String> nameSet) throws DbException {

        // If any parameter is null, return immediately

        if (db == null || tree == null || parentPath == null || getRegionMap() == null
                || nameSet == null) {
            if (isDebug()) System.out.println("    (pt) return immed");
            return;
        }

        // If current tree is not a leaf, ignore it and process left and right branches

        if (tree.getOp() != Operator.LEAF) {
            if (isDebug()) printTreeInfo("    (pt->pt left)", tree.getLeft(), parentPath);
            processTree(db, tree.getLeft(), parentPath, nameSet);
            if (isDebug()) printTreeInfo("    (pt->pt right)", tree.getRight(), parentPath);
            processTree(db, tree.getRight(), parentPath, nameSet);
            return;
        }

        // Current tree is a leaf.  Get the data about the leaf from the BRL-CAD
        // file and process as either (1) the name of another group or (2) a region

        Combination combo = new Combination(db.getDbExternal(tree.getLeafName()));
        Map<String, String> comboAttrs = combo.getAttributes();

        if (comboAttrs.containsKey("region")) {

            // This Combination is a region; save the data contained in the node,
            // along with pathing, to the results map (remove any leading delimiter
            // before saving)

            if (isDebug()) System.out.println("(Region) Name: " + combo.getName()
                    + "; Path: " + parentPath + delimiter + combo.getName()
                    + "; RegionID: " + comboAttrs.get("region_id")
                    + "; LOS: " + comboAttrs.get("los")
                    + "; MatID: " + comboAttrs.get("material_id")
                    + "; AirCode: " + comboAttrs.get("aircode"));
            Map<String, String> attrs = new TreeMap<String, String>(comboAttrs);
            String path = parentPath + delimiter + combo.getName();
            attrs.put("regionPath", path);
            getRegionMap().put(combo.getName(), attrs);

            String identStr = comboAttrs.get("region_id");
            Integer ident = 0;
            if (identStr != null) {
                ident = Integer.valueOf(comboAttrs.get("region_id"));
            }
            List<String> regions = getIdentMap().get(ident);
            if( regions == null ) {
                regions = new ArrayList<String>();
                getIdentMap().put(ident, regions);
            }
            regions.add(path);

        } else {

            // This Combination is not a region; save the name of the combo as
            // part of the nameSet so it can be processed further

            if (isDebug()) printComboInfo("    (pt-save-name)", combo, parentPath);
            nameSet.add(combo.getName());
        }
    }


    /**
     * Print info from a BRL-CAD Tree
     * @param where Location from where this info is being printed
     * @param combo BRL-CAD object from which data is printed
     * @param path Current path to get to this combo (used for regions)
     */
    private void printTreeInfo(String where, Tree tree, String path) {
        StringBuilder sb = new StringBuilder();
        sb.append(where + " Tree Name: '" + tree.getLeafName() + "'"
                + "; Tree: " + tree.toString()
                + "; Path: '" + path + "'");
        System.out.println(sb.toString());
        System.out.flush();
    }


    /**
     * Print info from a BRL-CAD Combination
     * @param where Location from where this info is being printed
     * @param combo BRL-CAD object from which data is printed
     * @param path Current path to get to this combo (used for regions)
     */
    private void printComboInfo(String where, Combination combo, String path) {
        StringBuilder sb = new StringBuilder();
        sb.append(where + " Combo Name: " + combo.getName()
                + "; Tree: " + combo.getTree().toString()
                + "; Path: '" + path + "'");
        System.out.println(sb.toString());
        System.out.flush();
    }


    /**
     * Print raw (unparsed) attribute and body from DbExternal
     * @param where Location from where this info is being printed
     * @param dbExt BRL-CAD object from which data is printed
     */
    private void printRawInfo(String where, DbExternal dbExt) {
        StringBuilder sb = new StringBuilder();
        sb.append(where + " Raw Name: " + dbExt.getName()
                + "; Raw attrs: '" + bytesToString(dbExt.getAttributes()) + "'"
                + "; Raw body: '" + bytesToString(dbExt.getBody()) + "'");
        System.out.println(sb.toString());
        System.out.flush();
    }


    /**
     * Convert byte array to (character) String
     * @param byteArray Input array of bytes
     * @return String which has converted bytes to chars
     */
    private String bytesToString(byte[] byteArray) {
        StringBuilder sb = new StringBuilder();
        if (byteArray != null) {
            for (byte b : byteArray) {
                sb.append((char) b);
            }
        }
        return sb.toString();
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * @return the regionMap
     */
    public Map<String, Map<String, String>> getRegionMap() {
        return regionMap;
    }

    /**
     * getIdentMap returns the map if region ident numbers to lists of path names for regions that have that ident number
     * 
     * @return the identMap
     */
    public Map<Integer, List<String>> getIdentMap() {
        return identMap;
    }
}
