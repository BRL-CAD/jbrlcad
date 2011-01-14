/**
 * Node.java
 *
 * @author Created by Omnicore CodeGuide
 */

package org.brlcad.spacePartition;
import org.brlcad.numerics.Ray;



public abstract class Node
{
	public abstract void shootRay( PreppedDb db, Ray ray, RayData rayData );
}

