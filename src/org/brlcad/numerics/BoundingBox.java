
/**
 * Represents the bounding box for an object
 */

package org.brlcad.numerics;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;



public class BoundingBox implements Serializable
{
	// the minimum point of this bounding box
    private Point min;
    
    // the mximum point of this bounding box
    private Point max;
	
    /**
     * Empty constructor
     *
     */
	public BoundingBox()
	{
	}
	
	/**
	 * copy constructor
	 */
	public BoundingBox( BoundingBox bb )
	{
		this.min = new Point( bb.getMin() );
		this.max = new Point( bb.getMax() );
	}

    /**
     * Create a new bounding box. The two parameters are opposite corners of the
     * box.
     * @param a Point describing corner of the box, opposite b.
     * @param b Point describing corner of the box, opposite a.
     */
    public BoundingBox (Point a, Point b)
    {
	min =
	    new Point (Math.min (a.getX (), b.getX ()),
		       Math.min (a.getY (), b.getY ()), Math.min (a.getZ (),
							  b.getZ ()));
	max =
	    new Point (Math.max (a.getX (), b.getX ()),
		       Math.max (a.getY (), b.getY ()), Math.max (a.getZ (),
							  b.getZ ()));
    }

	
	/**
	 * Does this BoundingBox overlap another BoundingBox??
	 *
	 * @param    boundingBox         another  BoundingBox
	 *
	 * @return   True if BoundingBoxes overlap, false otherwise
	 */
	public boolean overlaps( BoundingBox bb )
	{
		if( this.min == null )
		{
			return false;
		}
		Point minOther = bb.getMin();
		Point maxOther = bb.getMax();
		if( minOther == null )
		{
			return false;
		}
		
		if( minOther.getX() > this.max.getX() ||
		    minOther.getY() > this.max.getY() ||
		    minOther.getZ() > this.max.getZ() )
		{
			return false;
		}

		if( maxOther.getX() < this.min.getX() ||
		    maxOther.getY() < this.min.getY() ||
		    maxOther.getZ() < this.min.getZ() )
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * @return Returns the max.
	 */
    public Point getMax ()
    {
	return max;
    }

	/**
	 * @param max The max to set.
	 */
    public void setMax (Point max)
    {
	this.max = new Point( max );
    }

	/**
	 * @return Returns the min.
	 */
    public Point getMin ()
    {
	return min;
    }

	/**
	 * @param min The min to set.
	 */
    public void setMin (Point min)
    {
	this.min = new Point( min );
    }

    /**
     * Test if the point is constrained inside of the bounding box. Points on
     * the surface are not considered to be 'bound'.
     * @param p Point to test.
     * @return true or false...
     */
    public boolean bound (Point p)
    {
	if (p.getX () < max.getX () && p.getX () > min.getX ()
	    && p.getY () < max.getY () && p.getY () > min.getY ()
	    && p.getZ () < max.getZ () && p.getZ () > min.getZ ())
	    return true;
	return false;
    }
	
    /**
     * Extend this BoundingBox to include a Point
     * @param p	The point to include
     */
	public void extend( Point p )
	{
		if( min == null )
		{
			min = new Point( p );
		}
		else
		{
			if( p.getX() < min.getX() )
			{
				min.setX( p.getX() );
			}
			if( p.getY() < min.getY() )
			{
				min.setY( p.getY() );
			}
			if( p.getZ() < min.getZ() )
			{
				min.setZ( p.getZ() );
			}
		}
		
		if( max == null )
		{
			max = new Point( p );
		}
		else
		{
			if( p.getX() > max.getX() )
			{
				max.setX( p.getX() );
			}
			if( p.getY() > max.getY() )
			{
				max.setY( p.getY() );
			}
			if( p.getZ() > max.getZ() )
			{
				max.setZ( p.getZ() );
			}
		}
	}
	
	/**
	 * Extend this BoundingBox to include another BoundingBox
	 * @param bb	The BoundingBox to include
	 */
	public void extend( BoundingBox bb )
	{
		if( this.min == null )
		{
			Point minOther = bb.getMin();
			Point maxOther = bb.getMax();
			if( minOther == null )
			{
				return;
			}
			this.min = new Point( minOther );
			this.max = new Point( maxOther );
			return;
		}
		Point bbMin = bb.getMin();
		Point bbMax = bb.getMax();
		
		if( bbMin.getX() < this.min.getX() )
		{
			this.min.setX( bbMin.getX() );
		}
		if( bbMin.getY() < this.min.getY() )
		{
			this.min.setY( bbMin.getY() );
		}
		if( bbMin.getZ() < this.min.getZ() )
		{
			this.min.setZ( bbMin.getZ() );
		}

		if( bbMax.getX() > this.max.getX() )
		{
			this.max.setX( bbMax.getX() );
		}
		if( bbMax.getY() > this.max.getY() )
		{
			this.max.setY( bbMax.getY() );
		}
		if( bbMax.getZ() > this.max.getZ() )
		{
			this.max.setZ( bbMax.getZ() );
		}
	}
	
	/**
	 * Intersect this BoundingBox with another. This BoundingBox is
	 * adjusted to represent the intersection. A null intersection is
	 * represented by null min and null max
	 * 
	 * @param bb	Another BoundingBox
	 */
	public void intersect( BoundingBox bb )
	{
		if( this.min == null )
		{
			return;
		}
		Point minOther = bb.getMin();
		Point maxOther = bb.getMax();
		if( minOther == null )
		{
			this.min = null;
			this.max = null;
			return;
		}
		
		if( minOther.getX() > this.max.getX() ||
		    minOther.getY() > this.max.getY() ||
		    minOther.getZ() > this.max.getZ() )
		{
			this.min = null;
			this.max = null;
			return;
		}

		if( maxOther.getX() < this.min.getX() ||
		    maxOther.getY() < this.min.getY() ||
		    maxOther.getZ() < this.min.getZ() )
		{
			this.min = null;
			this.max = null;
			return;
		}
		
		if( minOther.getX() > this.min.getX() )
		{
			this.min.setX( minOther.getX() );
		}
		if( minOther.getY() > this.min.getY() )
		{
			this.min.setY( minOther.getY() );
		}
		if( minOther.getZ() > this.min.getZ() )
		{
			this.min.setZ( minOther.getZ() );
		}
		if( maxOther.getX() < this.max.getX() )
		{
			this.max.setX( maxOther.getX() );
		}
		if( maxOther.getY() < this.max.getY() )
		{
			this.max.setY( maxOther.getY() );
		}
		if( maxOther.getZ() < this.max.getZ() )
		{
			this.max.setZ( maxOther.getZ() );
		}
	}
	
	/**
	 * Get the diameter of this BoundingBox
	 * @return	A vector from the minimum to the maximum of this BoundingBox
	 */
	public Vector3 getDiameter()
	{
		return this.max.subtract( this.min );
	}

	/**
	 * Method getExtentsInDirection - calculates the extent of the bounding box
	 * in the specified direction
	 *
	 * @param    dir                 a unit vector in the desired direction
	 *
	 * @return   a List of two Doubles, the <imum and maximum (in that order)
	 * of the extent of this bounding box along a vector in the specified direction
	 *
	 */
	public List<Double> getExtentsInDirection( Vector3 dir ) {
		if( this.min == null )
		{
			return null;
		}
		Double minExtent = new Double(Double.POSITIVE_INFINITY);
		Double maxExtent = new Double(Double.NEGATIVE_INFINITY);
		List<Double> ret = new ArrayList<Double>();

		// Check all 8 vertices of the bounding box

		// (xmin, ymin, zmin)
		Vector3 vt = new Vector3( this.min.getX(), this.min.getY(), this.min.getZ() );
		double dot = vt.dotProduct( dir );
		if( dot > maxExtent ) maxExtent = dot;
		if( dot < minExtent ) minExtent = dot;

		// (xmax, ymin, zmin)
		vt.setX( this.max.getX() );
		dot = vt.dotProduct( dir );
		if( dot > maxExtent ) maxExtent = dot;
		if( dot < minExtent ) minExtent = dot;

		// (xmax, ymax, zmin )
		vt.setY( this.max.getY() );
		dot = vt.dotProduct( dir );
		if( dot > maxExtent ) maxExtent = dot;
		if( dot < minExtent ) minExtent = dot;

		// (xmin, ymax, zmin )
		vt.setX( this.min.getX() );
		dot = vt.dotProduct( dir );
		if( dot > maxExtent ) maxExtent = dot;
		if( dot < minExtent ) minExtent = dot;

		// (xmin, ymax, zmax )
		vt.setZ( this.max.getZ() );
		dot = vt.dotProduct( dir );
		if( dot > maxExtent ) maxExtent = dot;
		if( dot < minExtent ) minExtent = dot;

		// (xmax, ymax, zmax )
		vt.setX( this.max.getX() );
		dot = vt.dotProduct( dir );
		if( dot > maxExtent ) maxExtent = dot;
		if( dot < minExtent ) minExtent = dot;

		// (xmax, ymin, zmax )
		vt.setY( this.min.getY() );
		dot = vt.dotProduct( dir );
		if( dot > maxExtent ) maxExtent = dot;
		if( dot < minExtent ) minExtent = dot;

		// (xmin, ymin, zmax )
		vt.setX( this.min.getX() );
		dot = vt.dotProduct( dir );
		if( dot > maxExtent ) maxExtent = dot;
		if( dot < minExtent ) minExtent = dot;

		ret.add( minExtent );
		ret.add( maxExtent );
		return ret;
	}
	
    @Override
	public String toString()
	{
		return "BoundingBox: min=" + this.min + ", max=" + this.max;
	}
}
