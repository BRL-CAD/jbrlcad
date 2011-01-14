package org.brlcad.samples;

import org.brlcad.info.RegionInfo;

/**
 *
 */
public class GetRegionMap {
    
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
        
        if (args.length < 1 ) {
            System.err.println("Usage: java -jar jbrlcad.jar .g_file {root object1 object2 ...}");
            System.err.println("  Default {root object} is '" + rootObjName + "'");
            System.exit(1);
        }
        
        // If arg[1] doesn't exist, set to default value
        
        String rootObject = (args.length == 1) ? rootObjName : args[1];
        
        // Print command line parameters for verification
        
        System.err.println("Params:  .g_file: '" + args[0]
                + "'; root object: '" + rootObject + "'"); 
        
        // Now call other methods to read/process input and write output

        String[] objects = new String[args.length-1];
        for( int i=1 ; i<args.length ; i++ ) {
            objects[i-1] = args[i];
        }
        RegionInfo regInfo = new RegionInfo(args[0], objects);
        regInfo.writeOutput();
    }
}
