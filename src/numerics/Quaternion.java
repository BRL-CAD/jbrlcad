/**
 * Quaternion.java
 *
 * @author Created by Omnicore CodeGuide
 */

package numerics;
import jade.physics.Angle;



public class Quaternion
{
	public double q[];
	
	public Quaternion()
	{
		q = new double[4];
		q[0] = 0.0;
		q[1] = 0.0;
		q[2] = 0.0;
		q[3] = 1.0;
	}
	
	public Quaternion( Quaternion in )
	{
		q = new double[4];
		q[0] = in.q[0];
		q[1] = in.q[1];
		q[2] = in.q[2];
		q[3] = in.q[3];
	}
	
	public Quaternion( Angle rot, Vector3 axis )
	{
		q = new double[4];
		
		Vector3 copy = new Vector3( axis );
		copy.normalize();
		double halfAngle = rot.doubleValue() / 2.0 ;
		double sin = Math.sin( halfAngle );
		
		q[0] = copy.getX() * sin;
		q[1] = copy.getY() * sin;
		q[2] = copy.getZ() * sin;
		q[3] = Math.cos( halfAngle );
	}
	
	public Quaternion( Angle yaw, Angle pitch, Angle roll )
	{
		q = new double[4];
		
		double halfYaw = yaw.doubleValue()/2.0;
		double halfPitch = pitch.doubleValue()/2.0;
		double halfRoll = roll.doubleValue()/2.0;
		
		double siny = Math.sin( halfYaw );
		double cosy = Math.cos( halfYaw );
		double sinp = Math.sin( halfPitch );
		double cosp = Math.cos( halfPitch );
		double sinr = Math.sin( halfRoll );
		double cosr = Math.cos( halfRoll );
		
		q[0] = cosr * sinp * siny + sinr * cosp * cosy;
		q[1] = cosr * sinp * cosy - sinr * cosp * siny;
		q[2] = cosr * cosp * siny + sinr * sinp * cosy;
		q[3] = cosr * cosp * cosy - sinr * sinp * siny;
	}
	
	public void normalize()
	{
		double len = Math.sqrt( q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3] );
		
		for( int i=0 ; i<4 ; i++ )
		{
			q[i] = q[i] / len;
		}
	}
	
	public void negate()
	{
		q[0] = -q[0];
		q[1] = -q[1];
		q[2] = -q[2];
	}
	
	public Vector3 mult( Vector3 in )
	{
		Vector3 qvec = new Vector3( q[0], q[1], q[2] );
		Vector3 out = Vector3.crossProduct( qvec, in );
		out.scale( 2.0 * q[3] );
		out.plus( Vector3.scale( qvec, 2.0 * Vector3.dotProduct( qvec, in ) ) );
		out.plus( Vector3.scale( in, 2.0 * q[3] * q[3] - 1.0 ) );
		
		
		return out;
	}
	
	public static Quaternion multiply( Quaternion b, Quaternion c )
	{
		Quaternion a = new Quaternion();
		
	    a.q[0] = b.q[3]*c.q[0] + b.q[0]*c.q[3] + b.q[1]*c.q[2] - b.q[2]*c.q[1];
	    a.q[1] = b.q[3]*c.q[1] + b.q[1]*c.q[3] + b.q[2]*c.q[0] - b.q[0]*c.q[2];
	    a.q[2] = b.q[3]*c.q[2] + b.q[2]*c.q[3] + b.q[0]*c.q[1] - b.q[1]*c.q[0];
    	a.q[3] = b.q[3]*c.q[3] - b.q[0]*c.q[0] - b.q[1]*c.q[1] - b.q[2]*c.q[2];

		return a;
	}
	
	public String toString()
	{
		return "[ " + q[0] + " " + q[1] + " " + q[2] + " " + q[3] + "]";
	}
}
