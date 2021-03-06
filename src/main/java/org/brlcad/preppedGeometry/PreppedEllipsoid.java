package org.brlcad.preppedGeometry;
/**
 * The prepped version of the BRL-CAD ellipsoid primitive
 */




import org.brlcad.numerics.*;import org.brlcad.geometry.BadGeometryException;
import org.brlcad.geometry.BrlcadDb;
import org.brlcad.geometry.Ellipsoid;
import org.brlcad.geometry.Hit;
import org.brlcad.geometry.Segment;

import java.util.List;
import java.util.ArrayList;
import org.brlcad.numerics.Matrix;
import org.brlcad.numerics.Vector3;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.BoundingBox;
import org.brlcad.spacePartition.RayData;
import java.util.Set;

public class PreppedEllipsoid extends PreppedObject
{
	// Is this ellipsoid a sphere??
	private boolean isSphere;
	
	// radius squared (only used if this is sphere)
	private double rSquared;
	
	// inverse of the semi-axis lengths (only used if this is not a sphere)
	private Vector3 invsq;
	
	private Matrix scaleRot; // maps to unit sphere
	private Matrix invScaleRot; // maps back from unit sphere
	
	/**
	 * Consructor
	 * 
	 * @param ell	The ellipsoid primitive
	 * @param matrix	The transformation matrix to apply to this ellipsoid
	 */
	public PreppedEllipsoid( Ellipsoid ell, Matrix matrix) throws BadGeometryException
	{
		super( ell.getName() );
		this.center = new Point( ell.getCenter() );
		matrix.mult( this.center );
		Vector3 a = new Vector3( ell.getA() );
		Vector3 b = new Vector3( ell.getB() );
		Vector3 c = new Vector3( ell.getC() );
		matrix.mult( a );
		matrix.mult( b );
		matrix.mult( c );
		double aSquared = a.dotProduct( a );
		double bSquared = b.dotProduct( b );
		double cSquared = c.dotProduct( c );
		
		if( aSquared < BrlcadDb.tolerance.getDist() )
		{
			throw new BadGeometryException( "Ellipsoid " +
											   this.name +
											   " has too small \"A\" vector" );
		}
		
		if( bSquared < BrlcadDb.tolerance.getDist() )
		{
			throw new BadGeometryException( "Ellipsoid " +
											   this.name +
											   " has too small \"B\" vector" );
		}
		
		if( cSquared < BrlcadDb.tolerance.getDist() )
		{
			throw new BadGeometryException( "Ellipsoid " +
											   this.name +
											   " has too small \"C\" vector" );
		}
		
		if( Math.abs( aSquared - bSquared ) <= 0.0001 &&
		   Math.abs( aSquared - cSquared ) <= 0.0001 )
		{
			// this is sphere
			this.isSphere = true;
			this.rSquared = aSquared;
			double radius = Math.sqrt( aSquared );
			Point max = new Point( this.center.getX() + radius,
								  this.center.getY() + radius,
								  this.center.getZ() + radius );
			Point min = new Point( this.center.getX() - radius,
								  this.center.getY() - radius,
								  this.center.getZ() - radius );
			this.boundingRadius = this.rSquared;
			this.boundingBox = new BoundingBox( min, max );
			return;
		}
		else
		{
			// this is a general elipsoid
			this.isSphere = false;
			
			Vector3 rowa = new Vector3( a );
			Vector3 rowb = new Vector3( b );
			Vector3 rowc = new Vector3( c );
			rowa.normalize();
			rowb.normalize();
			rowc.normalize();
			
			Matrix rot = new Matrix( 4, 4 );
			rot.unit();
			rot.set(0,0,rowa.getX());
			rot.set(0,1,rowa.getY());
			rot.set(0,2,rowa.getZ());
			rot.set(1,0,rowb.getX());
			rot.set(1,1,rowb.getY());
			rot.set(1,2,rowb.getZ());
			rot.set(2,0,rowc.getX());
			rot.set(2,1,rowc.getY());
			rot.set(2,2,rowc.getZ());
			Matrix invRot = Matrix.transpose( rot );
			
			this.invsq = new Vector3( 1.0 / aSquared,
									 1.0 / bSquared,
									 1.0 / cSquared);
			
			Matrix ss = new Matrix(4,4);
			ss.unit();
			ss.set(0,0,invsq.getX());
			ss.set(1,1,invsq.getY());
			ss.set(2,2,invsq.getZ());
			
			this.scaleRot = new Matrix( 4, 4 );
			this.scaleRot.unit();
			this.scaleRot.set(0,0,a.getX() * invsq.getX());
			this.scaleRot.set(0,1,a.getY() * invsq.getX());
			this.scaleRot.set(0,2,a.getZ() * invsq.getX());
			this.scaleRot.set(1,0,b.getX() * invsq.getY());
			this.scaleRot.set(1,1,b.getY() * invsq.getY());
			this.scaleRot.set(1,2,b.getZ() * invsq.getY());
			this.scaleRot.set(2,0,c.getX() * invsq.getZ());
			this.scaleRot.set(2,1,c.getY() * invsq.getZ());
			this.scaleRot.set(2,2,c.getZ() * invsq.getZ());
			
			this.invScaleRot = new Matrix( 4, 4 );
			this.invScaleRot.unit();
			
			this.invScaleRot.mult( invRot );
			this.invScaleRot.mult( ss );
			this.invScaleRot.mult( rot );
			
			this.boundingRadius = Math.sqrt( Math.max( aSquared, Math.max( bSquared, cSquared ) ) );
			
			Point max = new Point();
			Point min = new Point();
			
			Vector3 w1 = new Vector3( aSquared, bSquared, cSquared );
			Vector3 p = new Vector3( 1.0, 0.0, 0.0 );
			rot.mult(p);
			p.squareElements();
			double f = Math.sqrt( w1.dotProduct( p ) );
			min.setX( this.center.getX() - f );
			max.setX( this.center.getX() + f );
			
			p = new Vector3( 0.0, 1.0, 0.0 );
			rot.mult(p);
			p.squareElements();
			f = Math.sqrt( w1.dotProduct( p ) );
			min.setY( this.center.getY() - f );
			max.setY( this.center.getY() + f );
			
			p = new Vector3( 0.0, 0.0, 1.0 );
			rot.mult(p);
			p.squareElements();
			f = Math.sqrt( w1.dotProduct( p ) );
			min.setZ( this.center.getZ() - f );
			max.setZ( this.center.getZ() + f );
			
			this.boundingBox = new BoundingBox( min, max );
		}
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
		List<Segment> segments = new ArrayList<Segment>();
		
		if( this.isSphere )
		{
			Vector3 rayOriginToCenter = Vector3.minus( this.center, ray.getStart() );
			double magsq_OTV = rayOriginToCenter.dotProduct( rayOriginToCenter );
			double dot = rayOriginToCenter.dotProduct( ray.getDirection() );
			double root;
			if( magsq_OTV >= this.rSquared )
			{
				// ray origin is outside of sphere
				if( dot < 0.0 )
				{
					// ray direction is away from sphere
					return segments;		// No hit
				}
				root = dot * dot - magsq_OTV + this.rSquared;
				if( root <= 0 )
				{
					// no real roots
					return segments;		// No hit
				}
			}
			else
			{
				root = dot * dot - magsq_OTV + this.rSquared;
			}
			root = Math.sqrt(root);
			
			double dist1 = dot - root;
			Point pt1 = new Point( ray.getStart() );
			pt1.join( dist1, ray.getDirection() );
			Vector3 norm1 = Vector3.minus( pt1, this.center );
			norm1.normalize();
			Hit hit1 = new Hit( dist1, pt1, norm1, 1, rayData, this.name );
			
			double dist2 = dot + root;
			Point pt2 = new Point( ray.getStart() );
			pt2.join( dist2, ray.getDirection() );
			Vector3 norm2 = Vector3.minus( pt2, this.center );
			norm2.normalize();
			Hit hit2 = new Hit( dist2, pt2, norm2, 1, rayData, this.name );
			
			Segment seg = new Segment( hit1, hit2 );
			segments.add( seg );
		}
		else
		{
			Vector3 dprime = new Vector3( ray.getDirection() );
			this.scaleRot.mult( dprime );
            Vector3 pprime = Vector3.minus(ray.getStart(), this.center);
			this.scaleRot.mult( pprime );
			
			double dp = dprime.dotProduct(pprime);
			double dd = dprime.dotProduct(dprime);
			
			double root = dp * dp - dd * ( pprime.dotProduct(pprime) - 1.0 );
			if( root < 0.0 )
			{
				// missed
				return segments;
			}
			
			root = Math.sqrt( root );
			double k1 = (-dp + root)/dd;
			double k2 = (-dp - root)/dd;
			Point inPoint = new Point( ray.getStart() );
			Point outPoint = new Point( ray.getStart() );
			if( k2 < k1 )
			{
				double temp = k1;
				k1 = k2;
				k2 = temp;
			}
			inPoint.join( k1, ray.getDirection() );
			outPoint.join( k2, ray.getDirection() );
			Vector3 inNormal = Vector3.minus(inPoint, this.center);
			this.invScaleRot.mult( inNormal );
			inNormal.normalize();
			Vector3 outNormal = Vector3.minus(outPoint, this.center);
			this.invScaleRot.mult( outNormal );
			outNormal.normalize();
			
			Hit inHit = new Hit( k1, inPoint, inNormal, 1, rayData, this.name );
			Hit outHit = new Hit( k2, outPoint, outNormal, 1, rayData, this.name );
			
			Segment seg = new Segment( inHit, outHit );
			segments.add( seg );
		}
		
		return segments;
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

