package org.brlcad.preppedGeometry;
/**
 * The Prepped version of the Bot primitive
 */



import org.brlcad.spacePartition.RayData;
import org.brlcad.numerics.Ray;
import org.brlcad.geometry.BadGeometryException;
import org.brlcad.geometry.Bot;
import org.brlcad.geometry.Hit;
import org.brlcad.geometry.Segment;

import java.util.List;
import java.util.ArrayList;
import org.brlcad.numerics.Matrix;
import org.brlcad.numerics.BoundingBox;
import java.util.Set;
import java.util.TreeSet;
import java.util.Iterator;
import org.brlcad.numerics.Vector3;
import org.brlcad.numerics.Point;

public class PreppedBot extends PreppedObject
{
	// The prepped triangles that make up this Bot
	private List<PreppedTriangle> triangles;
	
	// The original Bot
	private Bot theBot;
	
	/**
	 * Constructor (does the actual prepping of the Bot)
	 * @param bot	The original Bot
	 * @param matrix	The transformation matrix to apply to the Bot
	 */
	public PreppedBot( Bot bot, Matrix matrix )
	{
		super( bot.getName() );
		
		this.theBot = bot;
		this.triangles = new ArrayList<PreppedTriangle>();
		this.boundingBox = new BoundingBox();
		
		for( int i=0 ; i<bot.getFaceCount() ; i++ )
		{
			try
			{
				// prep each triangle in the Bot
				PreppedTriangle pt = new PreppedTriangle( this, bot, i, matrix );
				this.boundingBox.extend( pt.getBoundingBox() );
				this.triangles.add( pt );
			}
			catch (BadGeometryException e)
			{
				// degenerate triangle, ignore it
				continue;
			}
		}
	}
	
	public List<PreppedTriangle> getTriangles()
	{
		return this.triangles;
	}
	
	public int getTriangleCount()
	{
		return this.triangles.size();
	}
	
	/**
	 * Intersect this object with the specified Ray
	 *
	 * @param    ray                 a  Ray
	 *
	 * @return   number of hits
	 *
	 */
	public List<Segment> shoot(Ray ray, RayData rayData)
	{
		Set<Hit> hits = new TreeSet<Hit>();
		
		// intersect with each triangle of the Bot
		for( PreppedTriangle tri : this.triangles )
		{
			Hit hit = tri.shoot( ray, rayData );
			if( hit != null )
			{
				hits.add( hit );
			}
		}
		
		if( hits.size() < 1 )
		{
			return null;
		}
		
		List<Segment> segs =  this.makeSegs( hits, ray, rayData );
        rayData.addSegs(this, segs);

        return segs;
	}
	
