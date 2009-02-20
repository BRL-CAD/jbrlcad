package org.brlcad.geometry;
/**
 * OverlapHandler.java
 */



import java.util.SortedSet;
import org.brlcad.numerics.Ray;

public interface OverlapHandler
{
	/**
	 * Method to reconcile overlaps along a ray trace.
	 * The Partitions are sorted according to their distance along the ray.
	 * @param parts	A SortedSet of Partitions with possible overlaps
	 * @return	A SortedSet of Partitions with no overlaps
	 */
	public SortedSet<Partition> handleOverlaps( SortedSet<Partition> parts, Ray ray );
}

