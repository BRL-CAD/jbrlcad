package org.brlcad.samples;
/**
 * Pure Java console application.
 * This application demonstrates console I/O.
 *
 * This file was automatically generated by
 * Omnicore CodeGuide.
 */


import java.util.SortedSet;

import org.brlcad.geometry.BrlcadDb;
import org.brlcad.geometry.Partition;
import org.brlcad.geometry.SimpleOverlapHandler;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Ray;
import org.brlcad.numerics.Vector3;
import org.brlcad.spacePartition.PreppedDb;
import org.brlcad.utils.ArrayCopy;

public class BrlcadJava
{
    private static final String usage = "Usage:\n\tjava -cp build/dist/jbrlcad.jar:lib/jscience.jar" +
            " [-p x y z] [-d x y z] file.g obj1 [ obj2 obj3 ...]";
	public static void main(String[] args)
	{
        Point start = new Point(0,0,0);
        Vector3 dir = new Vector3(1,0,0);
        String[] tlos = null;
        int argNo = 0;
        if( args.length < 2 ) {
            System.err.println( usage );
            return;
        }
        while( argNo < args.length ) {
            String arg = args[argNo];
            if( "-p".equals(arg) ) {
                // get start point
                for( int i=0 ; i<3 ; i++ ) {
                    argNo++;
                    start.set(i, Double.parseDouble(args[argNo]));
                }
                argNo++;
                continue;
            } else if( "-d".equals(arg) ) {
                // get direction
                for( int i=0 ; i<3 ; i++ ) {
                    argNo++;
                    dir.set(i, Double.parseDouble(args[argNo]));
                }
                argNo++;
                continue;
            } else {
                break;
            }
        }

        if( (args.length - argNo) < 2 ) {
            System.err.println( usage );
            return;
        }
		
		try
		{
			BrlcadDb brlcadDb = new BrlcadDb( args[argNo++] );
            tlos = new String[args.length - argNo];
            tlos = ArrayCopy.copyOfRange(args, argNo, args.length);
			PreppedDb prepped = new PreppedDb( brlcadDb, tlos );
			Ray ray = new Ray( start, dir );
			SortedSet<Partition> parts = prepped.shootRay( ray, new SimpleOverlapHandler() );
			System.out.println( "partitions (" + parts.size() + "):");
			for( Partition part : parts ) {
				System.out.println( "Partition:" + part);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit( 1 );
		}
	}
	
	/**
	 * This method prints available command-line arguments
	 * to the console.
	 */
	private static void printArguments(String[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			System.out.print("Argument " + i + ": ");
			System.out.println(args[i]);
		}
		
	}
}
