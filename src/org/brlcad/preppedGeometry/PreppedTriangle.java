package org.brlcad.preppedGeometry;
/**
 * Contains  single prepped triangle
 */



import org.brlcad.geometry.BadGeometryException;
import org.brlcad.geometry.Bot;
import org.brlcad.geometry.Face;
import org.brlcad.geometry.Hit;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Vector3;
import org.brlcad.spacePartition.RayData;
import org.brlcad.numerics.Ray;
import org.brlcad.numerics.BoundingBox;
import org.brlcad.numerics.Matrix;

public class PreppedTriangle
{
	public static final double MIN_DOT_NORMAL = 1.0E-9;
	private Point A;			// triangle vertex (A)
	private Vector3 BA;			// Vector from A to second point (B)
	private Vector3 CA;			// Vector from A to third point (C)
	private Vector3 normal;		// non-unit normal BA x CA
	private Vector3 norm;		// unit normal
	private Vector3[] vertexNormals;	// vertex normals
	private BoundingBox boundingBox;
	private int surfaceNumber;
	private PreppedObject parent;
	
	/**
	 * Constructor
	 * @param parent	The PreppedObject that contains this triangle
	 * @param bot	The original Bot containing this triangle
	 * @param faceNumber	The face number for this triangle
	 * @param matrix	The matrix to be applied to this triangle
	 * @throws BadGeometryException
	 */
	public PreppedTriangle( PreppedObject parent, Bot bot, int faceNumber, Matrix matrix ) throws BadGeometryException
	{
		this.parent = parent;
		Face f = bot.getFace( faceNumber );
		this.boundingBox = new BoundingBox();
		this.A = new Point( bot.getVertex(f.v[0]) );
		matrix.mult( this.A );
		Point B = new Point( bot.getVertex(f.v[1]) );
		matrix.mult( B );
		Point C = new Point( bot.getVertex(f.v[2]) );
		matrix.mult( C );
		
		this.boundingBox.extend( this.A );
		this.boundingBox.extend( B );
		this.boundingBox.extend( C );
		
		this.BA = Vector3.minus(B, this.A);
		this.CA = Vector3.minus(C, this.A);
		this.normal = this.BA.crossProduct( this.CA );
		this.surfaceNumber = faceNumber;
		
		// get lengths of the three sides of the triangle
		double m1 = this.BA.magnitude();
		double m2 = this.CA.magnitude();
		double m3 = Vector3.minus(B,C).magnitude();
		
		// and the length of the normal
		double m4 = this.normal.magnitude();
		
		// if any of these are too small, we have degenerate triangle
		if( m1 < 0.00001 || m2 < 0.00001 || m3 < 0.00001 || m4 < 0.00001 )
		{
			// bad triangle
			throw new BadGeometryException( "degenerate triangle in " + bot.getName() + " (face number " + faceNumber + ")" );
		}
		
		this.norm = new Vector3( this.normal );
		this.norm.normalize();
		
		if( bot.hasVertexNormals() && bot.useVertexNormals() )
		{
			this.vertexNormals = new Vector3[3];
			for( int i=0 ; i<3 ; i++ )
			{
				this.vertexNormals[i] = new Vector3( bot.getVertexNormal( faceNumber, i ) );
				matrix.mult( this.vertexNormals[i] );
			}
		}
		
		if( bot.isCW() )
		{
			this.norm.negate();
		}
	}
	
	public BoundingBox getBoundingBox()
	{
		return this.boundingBox;
	}
	
	/**
	 * Method shoot
	 *
	 * @param    ray                 a  Ray
	 * @param    rayData             a  RayData
	 *
	 * @return   a  Hit
	 */
	public Hit shoot(Ray ray, RayData rayData)
	{
		double dn = this.normal.dotProduct( ray.getDirection() );
		double abs_dn = Math.abs(dn);
		if( abs_dn < PreppedTriangle.MIN_DOT_NORMAL )
		{
			// ray is parallel to triangle plane
			return null;
		}
		double tolDist = rayData.getTolerance().getDist();
		double dn_plus_tol = abs_dn + tolDist;
		
		Vector3 wxb = Vector3.minus(this.A, ray.getStart());
		Vector3 xp = wxb.crossProduct( ray.getDirection() );
		
		double alpha = this.CA.dotProduct( xp );
		if( dn < 0.0 ) alpha = -alpha;
		if( alpha < -tolDist || alpha > dn_plus_tol )
		{
			// missed
			return null;
		}
		
		double beta = this.BA.dotProduct( xp );
		if( dn > 0.0 ) beta = -beta;
		if( beta < -tolDist || beta > dn_plus_tol )
		{
			// missed
			return null;
		}
		
		if( alpha + beta > dn_plus_tol )
		{
			// missed
			return null;
		}
		
		double hitDist = wxb.dotProduct( this.normal ) / dn;
		Point hitPoint = new Point( ray.getStart() );
		hitPoint.join( hitDist, ray.getDirection() );
		return new Hit( hitDist, hitPoint, this.norm, this.surfaceNumber, rayData );
	}
}

