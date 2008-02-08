
/*
* Copyright (C) 2001 De Montfort University, All Rights Reserved.
*
* De Montfort University grants to you ("Licensee") a non-exclusive,
* non-transferable, royalty free, license to use, copy, and modify
* this software and its documentation. Licensee may redistribute
* the software in source and binary code form provided that this
* copyright notice appears in all copies.
*
* DE MONTFORT UNIVERSITY MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT
* THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING
* BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
* FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. THE AUTHORS SHALL NOT
* BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
* MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
*/


/**
 * Plane in 3D space
 *
 * The plane is represented by 4 parameters: a, b, c, d.  The first three parameters define a normal to the plane
 * and the third is the distance along that normal from the origin to the plane.  The plane is defined as all the points (p)
 * satisfying the equation: VDOT( normal, p) - d = 0.  If VDOT( normal, p) - d is greater than zero, then the point is "above"
 * the plane.  If VDOT( normal, p ) - d is less than zero, then the point is "below" the plane.
 *
 * @version    1.0 March 2001
 * @author     Paul Cropper
 *
 * Corrected by jra to implement the plane equation correctly
 *
 * Acquired from http://www.iesd.dmu.ac.uk/~pcc/dls/docs/intro.htm
 */

/*
 * $Header
 */

package numerics;

public class Plane3D
{
    private double a;
    private double b;
    private double c;
    private double d;


  /**
   * Construct a plane, with the supplied normal, and passing
   * through the origin.
   *
   * @param norm vector normal to the plane
   */
    public Plane3D (Vector3 norm)
    {
	a = norm.getX ();
	b = norm.getY ();
	c = norm.getZ ();
	d = 0;
    }



  /**
   * Construct a plane, with the supplied normal, and passing
   * through the supplied point.
   *
   * @param norm vector normal to the plane
   * @param pt point in the plane
   */
    public Plane3D (Vector3 norm, Point pt)
    {
	a = norm.getX ();
	b = norm.getY ();
	c = norm.getZ ();
	d = (a * pt.getX ()) + (b * pt.getY ()) + (c * pt.getZ ());
    }



  /**
   * Construct plane in which the three supplied points lie
   *
   * @param p1 point 1
   * @param p2 point 2
   * @param p3 point 3
   */
    public Plane3D (Point p1, Point p2, Point p3)
    {
	Vector3 norm;

	norm =
	    Vector3.crossProduct (Vector3.minus (p1, p2),
				  Vector3.minus (p1, p3));

	norm.normalize ();

	a = norm.getX ();
	b = norm.getY ();
	c = norm.getZ ();
	d = (a * p1.getX ()) + (b * p1.getY ()) + (c * p1.getZ ());
    }



  /**
   * Does the point pt lie above the plane ?
   *
   * @param pt the point to be tested
   * @return true if above
   */
    public boolean above (Point pt)
    {
	double result;

	result = (a * pt.getX ()) + (b * pt.getY ()) + (c * pt.getZ ()) - d;

	return (result > 0.0f);
    }



  /**
   * does the point pt lie below the plane ?
   *
   * @param pt the point to be tested
   * @return true if below
   */
    public boolean below (Point pt)
    {
	double result;

	result = (a * pt.getX ()) + (b * pt.getY ()) + (c * pt.getZ ()) - d;

	return (result < 0.0f);
    }



  /**
   * Does the point pt lie in the plane ?
   *
   * @param pt the point to be tested
   * @return true in in the plane
   */
    public boolean liesIn (Point pt)
    {
	double result;

	result = (a * pt.getX ()) + (b * pt.getY ()) + (c * pt.getZ ()) - d;

	return (result == 0.0f);
    }
	
  /**
   * Does the point pt lie in the plane (within tolerance)?
   *
   * @param pt the point to be tested
	 * @param tolerance
   * @return true in in the plane
   */
    public boolean liesIn (Point pt, double tolerance)
    {
	double result;

	result = (a * pt.getX ()) + (b * pt.getY ()) + (c * pt.getZ ()) - d;

	return (Math.abs( result ) < tolerance);
    }
	
	public double distToPlane( Point pt )
	{
		return (a * pt.getX ()) + (b * pt.getY ()) + (c * pt.getZ ()) - d;
	}



  /**
   * Get normal to the plane
   *
   * @return normal vector
   */
    public Vector3 getNormal ()
    {
	return new Vector3 (a, b, c);
    }

    public String toString ()
    {
	return "( " + a + ", " + b + ", " + c + ", " + d + " )";
    }
}