package org.brlcad.preppedGeometry;
/**
 * PreppedCombination.java
 *
 * @author Created by Omnicore CodeGuide
 */



import org.brlcad.geometry.Combination;
import org.brlcad.geometry.Hit;
import org.brlcad.geometry.Partition;
import org.brlcad.geometry.Segment;
import org.brlcad.geometry.Tree;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.brlcad.numerics.Ray;
import org.brlcad.shading.Material;
import org.brlcad.spacePartition.RayData;

public class PreppedCombination extends PreppedObject
{
	private Tree tree;
	private boolean isRegion;
    private Material material;
	
	public PreppedCombination( Combination comb )
	{
		super( comb.getName() );
		this.tree = comb.getTree();
		this.index = comb.getIndex();
		this.isRegion = comb.getAttribute( "region" ) != null;
        this.material = comb.getMaterial();
	}

    protected PreppedCombination() {
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
        SortedSet<Partition> parts = null;
		if( reg != null )
		{
            parts = rayData.getPartitions(reg);
            if( parts == null ) {
                parts = this.tree.evaluate(reg, rayData);
                rayData.addPartitions(reg, parts);
            }
		}
		else if( this.isRegion )
		{
            parts = rayData.getPartitions(this);
            if( parts == null ) {
                parts = this.tree.evaluate(this, rayData);
                rayData.addPartitions(this, parts);
            }
		}
		else
		{
			parts = this.tree.evaluate( null, rayData );
		}

        return parts;
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

    /**
     * @return the material
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * @param material the material to set
     */
    public void setMaterial(Material material) {
        this.material = material;
    }
}

