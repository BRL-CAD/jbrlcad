package geometry;
/**
 * Represents a single "piece" of Bot (some number of triangles from a 
 * single Bot
 * @see PreppedTriangle
 */



import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import preppedGeometry.PreppedBot;
import preppedGeometry.PreppedObjectPiece;
import preppedGeometry.PreppedTriangle;

import numerics.Ray;
import spacePartition.RayData;

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
}

