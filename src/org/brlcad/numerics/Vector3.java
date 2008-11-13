
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

/*
 * $Header$
 */


package org.brlcad.numerics;
import jade.physics.Angle;
import jade.units.SI;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;


/**
 * Vector in 3-space from point 0,0,0
 *
 * @version    1.0 March 2001
 * @author     Paul Cropper
 *
 * Acquired from http://www.iesd.dmu.ac.uk/~pcc/dls/docs/intro.htm
 */

public class Vector3 implements Serializable
{
    private double tolerance = 0.00000000001;
    protected double x = 0.0;
    protected double y = 0.0;
    protected double z = 0.0;

    protected double mag = 0.0;

    private static final double PI_BY_2 = Math.PI / 2.0;


	/**
	 * Generate a new vector, magnitude zero
	 *
	 */
    public Vector3 ()
    {
    }


	/**
	 * Generate new vector with supplied axis values
	 *
	 * @param x1 X value
	 * @param y1 Y value
	 * @param z1 Z value
	 */
    public Vector3 (double x1, double y1, double z1)
    {
	x = x1;
	y = y1;
	z = z1;

	findMagnitude ();
    }



	/**
	 * Generate new vector with values copied from supplied vector
	 *
	 * @param v vector to copy
	 */
    public Vector3 (Vector3 v)
    {
	x = v.x;
	y = v.y;
	z = v.z;

	findMagnitude ();
    }



	/**
	 * Generate new vector with magnitude 1.0
	 *
	 * @param theta angle from Z axis in Radians
	 * @param phi angle from X axis in Radians
	 */
    public Vector3 (double theta, double phi)
    {
	double th = theta;
	double p = phi;

	if (p >= (Math.PI + PI_BY_2))
	  {
	      p = p - (Math.PI + PI_BY_2);
	      x = Math.sin (th) * Math.sin (p);
	      y = -(Math.sin (th) * Math.cos (p));
	      z = Math.cos (th);
	  }
	else if (p >= Math.PI)
	  {
	      p = p - Math.PI;
	      x = -(Math.sin (th) * Math.cos (p));
	      y = -(Math.sin (th) * Math.sin (p));
	      z = Math.cos (th);
	  }
	else if (p >= PI_BY_2)
	  {
	      p = p - PI_BY_2;
	      x = -(Math.sin (th) * Math.sin (p));
	      y = Math.sin (th) * Math.cos (p);
	      z = Math.cos (th);
	  }
	else
	  {
	      x = Math.sin (th) * Math.cos (p);
	      y = Math.sin (th) * Math.sin (p);
	      z = Math.cos (th);
	  }

	normalize ();
    }
	
	/**
	 * Construct a direction vector for firing a threat from the indicated azimuth, elevation Angles
	 *
	 * @param    azimuth             an Angle
	 * @param    elevation           an Angle
	 *
	 */
	public Vector3( Angle azimuth, Angle elevation) {
		this(Math.PI/2.0 - elevation.doubleValue(), azimuth.doubleValue() );
	}
	
	/**
	 * Construct a direction vector for firing a threat in the direction indicated
	 * by the specified yaw, pitch, and roll angles, note that the roll angle is ignored
	 *
	 * @param    yaw                 an Angle
	 * @param    pitch               an Angle
	 * @param    roll                an Angle
	 *
	 */
	public Vector3( Angle yaw, Angle pitch, Angle roll )
	{
		this( (Angle)yaw.negate(), (Angle)pitch.negate() );
	}



	/**
	 * Read x,y,z coordinated from a DataInput stream
	 *
	 * @param in DataInput stream
	 */
    public Vector3 (DataInput in) throws IOException
    {
	x = in.readDouble ();
	y = in.readDouble ();
	z = in.readDouble ();

	findMagnitude ();
    }
	
	/**
	 * Sets X
	 *
	 * @param    x                   a  double
	 */
	public void setX(double x)
	{
		this.x = x;
		findMagnitude ();
	}
	
	/**
	 * Sets Y
	 *
	 * @param    y                   a  double
	 */
	public void setY(double y)
	{
		this.y = y;
		findMagnitude ();
	}
	
	/**
	 * Sets Z
	 *
	 * @param    z                   a  double
	 */
	public void setZ(double z)
	{
		this.z = z;
		findMagnitude ();
	}




    private void findMagnitude ()
    {
	mag = Math.sqrt ((x * x) + (y * y) + (z * z));
    }





