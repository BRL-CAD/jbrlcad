
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

package org.brlcad.numerics;
import java.io.*;
import javax.measure.quantity.Angle;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;

/**
 * Two dimensional Matrix
 *
 * @version    1.0 March 2001
 * @author     Paul Cropper
 *
 * Acquired from http://www.iesd.dmu.ac.uk/~pcc/dls/docs/intro.htm
 */

/*
 * $Header$
 */

public class Matrix implements Serializable
{
    public double mat[][];

    protected int rows;
    protected int columns;

    public static final long serialVersionUID = 1l;

  /**
   * Create new matrix r rows by c columns
   *
   * @param r rows
   * @param c columns
   */
    public Matrix (int r, int c)
    {
	int i, j;

	 rows = r;
	 columns = c;

	 mat = new double[rows][columns];

	for (i = 0; i < r; i++)
	  {
	      for (j = 0; j < c; j++)
		{
		    mat[i][j] = 0.0;
		}
	  }
    }
	
	/**
	 * Constructor for a 4x4 matrix with each element specified
	 *
	 * @param    a                   m[0][0]
	 * @param    b                   m[0][1]
	 * @param    c                   m[0][2]
	 * @param    d                   m[0][3]
	 * @param    e                   m[1][0]
	 * @param    f                   m[1][1]
	 * @param    g                   m[1][2]
	 * @param    h                   m[1][3]
	 * @param    i                   m[2][0]
	 * @param    j                   m[2][1]
	 * @param    k                   m[2][2]
	 * @param    l                   m[2][3]
	 * @param    m                   m[3][0]
	 * @param    n                   m[3][1]
	 * @param    o                   m[3][2]
	 * @param    p                   m[3][3]
	 *
	 */
	public Matrix( double a, double b, double c, double d,
				   double e, double f, double g, double h,
	               double i, double j, double k, double l,
	               double m, double n, double o, double p )
	{
		this.rows = 4;
		this.columns = 4;
		this.mat = new double[4][4];
		mat[0][0] = a;
		mat[0][1] = b;
		mat[0][2] = c;
		mat[0][3] = d;
		mat[1][0] = e;
		mat[1][1] = f;
		mat[1][2] = g;
		mat[1][3] = h;
		mat[2][0] = i;
		mat[2][1] = j;
		mat[2][2] = k;
		mat[2][3] = l;
		mat[3][0] = m;
		mat[3][1] = n;
		mat[3][2] = o;
		mat[3][3] = p;
	}

	/**
	 * Creat a 4x4 Matrix based on  direction vectors and a scale factor
	 *
	 * @param    row0                x-direction vector
	 * @param    row1                y-direction vector
	 * @param    row2                z-direction vector
	 * @param    col3                translation vector
	 * @param    scale               global scale factor
	 *
	 */
	public Matrix ( Vector3 col0, Vector3 col1, Vector3 col2, Vector3 col3, double scale )
	{
		this.rows = 4;
		this.columns = 4;
		this.mat = new double[4][4];
		
		this.mat[0][0] = col0.getX();
		this.mat[1][0] = col0.getY();
		this.mat[2][0] = col0.getZ();
		this.mat[0][1] = col1.getX();
		this.mat[1][1] = col1.getY();
		this.mat[2][1] = col1.getZ();
		this.mat[0][2] = col2.getX();
		this.mat[1][2] = col2.getY();
		this.mat[2][2] = col2.getZ();
		this.mat[3][0] = 0.0;
		this.mat[3][1] = 0.0;
		this.mat[3][2] = 0.0;
		this.mat[0][3] = col3.getX();
		this.mat[1][3] = col3.getY();
		this.mat[2][3] = col3.getZ();
		this.mat[3][3] = scale;
	}

  /**
   * Create a 4 x 1 matrix <br>
   * [x] <br>
   * [y] <br>
   * [z] <br>
   * [1] <br>
   *
   * @param v Point
   */
    public Matrix (Point v)
    {
	rows = 4;
	columns = 1;

	mat = new double[rows][columns];

	mat[0][0] = v.getX ();
	mat[1][0] = v.getY ();
	mat[2][0] = v.getZ ();
	mat[3][0] = 1.0;
    }

