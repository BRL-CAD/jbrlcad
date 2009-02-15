package org.brlcad.geometry;
/**
 * A very simple handler for overlapping Partitions
 */



import java.util.SortedSet;
import java.util.Iterator;

public class SimpleOverlapHandler implements OverlapHandler
{
	
	/**
	 * Method to reconcile overlaps along a ray trace.
	 * The Partitions are sorted according to their distance along the ray.
	 * @param    parts	a  SortedSet of Partitions that possibly overlap
	 *
	 * @return   a SortedSet	a SortedSet of Partitions without overlaps
	 *
	 */
	public SortedSet<Partition> handleOverlaps(SortedSet<Partition> parts)
	{
		if( parts == null || parts.size() < 2 )
		{
			return parts;
		}
		
		Iterator<Partition> iter = parts.iterator();
		Partition part1 = iter.next();
		while( iter.hasNext() )
		{
			Partition part2 = iter.next();
			
			if( part2.getInHit().getHit_dist() < part1.getOutHit().getHit_dist() )
			{
				// we have an overlap
				
				
				if( part2.getOutHit().getHit_dist() < part1.getOutHit().getHit_dist() )
				{
					// part2 is entirely inside part1 (delete it)
					if( part1.getFromRegion() != part2.getFromRegion() )
					{
						System.err.println( "OVERLAP:\n\t" + part1 + "\n\t" + part2 );
					}
					iter.remove();
					continue;
				}
				else
				{
					if( part1.getFromRegion() == part2.getFromRegion() )
					{
						// not really an overlap, but handle it
						part1.setOutHit( part2.getOutHit(), false );
						part1.setFlipOutNormal( part2.isFlipOutNormal() );
						iter.remove();
						continue;
					}
					else
					{
						// two different regions, select part1
						System.err.println( "OVERLAP:\n\t" + part1 + "\n\t" + part2 );
						part2.setInHit( part1.getOutHit(), false );
						part2.setFlipInNormal( !part1.isFlipOutNormal() );
					}
				}
			}
			
			
			part1 = part2;
		}
		return parts;
	}
	
}

