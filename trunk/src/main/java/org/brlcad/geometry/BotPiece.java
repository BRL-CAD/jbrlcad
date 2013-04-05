package org.brlcad.geometry;
/**
 * Represents a single "piece" of Bot (some number of triangles from a 
 * single Bot
 * @see PreppedTriangle
 */



import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.brlcad.preppedGeometry.PreppedBot;
import org.brlcad.preppedGeometry.PreppedObjectPiece;
import org.brlcad.preppedGeometry.PreppedTriangle;

import org.brlcad.numerics.Ray;
import org.brlcad.numerics.Vector3;
import org.brlcad.spacePartition.PreppedDb;
import org.brlcad.spacePartition.RayData;

public class BotPiece extends PreppedObjectPiece
{
	// The PreppedTriangles in this piece
	private List<PreppedTriangle> triangles;
	
	/**
	 * Constructor (builds an empty piece)
	 * @param botName	The name of the Bot
	 * @param pBot	The Prepped verion of the Bot
	 */
	public BotPiece( String botName, PreppedBot pBot )
	{
		super( botName, pBot );
		this.triangles = new ArrayList<PreppedTriangle>();
	}
	
	/**
	 * Add a PreppedTriangle to this BotPiece
	 * @param tri	The PreppedTriangle to add
	 * @see PreppedTriangle
	 */
	public void addTriangle( PreppedTriangle tri )
	{
		this.triangles.add( tri );
		this.boundingBox.extend( tri.getBoundingBox() );
	}

    public void addTriangles( List<PreppedTriangle> tris ) {
        for( PreppedTriangle pt : tris ) {
            addTriangle(pt);
        }
    }
	
	/**
	 * Shoot a ray at this BotPiece
	 * @param ray	The ray to shoot
	 * @param rayData	The RayData to use for the shot
	 * @return	A Set of Hit objects (one for each PreppedTriangle that the ray intersects)
	 */
	public Set<Hit> shoot( Ray ray, RayData rayData )
	{
		Set<Hit> hits = new TreeSet<Hit>();
		for( PreppedTriangle ptri:this.triangles )
		{
			Hit hit = ptri.shoot( ray, rayData );
			if( hit != null )
			{
				hits.add( hit );
			}
		}
		
		return hits;
	}

    void subDivide(PreppedDb preppedDb) {
        subdivide( this, preppedDb );
        List<PreppedObjectPiece> pieces = preppedDb.getPieces();

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int total = 0;
        int count = 0;
        for( PreppedObjectPiece pop : pieces ) {
            if( pop instanceof BotPiece ) {
                BotPiece bp = (BotPiece) pop;
                int triCount = bp.triangles.size();
                total += triCount;
                if( triCount < min ) {
                    min = triCount;
                }
                if( triCount > max ) {
                    max = triCount;
                }
                count++;
            }
        }

//        System.out.println( count + " pieces");
//        System.out.println( "min=" + min);
//        System.out.println( "max=" + max);
//        System.out.println( "tris=" + total);
//        System.out.println( "ave=" + (total/count));
//        System.out.println( "BB=" + preppedDb.getBoundingBox());
    }

    private static void subdivide(BotPiece bp, PreppedDb preppedDb) {
        if( bp.triangles.size() <= BrlcadDb.BOT_FACES_PER_PIECE ) {
            preppedDb.addPreppedObjectPieceToInitialBox(bp);
            return;
        }

        Vector3 diag = Vector3.minus(bp.boundingBox.getMax(), bp.boundingBox.getMin());
        int axis = -1;
        double bbLength = Double.NEGATIVE_INFINITY;
        for( int i=0 ; i<3 ; i++ ) {
            double length = diag.get(i);
            if( length > bbLength ) {
                axis = i;
                bbLength = length;
            }
        }
        double cutCoord = bp.boundingBox.getMin().get(axis) + bbLength/2.0;
//        System.out.println( "Conside cut: " + bp + " on axis=" + axis + " at " + cutCoord);

        BotPiece bp1 = new BotPiece(bp.getName(), (PreppedBot)bp.getPreppedObject());
        BotPiece bp2 = new BotPiece(bp.getName(), (PreppedBot)bp.getPreppedObject());

        for( PreppedTriangle pt : bp.triangles ) {
            double maxCoord = pt.getBoundingBox().getMax().get(axis);
            double minCoord = pt.getBoundingBox().getMin().get(axis);
            if( maxCoord < cutCoord ) {
                bp1.addTriangle(pt);
            } else if( minCoord > cutCoord ) {
                bp2.addTriangle(pt);
            } else {
                int count1 = bp1.triangles.size();
                int count2 = bp2.triangles.size();
                if( count1 < count2 ) {
                    bp1.addTriangle(pt);
                } else {
                    bp2.addTriangle(pt);
                }
            }
        }

        if( bp1.triangles.size() == bp.triangles.size() ||
             bp2.triangles.size() == bp.triangles.size() ) {
            preppedDb.addPreppedObjectPieceToInitialBox(bp);
            return;
        }

//        System.out.println( "after cut:");
//        System.out.println( "bp1: " + bp1);
//        System.out.println( "bp2: " + bp2);
        subdivide(bp1, preppedDb);
        subdivide(bp2, preppedDb);
    }

    @Override
    public String toString() {
        return this.boundingBox.toString() + " with " + this.triangles.size() + " triangles";
    }
}

