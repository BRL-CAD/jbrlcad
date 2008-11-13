package org.brlcad.geometry;
/**
 * Partition.java
 *
 * @author Created by Omnicore CodeGuide
 */



import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.brlcad.preppedGeometry.PreppedCombination;

public class Partition implements Comparable
{
	
	private Hit in_hit;
	private boolean flipInNormal;
	private Hit out_hit;
	private boolean flipOutNormal;
	private PreppedCombination fromRegion;
	
	public Partition( Segment seg, PreppedCombination reg )
	{
		this.in_hit = seg.getInHit();
		this.out_hit = seg.getOutHit();
		this.fromRegion = reg;
		this.flipInNormal = false;
		this.flipOutNormal = false;
	}
	
	public Partition( Hit inHit, boolean inFlip, Hit outHit, boolean outFlip, PreppedCombination reg )
	{
		this.in_hit = inHit;
		this.flipInNormal = inFlip;
		this.out_hit = outHit;
		this.flipOutNormal = outFlip;
		this.fromRegion = reg;
	}
	
	public Partition( Partition part )
	{
		this.in_hit = part.in_hit;
		this.out_hit = part.out_hit;
		this.fromRegion = part.fromRegion;
		this.flipInNormal = part.flipInNormal;
		this.flipOutNormal = part.flipOutNormal;
	}
	
	/**
	 * Sets Out_hit
	 *
	 * @param    Out_hit             a  Hit
	 */
	public void setOut_hit(Hit out_hit)
	{
		this.out_hit = out_hit;
	}
	
	/**
	 * Returns Out_hit
	 *
	 * @return    a  Hit
	 */
	public Hit getOut_hit()
	{
		return out_hit;
	}
	
	/**
	 * Sets In_hit
	 *
	 * @param    In_hit              a  Hit
	 */
	public void setIn_hit(Hit in_hit)
	{
		this.in_hit = in_hit;
	}
	
	/**
	 * Returns In_hit
	 *
	 * @return    a  Hit
	 */
	public Hit getIn_hit()
	{
		return in_hit;
	}
	
	/**
	 * Sets FlipOutNormal
	 *
	 * @param    FlipOutNormal       a  boolean
	 */
	public void setFlipOutNormal(boolean flipOutNormal)
	{
		this.flipOutNormal = flipOutNormal;
	}
	
	/**
	 * Returns FlipOutNormal
	 *
	 * @return    a  boolean
	 */
	public boolean isFlipOutNormal()
	{
		return flipOutNormal;
	}
	
	/**
	 * Sets FlipInNormal
	 *
	 * @param    FlipInNormal        a  boolean
	 */
	public void setFlipInNormal(boolean flipInNormal)
	{
		this.flipInNormal = flipInNormal;
	}
	
	/**
	 * Returns FlipInNormal
	 *
	 * @return    a  boolean
	 */
	public boolean isFlipInNormal()
	{
		return flipInNormal;
	}
	
	/**
	 * Sets FromRegion
	 *
	 * @param    FromRegion          a  PreppedCombination
	 */
	public void setFromRegion(PreppedCombination fromRegion)
	{
		this.fromRegion = fromRegion;
	}
	
	/**
	 * Returns FromRegion
	 *
	 * @return    a  PreppedCombination
	 */
	public PreppedCombination getFromRegion()
	{
		return fromRegion;
	}
	
	public void setInHit( Hit hit, boolean flip )
	{
		this.in_hit = hit;
		if( flip )
		{
			this.flipInNormal = !this.flipInNormal;
		}
	}
	
	public void setOutHit( Hit hit, boolean flip )
	{
		this.out_hit = hit;
		if( flip )
		{
			this.flipOutNormal = !this.flipOutNormal;
		}
	}
	
	/**
	 * Method not
	 *
	 * @param    partsL              a  SortedSet<Partition>
	 *
	 * @return   a  SortedSet<Partition>
	 */
	public static SortedSet<Partition> not(SortedSet<Partition> partsL)
	{
		// TODO
		return null;
	}
	
	/**
	 * Method intersect
	 *
	 * @param    partsL              a  SortedSet<Partition>
	 * @param    partsR              a  SortedSet<Partition>
	 *
	 * @return   a  SortedSet<Partition>
	 */
	public static SortedSet<Partition> intersect(SortedSet<Partition> partsL, SortedSet<Partition> partsR)
	{
		if( partsL == null || partsR == null) return null;
		
		SortedSet<Partition> parts = new TreeSet<Partition>();
		
		for( Partition part1:partsL )
		{
			double inDist = part1.in_hit.getHit_dist();
			double outDist = part1.out_hit.getHit_dist();
			Hit inHit = null;
			Hit outHit = null;
			
			System.out.println( "Starting seg: <" + inDist + " - " + outDist + ">" );
			
			for( Partition part2:partsR )
			{
				double inDist2 = part2.in_hit.getHit_dist();
				double outDist2 = part2.out_hit.getHit_dist();
				
				System.out.println( "intersect seg: <" + inDist2 + " - " + outDist2 + ">" );
				
				if( inDist2 > outDist || outDist2 < inDist ) continue;
				
				if( inDist2 > inDist && outDist2 < outDist )
				{
					if( inHit == null || inHit.getHit_dist() > inDist2 )
					{
						inHit = part2.in_hit;
					}
					if( outHit == null || outHit.getHit_dist() < outDist2 )
					{
						outHit = part2.out_hit;
					}
					continue;
				}
				
				if( inDist2 > inDist )
				{
					if( inHit == null || inHit.getHit_dist() > inDist2 )
					{
						inHit = part2.in_hit;
					}
					if( outHit == null || outHit.getHit_dist() < outDist )
					{
						outHit = part1.out_hit;
					}
					continue;
				}
				
				if( outDist2 < outDist )
				{
					if( inHit == null || inHit.getHit_dist() > inDist )
					{
						inHit = part1.in_hit;
					}
					if( outHit == null || outHit.getHit_dist() < outDist2 )
					{
						outHit = part2.out_hit;
					}
					continue;
				}
			}
			if( inHit != null && outHit != null )
			{
				if( inHit.getHit_dist() < outHit.getHit_dist() )
				{
					parts.add( new Partition( inHit, false, outHit, false, part1.fromRegion ) );
				}
			}
		}
		return parts;
	}
	
