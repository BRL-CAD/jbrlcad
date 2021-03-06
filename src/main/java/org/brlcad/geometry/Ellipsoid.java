package org.brlcad.geometry;
/**
 * This class provides a representation of a BRL-CAD ellipsoid
 *
 * @author Created by Omnicore CodeGuide
 */

import org.brlcad.preppedGeometry.PreppedCombination;
import org.brlcad.preppedGeometry.PreppedEllipsoid;
import org.brlcad.numerics.Matrix;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Vector3;
import org.brlcad.spacePartition.PreppedDb;

public class Ellipsoid extends DbObject
{
	// geometry of the ellipsoid
	private Point center;
	private Vector3 a;
	private Vector3 b;
	private Vector3 c;
	
	// the major and minor types for an Ellipsoid
	public static final byte majorType = 1;
	public static final byte minorType = 3;
	
	/**
	 * Constructs an Ellipsoid object using a DbExternal object
	 *
	 * @param    dbExt               a  DbExternal
	 *
	 * @exception   DbException
	 *
	 */
	public Ellipsoid( DbExternal dbExt ) throws DbException
	{
		super( dbExt );
		
		if( dbExt.getMajorType() != Ellipsoid.majorType || dbExt.getMinorType() != Ellipsoid.minorType )
		{
			throw new DbException( "Attempted to import an Ellipsoid, but external is " +
								  " major type: " + dbExt.getMajorType() +
								  " minor type: " + dbExt.getMinorType() );
		}
		
		// create the geometry data objects
		this.center = new Point();
		this.a = new Vector3();
		this.b = new Vector3();
		this.c = new Vector3();
		
		// get the body bytes from the DbExternal object
		byte[] body = dbExt.getBody();
		
		// parse the body for the geometry data
		int pointer = 0;
		this.center.setX( BrlcadDb.getDouble( body, pointer ) );
		pointer += 8;
		this.center.setY( BrlcadDb.getDouble( body, pointer ) );
		pointer += 8;
		this.center.setZ( BrlcadDb.getDouble( body, pointer ) );
		pointer += 8;
		this.a.setX( BrlcadDb.getDouble( body, pointer ) );
		pointer += 8;
		this.a.setY( BrlcadDb.getDouble( body, pointer ) );
		pointer += 8;
		this.a.setZ( BrlcadDb.getDouble( body, pointer ) );
		pointer += 8;
		this.b.setX( BrlcadDb.getDouble( body, pointer ) );
		pointer += 8;
		this.b.setY( BrlcadDb.getDouble( body, pointer ) );
		pointer += 8;
		this.b.setZ( BrlcadDb.getDouble( body, pointer ) );
		pointer += 8;
		this.c.setX( BrlcadDb.getDouble( body, pointer ) );
		pointer += 8;
		this.c.setY( BrlcadDb.getDouble( body, pointer ) );
		pointer += 8;
		this.c.setZ( BrlcadDb.getDouble( body, pointer ) );
	}
	
	/**
	 * Sets Center
	 *
	 * @param    Center              a  Point
	 */
	public void setCenter(Point center)
	{
		this.center = center;
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
	 * Sets A
	 *
	 * @param    A                   a  Vector3
	 */
	public void setA(Vector3 a)
	{
		this.a = a;
	}
	
	/**
	 * Returns A
	 *
	 * @return    a  Vector3
	 */
	public Vector3 getA()
	{
		return a;
	}
	
	/**
	 * Sets B
	 *
	 * @param    B                   a  Vector3
	 */
	public void setB(Vector3 b)
	{
		this.b = b;
	}
	
	/**
	 * Returns B
	 *
	 * @return    a  Vector3
	 */
	public Vector3 getB()
	{
		return b;
	}
	
	/**
	 * Sets C
	 *
	 * @param    C                   a  Vector3
	 */
	public void setC(Vector3 c)
	{
		this.c = c;
	}
	
	/**
	 * Returns C
	 *
	 * @return    a  Vector3
	 */
	public Vector3 getC()
	{
		return c;
	}
	
    @Override
	public PreppedEllipsoid prep( PreppedCombination reg, PreppedDb preppedDb, Matrix matrix) throws BadGeometryException
	{
		PreppedEllipsoid prepped = new PreppedEllipsoid( this, matrix );
		preppedDb.addPreppedObjectToInitialBox( prepped );
		if( reg != null )
		{
			prepped.addRegion( reg );
		}
		return prepped;
	}
	
	/**
	 * Create a String representation of this Ellipsoid
	 *
	 * @return   a String
	 *
	 */
    @Override
	public String toString()
	{
		return super.toString() + " Ellipsoid, V=" + this.center +
			", a=" + this.a +
			", b=" + this.b +
			", c=" + this.c;
	}
}

