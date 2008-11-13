package org.brlcad.geometry;
/**
 * Represents an intersection of a ray with an object. The intersection starts at
 * inHit and ends at outHit.
 */


public class Segment
{
	// The entrance hit 
	private Hit inHit;
	
	// the exit hit
	private Hit outHit;
	
	/**
	 * Constructor
	 * @param in	The entrance hit
	 * @param out	The exit hit
	 */
	public Segment( Hit in, Hit out )
	{
		this.inHit = in;
		this.outHit = out;
	}
	
	/**
	 * Sets InHit
	 *
	 * @param    InHit               a  Hit
	 */
	public void setInHit(Hit inHit)
	{
		this.inHit = inHit;
	}
	
	/**
	 * Returns InHit
	 *
	 * @return    a  Hit
	 */
	public Hit getInHit()
	{
		return inHit;
	}
	
	/**
	 * Sets OutHit
	 *
	 * @param    OutHit              a  Hit
	 */
	public void setOutHit(Hit outHit)
	{
		this.outHit = outHit;
	}
	
	/**
	 * Returns OutHit
	 *
	 * @return    a  Hit
	 */
	public Hit getOutHit()
	{
		return outHit;
	}
	
	public String toString()
	{
		return "Segment:\ninHit: " + inHit + "\noutHit: " + outHit;
	}
}

