package org.brlcad.preppedGeometry;
/**
 * base class for prepped objects
 */




import org.brlcad.numerics.*;import org.brlcad.geometry.Hit;
import org.brlcad.geometry.Segment;

import java.util.List;
import java.util.Set;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.BoundingBox;
import org.brlcad.spacePartition.RayData;
import java.util.HashSet;

public abstract class PreppedObject
{
	/** name of the primitive this was produced from */
	protected String name;
	
	/** center point of the bounding radius of this object */
	protected Point center;
	
	/** the bounding radius of this object */
	protected double boundingRadius;
	
	/** bounding box that contins this object */
	protected BoundingBox boundingBox;
	
	/** List of Regions that involve this object */
	protected Set<PreppedCombination> regions;
	
	/** index into PreppedDb's bit vector for this PreppedObject */
	protected int index;
	
	protected PreppedObject( String name )
	{
		this.name = name;
		this.regions = new HashSet<PreppedCombination>();
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
	 * Add a PreppedCombination to the list of regions that contain this object
	 * @param region	The region to be added
	 */
	public void addRegion( PreppedCombination region )
	{
		this.regions.add( region );
	}
	
	/**
	 * Get the list of regions that contain this object
	 * @return	The list of regions
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
	public abstract List<Segment> shoot( Ray ray, RayData rayData );

	/**
	 * Method makeSegs
	 *
	 * @param    get                 a  Set<Hit>
	 *
	 * @return   a  List<Segment>
	 */
	public abstract List<Segment> makeSegs(Set<Hit> hits, Ray ray, RayData rayData);
	
}


