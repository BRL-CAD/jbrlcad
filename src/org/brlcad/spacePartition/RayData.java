/**
 * RayData.java
 *
 * @author Created by Omnicore CodeGuide
 */

package org.brlcad.spacePartition;
import org.brlcad.geometry.Hit;
import org.brlcad.geometry.Segment;
import org.brlcad.preppedGeometry.PreppedObject;
import org.brlcad.preppedGeometry.PreppedCombination;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.brlcad.numerics.Point;
import org.brlcad.numerics.Ray;
import org.brlcad.numerics.Tolerance;



public class RayData
{
	private Ray theRay;
	private Point locator;
	private double dist;
	private BitSet solidBits;
	private BitSet regbits;
	private Tolerance tolerance;
	private Map<PreppedObject,List<Segment>> segs;
	private Map<PreppedObject,Set<Hit>> hits;
	
	public RayData( Point pt, double d, BitSet solidBits, BitSet regbits, Tolerance tol, Ray ray )
	{
		this.locator = pt;
		this.dist = d;
		this.solidBits = solidBits;
		this.regbits = regbits;
		this.tolerance = tol;
		this.theRay = ray;
		this.segs = new HashMap<PreppedObject,List<Segment>>();
		this.hits = new HashMap<PreppedObject,Set<Hit>>();
	}
	
	/**
	 * Method addHits
	 *
	 * @param    preppedObject       a  PreppedObject
	 * @param    hits                a  Set<Hit>
	 *
	 */
	public void addHits(PreppedObject preppedObject, Set<Hit> hits)
	{
		Set<Hit> objHits = this.hits.get( preppedObject );
		
		if( objHits == null )
		{
			this.hits.put( preppedObject, hits );
		}
		else
		{
			objHits.addAll( hits );
		}
		if( ! (preppedObject instanceof PreppedCombination) )
		{
			for( PreppedCombination reg:preppedObject.getRegions() )
			{
				this.regbits.set( reg.getIndex() );
			}
		}
	}
	
	public void makeSegs()
	{
		Set<PreppedObject> pos = this.hits.keySet();
		
		for( PreppedObject obj : pos )
		{
			List<Segment> segs = obj.makeSegs( this.hits.get( obj ), this.theRay, this );
			this.addSegs( obj, segs );
		}
	}
	
	/**
	 * Sets TheRay
	 *
	 * @param    TheRay              a  Ray
	 */
	public void setTheRay(Ray theRay)
	{
		this.theRay = theRay;
	}
	
	/**
	 * Returns TheRay
	 *
	 * @return    a  Ray
	 */
	public Ray getTheRay()
	{
		return theRay;
	}
	
	/**
	 * Sets Tolerance
	 *
	 * @param    Tolerance           a  Tolerance
	 */
	public void setTolerance(Tolerance tolerance)
	{
		this.tolerance = tolerance;
	}
	
	/**
	 * Returns Tolerance
	 *
	 * @return    a  Tolerance
	 */
	public Tolerance getTolerance()
	{
		return tolerance;
	}
	
	public synchronized void setBit( int i )
	{
		this.solidBits.set( i );
	}
	
	public synchronized boolean getBit( int i )
	{
		return this.solidBits.get( i );
	}
	
	public synchronized void addSegs( PreppedObject obj, List<Segment> segs )
	{
		if( ! (obj instanceof PreppedCombination) )
		{
			for( PreppedCombination reg:obj.getRegions() )
			{
				this.regbits.set( reg.getIndex() );
			}
		}
		this.segs.put( obj, segs );
	}
	
	public synchronized List<Segment> getSegs( PreppedObject obj )
	{
		return this.segs.get( obj );
	}
	
	/**
	 * Sets Locator
	 *
	 * @param    Locator             a  Point
	 */
	public void setLocator(Point locator)
	{
		this.locator = locator;
	}
	
	/**
	 * Returns Locator
	 *
	 * @return    a  Point
	 */
	public Point getLocator()
	{
		return locator;
	}
	
	/**
	 * Sets Dist
	 *
	 * @param    Dist                a  double
	 */
	public void setDist(double dist)
	{
		this.dist = dist;
	}
	
	/**
	 * Returns Dist
	 *
	 * @return    a  double
	 */
	public double getDist()
	{
		return dist;
	}
}