	/**
	 * Get the X coordinate relative to 0,0,0
	 *
	 * @return X value
	 */
    public double getX ()
    {
	return x;
    }



	/**
	 * Get the Y coordinate relative to 0,0,0
	 *
	 * @return Y value
	 */
    public double getY ()
    {
	return y;
    }



	/**
	 * Get the Z coordinate relative to 0,0,0
	 *
	 * @return Z value
	 */
    public double getZ ()
    {
	return z;
    }
	
	public double get( int index )
	{
		switch( index )
		{
			case 0:
				return x;
			case 1:
				return y;
			case 2:
				return z;
		}
		
		return 0.0;
	}
	
	public void set( int index, double val )
	{
		switch( index )
		{
			case 0:
				x = val;
				break;
			case 1:
				y = val;
				break;
			case 2:
				z = val;
				break;
		}
		findMagnitude ();
	}


	/**
	 * Is the magnitude of the vector non-zero
	 *
	 * @return true if non-zero
	 */
    public boolean isNonZero ()
    {
	return (mag != 0.0);
    }

    /**
     * Set the tolernace for isEqual().
     * @param t tolerance.
     */
    public void setTolerance (double t)
    {
	tolerance = t;
    }

    /**
     * Get the tolerance for isEqual().
     * @return The tolerance.
     */
    public double getTolerance ()
    {
	return tolerance;
    }

	/**
	 * Is this vector equal to the other
	 *
	 * @return true if equal
	 */
    public boolean isEqual (Vector3 other)
    {
	return ((java.lang.Math.abs (x - other.getX ()) < tolerance)
		&& (java.lang.Math.abs (y - other.getY ()) < tolerance)
		&& (java.lang.Math.abs (z - other.getZ ()) < tolerance));
    }

	/**
	 * Test if the two vectors are equal.
	 * @param v1 One vector.
	 * @param v2 The other vector.
	 * @return If they're equal or not (within tolerance).
	 */
    public static boolean isEqual (Vector3 v1, Vector3 v2)
    {
	return v1.isEqual (v2);
    }

	/**
	 * Test if the two vectors have the same direction. Duplicates both
	 * vectors and normalizes them, then calls the normal isEqual() on
	 * those.
	 * @param other Vector to compare against.
	 * @return If they're in the same direction or not (within tolerance).
	 */
    public boolean isEqualDir (Vector3 other)
    {
	Vector3 v1 = new Vector3 (x, y, z);
	Vector3 v2 = new Vector3 (other);

	v1.normalize ();
	v2.normalize ();
	return isEqual (v1, v2);
    }

	/**
	 * Test if the two vectors have the same direction. Duplicates both
	 * vectors and normalizes them, then calls the normal isEqual() on
	 * those.
	 * @param v1 One vector.
	 * @param v2 The other vector.
	 * @return If they're in the same direction or not (within tolerance).
	 */
    public static boolean isEqualDir (Vector3 v1, Vector3 v2)
    {
	return v1.isEqualDir (v2);
    }

	/**
	 * Angle between the vector and Z axis
	 *
	 * @return angle in Radians
	 */
    public double getTheta ()
    {
	return Math.acos ((z / mag));
    }



	/**
	 * Cosine of the angle between the vector and Z axis
	 *
	 * @return Cosine of the angle
	 */
    public double getCosTheta ()
    {
	return (z / mag);
    }



	/**
	 * Angle between the vector and X axis, positive towards
	 * the Y axis.
	 *
	 * @return angle in Radians
	 */
    public double getPhi ()
    {
	double phi = 0.0;

	if ((x == 0.0) && (y == 0.0))
	  {
	      return 0.0;
	  }

	if ((x != 0.0) && (y != 0.0))
	  {
	      phi = Math.asin (Math.abs (y) / Math.sqrt ((x * x) + (y * y)));

	      if ((x < 0.0) && (y > 0.0))
		{
		    phi = Math.PI - phi;
		}
	      else if ((x < 0.0) && (y < 0.0))
		{
		    phi = Math.PI + phi;
		}
	      else if ((x > 0.0) && (y < 0.0))
		{
		    phi = (2.0 * Math.PI) - phi;
		}
	  }
	else
	  {
	      if ((x == 0.0) && (y > 0.0))
		{
		    phi = PI_BY_2;
		}
	      else if ((x == 0.0) && (y < 0.0))
		{
		    phi = Math.PI + PI_BY_2;
		}
	      else if ((x > 0.0) && (y == 0.0))
		{
		    phi = 0.0;
		}
	      else if ((x < 0.0) && (y == 0.0))
		{
		    phi = Math.PI;
		}
	  }

	return phi;
    }



