/**
 * BoxNode.java
 *
 * @author Created by Omnicore CodeGuide
 */

package org.brlcad.spacePartition;
import org.brlcad.geometry.Hit;
import org.brlcad.preppedGeometry.PreppedObject;
import org.brlcad.preppedGeometry.PreppedObjectPiece;
import org.brlcad.geometry.Segment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.brlcad.numerics.BoundingBox;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Ray;



public class BoxNode extends Node
{
	public static final double MIN_BOX_WIDTH = 0.01;
	private BoundingBox boundingBox;
	private List<PreppedObject> preppedObjects;
	private List<PreppedObjectPiece> preppedPieces;
	
	public BoxNode()
	{
		this.preppedObjects = new ArrayList<PreppedObject>();
		this.preppedPieces = new ArrayList<PreppedObjectPiece>();
		this.boundingBox = new BoundingBox();
	}
	
	public void addPreppedObjectAndExtendBB( PreppedObject obj )
	{
		this.preppedObjects.add( obj );
		this.boundingBox.extend( obj.getBoundingBox() );
	}
	
	public void addPreppedObjectPieceAndExtendBB( PreppedObjectPiece obj )
	{
		this.preppedPieces.add( obj );
		this.boundingBox.extend( obj.getBoundingBox() );
	}

	public BoundingBox getBoundingBox()
	{
		return this.boundingBox;
	}
	
	public void setBoundingBox( BoundingBox bb )
	{
		this.boundingBox = bb;
	}
	
	public void populate( BoxNode box )
	{
		for( PreppedObject obj:box.preppedObjects )
		{
			if( obj.getBoundingBox().overlaps( this.getBoundingBox() ) )
			{
				this.addPreppedObject( obj );
			}
		}
		
		for( PreppedObjectPiece obj : box.preppedPieces )
		{
			if( obj.getBoundingBox().overlaps( this.getBoundingBox() ) )
			{
				this.addPreppedObjectPiece( obj );
			}
		}
	}
	
	/**
	 * Method addPreppedObjectPiece
	 *
	 * @param    obj                 a  PreppedObjectPiece
	 *
	 */
	private void addPreppedObjectPiece(PreppedObjectPiece obj)
	{
		this.preppedPieces.add( obj );
	}
	
	public int size()
	{
		return this.preppedObjects.size() + this.preppedPieces.size();
	}
	
	/**
	 * Method addPreppedObject
	 *
	 * @param    obj                 a  PreppedObject
	 *
	 */
	private void addPreppedObject(PreppedObject obj)
	{
		this.preppedObjects.add( obj );
	}
	
	/**
	 * Method shootRay
	 *
	 * @param    db                  a  PreppedDb
	 * @param    ray                 a  Ray
	 * @param    rayData             a  RayData
	 *
	 * @return   a List
	 *
	 */
	public void shootRay(PreppedDb db, Ray ray, RayData rayData)
	{
		for( PreppedObject obj:this.preppedObjects )
		{
			if( rayData.getBit( obj.getIndex() ) )
			{
				// already intersected
				continue;
			}
			List<Segment> segs = obj.shoot( ray, rayData );
			rayData.setBit( obj.getIndex() );
			if( segs.size() > 0 )
			{
				rayData.addSegs( obj, segs );
			}
		}
		for( PreppedObjectPiece obj:this.preppedPieces )
		{
			if( rayData.getBit( obj.getIndex() ) )
			{
				// already intersected
				continue;
			}
			Set<Hit> hits = obj.shoot( ray, rayData );
			rayData.setBit( obj.getIndex() );
			if( hits.size() > 0 )
			{
				rayData.addHits( obj.getPreppedObject(), hits );
			}
		}
		Segment seg = db.shootBoundingBox( ray, this.boundingBox );
		rayData.setDist( seg.getOutHit().getHit_dist() );
		Point locator = new Point( ray.getStart() );
		locator.join( seg.getOutHit().getHit_dist() + BoxNode.MIN_BOX_WIDTH/10.0, ray.getDirection() );
		rayData.setLocator( locator );
	}
	
}

