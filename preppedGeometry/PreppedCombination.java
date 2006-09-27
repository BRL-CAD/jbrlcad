package preppedGeometry;
/**
 * PreppedCombination.java
 *
 * @author Created by Omnicore CodeGuide
 */



import geometry.Combination;
import geometry.Hit;
import geometry.Partition;
import geometry.Segment;
import geometry.Tree;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import numerics.Ray;
import spacePartition.RayData;

public class PreppedCombination extends PreppedObject
{
	private Tree tree;
	private boolean isRegion;
	
	public PreppedCombination( Combination comb )
	{
		super( comb.getName() );
		this.tree = comb.getTree();
		this.index = comb.getIndex();
		this.isRegion = comb.getAttribute( "region" ) != null;
	}
	
	/**
	 * Intersect this object with the specified Ray
	 *
	 * @param    ray                 a  Ray
	 *
	 * @return   number of hits
	 *
	 */
	public List<Segment> shoot(Ray ray, RayData rayData)
	{
		// TODO
		return null;
	}
	
	public SortedSet<Partition> evaluate( PreppedCombination reg, RayData rayData )
	{
		if( reg != null )
		{
			return this.tree.evaluate( reg, rayData );
		}
		else if( this.isRegion )
		{
			return this.tree.evaluate( this, rayData );
		}
		else
		{
			return this.tree.evaluate( null, rayData );
		}
	}
	
	/**
	 * Method makeSegs
	 *
	 * @param    get                 a  Set<Hit>
	 *
	 * @return   a  List<Segment>
	 */
	public List<Segment> makeSegs(Set<Hit> hits, Ray ray, RayData rayData)
	{
		// this should never get called
		return null;
	}
}

