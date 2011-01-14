package org.brlcad.preppedGeometry;
/**
 * Base class for pieces
 */



import org.brlcad.geometry.Hit;

import java.util.HashSet;
import java.util.Set;

import org.brlcad.numerics.BoundingBox;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Ray;
import org.brlcad.spacePartition.RayData;

public abstract class PreppedObjectPiece
{
	/** name of the primitive this was produced from */
	protected String name;
	
	protected Point center;
	
	protected double boundingRadius;
	
	protected BoundingBox boundingBox;
	
	/** List of Regions that involve this object */
	protected Set<PreppedCombination> regions;
	
	/** index into PreppedDb's bit vector for this PreppedObject */
	protected int index;
	
	private PreppedObject preppedObject;
	
	protected PreppedObjectPiece( String name, PreppedObject obj )
	{
		this.name = name;
		this.preppedObject = obj;
		this.regions = new HashSet<PreppedCombination>();
		this.boundingBox = new BoundingBox();
	}
	
	public PreppedObject getPreppedObject()
	{
		return this.preppedObject;
	}
	
	/**
	 * Sets Name
	 *
	 * @param    Name                a  String
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * Returns Name
	 *
	 * @return    a  String
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Sets Index
	 *
	 * @param    Index               an int
	 */
	public void setIndex(int index)
	{
		this.index = index;
	}
	
	/**
	 * Returns Index
	 *
	 * @return    an int
	 */
	public int getIndex()
	{
		return index;
	}
	
	/**
	 * Sets Center
	 *
	 * @param    Center              a  Point
	 */
	public void setCenter(Point center)
	{
		this.center = new Point( center );
	}
	
	/**
	 * Returns Center
	 *
	 * @return    a  Point
	 */
	public Point getCenter()
	{
		return center;
	}
	
	/**
	 * Sets BoundingRadius
	 *
	 * @param    BoundingRadius      a  double
	 */
	public void setBoundingRadius(double boundingRadius)
	{
		this.boundingRadius = boundingRadius;
	}
	
	/**
	 * Returns BoundingRadius
	 *
	 * @return    a  double
	 */
	public double getBoundingRadius()
	{
		return boundingRadius;
	}
	
	/**
	 * Sets BoundingBox
	 *
	 * @param    BoundingBox         a  BoundingBox
	 */
	public void setBoundingBox(BoundingBox boundingBox)
	{
		this.boundingBox = new BoundingBox( boundingBox );
	}
	
	/**
	 * Returns BoundingBox
	 *
	 * @return    a  BoundingBox
	 */
	public BoundingBox getBoundingBox()
	{
		return boundingBox;
	}
	
	/**
	 * Add a region to this objects list of regions
	 * @param region	A region that contains this object
	 */
	public void addRegion( PreppedCombination region )
	{
		this.regions.add( region );
	}
	
	/**
	 * Get the set of PreppedRegions that contain this piece
	 * @return	A set of PreppedRegions
	 */
	public Set<PreppedCombination> getRegions()
	{
		return this.regions;
	}
	/**
	 * Intersect this object with the specified Ray
	 *
	 * @param    ray                 a  Ray
	 *
	 * @return   number of hits
	 *
	 */
	public abstract Set<Hit> shoot( Ray ray, RayData rayData );
}