	/**
	 * Method subtract
	 *
	 * @param    partsL              a  SortedSet<Partition>
	 * @param    partsR              a  SortedSet<Partition>
	 *
	 * @return   a  SortedSet<Partition>
	 */
	public static SortedSet<Partition> subtract(SortedSet<Partition> partsL, SortedSet<Partition> partsR)
	{
		if( partsL == null ) return null;
		
		if( partsR == null ) return partsL;
		
		for( Partition part1:partsL )
		{
			double inDist = part1.in_hit.getHit_dist();
			double outDist = part1.out_hit.getHit_dist();
			
			System.out.println( "Starting seg: <" + inDist + " - " + outDist + ">" );
			
			for( Partition part2:partsR )
			{
				double inDist2 = part2.in_hit.getHit_dist();
				double outDist2 = part2.out_hit.getHit_dist();
				
				System.out.println( "Subtract seg: <" + inDist2 + " - " + outDist2 + ">" );
				
				if( inDist2 >= outDist ) continue;
				if( outDist2 <= inDist ) continue;
				
				if( inDist2 > inDist && outDist2 < outDist )
				{
					Partition newPart = new Partition( part1 );
					newPart.setInHit( part2.out_hit, true );
					partsL.add( newPart );
					part1.setOutHit( part2.in_hit, true );
					continue;
				}
				
				if( inDist2 > inDist )
				{
					part1.setOutHit( part2.in_hit, true );
					continue;
				}
				
				if( outDist2 < outDist )
				{
					part1.setInHit( part2.out_hit, true );
					continue;
				}
			}
		}
		
		return partsL;
	}
	
	/**
	 * Method xor
	 *
	 * @param    partsL              a  SortedSet<Partition>
	 * @param    partsR              a  SortedSet<Partition>
	 *
	 * @return   a  SortedSet<Partition>
	 */
	public static SortedSet<Partition> xor(SortedSet<Partition> partsL, SortedSet<Partition> partsR)
	{
		// TODO
		return null;
	}
	
	/**
	 * Method union
	 *
	 * @param    partsL              a  SortedSet<Partition>
	 * @param    partsR              a  SortedSet<Partition>
	 *
	 * @return   a  SortedSet<Partition>
	 */
	public static SortedSet<Partition> union(SortedSet<Partition> partsL, SortedSet<Partition> partsR)
	{
		System.out.println( "Partition.union():" );
		if( partsL == null && partsR == null ) return null;
		
		SortedSet<Partition> parts = new TreeSet<Partition>();
		if( partsL != null )
		{
			parts.addAll( partsL );
		}
		if( partsR != null )
		{
			parts.addAll( partsR );
		}
		
		Iterator<Partition> iter = parts.iterator();
		if( !iter.hasNext() ) return null;
		
		Partition part1 = iter.next();
		System.out.println( "Starting Partition: " + part1 );
		while( iter.hasNext() )
		{
			Partition part2 = iter.next();
			System.out.println( "  unioning :" + part2 );
			if( part2.in_hit.getHit_dist() < part1.out_hit.getHit_dist() )
			{
				if( part2.out_hit.getHit_dist() > part1.out_hit.getHit_dist() )
				{
					part1.setOutHit( part2.out_hit, false );
					System.out.println( "   new part1 = " + part1 );
				}
				iter.remove();
				System.out.println( "After remove, parts has " + parts.size() + " Partitions" );
				continue;
			}
			part1 = part2;
		}
		System.out.println( "At return, parts has " + parts.size() + " Partitions" );
		return parts;
	}
	
	public int compareTo(Object o)
	{
		if( !( o instanceof Partition ) )
		{
			throw new ClassCastException( "Cannot compare Partition to " + o.getClass().getName() );
		}
		Partition p = (Partition)o;
		int compare = Double.compare( this.in_hit.getHit_dist(), p.in_hit.getHit_dist() );
		
		if( compare == 0 )
		{
			compare = Double.compare( this.out_hit.getHit_dist(), p.out_hit.getHit_dist() );
		}
		
		if( compare == 0 )
		{
			compare = String.CASE_INSENSITIVE_ORDER.compare( this.fromRegion.getName(), p.fromRegion.getName() );
		}
		
		return compare;
	}
	
	public String toString()
	{
		return this.fromRegion.getName() +
			"\n\tin hit: " + this.in_hit.toString( this.flipInNormal ) +
			"\n\tout hit: " + this.out_hit.toString( this.flipOutNormal );
	}
}
