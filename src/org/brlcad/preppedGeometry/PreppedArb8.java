package org.brlcad.preppedGeometry;
/**
 * PreppedArb8.java
 *
 * @author Created by Omnicore CodeGuide
 */



import org.brlcad.spacePartition.RayData;
import org.brlcad.numerics.Ray;
import org.brlcad.geometry.Arb8;
import org.brlcad.geometry.BadGeometryException;
import org.brlcad.geometry.BrlcadDb;
import org.brlcad.geometry.Hit;
import org.brlcad.geometry.Segment;

import java.util.List;
import org.brlcad.numerics.Matrix;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Plane3D;
import org.brlcad.numerics.Vector3;
import org.brlcad.numerics.BoundingBox;
import java.util.ArrayList;
import java.util.Set;

public class PreppedArb8 extends PreppedObject
{
	
	private ArbFace[] aface;
	private Oface[] oface;
	
	private static final ArbInfo[] arbInfo;
	
	static {
		arbInfo = new ArbInfo[6];
		arbInfo[0] = new ArbInfo( "1234", 3, 2, 1, 0 );
		arbInfo[1] = new ArbInfo( "8765", 4, 5, 6, 7 );
		arbInfo[2] = new ArbInfo( "1485", 4, 7, 3, 0 );
		arbInfo[3] = new ArbInfo( "2673", 2, 6, 5, 1 );
		arbInfo[4] = new ArbInfo( "1562", 1, 5, 4, 0 );
		arbInfo[5] = new ArbInfo( "4378", 7, 6, 2, 3 );
	}
	
	public PreppedArb8( Arb8 arb8, Matrix matrix ) throws BadGeometryException
	{
		super( arb8.getName() );
		
		Point[] vertices = new Point[8];
		
		// apply the matrix
		for( int i=0 ; i<8 ; i++ )
		{
			vertices[i] = new Point( arb8.getVertex(i) );
			matrix.mult( vertices[i] );
		}
		
		PrepArb pa = new PrepArb();
		
		// find an internal point by averaging vertice
		for( int i=0 ; i<8 ; i++ )
		{
			pa.center.plus( vertices[i] );
		}
		pa.center.scale( 1.0 / 8.0 );
		
		int[] equivPoints = { -1, -1, -1, -1, -1, -1, -1, -1 };
		
		for( int i=0 ; i<8 ; i++ )
		{
			for( int j=i-1 ; j>=0 ; j-- )
			{
				Vector3 work;
				
				work = Vector3.minus(vertices[j], vertices[i]);
				if( work.magnitude() < BrlcadDb.tolerance.getDistSquared() )
				{
					equivPoints[i] = equivPoints[j];
				}
			}
			if( equivPoints[i] == -1 )
			{
				equivPoints[i] = i;
			}
		}
		
		for( int i=0 ; i<6 ; i++ )
		{
			int numPoints = 0;
			
			for( int j=0 ; j<4 ; j++ )
			{
				int index = arbInfo[i].faceVertices[j];
				index = equivPoints[index];
				
				boolean skip = false;
				
				for( int k = numPoints-1 ; k>0 ; k-- )
				{
					if( pa.pindex[k][pa.faces] == index )
					{
						// skip this point
						skip = true;
						break;
					}
				}
				if( !skip )
				{
					if( arbAddPoint( vertices[index], arbInfo[i].faceName,
									pa, numPoints, name ) )
					{
						pa.pindex[numPoints][pa.faces] = index;
						numPoints++;
					}
				}
			}
			if( numPoints < 3 )
			{
				continue;
			}
			pa.npts[pa.faces] = numPoints;
			pa.faces++;
		}
		if( pa.faces < 4 || pa.faces > 6 )
		{
			throw new BadGeometryException( "Arb8 has illegal number of faces (" + pa.faces + ")" );
		}
		
		this.aface = pa.aface;
		this.oface = pa.oface;
		this.boundingBox = new BoundingBox();
		for( int i=0 ; i<8 ; i++ )
		{
			this.boundingBox.extend( vertices[i] );
		}
		Vector3 diameter = this.boundingBox.getDiameter();
		this.center = new Point( this.boundingBox.getMin() );
		this.center.join( 0.5, diameter );
		this.boundingRadius = diameter.magnitude() / 2.0;
	}
	
