/**
 * SpacePartition.java
 *
 * @author Created by Omnicore CodeGuide
 */


package org.brlcad.spacePartition;
import org.brlcad.geometry.BadGeometryException;
import org.brlcad.geometry.BrlcadDb;
import org.brlcad.geometry.DbException;
import org.brlcad.geometry.DbNameNotFoundException;
import org.brlcad.geometry.DbObject;
import org.brlcad.geometry.Hit;
import org.brlcad.geometry.OverlapHandler;
import org.brlcad.geometry.Partition;
import org.brlcad.preppedGeometry.PreppedCombination;
import org.brlcad.preppedGeometry.PreppedObject;
import org.brlcad.preppedGeometry.PreppedObjectPiece;
import org.brlcad.geometry.Segment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.brlcad.numerics.BoundingBox;
import org.brlcad.numerics.Matrix;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Ray;
import org.brlcad.numerics.Vector3;



public class PreppedDb
{
	private BrlcadDb db;
	private List<String> topLevelObjects;
	private List<PreppedCombination> regions;
	private Node spacePartition;
	private BoundingBox boundingBox;
	private BoxNode initialBox;
	private int preppedSolidCount = 0;
	private int preppedRegionCount = 0;
	
	public PreppedDb( BrlcadDb db, String ... objs ) throws BadGeometryException, DbException, IOException, DbNameNotFoundException
	{
		this.regions = new ArrayList<PreppedCombination>();
		this.initialBox = new BoxNode();
		Matrix m = new Matrix( 4, 4 );
		this.db = db;
		this.topLevelObjects = new ArrayList<String>();
		for( String obj:objs )
		{
			this.topLevelObjects.add( obj );
			DbObject dbObject = null;
			try
			{
				System.out.println( "db.getInternal on " + obj);
				dbObject = db.getInternal( obj );
				System.out.println( "dbObject: " + dbObject);
			}
			catch (DbException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			catch (DbNameNotFoundException e) {
				e.printStackTrace();
			}
			
			m.unit();
			dbObject.prep( null, this, m );
		}
		this.boundingBox = new BoundingBox( this.initialBox.getBoundingBox() );
		
		//start cutting initialBox
		this.spacePartition = this.cut( this.initialBox );
	}
	
	private Node cut( BoxNode box)
	{
		if( box.size() < 5 )
		{
			return box;
		}
		
		Point max = box.getBoundingBox().getMax();
		Point min = box.getBoundingBox().getMin();
		Vector3 diff = max.subtract( min );
		
		double x = Math.abs( diff.getX() );
		double y = Math.abs( diff.getY() );
		double z = Math.abs( diff.getZ() );
		
		int cutAxis = -1;
		double cutValue = 0.0;
		
		// select largest dimension to cut
		if( x > y )
		{
			if( x > z )
			{
				// cut along X-axis
				cutAxis = 0;
			}
			else
			{
				// cut along Z-axis
				cutAxis = 2;
			}
		}
		else
		{
			if( y > z )
			{
				// cut along Y-axis
				cutAxis = 1;
			}
			else
			{
				// cut along Z-axis
				cutAxis = 2;
			}
		}
		
		BoxNode lower = new BoxNode();
		BoxNode upper = new BoxNode();
		
		BoundingBox upperBB = new BoundingBox( box.getBoundingBox() );
		BoundingBox lowerBB = new BoundingBox( box.getBoundingBox() );
		switch( cutAxis )
		{
			case 0:
				if( max.getX() - min.getX() <= BoxNode.MIN_BOX_WIDTH ) return box;
				cutValue = (max.getX() + min.getX()) / 2.0;
				upperBB.getMin().setX( cutValue );
				lowerBB.getMax().setX( cutValue );
				break;
			case 1:
				if( max.getY() - min.getY() <= BoxNode.MIN_BOX_WIDTH ) return box;
				cutValue = (max.getY() + min.getY()) / 2.0;
				upperBB.getMin().setY( cutValue );
				lowerBB.getMax().setY( cutValue );
				break;
			case 2:
				if( max.getZ() - min.getZ() <= BoxNode.MIN_BOX_WIDTH ) return box;
				cutValue = (max.getZ() + min.getZ()) / 2.0;
				upperBB.getMin().setZ( cutValue );
				lowerBB.getMax().setZ( cutValue );
				break;
		}
		lower.setBoundingBox( lowerBB );
		upper.setBoundingBox( upperBB );
		
		lower.populate( box );
		upper.populate( box );
		
		if( lower.size() == box.size() && upper.size() == box.size() )
		{
			return box;
		}

		return new CutNode( cutAxis, cutValue, this.cut(lower), this.cut(upper) );
	}
	
	public BoundingBox getBoundingBox()
	{
		return this.boundingBox;
	}
	
	public void addPreppedObjectToInitialBox( PreppedObject preppedObject )
	{
		this.initialBox.addPreppedObjectAndExtendBB( preppedObject );
		preppedObject.setIndex( this.preppedSolidCount++ );
	}
	
	/**
	 * Method addPreppedObjectPieceToInitialBox
	 *
	 * @param    pbp                 a  BotPiece
	 *
	 */
	public void addPreppedObjectPieceToInitialBox(PreppedObjectPiece pbp)
	{
		this.initialBox.addPreppedObjectPieceAndExtendBB( pbp );
		pbp.setIndex( this.preppedSolidCount++ );
	}
	
	public void addPreppedRegion( PreppedCombination reg )
	{
		this.regions.add( this.preppedRegionCount, reg );
		reg.setIndex( this.preppedRegionCount++ );
	}
	
	public BrlcadDb getDb()
	{
		return this.db;
	}
	
	public Segment shootBoundingBox( Ray ray, BoundingBox bb )
	{
		double rmin = Double.NEGATIVE_INFINITY;
		double rmax = Double.POSITIVE_INFINITY;
		
		double dist1;
		double dist2;
		double min;
		double max;
		int imax = -1;
		int imin = -1;
		
		for(int i=0 ; i<3 ; i++ )
		{
			dist1 = (bb.getMin().get(i) - ray.getStart().get(i)) / ray.getDirection().get(i);
			dist2 = (bb.getMax().get(i) - ray.getStart().get(i)) / ray.getDirection().get(i);
			min = Math.min( dist1, dist2 );
			max = Math.max( dist1, dist2 );
			if( min > rmin )
			{
				imin = i;
				rmin = min;
			}
			if( max < rmax )
			{
				imax = i;
				rmax = max;
			}
		}
		
		if( rmin >= rmax || rmax < 0.0 )
		{
			return null;
		}
		
		if( rmin < 0.0 )
		{
			rmin = 0.0;
		}
		
		Point inHitPoint = new Point( ray.getStart() );
		inHitPoint.join( rmin, ray.getDirection() );
		Vector3 norm1 = new Vector3( 0.0, 0.0, 0.0 );
		norm1.set(imin, 1.0 );
		if( norm1.dotProduct( ray.getDirection() ) > 0.0 )
		{
			norm1.reverse();
		}
		Point outHitPoint = new Point( ray.getStart() );
		outHitPoint.join( rmax, ray.getDirection() );
		Hit inHit = new Hit( rmin, inHitPoint, norm1, imin, null );
		Vector3 norm2 = Vector3.negate( norm1 );
		Hit outHit = new Hit( rmax, outHitPoint, norm2, imax, null );
		
		return new Segment( inHit, outHit );
	}
	
	public SortedSet<Partition> shootRay( Ray ray, OverlapHandler overlapHandler )
	{
		ray.getDirection().normalize();
		
		
		// first intersect with model bounding box
		Segment seg = this.shootBoundingBox( ray, this.boundingBox );
		
		if( seg == null )
		{
			System.out.println( "Missed" );
			return new TreeSet<Partition>();
		}

		SortedSet<Partition> parts = new TreeSet<Partition>();
		
		double maxDist = seg.getOutHit().getHit_dist();
		Point locator = new Point( ray.getStart() );
		locator.join( BoxNode.MIN_BOX_WIDTH/10.0, ray.getDirection() );
		BitSet regbits = new BitSet( this.preppedRegionCount );
		BitSet solidBits = new BitSet( this.preppedSolidCount );
		RayData rayData = new RayData( locator, BoxNode.MIN_BOX_WIDTH/10.0, solidBits, regbits, BrlcadDb.getTolerance(), ray );
		while( rayData.getDist() < maxDist )
		{
			this.spacePartition.shootRay( this, ray, rayData );
		}
		
		// make segments from hits on pieces
		rayData.makeSegs();
		
		for( int i=regbits.nextSetBit(0) ; i>-1 ; i = regbits.nextSetBit(i+1) )
		{
			PreppedCombination region = this.regions.get(i);
			SortedSet<Partition> regParts = region.evaluate( region, rayData );
			if( regParts != null && regParts.size() > 0 )
			{
				parts.addAll( regParts );
			}
		}
		
		parts = overlapHandler.handleOverlaps( parts );
		
		System.out.println(  parts.size() + " Partitions:" );
		for( Partition part:parts )
		{
			System.out.println( part );
		}
		
		return parts;
	}
}
