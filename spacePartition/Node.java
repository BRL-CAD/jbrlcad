/**
 * Node.java
 *
 * @author Created by Omnicore CodeGuide
 */

package spacePartition;
import numerics.Ray;



public abstract class Node
{
	public abstract void shootRay( PreppedDb db, Ray ray, RayData rayData );
}