	/**
	 * Method arbAddPoint
	 *
	 * @param    vertex              a  Point
	 * @param    faceName            a  String
	 * @param    pa                  a  PreppedArb8.PrepArb
	 * @param    numPoints           an int
	 * @param    name                a  String
	 *
	 */
	private boolean arbAddPoint(Point vertex, String faceName, PreppedArb8.PrepArb pa, int numPoints, String name)
	{
		boolean returnVal = true;

		ArbFace aface = pa.aface[pa.faces];
		Oface oface = pa.oface[pa.faces];
		
		switch( numPoints )
		{
			case 0:
				aface.a = vertex;
				if( pa.doOpt )
				{
					oface.uvOrig = vertex;
				}
				break;
			case 1:
				oface.u = Vector3.minus(vertex, aface.a);
				oface.uLen = oface.u.magnitude();
				double scale = 1.0/oface.uLen;
				if( scale == Double.POSITIVE_INFINITY || scale == Double.NEGATIVE_INFINITY )
				{
					returnVal = false;
					break;
				}
				oface.u.scale( scale );
				break;
			case 2:
				Vector3 p_a = Vector3.minus(vertex, aface.a);
				Vector3 norm = p_a.crossProduct( oface.u );
				double f = norm.magnitude();
				scale = 1.0 / f;
				if( scale == Double.POSITIVE_INFINITY || scale == Double.NEGATIVE_INFINITY )
				{
					returnVal = false;
					break;
				}
				norm.scale( scale );
				if( pa.doOpt )
				{
					oface.v = norm.crossProduct( oface.u );
					oface.v.normalize();
					f = oface.v.dotProduct( p_a );
					oface.v.scale( f );
					oface.vLen = oface.v.magnitude();
					scale = 1.0 / oface.vLen;
					oface.v.scale( scale );
					
					p_a = Vector3.minus(vertex, oface.uvOrig);
					f = p_a.dotProduct( oface.u );
					if( f > oface.uLen )
					{
						oface.uLen = f;
					}
					else if( f < 0.0 )
					{
						oface.uvOrig.join( f, oface.u );
						oface.uLen += (-f);
					}
				}
				Vector3 work = Vector3.minus(aface.a, pa.center);
				f = work.dotProduct( norm );
				if( f < 0.0 )
				{
					norm.negate();
					pa.clockwise[pa.faces] = 1;
				}
				else
				{
					pa.clockwise[pa.faces] = 0;
				}
				aface.plane = new Plane3D( norm, aface.a );
				break;
			default:
				if( pa.doOpt )
				{
					p_a = Vector3.minus(vertex, oface.uvOrig);
					f = p_a.dotProduct( oface.u );
					if( f > oface.uLen )
					{
						oface.uLen = f;
					}
					else if( f < 0.0 )
					{
						oface.uvOrig.join( f, oface.u );
						oface.uLen += (-f);
					}
					f = p_a.dotProduct( oface.v );
					if( f > oface.vLen )
					{
						oface.vLen = f;
					}
					else if( f < 0.0 )
					{
						oface.uvOrig.join( f, oface.v );
						oface.vLen += (-f);
					}
				}
				p_a = Vector3.minus(vertex, aface.a);
				p_a.normalize();
				if( !aface.plane.liesIn( vertex, BrlcadDb.DEFAULT_TOL_DIST ) )
				{
					returnVal = false;
				}
				break;
		}
		
		return returnVal;
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
		List<Segment>segs = new ArrayList<Segment>();
		Plane3D inPlane = null;
		Plane3D outPlane = null;
		double indist = Double.NEGATIVE_INFINITY;
		double outdist = Double.POSITIVE_INFINITY;
		int inSurfNum = -1;
		int outSurfNum = -1;
		boolean done = false;
		
		for( int j=0 ; j<this.aface.length ; j++ )
		{
			ArbFace af = this.aface[j];
			double dxbdn = af.plane.distToPlane(ray.getStart());
			double dn = -(af.plane.getNormal().dotProduct(ray.getDirection()));
			double s = dxbdn / dn;

			if( s == Double.POSITIVE_INFINITY || s == Double.NEGATIVE_INFINITY )
			{
				// ray is parallel to face
				// if ray is on the outside of this face, then we missed the entire arb8
				if( dxbdn > Double.MIN_VALUE )
				{
					done = true;
					break;
				}
			}
			else
			{
				if( dn < 0.0 )
				{
					if( outdist > s )
					{
						outdist = s;
						outPlane = af.plane;
						outSurfNum = j;
					}
				}
				else
				{
					if( indist < s )
					{
						indist = s;
						inPlane = af.plane;
						inSurfNum = j;
					}
				}
			}
		}
				
		if( inPlane != null && outPlane != null )
		{
			if( indist < outdist && outdist < Double.POSITIVE_INFINITY )
			{
				Point inPoint = new Point( ray.getStart() );
				inPoint.join( indist, ray.getDirection() );
				Hit inhit = new Hit( indist, inPoint, inPlane.getNormal(), inSurfNum, rayData );
				Point outPoint = new Point( ray.getStart() );
				outPoint.join( outdist, ray.getDirection() );
				Hit outhit = new Hit( outdist, outPoint, outPlane.getNormal(), outSurfNum, rayData );
				Segment seg = new Segment( inhit, outhit );
				segs.add( seg );
				rayData.addSegs( this, segs );
			}
		}
		
		return segs;
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
	
	private class ArbFace
	{
		Point a;
		Plane3D plane;
		
		public String toString()
		{
			return "ArbFace: a=" + a + ", plane=" + plane;
		}
	}
	
	private class Oface
	{
		Point uvOrig;
		Vector3 u;
		Vector3 v;
		double uLen;
		double vLen;
		
		public String toString()
		{
			return "Oface: uvOrig=" + uvOrig + ", u=" + u + ", v=" + v +
				", uLen=" + uLen + ", vLen=" + vLen;
		}
	}
	
	private static class ArbInfo
	{
		String faceName;
		int[] faceVertices;
		
		public ArbInfo( String name, int v1, int v2, int v3, int v4 )
		{
			this.faceName = name;
			this.faceVertices = new int[4];
			this.faceVertices[0] = v1;
			this.faceVertices[1] = v2;
			this.faceVertices[2] = v3;
			this.faceVertices[3] = v4;
		}
		
		/**
		 * Returns FaceVertices
		 *
		 * @return    an int[]
		 */
		public int[] getFaceVertices()
		{
			return faceVertices;
		}
		
		/**
		 * Returns FaceName
		 *
		 * @return    a  String
		 */
		public String getFaceName()
		{
			return faceName;
		}
	}
	
	private class PrepArb
	{
		Point center;
		int faces;
		int[] npts;
		int[][] pindex;
		int[] clockwise;
		ArbFace[] aface;
		Oface[] oface;
		boolean doOpt;
		
		public PrepArb()
		{
			center = new Point( 0.0, 0.0, 0.0 );
			faces = 0;
			npts = new int[6];
			pindex = new int[4][6];
			clockwise = new int[6];
			aface = new ArbFace[6];
			oface = new Oface[6];
			for( int i=0 ; i<6 ; i++ )
			{
				aface[i] = new ArbFace();
				oface[i] = new Oface();
			}
			doOpt = false;
		}
		
		public String toString()
		{
			StringBuilder str = new StringBuilder();
			str.append( "PrepArb:\n\tnumber of faces:" + this.faces + '\n');
			str.append( "\tcenter: " + this.center + '\n');
			str.append( "\tFaces:\n");
			for( int i=0 ; i<6 ; i++ ) {
				str.append( "\t\tface #" + i + '\n');
				str.append( "\t\t\t" + this.npts[i] + " points: ");
				for( int j=0 ; j<this.npts[i] ; j++ ) {
					str.append( this.pindex[j][i] );
				}
				str.append( '\n');
				str.append( "\t\t\t" + this.aface[i] + '\n');
				str.append( "\t\t\t" + this.oface[i] + '\n');
			}
			
			return str.toString();
		}
	}
}