	/**
	 * Constructor to build a Matrix thats transforms coordinates into a rotated
	 * and translated coordinate system. The roll rotation is performed first,
	 * then the pitch rotation, then the yaw rotation, finally the translation.
	 *
	 * @param    yaw                 the yaw angle of the transformed coordinate system
	 * @param    pitch               the pitch angle of the transformed coordinate system
	 * @param    roll                the roll angle of the transformed coordinate system
	 * @param    location            the location of the origin of the transformed coordinate system
	 *
	 */
	public Matrix( Amount<Angle> yaw, Amount<Angle> pitch, Amount<Angle> roll, Point location )
	{
		rows = 4;
		columns = 4;
		
		mat = new double[rows][columns];
		
		double cosy = Math.cos( yaw.doubleValue(SI.RADIAN) );
		double siny = Math.sin( yaw.doubleValue(SI.RADIAN) );
		double cosp = Math.cos( pitch.doubleValue(SI.RADIAN) );
		double sinp = Math.sin( pitch.doubleValue(SI.RADIAN) );
		double cosr = Math.cos( roll.doubleValue(SI.RADIAN) );
		double sinr = Math.sin( roll.doubleValue(SI.RADIAN) );
		
		mat[0][0] = cosp * cosy;
		mat[0][1] = -cosp * siny;
		mat[0][2] = sinp;
		mat[0][3] = location.getX();
		mat[1][0] = sinr * sinp * cosy + cosr * siny;
		mat[1][1] = -sinr * sinp * siny + cosr * cosy;
		mat[1][2] = -sinr * cosp;
		mat[1][3] = location.getY();
		mat[2][0] = sinr * siny - cosr * sinp * cosy;
		mat[2][1] = sinr * cosy + cosr * sinp * siny;
		mat[2][2] = cosr * cosp;
		mat[2][3] = location.getZ();
		mat[3][0] = 0.0;
		mat[3][1] = 0.0;
		mat[3][2] = 0.0;
		mat[3][3] = 1.0;
	}

	/**
	 * Constructor to build a Matrix thats transforms coordinates into a rotated
	 * and translated coordinate system. The roll rotation is performed first,
	 * then the pitch rotation, then the yaw rotation.
	 *
	 * @param    yaw                 the yaw angle of the transformed coordinate system
	 * @param    pitch               the pitch angle of the transformed coordinate system
	 * @param    roll                the roll angle of the transformed coordinate system
	 *
	 */
	public Matrix( Amount<Angle> yaw, Amount<Angle> pitch, Amount<Angle> roll )
	{
		rows = 4;
		columns = 4;
		
		mat = new double[rows][columns];
		
		double cosy = Math.cos( yaw.doubleValue(SI.RADIAN) );
		double siny = Math.sin( yaw.doubleValue(SI.RADIAN) );
		double cosp = Math.cos( pitch.doubleValue(SI.RADIAN) );
		double sinp = Math.sin( pitch.doubleValue(SI.RADIAN) );
		double cosr = Math.cos( roll.doubleValue(SI.RADIAN) );
		double sinr = Math.sin( roll.doubleValue(SI.RADIAN) );
		
		mat[0][0] = cosp * cosy;
		mat[0][1] = -cosp * siny;
		mat[0][2] = sinp;
		mat[0][3] = 0.0;
		mat[1][0] = sinr * sinp * cosy + cosr * siny;
		mat[1][1] = -sinr * sinp * siny + cosr * cosy;
		mat[1][2] = -sinr * cosp;
		mat[1][3] = 0.0;
		mat[2][0] = sinr * siny - cosr * sinp * cosy;
		mat[2][1] = sinr * cosy + cosr * sinp * siny;
		mat[2][2] = cosr * cosp;
		mat[2][3] = 0.0;
		mat[3][0] = 0.0;
		mat[3][1] = 0.0;
		mat[3][2] = 0.0;
		mat[3][3] = 1.0;
	}
	
