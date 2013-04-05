/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.geometry;

import java.io.IOException;
import org.brlcad.numerics.Matrix;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Vector3;
import org.brlcad.preppedGeometry.PreppedCombination;
import org.brlcad.preppedGeometry.PreppedTorus;
import org.brlcad.spacePartition.PreppedDb;

/**
 *
 * @author jra
 */
public class Torus extends DbObject {
    public static final byte majorType = 1;
    public static final byte minorType = 1;

    private Point	center;		/**< @brief  center point */
    private Vector3	normal;		/**< @brief  normal, unit length */
    private double	rNormal;		/**< @brief  radius in normal direction (r2) */
    private double	r1;		/**< @brief  radius in direction perpendicular to normal (r1) */

	/**
	 * Constructs an Torus object using a DbExternal object
	 *
	 * @param    dbExt               a  DbExternal
	 *
	 * @exception   DbException
	 *
	 */
	public Torus( DbExternal dbExt ) throws DbException
	{
		super( dbExt );

		if( dbExt.getMajorType() != Torus.majorType || dbExt.getMinorType() != Torus.minorType ) {
			throw new DbException( "Attempted to import a Torus, but external is " +
								  " major type: " + dbExt.getMajorType() +
								  " minor type: " + dbExt.getMinorType() );
        }
        
        center = new Point();
        normal = new Vector3();
        
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
		this.normal.setX( BrlcadDb.getDouble( body, pointer ) );
        pointer += 8;
		this.normal.setY( BrlcadDb.getDouble( body, pointer ) );
        pointer += 8;
		this.normal.setZ( BrlcadDb.getDouble( body, pointer ) );
        pointer += 8;
		this.r1 =  BrlcadDb.getDouble( body, pointer );
        pointer += 8;
		this.rNormal =  BrlcadDb.getDouble( body, pointer );
    }

    @Override
    public PreppedTorus prep(PreppedCombination reg, PreppedDb preppedDb, Matrix matrix) throws BadGeometryException, DbException, IOException, DbNameNotFoundException {
		PreppedTorus prepped = new PreppedTorus( this, matrix );
		preppedDb.addPreppedObjectToInitialBox( prepped );
		if( reg != null )
		{
			prepped.addRegion( reg );
		}
		return prepped;
    }

	/**
	 * Create a String representation of this Torus
	 *
	 * @return   a String
	 *
	 */
    @Override
	public String toString()
	{
		return super.toString() + " Torus, center=" + this.center +
			", normal=" + this.normal +
			", r1=" + this.r1 +
			", r2=" + this.rNormal;
	}

    /**
     * @return the center
     */
    public Point getCenter() {
        return center;
    }

    /**
     * @param center the center to set
     */
    public void setCenter(Point center) {
        this.center = center;
    }

    /**
     * @return the normal
     */
    public Vector3 getNormal() {
        return normal;
    }

    /**
     * @param normal the normal to set
     */
    public void setNormal(Vector3 normal) {
        this.normal = normal;
    }

    /**
     * @return the rNormal
     */
    public double getRNormal() {
        return rNormal;
    }

    /**
     * @param rNormal the rNormal to set
     */
    public void setRNormal(double rNormal) {
        this.rNormal = rNormal;
    }

    /**
     * @return the r1
     */
    public double getR1() {
        return r1;
    }

    /**
     * @param r1 the r1 to set
     */
    public void setR1(double r1) {
        this.r1 = r1;
    }

}
