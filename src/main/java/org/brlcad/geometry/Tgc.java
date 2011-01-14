/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.geometry;

import org.brlcad.preppedGeometry.PreppedTgc;
import java.io.IOException;
import org.brlcad.numerics.Constants;
import org.brlcad.numerics.Matrix;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Vector3;
import org.brlcad.preppedGeometry.PreppedCombination;
import org.brlcad.preppedGeometry.PreppedObject;
import org.brlcad.preppedGeometry.PreppedRec;
import org.brlcad.spacePartition.PreppedDb;

/**
 *
 * @author jra
 */
public class Tgc extends DbObject {
    public static final int BODY = 1;
    public static final int TOP = 2;
    public static final int BOTTOM = 3;

	// geometry of the TGC

    /** center of the base **/
    private Point baseCenter;

    /** vector from center of base to center of top **/
    private Vector3	h;

    /** vector from center of base to edge of base (with vector b, defines elliptical base) **/
    private Vector3	a;

    /** vector from center of base to edge of base (with vector a, defines elliptical base) **/
    private Vector3	b;

    /** vector from center of top to edge of top (with vector d, defines elliptical top) **/
    private Vector3	c;

    /** vector from center of top to edge of top (with vector c, defines elliptical top) **/
    private Vector3	d;

	// the major and minor types for an Ellipsoid
	public static final byte majorType = 1;
	public static final byte minorType = 2;

	/**
	 * Constructs an Ellipsoid object using a DbExternal object
	 *
	 * @param    dbExt               a  DbExternal
	 *
	 * @exception   DbException
	 *
	 */
	public Tgc( DbExternal dbExt ) throws DbException
	{
		super( dbExt );

		if( dbExt.getMajorType() != Tgc.majorType || dbExt.getMinorType() != Tgc.minorType )
		{
			throw new DbException( "Attempted to import a TGC, but external is " +
								  " major type: " + dbExt.getMajorType() +
								  " minor type: " + dbExt.getMinorType() );
		}

		// create the geometry data objects
		this.baseCenter = new Point();
		this.h = new Vector3();
		this.a = new Vector3();
		this.b = new Vector3();
		this.c = new Vector3();
		this.d = new Vector3();

		// get the body bytes from the DbExternal object
		byte[] body = dbExt.getBody();

		// parse the body for the geometry data
		int pointer = 0;
		this.baseCenter.setX( BrlcadDb.getDouble( body, pointer ) );
		pointer += 8;
		this.baseCenter.setY( BrlcadDb.getDouble( body, pointer ) );
		pointer += 8;
		this.baseCenter.setZ( BrlcadDb.getDouble( body, pointer ) );
		pointer += 8;
		this.h.setX( BrlcadDb.getDouble( body, pointer ) );
		pointer += 8;
		this.h.setY( BrlcadDb.getDouble( body, pointer ) );
		pointer += 8;
		this.h.setZ( BrlcadDb.getDouble( body, pointer ) );
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
		pointer += 8;
		this.d.setX( BrlcadDb.getDouble( body, pointer ) );
		pointer += 8;
		this.d.setY( BrlcadDb.getDouble( body, pointer ) );
		pointer += 8;
		this.d.setZ( BrlcadDb.getDouble( body, pointer ) );
	}

    /**
     * @return the baseCenter
     */
    public Point getBaseCenter() {
        return baseCenter;
    }

    /**
     * @param baseCenter the baseCenter to set
     */
    public void setBaseCenter(Point baseCenter) {
        this.baseCenter = baseCenter;
    }

    /**
     * @return the h
     */
    public Vector3 getH() {
        return h;
    }

    /**
     * @param h the h to set
     */
    public void setH(Vector3 h) {
        this.h = h;
    }

    /**
     * @return the a
     */
    public Vector3 getA() {
        return a;
    }

    /**
     * @param a the a to set
     */
    public void setA(Vector3 a) {
        this.a = a;
    }

    /**
     * @return the b
     */
    public Vector3 getB() {
        return b;
    }

    /**
     * @param b the b to set
     */
    public void setB(Vector3 b) {
        this.b = b;
    }

    /**
     * @return the c
     */
    public Vector3 getC() {
        return c;
    }

    /**
     * @param c the c to set
     */
    public void setC(Vector3 c) {
        this.c = c;
    }

    /**
     * @return the d
     */
    public Vector3 getD() {
        return d;
    }

    /**
     * @param d the d to set
     */
    public void setD(Vector3 d) {
        this.d = d;
    }

    @Override
	public PreppedObject prep( PreppedCombination reg, PreppedDb preppedDb, Matrix matrix) throws BadGeometryException, DbException, IOException, DbNameNotFoundException
	{
        PreppedObject prepped = null;
        boolean isRec = true;

        Vector3 Hunit = new Vector3(this.h);
        Point V = new Point(this.baseCenter);
        Vector3 H = new Vector3(this.h);
        Vector3 A = new Vector3(this.a);
        Vector3 B = new Vector3(this.b);
        Vector3 C = new Vector3(this.c);
        Vector3 D = new Vector3(this.d);
        matrix.mult(V);
        matrix.mult(H);
        matrix.mult(A);
        matrix.mult(B);
        matrix.mult(C);
        matrix.mult(D);
        matrix.mult(Hunit);

        // check if this is an Rec
        /* Validate that |H| > 0, compute |A| |B| |C| |D| */
        double mag_h = Hunit.magnitude();
        double mag_a = A.magnitude();
        double mag_b = B.magnitude();
        double mag_c = C.magnitude();
        double mag_d = D.magnitude();
        Hunit.scale(1.0/mag_h);

        /* Check for |H| > 0, |A| > 0, |B| > 0 */
        if (mag_h < Constants.RT_LEN_TOL ||
                mag_a < Constants.RT_LEN_TOL ||
                mag_b < Constants.RT_LEN_TOL) {
            isRec = false;		/* BAD, too small */
        }

        if( isRec ) {
            /* Make sure that A == C, B == D */
            Vector3 work = Vector3.minus(this.a, this.c);
            double f = work.magnitude();
            if (!(f < Constants.RT_LEN_TOL)) {
                isRec = false;		/* BAD, !cylinder */
            }
        }
        if( isRec ) {
            Vector3 work = Vector3.minus(this.b, this.d);
            double f = work.magnitude();
            if (!(f < Constants.RT_LEN_TOL)) {
                isRec = false;		/* BAD, !cylinder */
            }
        }

        if( isRec ) {
        /* Check for A.B == 0, H.A == 0 and H.B == 0 */
            double f = this.a.dotProduct(this.b) / (mag_a * mag_b);
            if (!(f < Constants.RT_DOT_TOL)) {
                isRec = false;		/* BAD */
            }
            f = this.h.dotProduct(this.a) / (mag_h * mag_a);
            if (!(f < Constants.RT_DOT_TOL)) {
                isRec = false;		/* BAD */
            }
            f = this.h.dotProduct(this.b) / (mag_h * mag_b);
            if (!(f < Constants.RT_DOT_TOL)) {
                isRec = false;		/* BAD */
            }
        }

        if( isRec ) {
            prepped = new PreppedRec(this, matrix, V, A, B, Hunit, mag_a, mag_b, mag_c, mag_d, mag_h);
        } else {
            prepped = new PreppedTgc(this, matrix, V, H, A, B, C, D, Hunit, mag_a, mag_b, mag_c, mag_d, mag_h);
        }
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
		return super.toString() + " TGC, V=" + this.baseCenter +
			", h=" + this.h +
			", a=" + this.a +
			", b=" + this.b +
			", c=" + this.c +
			", d=" + this.d;
	}

}