	/**
	 * Convert the list of hit points for this Bot into a list of Segments
	 *
	 * @param    hits                a  List<Hit>
	 * @param    ray                 a  Ray
	 * @param    rayData             a  RayData
	 *
	 * @return   a  List<Segment>
	 */
	public List<Segment> makeSegs(Set<Hit> hits, Ray ray, RayData rayData)
	{
		if( hits.size() < 1 )
		{
			return null;
		}
		
		hits = this.removeDuplicateHits( hits, ray, rayData );
		
		List<Segment> segs = new ArrayList<Segment>();
		
		if( this.theBot.isSurface() )
		{
			// each hit is a segment
			for( Hit hit : hits )
			{
				Hit inHit;
				Hit outHit;
				if( hit.getHit_normal().dotProduct( ray.getDirection() ) > 0.0 )
				{
					// create a new in Hit wih the opposite normal
					outHit = hit;
					Vector3 inNorm = new Vector3( outHit.getHit_normal() );
					inNorm.negate();
					inHit = new Hit( outHit.getHit_dist(), outHit.getHit_pt(), inNorm,
									outHit.getHit_surfno(), rayData, this.name );
				}
				else
				{
					// create a new out Hit with the opposite normal
					inHit = hit;
					Vector3 outNorm = new Vector3( inHit.getHit_normal() );
					outNorm.negate();
					outHit = new Hit( inHit.getHit_dist(), inHit.getHit_pt(), outNorm,
									 inHit.getHit_surfno(), rayData, this.name );
				}
				segs.add( new Segment( inHit, outHit ));
			}
			
			return segs;
		}
		else if( this.theBot.isPlate() || this.theBot.isPlateNoCos() )
		{
			double los;
			boolean noCos = this.theBot.isPlateNoCos();
			
			Iterator<Hit> iter = hits.iterator();
			while( iter.hasNext() )
			{
				Hit hit = iter.next();
				los = this.theBot.getFaceThickness( hit.getHit_surfno() );
				// each hits is a segment
				if( !noCos )
				{
					los = los / hit.getHit_normal().dotProduct( ray.getDirection() );
					if( los < 0.0 ) los = -los;
				}

				// get the in Hit normal pointed in the correct direction
				if( hit.getHit_normal().dotProduct( ray.getDirection() ) > 0.0 )
				{
					hit.getHit_normal().negate();
				}
					
				if( this.theBot.isFaceThicknessAppendedtoHit( hit.getHit_surfno() ) )
				{
					// create an out Hit at a distnce (los) further along the ray
					Vector3 outNorm = new Vector3( hit.getHit_normal() );
					outNorm.negate();
					double outDist = hit.getHit_dist() + los;
					Point hitPoint = new Point( ray.getStart() );
					hitPoint.join( outDist, ray.getDirection() );
					Hit outHit = new Hit( outDist, hitPoint, outNorm, hit.getHit_surfno(), rayData, this.name );
					
					Segment seg = new Segment( hit, outHit );
					segs.add( seg );
				}
				else
				{
					// move the in Hit a distance (los/2) back along the ray
					hit.setHit_dist( hit.getHit_dist() - los / 2.0 );
					hit.getHit_pt().join( los/2.0, ray.getDirection() );
					
					// create another Hit (los/2) forward along the ray
					double outDist = hit.getHit_dist() + los;
					Point outPoint = new Point( ray.getStart() );
					outPoint.join( outDist, ray.getDirection() );
					Vector3 outNorm = new Vector3( hit.getHit_normal() );
					outNorm.negate();
					Hit outHit = new Hit( outDist, outPoint, outNorm, hit.getHit_surfno(), rayData, this.name );
					
					Segment seg = new Segment( hit, outHit );
					segs.add( seg );
				}
			}
			return segs;
		}
		else if( this.theBot.isUnOriented() )
		{
			// solid Bot, unoriented faces, each pair of hits is a segment
			Iterator<Hit> iter = hits.iterator();
			
			while( iter.hasNext() )
			{
				Hit first = iter.next();
				Hit second = null;
				
				if( iter.hasNext() )
				{
					second = iter.next();
					if( first.getHit_normal().dotProduct(ray.getDirection()) > 0.0 )
					{
						first.getHit_normal().negate();
					}
					if( second.getHit_normal().dotProduct(ray.getDirection()) < 0.0 )
					{
						second.getHit_normal().negate();
					}
					
					Segment seg = new Segment( first, second );
					segs.add( seg );
				}
			}
			return segs;
		}
		else
		{
			if( hits.size() < 2 )
			{
				return null;
			}
			
			boolean done = false;
			
			Iterator<Hit> iter = hits.iterator();
			
			while( !done )
			{
				Hit inHit = null;
				Hit outHit = null;
				
				// find first entrance
				if( !iter.hasNext() ) break;
				while( iter.hasNext() )
				{
					inHit = iter.next();
					if( inHit.getHit_normal().dotProduct( ray.getDirection() ) < 0.0 )
					{
						// found enter hit
						break;
					}
				}
				
				// find first exit
				if( !iter.hasNext() ) break;
				while( iter.hasNext() )
				{
					outHit = iter.next();
					if( outHit.getHit_normal().dotProduct( ray.getDirection() ) > 0.0 )
					{
						// found exit hit
						break;
					}
				}
				
				if( inHit == null || outHit == null )
				{
					break;
				}
				
				Segment seg = new Segment( inHit, outHit );
				segs.add( seg );
			}
			return segs;
		}
	}
	
	/**
	 * Method removeDuplicateHits
	 *
	 * @param    hits                a  Set<Hit>
	 *
	 * @return   a  Set<Hit>
	 */
	private Set<Hit> removeDuplicateHits(Set<Hit> hits, Ray ray, RayData rayData)
	{
		Iterator<Hit> iter = hits.iterator();
		
		if( !iter.hasNext() ) return hits;
		Hit prev = iter.next();
		double prevDn = prev.getHit_normal().dotProduct( ray.getDirection() );
		while( iter.hasNext() )
		{
			Hit curr = iter.next();
			double currDn = curr.getHit_normal().dotProduct( ray.getDirection() );
			if( (curr.getHit_dist() - prev.getHit_dist()) < rayData.getTolerance().getDist() )
			{
				// prev and curr are at the same hit distance
				
				if( Math.signum(prevDn) == Math.signum(currDn) )
				{
					// prev and curr are both entrances or both exits
					iter.remove();
					continue;
				}
			}
			prev = curr;
		}
		
		return hits;
	}

    /**
     * @return the theBot
     */
    public Bot getTheBot() {
        return theBot;
    }
}

