package org.brlcad.geometry;
/**
 * A simple containiner class for the indices of a triangular face
 */


public class Face
{
	/**
	 * Array of indices. These are indices into an array of vertices
	 */
	public final int[] v;
	
	/**
	 * Constructor
	 * @param i1	index of first vertex
	 * @param i2	index of second vertex
	 * @param i3	index of third vertex
	 */
	public Face( int i1, int i2, int i3 )
	{
		this.v = new int[3];
		this.v[0] = i1;
		this.v[1] = i2;
		this.v[2] = i3;
	}
	
	public String toString()
	{
		return "(" + v[0] + ", " + v[1] + ", " + v[2] + ")";
	}
}

