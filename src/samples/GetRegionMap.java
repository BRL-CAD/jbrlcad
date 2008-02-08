package samples;

import geometry.BrlcadDb;
import geometry.Combination;
import geometry.DbException;
import geometry.DbExternal;
import geometry.Operator;
import geometry.Tree;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 */
public class GetRegionMap {
    
    /**
     * Delimiter between parts of the region path name
     */
    private String delimiter = "/";
    
    /**
     * Show debugging info
     */
    private boolean debug = false;
    
    /**
     * Default top object name if one is not specified
     */
    private static String rootObjName = "component";

    /**
     * Checks input params, then calls methods to read/process input and write output
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // Check for proper number of args
        
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: java -jar jbrlcad.jar .g_file {root object}");
            System.err.println("  Default {root object} is '" + rootObjName + "'");
            System.exit(1);
        }
        
        // If arg[1] doesn't exist, set to default value
        
        String rootObject = (args.length == 1) ? rootObjName : args[1];
        
        // Now call other methods to read/process input and write output
        
        GetRegionMap grm = new GetRegionMap();
        Map<String, Map<String, String>> regionMap = grm.processInput(args[0], rootObject);
        grm.writeOutput(regionMap);
    }

    
    public void writeOutput(Map<String, Map<String, String>> regionMap) {

        System.out.println("Final results:");
        for (Map.Entry<String, Map<String, String>> me : regionMap.entrySet()) {
            if ( ! me.getValue().containsKey("aircode")) {
                System.out.print(me.getKey());
                for (Map.Entry<String, String> me_inside : me.getValue().entrySet()) {
                    System.out.print("; " + me_inside.getKey()
                            + ": " + me_inside.getValue());
                }
                System.out.println();
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
    public Map<String, Map<String, String>> processInput(String inFileName, String rootObject) {

        // The following holds a region's attributes as key-value pairs in Map<String, String>;
        // the name of the region is the String part of Map<String, Map>
        
        Map<String, Map<String, String>> regionMap = new TreeMap<String, Map<String, String>>();

        try {
            // Open, read, and get pointers to objects in a BRL-CAD file
            
            BrlcadDb db = new BrlcadDb(inFileName);
            
            // Get the data for the root object.  If it isn't a BRL-CAD Component,
            // then throw an exception and quit
            
            DbExternal rootDbExt = db.getDbExternal(rootObject);
            if (debug) printRawInfo("    (pi)", rootDbExt);
            Combination rootCombo = new Combination(rootDbExt);
            
            // Assume that rootCombo now represents the top of an implied hierarchy
            // (i.e., tree) of smaller trees, each of which contains a combination
            // of combinations and/or components which contain regions.  Recursively
            // process the hierarchy using this "root node" as the start.

            if (debug) printComboInfo("    (pi->ph)", rootCombo, "");
            processHierarchy(db, rootCombo.getTree(), "", regionMap);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
        
        // If no exceptions (everything processed), then return create region map
        
        return regionMap;
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
    private void processHierarchy(BrlcadDb db, Tree tree, String parentPath,
            Map<String, Map<String, String>> regionMap) throws DbException {

        // If any parameter is null, return immediately
        
        if (db == null || tree == null || parentPath == null || regionMap == null) {
            if (debug) System.out.println("    (ph) return immed");
            return;
        }
        
        // First process the input tree.  Look for two things: (1) regions--save
        // the data in the regionMap for later output; and, (2) leaf nodes that
        // are not regions--the names of these are returned in nameSet.
        
        Set<String> nameSet = new HashSet<String>();
        if (debug) printTreeInfo("    (ph->pt)", tree, parentPath);
        processTree(db, tree, parentPath, regionMap, nameSet);
        
        // Process nameSet to move further down the region path name

        for (String s : nameSet) {
            if (debug) printRawInfo("    (ph)", db.getDbExternal(s));
            Combination combo = new Combination(db.getDbExternal(s));
            if (debug) printComboInfo("    (ph->ph)", combo, parentPath + delimiter + s);
            processHierarchy(db, combo.getTree(), parentPath + delimiter + s, regionMap);
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
            Map<String, Map<String, String>> regionMap, Set<String> nameSet) throws DbException {

        // If any parameter is null, return immediately

        if (db == null || tree == null || parentPath == null || regionMap == null
                || nameSet == null) {
            if (debug) System.out.println("    (pt) return immed");
            return;
        }
        
        // If current tree is not a leaf, ignore it and process left and right branches
        
        if (tree.getOp() != Operator.LEAF) {
            if (debug) printTreeInfo("    (pt->pt left)", tree.getLeft(), parentPath);
            processTree(db, tree.getLeft(), parentPath, regionMap, nameSet);
            if (debug) printTreeInfo("    (pt->pt right)", tree.getRight(), parentPath);
            processTree(db, tree.getRight(), parentPath, regionMap, nameSet);
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
            
            if (debug) System.out.println("(Region) Name: " + combo.getName()
                    + "; Path: " + parentPath + delimiter + combo.getName()
                    + "; RegionID: " + comboAttrs.get("region_id")
                    + "; LOS: " + comboAttrs.get("los")
                    + "; MatID: " + comboAttrs.get("material_id")
                    + "; AirCode: " + comboAttrs.get("aircode"));
            Map<String, String> attrs = new TreeMap<String, String>(comboAttrs);
            attrs.put("regionPath", parentPath + delimiter + combo.getName());
            regionMap.put(combo.getName(), attrs);

        } else {
            
            // This Combination is not a region; save the name of the combo as
            // part of the nameSet so it can be processed further
            
            if (debug) printComboInfo("    (pt-save-name)", combo, parentPath);
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

}
