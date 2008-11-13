/**
 * Tolerance.java
 *
 * @author Created by Omnicore CodeGuide
 */

package org.brlcad.numerics;

public class Tolerance
{
	private double dist;
	private double distSquared;
	private double perp;
	private double para;
	
	public Tolerance( double dist, double perp )
	{
		this.dist = dist;
		this.distSquared = dist * dist;
		this.perp = perp;
		this.para = 1.0 - perp;
	}
	
	/**
	 * Returns Dist
	 *
	 * @return    a  double
	 */
	public double getDist()
	{
		return dist;
	}
	
	/**
	 * Returns DistSquared
	 *
	 * @return    a  double
	 */
	public double getDistSquared()
	{
		return distSquared;
	}
	
	/**
	 * Returns Perp
	 *
	 * @return    a  double
	 */
	public double getPerp()
	{
		return perp;
	}
	
	/**
	 * Returns Para
	 *
	 * @return    a  double
	 */
	public double getPara()
	{
		return para;
	}
}