	/**
	 * Update vector coordinates
	 *
	 * @param x1 new X value
	 * @param y1 new Y value
	 * @param z1 new Z value
	 */
    public void update (double x1, double y1, double z1)
    {
	x = x1;
	y = y1;
	z = z1;

	findMagnitude ();
    }



	/**
	 * Update vector coordinates
	 *
	 * @param v Vector3
	 */
    public void update (Vector3 v)
    {
	x = v.x;
	y = v.y;
	z = v.z;

	findMagnitude ();
    }



	/**
	 * Update vector coordinates
	 *
	 * @param in DataInput stream
	 */
    public void update (DataInput in) throws IOException
    {
	x = in.readDouble ();
	y = in.readDouble ();
	z = in.readDouble ();

	findMagnitude ();
    }



	/**
	 * Add vector
	 *
	 * @param v Vector3
	 */
    public void plus (Vector3 v)
    {
	x += v.x;
	y += v.y;
	z += v.z;

	findMagnitude ();
    }



	/**
	 * Subtract vector
	 *
	 * @param v Vector3
	 */
    public void minus (Vector3 v)
    {
	x -= v.x;
	y -= v.y;
	z -= v.z;

	findMagnitude ();
    }



	/**
	 * Get the magnitude of the vector
	 *
	 * @return magnitude
	 */
    public double magnitude ()
    {
	return mag;
    }



	/**
	 * Find the dot product of this vector and the supplied vector
	 *
	 * @param v Vector3
	 * @return dot product
	 */
    public double dotProduct (Vector3 v)
    {
	return (x * v.x) + (y * v.y) + (z * v.z);
    }


	/**
	 * Find the cross product of this vector and the supplied vector
	 *
	 * @param v Vector3
	 * @return cross product
	 */
    public Vector3 crossProduct (Vector3 v)
    {
	Vector3 result = new Vector3 ();

	result.x = (y * v.z) - (z * v.y);
	result.y = (z * v.x) - (x * v.z);
	result.z = (x * v.y) - (y * v.x);
	result.findMagnitude();

	return result;
    }



	/**
	 * Set the magnitude of the vector to 1.0
	 *
	 */
    public void normalize ()
    {
	findMagnitude ();

	x = x / mag;
	y = y / mag;
	z = z / mag;

	mag = 1.0;
    }



	/**
	 * Find the angle between this vector and the supplied vector
	 *
	 * @param v Vector3
	 * @return angle in Radians
	 */
    public double angleBetween (Vector3 v)
    {
	return Math.acos ((dotProduct (v) / (mag * v.magnitude ())));
    }



	/**
	 * Return a new vector mid-way between this vector and the
	 * supplied vector, in the same plane
	 *
	 * @param v Vector3
	 * @return Vector3
	 */
    public Vector3 vectorBetween (Vector3 v)
    {
	Vector3 result = new Vector3 ();

	result.x = (x + v.x) / 2.0;
	result.y = (y + v.y) / 2.0;
	result.z = (z + v.z) / 2.0;

	result.normalize ();

	return result;
    }



	/**
	 * Negate the vector
	 *
	 */
    public void negate ()
    {
	x = -x;
	y = -y;
	z = -z;
    }



	/**
	 * Write x,y,z coordinated to a DataOutput stream
	 *
	 * @param out DataOutput stream
	 */
    public void write (DataOutput out) throws IOException
    {
	out.writeDouble (x);
	out.writeDouble (y);
	out.writeDouble (z);
    }



	/**
	 * Return a string containing x,y,z coordinates
	 *
	 * @return String
	 */
    public String toString ()
    {
	return ("(" + x + ", " + y + ", " + z + ")");
    }



	/**
	 * Angle between the vector and Z axis
	 *
	 * @param v vector to compare
	 * @return angle in Radians
	 */
    public static double getTheta (Vector3 v)
    {
	return Math.acos ((v.z / v.magnitude ()));
    }
	
