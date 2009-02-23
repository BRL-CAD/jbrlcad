package org.brlcad.geometry;
/**
 * A very simple handler for overlapping Partitions
 */



import java.util.SortedSet;
import java.util.Iterator;
import org.brlcad.numerics.Ray;

public class SimpleOverlapHandler implements OverlapHandler
{
    private boolean quiet = false;
	
	/**
	 * Method to reconcile overlaps along a ray trace.
	 * The Partitions are sorted according to their distance along the ray.
	 * @param    parts	a  SortedSet of Partitions that possibly overlap
	 *
	 * @return   a SortedSet	a SortedSet of Partitions without overlaps
	 *
	 */
	public SortedSet<Partition> handleOverlaps(SortedSet<Partition> parts, Ray ray)
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
					if( !quiet && !part1.getFromRegion().equals(part2.getFromRegion()) )
					{
						System.err.println( "OVERLAP on Ray: " + ray + "\n\t" + part1 + "\n\t" + part2 );
					}
					iter.remove();
					continue;
				}
				else
				{
					if( part1.getFromRegion().equals(part2.getFromRegion()) )
					{
						// not really an overlap, but handle it
						part1.setOutHit( part2.getOutHit(), false );
						iter.remove();
						continue;
					}
					else
					{
						// two different regions, select part1
                        if( !quiet ) {
                            System.err.println("OVERLAP on Ray: " + ray + "\n\t" + part1 + "\n\t" + part2);
                        }
						part2.setInHit( part1.getOutHit(), true );
					}
				}
			}
			
			
			part1 = part2;
		}
		return parts;
	}

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    public boolean isQuiet() {
        return this.quiet;
    }
	
}