	public Matrix( Quaternion q, Vector3 xlate )
	{
		rows = 4;
		columns = 4;
		mat = new double[4][4];
		
		mat[0][0] = q.q[0] * q.q[0] - q.q[1] * q.q[1] - q.q[2] * q.q[2] + q.q[3] * q.q[3];
		mat[1][0] = 2.0 * ( q.q[0] * q.q[1] + q.q[3] * q.q[2] );
		mat[2][0] = 2.0 * ( q.q[0] * q.q[2] - q.q[3] * q.q[1] );
		mat[0][3] = xlate.getX();
		mat[0][1] = 2.0 * ( q.q[0] * q.q[1] - q.q[3] * q.q[2] );
		mat[1][1] = -q.q[0] * q.q[0] + q.q[1] * q.q[1] - q.q[2] * q.q[2] + q.q[3] * q.q[3];
		mat[2][1] = 2.0 * ( q.q[1] * q.q[2] + q.q[3] * q.q[0] );
		mat[1][3] = xlate.getY();
		mat[0][2] = 2.0 * ( q.q[0] * q.q[2] + q.q[3] * q.q[1] );
		mat[1][2] = 2.0 * ( q.q[1] * q.q[2] - q.q[3] * q.q[0] );
		mat[2][2] = -q.q[0] * q.q[0] - q.q[1] * q.q[1] + q.q[2] * q.q[2] + q.q[3] * q.q[3];
		mat[2][3] = xlate.getZ();
		mat[3][0] = 0.0;
		mat[3][1] = 0.0;
		mat[3][2] = 0.0;
		mat[3][3] = 1.0;
	}
	
	/**
	 * Build a Matrix thats transforms coordinates from a rotated
	 * and translated coordinate system into the unrotated system.
	 *
	 * @param    yaw                 the yaw angle of the transformed coordinate system
	 * @param    pitch               the pitch angle of the transformed coordinate system
	 * @param    roll                the roll angle of the transformed coordinate system
	 * @param    location            the location of the origin of the transformed coordinate system
	 *
	 */
	public static Matrix inverseYPR( Amount<Angle> yaw, Amount<Angle> pitch, Amount<Angle> roll )
	{
		Matrix my = new Matrix( yaw.opposite(), Amount.valueOf(0, SI.RADIAN), Amount.valueOf(0, SI.RADIAN) );
		Matrix mp = new Matrix( Amount.valueOf(0, SI.RADIAN), pitch.opposite(), Amount.valueOf(0, SI.RADIAN) );
		Matrix mr = new Matrix( Amount.valueOf(0, SI.RADIAN), Amount.valueOf(0, SI.RADIAN), roll.opposite() );
		
		mr.mult( mp );
		mr.mult( my );
		
		return mr;
	}
	
	public void set( int row, int col, double val )
	{
		mat[row][col] = val;
	}
	
	public static Matrix transpose( Matrix in )
	{
		Matrix out = new Matrix( in.columns, in.rows );
		
		for( int row=0 ; row < out.rows ; row++ )
		{
			for( int col=0 ; col < out.columns ; col++ )
			{
				out.mat[row][col] = in.mat[col][row];
			}
		}
		
		return out;
	}


  /**
   * Make matrix a unit matrix <br>
   * e.g. <br>
   *   [1 0 0 0] <br>
   *   [0 1 0 0] <br>
   *   [0 0 1 0] <br>
   *   [0 0 0 1] <br>
   *
   */
    public void unit ()
    {
	int r, c;

	for (r = 0; r < rows; r++)
	  {
	      for (c = 0; c < columns; c++)
		{
		    if (r == c)
		      {
			  mat[r][c] = 1.0;
		      }
		    else
		      {
			  mat[r][c] = 0.0;
		      }
		}
	  }
    }