	/**
	 * Method getElevation
	 *
	 * @param    v                   a direction vector (not necessarily unit)
	 *
	 * @return   the angle between the specified vector and the XY-plane (reversed
	 * to agree with the common ARL understanding of an azimuth/elevation view,
	 * i.e, the vector is pointing toward the origin, not away)
	 *
	 */
	public static Angle getElevation( Vector3 v ) {
		double ang;
		
		if( v.z != 0.0 ) {
			ang = Math.atan2( -v.z, Math.sqrt( (v.x * v.x) + (v.y * v.y) ) );
		} else {
			ang = 0.0;
		}
		return (Angle)Angle.valueOf( ang, Math.ulp( ang ) * 4.0, SI.RADIAN );
	}

	/**
	 * Method getAzimuth
	 *
	 * @param    v                   a direction vector (not necessarily unit)
	 *
	 * @return   the angle between the specified direction and the positive X-axis (reversed
	 * to agree with the common ARL understanding of an azimuth/elevation view,
	 * i.e, the vector is pointing toward the origin, not away)
	 *
	 */
	public static Angle getAzimuth( Vector3 v ) {
		Vector3 revDir = Vector3.negate( v );
		double ang = Vector3.getPhi( revDir );
		return (Angle)Angle.valueOf( ang, Math.ulp( ang ) * 2.0, SI.RADIAN );
	}


	/**
	 * Angle between the vector and X axis, positive towards
	 * the Y axis.
	 *
	 * @param v the vector
	 * @return angle in Radians
	 */
    public static double getPhi (Vector3 v)
    {
	double phi = 0.0;

	if ((v.x == 0.0) && (v.y == 0.0))
	  {
	      return 0.0;
	  }

	if ((v.x != 0.0) && (v.y != 0.0))
	  {
	      phi =
		  Math.asin (Math.abs (v.y) /
			     Math.sqrt ((v.x * v.x) + (v.y * v.y)));

	      if ((v.x < 0.0) && (v.y > 0.0))
		{
		    phi = Math.PI - phi;
		}
	      else if ((v.x < 0.0) && (v.y < 0.0))
		{
		    phi = Math.PI + phi;
		}
	      else if ((v.x > 0.0) && (v.y < 0.0))
		{
		    phi = (2.0 * Math.PI) - phi;
		}
	  }
	else
	  {
	      if ((v.x == 0.0) && (v.y > 0.0))
		{
		    phi = PI_BY_2;
		}
	      else if ((v.x == 0.0) && (v.y < 0.0))
		{
		    phi = Math.PI + PI_BY_2;
		}
	      else if ((v.x > 0.0) && (v.y == 0.0))
		{
		    phi = 0.0;
		}
	      else if ((v.x < 0.0) && (v.y == 0.0))
		{
		    phi = Math.PI;
		}
	  }

	return phi;
    }



	/**
	 * Return a new vector mid-way between vector v1 and vector
	 * v2, in the same plane
	 *
	 * @param v1 Vector3
	 * @param v2 Vector3
	 * @return Vector3
	 */
    public static Vector3 vectorBetween (Vector3 v1, Vector3 v2)
    {
	Vector3 result = new Vector3 ();

	result.x = (v1.x + v2.x) / 2.0;
	result.y = (v1.y + v2.y) / 2.0;
	result.z = (v1.z + v2.z) / 2.0;

	result.normalize ();

	return result;
    }



	/**
	 * Find the angle between vector v1 and vector v2
	 *
	 * @param v1 Vector3
	 * @param v2 Vector3
	 * @return angle in Radians
	 */
    public static double angleBetween (Vector3 v1, Vector3 v2)
    {
	return Math.
	    acos ((Vector3.dotProduct (v1, v2) /
		   (v1.magnitude () * v2.magnitude ())));
    }




	/**
	 * Find the angle between vector v1 and vector v2
	 *
	 * @param v1 Vector3
	 * @param v2 Vector3
	 * @return Cosine of angle
	 */
    public static double cosOfAngleBetween (Vector3 v1, Vector3 v2)
    {
	return (Vector3.dotProduct (v1, v2) /
		(v1.magnitude () * v2.magnitude ()));
    }




	/**
	 * Find the cross product of vector v1 and vector v2
	 *
	 * @param v1 Vector3
	 * @param v2 Vector3
	 * @return cross product
	 */
    public static Vector3 crossProduct (Vector3 v1, Vector3 v2)
    {
	Vector3 result = new Vector3 ();

	result.x = (v1.y * v2.z) - (v1.z * v2.y);
	result.y = (v1.z * v2.x) - (v1.x * v2.z);
	result.z = (v1.x * v2.y) - (v1.y * v2.x);
	result.findMagnitude();

	return result;
    }



