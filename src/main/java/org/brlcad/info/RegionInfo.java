package org.brlcad.info;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.brlcad.geometry.*;

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
    
    private static final Logger logger = Logger.getLogger(RegionInfo.class.getName());

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
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public void writeOutput() {

        if( this.regionMap == null ) {
            System.out.println( "No data available, call \"processInput\" method first");
            return;
        }

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

        System.out.println( "IdentMap:");
        for( Map.Entry<Integer,List<String>> me : identMap.entrySet()) {
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
                    logger.warning("'" + objectName + "' is not valid a BRL-CAD top component. Object will be skipped");
                    continue;
                }
                if (isDebug()) {
                    printRawInfo("    (pi)", rootDbExt);
                }
                Combination rootCombo = new Combination(rootDbExt);               
                Map<String, String> comboAttrs = rootCombo.getAttributes();
                if (comboAttrs != null && comboAttrs.containsKey("region")) {
                        // This Root Combination is a region; save the region data contained
                        addRegionData(rootCombo, null, null);
                } else {
                    // Assume that rootCombo now represents the top of an implied hierarchy
                    // (i.e., tree) of smaller trees, each of which contains a combination
                    // of combinations and/or components which contain regions.  Recursively
                    // process the hierarchy using this "root node" as the start.
                    if (isDebug()) {
                        printComboInfo("    (pi->ph)", rootCombo, "");
                    }
                    processHierarchy(db, rootCombo.getTree(), objectName);                    
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
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
    @SuppressWarnings("UseOfSystemOutOrSystemErr") //Allow use of 'println' statements when 'isDebug() == true'
    private void processHierarchy(BrlcadDb db, Tree tree, String parentPath) throws DbException {

        // If any parameter is null, return immediately

        if (db == null || tree == null || parentPath == null || regionMap == null) {
            if (isDebug()) {
                System.out.println("    (ph) return immed");
            }
            return;
        }

        // First process the input tree.  Look for two things: (1) regions--save
        // the data in the regionMap for later output; and, (2) leaf nodes that
        // are not regions--the names of these are returned in nameSet.

        Set<String> nameSet = new HashSet<String>();
        if (isDebug()) {
            printTreeInfo("    (ph->pt)", tree, parentPath);
        }
        processTree(db, tree, parentPath, nameSet);

        // Process nameSet to move further down the region path name

        for (String s : nameSet) {
            DbExternalObject object = db.getDbExternal(s);
            if (object != null) {
                Combination combo = new Combination(db.getDbExternal(s));
                if (isDebug()) {
                    printComboInfo("    (ph->ph)", combo, parentPath + delimiter + s);
                }
                processHierarchy(db, combo.getTree(), parentPath + delimiter + s);
            } else {
                logger.warning("DbExternalObject '" + s + "' does not exist. Object will be skipped");
                continue;
            }
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
    @SuppressWarnings("UseOfSystemOutOrSystemErr") //Allow use of 'println' statements when 'isDebug() == true'
    private void processTree(BrlcadDb db, Tree tree, String parentPath,
            Set<String> nameSet) throws DbException {

        // If any parameter is null, return immediately

        if (db == null || tree == null || parentPath == null || regionMap == null
                || nameSet == null) {
            if (isDebug()) {
                System.out.println("    (pt) return immed");
            }
            return;
        }

        // If current tree is not a leaf, ignore it and process left and right branches

        if (tree.getOp() != Operator.LEAF) {
            if(tree.getOp() == Operator.UNION || tree.getOp() == Operator.INTERSECTION || tree.getOp() == Operator.XOR){         
                //Add both left and right branches of the tree.
                if (isDebug()) {
                    printTreeInfo("    (pt->pt left)", tree.getLeft(), parentPath);
                }
                processTree(db, tree.getLeft(), parentPath, nameSet);
                if (isDebug()) {
                    printTreeInfo("    (pt->pt right)", tree.getRight(), parentPath);
                }
                processTree(db, tree.getRight(), parentPath, nameSet);                
            }else if(tree.getOp() == Operator.SUBTRACTION){  
                //Add ONLY the left branch, exclude the right branch since it is subtracted from the left
                if (isDebug()) {
                    printTreeInfo("    (pt->pt left)", tree.getLeft(), parentPath);
                }
                processTree(db, tree.getLeft(), parentPath, nameSet);
                //Log warning message stating that the Combination to be subracted will be excluded.
                if(tree.getRight() != null){
                    logger.warning("DbExternalObject '" + tree.getRight().getLeafName() + "' is a subtraction from " + tree.getLeft().getLeafName() + " and will be excluded");
                }                
            }
            else{
                //Also ignore Operator.NOT since it is to be excluded. Log warning Message
                logger.warning("DbObject '" + tree.getLeafName() + "' uses operator " + tree.getOp() + " and will be excluded");
            }
            return;
        } 
        DbExternalObject leafDbext = db.getDbExternal(tree.getLeafName());
        if (leafDbext != null) {
            if (leafDbext.getMajorType() == 1 && leafDbext.getMinorType() == 31) {
                // Current tree is a leaf.  Get the data about the leaf from the BRL-CAD
                // file and process as either (1) the name of another group or (2) a region
                Combination combo = new Combination(leafDbext);
                addRegionData(combo, parentPath, nameSet);
            } else {
                String name = tree.getLeafName();
                addSolidData(parentPath, name);
                // This Leaf Combination is not a region; 
                logger.log(Level.INFO, "Attempted to import combination '" + leafDbext.getName() + "', but external is "
                        + " major type: " + leafDbext.getMajorType()
                        + " minor type: " + leafDbext.getMinorType());
            }
        } else {
            logger.warning("DbExternalObject '" + tree.getLeafName() + "' does not exist. Object will be skipped");
        }
    }

        @SuppressWarnings("UseOfSystemOutOrSystemErr") //Allow use of 'println' statements when 'isDebug() == true'
    private void addSolidData(String parentPath, String leafName){

        // This Combination is a region; save the data contained in the node,
        // along with pathing, to the results map (remove any leading delimiter
        // before saving)
        if (isDebug()) {
            System.out.println("(Region) Name: " + leafName
                    + "; Path: " + (parentPath != null ?  parentPath + delimiter : "") + leafName
                    + "; RegionID: 0"
                    + "; LOS: 100"
                    + "; MatID: 0"
                    + "; AirCode: 0");
        }
        Map<String, String> attrs = new TreeMap<String, String>();
        String path = (parentPath != null ?  parentPath + delimiter : "") + leafName;
        attrs.put("regionPath", path);
        regionMap.put(leafName, attrs);

        Integer ident = 0;
        List<String> regions = identMap.get(ident);
        if (regions == null) {
            regions = new ArrayList<String>();
            identMap.put(ident, regions);
        }
        regions.add(path);
    }
    
    @SuppressWarnings("UseOfSystemOutOrSystemErr") //Allow use of 'println' statements when 'isDebug() == true'
    private void addRegionData(Combination combo, String parentPath, Set<String> namesOfNonRegionCombinations){
        Map<String, String> comboAttrs = combo.getAttributes();
        if (comboAttrs.containsKey("region")) {

                // This Combination is a region; save the data contained in the node,
                // along with pathing, to the results map (remove any leading delimiter
                // before saving)
                if (isDebug()) {
                    System.out.println("(Region) Name: " + combo.getName()
                            + "; Path: " + (parentPath != null ?  parentPath + delimiter : "") + combo.getName()
                            + "; RegionID: " + comboAttrs.get("region_id")
                            + "; LOS: " + comboAttrs.get("los")
                            + "; MatID: " + comboAttrs.get("material_id")
                            + "; AirCode: " + comboAttrs.get("aircode"));
                }
                Map<String, String> attrs = new TreeMap<String, String>(comboAttrs);
                String path = (parentPath != null ?  parentPath + delimiter : "") + combo.getName();
                attrs.put("regionPath", path);
                regionMap.put(combo.getName(), attrs);

                String identStr = comboAttrs.get("region_id");
                Integer ident = 0;
                if (identStr != null) {
                    ident = Integer.valueOf(comboAttrs.get("region_id"));
                }
                List<String> regions = identMap.get(ident);
                if (regions == null) {
                    regions = new ArrayList<String>();
                    identMap.put(ident, regions);
                }
                regions.add(path);

            } else {

                // This Combination is not a region; save the name of the combo as
                // part of the nameSet so it can be processed further
                if (isDebug()) {
                    printComboInfo("    (pt-save-name)", combo, parentPath);
                }
                if(namesOfNonRegionCombinations != null){
                    namesOfNonRegionCombinations.add(combo.getName());
                }
            }
    }

    /**
     * Print info from a BRL-CAD Tree
     * @param where Location from where this info is being printed
     * @param combo BRL-CAD object from which data is printed
     * @param path Current path to get to this combo (used for regions)
     */
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private void printTreeInfo(String where, Tree tree, String path) {
        StringBuilder sb = new StringBuilder();
        sb.append(where).append(" Tree Name: '").append(tree.getLeafName()).append("'" + "; Tree: ").append(tree.toString()).append("; Path: '").append(path).append("'");
        System.out.println(sb.toString());
        System.out.flush();
    }


    /**
     * Print info from a BRL-CAD Combination
     * @param where Location from where this info is being printed
     * @param combo BRL-CAD object from which data is printed
     * @param path Current path to get to this combo (used for regions)
     */
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private void printComboInfo(String where, Combination combo, String path) {
        StringBuilder sb = new StringBuilder();
        sb.append(where).append(" Combo Name: ")
                .append(combo.getName()).append("; Tree: ")
                .append(combo.getTree().toString())
                .append("; Path: '").append(path).append("'");
        System.out.println(sb.toString());
        System.out.flush();
    }


    /**
     * Print raw (unparsed) attribute and body from DbExternal
     * @param where Location from where this info is being printed
     * @param dbExt BRL-CAD object from which data is printed
     */
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private void printRawInfo(String where, DbExternal dbExt) {
        StringBuilder sb = new StringBuilder();
        sb.append(where).append(" Raw Name: ")
                .append(dbExt.getName()).append("; Raw attrs: '")
                .append(bytesToString(dbExt.getAttributes()))
                .append("'" + "; Raw body: '")
                .append(bytesToString(dbExt.getBody())).append("'");
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
     * @return the regionMap(UnmodifiableMap)
     */
    public Map<String, Map<String, String>> getRegionMap() {
        return Collections.unmodifiableMap(regionMap);
    }

    /**
     * getIdentMap returns the map(UnmodifiableMap) if region ident numbers to lists of path names for regions that have that ident number
     * 
     * @return the identMap
     */
    public Map<Integer, List<String>> getIdentMap() {
        return Collections.unmodifiableMap(identMap);
    }
}