  /**
   * Multiply by supplied matrix, note that apllying the resulting matrix is equivalent to
   * applying the specified multiplier transformation first then applying the original
   * matrix
   *
   * @param multiplier Matrix
   */
    public void mult (Matrix multiplier)
    {
	int i, c, r;
	double result[][];


	if (columns == multiplier.rows)
	  {
	      result = new double[rows][multiplier.columns];

	      for (r = 0; r < rows; r++)
		{
		    for (c = 0; c < multiplier.columns; c++)
		      {
			  for (i = 0; i < columns; i++)
			    {
				result[r][c] +=
				    (multiplier.mat[r][i] * mat[i][c]);
			    }
		      }
		}
	      mat = result;
	  }
    }



  /**
   * Multiply supplied vertex by matrix
   *
   * @param v Point
   */
    public void mult (Point v)
    {
	if ((columns >= 4) && (rows >= 3))
	  {
	      double x =
		  (mat[0][0] * v.getX ()) + (mat[0][1] * v.getY ()) +
		  (mat[0][2] * v.getZ ()) + mat[0][3];
	      double y =
		  (mat[1][0] * v.getX ()) + (mat[1][1] * v.getY ()) +
		  (mat[1][2] * v.getZ ()) + mat[1][3];
	      double z =
		  (mat[2][0] * v.getX ()) + (mat[2][1] * v.getY ()) +
		  (mat[2][2] * v.getZ ()) + mat[2][3];
	      v.setX (x);
	      v.setY (y);
	      v.setZ (z);
	  }
    }
	
	/**
	 * Multiply supplied vector by this matrix (note that translations do not get applied to vectors)
	 *
	 * @param    v                   a  Vector3
	 *
	 */
	public void mult ( Vector3 v )
    {
	if ((columns >= 4) && (rows >= 3))
	  {
	      double x =
		  (mat[0][0] * v.getX ()) + (mat[0][1] * v.getY ()) +
		  (mat[0][2] * v.getZ ());
	      double y =
		  (mat[1][0] * v.getX ()) + (mat[1][1] * v.getY ()) +
		  (mat[1][2] * v.getZ ());
	      double z =
		  (mat[2][0] * v.getX ()) + (mat[2][1] * v.getY ()) +
		  (mat[2][2] * v.getZ ());
	      v.setX (x);
	      v.setY (y);
	      v.setZ (z);
	  }
    }
		

  /**
   * Subtract supplied matrix
   *
   * @param s Matrix
   */
    public void sub (Matrix s)
    {
	int c, r;

	if ((columns == s.columns) && (rows == s.rows))
	  {
	      for (c = 0; c < columns; c++)
		{
		    for (r = 0; r < rows; r++)
		      {
			  mat[r][c] = mat[r][c] - s.mat[r][c];
		      }
		}
	  }
    }



  /**
   * Add supplied matrix
   *
   * @param a Matrix
   */
    public void add (Matrix a)
    {
	int c, r;

	if ((columns == a.columns) && (rows == a.rows))
	  {
	      for (c = 0; c < columns; c++)
		{
		    for (r = 0; r < rows; r++)
		      {
			  mat[r][c] = mat[r][c] + a.mat[r][c];
		      }
		}
	  }
    }
	
	public void translate( Vector3 v )
	{
		if( (columns == 4 ) && (rows == 4 ) )
		{
			mat[0][3] += v.getX();
			mat[1][3] += v.getY();
			mat[2][3] += v.getZ();
		}
		
		
	}



  /**
   * Print matrix
   *
   */
    public String toPrettyString ()
    {
	int r, c;
	String out = "";

	for (r = 0; r < rows; r++)
	  {
	      out = out + "[";
	      for (c = 0; c < columns; c++)
		{
		    out = out + " " + mat[r][c] + " ";
		}
	      out = out + "]\n";
	  }
	return out;
    }

    public String toString ()
    {
	StringBuffer str = new StringBuffer (this.rows * this.columns * 10);

	str.append ("[");
	for (int row = 0; row < this.rows; row++)
	  {
	      for (int col = 0; col < this.columns; col++)
		{
		    str.append (" " + mat[row][col]);
		}
	  }
	str.append (" ]");

	return str.toString ();
    }

}