	/**
	 * Find the dot product of vector v1 and vector v2
	 *
	 * @param v1 Vector3
	 * @param v2 Vector3
	 * @return dot product
	 */
    public static double dotProduct (Vector3 v1, Vector3 v2)
    {
	return (v1.x * v2.x) + (v1.y * v2.y) + (v1.z * v2.z);
    }



	/**
	 * Return a new vector, the sum of vector v1 and vector v2
	 *
	 * @param v1 Vector3
	 * @param v2 Vector3
	 * @return Vector3 new vector
	 */
    public static Vector3 plus (Vector3 v1, Vector3 v2)
    {
	Vector3 result = new Vector3 ();

	result.x = v1.x + v2.x;
	result.y = v1.y + v2.y;
	result.z = v1.z + v2.z;
	result.findMagnitude();

	return result;
    }



	/**
	 * Return a new vector, vector v1 minus vector v2
	 *
	 * @param v1 Vector3
	 * @param v2 Vector3
	 * @return Vector3 new vector
	 */
    public static Vector3 minus (Vector3 v1, Vector3 v2)
    {
	Vector3 result = new Vector3 ();

	result.x = v1.x - v2.x;
	result.y = v1.y - v2.y;
	result.z = v1.z - v2.z;
	result.findMagnitude();
		
	return result;
    }
	
	public void squareElements()
	{
		x = x * x;
		y = y * y;
		z = z * z;
		findMagnitude ();
	}
	
	public void reverse()
	{
		x = -x;
		y = -y;
		z = -z;
	}



	/**
	 * Return a new vector, the negative of vector v
	 *
	 * @param v Vector3
	 * @return Vector3 new vector
	 */
    public static Vector3 negate (Vector3 v)
    {
	Vector3 result = new Vector3 ();

	result.x = -v.x;
	result.y = -v.y;
	result.z = -v.z;

	return result;
    }

    public static Vector3 minus (Point p1, Point p2)
    {
	return new Vector3 (p1.getX () - p2.getX (), p1.getY () - p2.getY (),
			    p1.getZ () - p2.getZ ());
    }
	
	public void scale( double factor ) {
		this.x *= factor;
		this.y *= factor;
		this.z *= factor;
		this.mag *= factor;
	}
	
	public static Vector3 scale( Vector3 v1, double scale ) {
		return new Vector3( v1.getX() * scale, v1.getY() * scale, v1.getZ() * scale );
	}

    /**
     * Rotate this vector around the specified up vector.
     * @param upVector the up vector to perform rotation around
     * @param angle the angle (in degrees) to rotate the vector
     */
    public void rotate(Vector3 upVector, double angle) {
		// create basis vectors with upVector as one of the directions
		Vector3 v3 = new Vector3( upVector );
		v3.normalize();
		Vector3 v1 = this.crossProduct(v3);
		v1.normalize();
		if( Double.isNaN( v1.getX() ) || Double.isNaN( v1.getY() ) || Double.isNaN( v1.getZ() ) ) {
			// this vector is parallel to the upvector, no need to change it
			return;
		}
		Vector3 v2 = Vector3.crossProduct(v3, v1 );
		v2.normalize();
		double radians = angle / 180.0 * java.lang.Math.PI;
		double sine = Math.sin( radians );
		double cosine = Math.cos( radians );
		double X = this.dotProduct(v1);
		double Y = this.dotProduct(v2);
		double Z = this.dotProduct(v3);
		double X1 = X * cosine - Y * sine;
		double Y1 = X * sine + Y * cosine;
		this.x = 0.0;
		this.y = 0.0;
		this.z = 0.0;
		this.plus( scale( v1, X1 ) );
		this.plus( scale( v2, Y1 ) );
		this.plus( scale( v3, Z ) );
    }

    /**
     * Test equality of points.
     *
     * @param o Object that may or may not be a point.
     * @return true if 'o' is a point and the points are in the same place,
     *         within the tolerance of 'this'.
     */
    public boolean equals(Object o) {
        if (o instanceof Vector3)
            return isEqual((Vector3) o);
        return false;
    }

	/**
	 * Method hashCode
	 *
	 * @return   an int
	 *
	 */
    public int hashCode() {
 
          	int result = 11;
		
			result = 7 * Double.toHexString( this.x ).hashCode();
			result = 7 * Double.toHexString( this.y ).hashCode();
			result = 7 * Double.toHexString( this.z ).hashCode();
		
          	return result;
    }

}
