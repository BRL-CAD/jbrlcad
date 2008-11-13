/**
 * CutNode.java
 *
 * @author Created by Omnicore CodeGuide
 */

package org.brlcad.spacePartition;
import org.brlcad.numerics.Ray;



public class CutNode extends Node
{
	private int cutAxis;
	private double cutValue;
	private Node ltCutValue;
	private Node gteCutValue;
	
	public CutNode( int cutAxis, double cutValue, Node lower, Node upper )
	{
		this.cutAxis = cutAxis;
		this.cutValue = cutValue;
		this.ltCutValue = lower;
		this.gteCutValue = upper;
	}
	
	public void shootRay( PreppedDb db, Ray ray, RayData rayData )
	{
		if( rayData.getLocator().get( cutAxis ) < cutValue )
		{
			ltCutValue.shootRay( db, ray, rayData );
		}
		else
		{
			gteCutValue.shootRay( db, ray, rayData );
		}
	}
}

