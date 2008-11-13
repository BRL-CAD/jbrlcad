package org.brlcad.geometry;
/**
 * Arb8 - based on the BRL-CAD ARB8
 *
 */



import org.brlcad.preppedGeometry.PreppedArb8;
import org.brlcad.preppedGeometry.PreppedCombination;
import org.brlcad.numerics.Point;
import org.brlcad.spacePartition.PreppedDb;
import org.brlcad.numerics.Matrix;

public class Arb8 extends DbObject
{
	// the arb vertices
	private Point[] points;
	
	// the major and minor types for an Arb8
	public static final byte majorType = 1;
	public static final byte minorType = 4;
	
	/**
	 * Constructor
	 * @param dbExt	The DbExternal object representing an arb8
	 * @throws DbException
	 */
	public Arb8( DbExternal dbExt ) throws DbException
	{
		super( dbExt );
		
		if( dbExt.getMajorType() != Arb8.majorType || dbExt.getMinorType() != Arb8.minorType )
		{
			throw new DbException( "Attempted to import an Arb8, but external is " +
									  " major type: " + dbExt.getMajorType() +
									  " minor type: " + dbExt.getMinorType() );
		}
		
		this.points = new Point[8];
		
		// get the body bytes from the DbExternal object
		byte[] body = dbExt.getBody();
		
		int pointer = 0;
		for( int i=0 ; i<8 ; i++ )
		{
			this.points[i] = new Point();
			this.points[i].setX( BrlcadDb.getDouble( body, pointer ) );
			pointer += 8;
			this.points[i].setY( BrlcadDb.getDouble( body, pointer ) );
			pointer += 8;
			this.points[i].setZ( BrlcadDb.getDouble( body, pointer ) );
			pointer += 8;
		}
	}
	
	/**
	 * Get a vertex of this ARB8
	 * @param i	The index of the desired vertex
	 * @return	The vertex corresponding to the specified index
	 */
	public Point getVertex( int i )
	{
		return this.points[i];
	}
	
	/**
	 * Prep this Arb8 for ray tracing
	 * @param reg	The region containing this Arb8 (or null)
	 * @param preppedDb	The PreppedDb that contains this Arb8
	 * @param matrix	The transformation matrix to be applied to this Arb8
	 */
	public PreppedArb8 prep( PreppedCombination reg, PreppedDb preppedDb, Matrix matrix) throws BadGeometryException
	{
		PreppedArb8 prepped = new PreppedArb8( this, matrix );
		preppedDb.addPreppedObjectToInitialBox( prepped );
		if( reg != null )
		{
			prepped.addRegion( reg );
		}
		return prepped;
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("arb8:\n");
		for( int i=0 ; i<8 ; i++ )
		{
			sb.append( "\tpt["+i+"] = " + this.points[i] + "\n" );
		}
		return sb.toString();
	}
}
