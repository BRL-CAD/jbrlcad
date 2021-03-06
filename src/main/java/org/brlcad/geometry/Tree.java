package org.brlcad.geometry;
/**
 * A binary tree
 */


import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.brlcad.preppedGeometry.PreppedCombination;
import org.brlcad.preppedGeometry.PreppedObject;

import org.brlcad.numerics.BoundingBox;
import org.brlcad.numerics.Matrix;
import org.brlcad.numerics.Point;
import org.brlcad.spacePartition.PreppedDb;
import org.brlcad.spacePartition.RayData;

public class Tree implements Serializable
{
	/** the operator for this node (union, subtraction, leaf, ...) */
	private Operator op;
	
	/** the left subtree (null for unary operators or leaf node) */
	private Tree left;
	
	/** the right subtree (null for leaf node) */
	private Tree right;
	
	/** the matrix to be applied to a leaf node */
	private Matrix mat;
	
	/** the name of the referenced object (null if not a leaf node) */
	private String leafName;
	
	/** the prepped version of the referenced object (null until prepped) */
	private PreppedObject leaf;
	
	/**
	 * Construct a leaf node
	 *
	 * @param    name                the name of the referenced object
	 * @param    mat                 a  Matrix to be applied to the referenced object
	 *
	 */
	public Tree( String name, Matrix mat )
	{
		this.op = Operator.LEAF;
		this.leafName = name;
		this.mat = mat;
	}
	
	/**
	 * Construct a node
	 *
	 * @param    left                the left sub-tree ( or null if the operator is unary)
	 * @param    right               the right sub-tree
	 * @param    op                  the operator ( left op right )
	 *
	 */
	public Tree( Tree left, Tree right, Operator op )
	{
		this.left = left;
		this.right = right;
		this.op = op;
	}
	
	/**
	 * Prep this Combination for raytracing
	 * @param reg	The containing region (or null)
	 * @param preppedDb	The containing PreppedDb
	 * @param matrix	The transformation matris to apply to this tree
	 * @return	The BoundingBox for this tree
	 * @throws BadGeometryException
	 * @throws DbException
	 * @throws IOException
	 * @throws DbNameNotFoundException
	 */
	public BoundingBox prep( PreppedCombination reg, PreppedDb preppedDb, Matrix matrix ) throws BadGeometryException, DbException, IOException, DbNameNotFoundException
	{
		BoundingBox bb = null;
		switch( this.op )
		{
			case UNION:
			case XOR:
				bb = new BoundingBox( this.left.prep( reg, preppedDb, matrix ) );
				bb.extend( this.right.prep( reg, preppedDb, matrix ) );
				break;
			case SUBTRACTION:
				bb = new BoundingBox( this.left.prep( reg, preppedDb, matrix ) );
				this.right.prep( reg, preppedDb, matrix );
				break;
			case INTERSECTION:
				bb = new BoundingBox( this.left.prep( reg, preppedDb, matrix ) );
				bb.intersect( this.right.prep( reg, preppedDb, matrix ) );
				break;
			case NOT:
				this.left.prep( reg, preppedDb, matrix );
				bb = new BoundingBox( new Point( Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY ),
									 new Point( Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY ) );
				break;
			case LEAF:
				Matrix m = new Matrix( 4, 4 );
				m.unit();
				m.mult( matrix );
				if( this.mat != null )
				{
					m.mult( this.mat );
				}
				this.leaf = preppedDb.getDb().getInternal( this.leafName ).prep( reg, preppedDb, m );
                                if (this.leaf.getBoundingBox() != null) {
                                    bb = new BoundingBox(this.leaf.getBoundingBox());
                                } else {
                                    bb = null;
                                }
				if( this.leaf instanceof PreppedCombination )
				{
					break;
				}
				if( reg != null )
				{
					this.leaf.addRegion( reg );
				}
				break;
		}

		return bb;
	}
	
	/**
	 * Evaluate this tree (after raytracing)
	 * @param reg	The region containing this tree
	 * @param rayData	The ray data from the raytrace
	 * @return	A sorted set of Partitions
	 */
	public SortedSet<Partition> evaluate( PreppedCombination reg, RayData rayData )
	{
		SortedSet<Partition> parts = null;
		SortedSet<Partition> partsL = null;
		SortedSet<Partition> partsR = null;
		
		switch( this.op )
		{
			case UNION:
				partsL = this.left.evaluate( reg, rayData );
				partsR = this.right.evaluate( reg, rayData );
				parts = Partition.union( partsL, partsR );
				break;
			case XOR:
				partsL = this.left.evaluate( reg, rayData );
				partsR = this.right.evaluate( reg, rayData );
				parts = Partition.xor( partsL, partsR );
				break;
			case SUBTRACTION:
				partsL = this.left.evaluate( reg, rayData );
				partsR = this.right.evaluate( reg, rayData );
				parts = Partition.subtract( partsL, partsR );
				break;
			case INTERSECTION:
				partsL = this.left.evaluate( reg, rayData );
				partsR = this.right.evaluate( reg, rayData );
				parts = Partition.intersect( partsL, partsR );
				break;
			case NOT:
				partsL = this.left.evaluate( reg, rayData );
				parts = Partition.not( partsL );
				break;
			case LEAF:
				if( this.leaf instanceof PreppedCombination )
				{
					PreppedCombination region = (PreppedCombination)this.leaf;
					parts = region.evaluate( region, rayData );
				}
				else
				{
					List<Segment> segs = rayData.getSegs( this.leaf );
					if( segs != null && segs.size() > 0 )
					{
						parts = new TreeSet<Partition>();
						for( Segment seg:segs )
						{
							parts.add( new Partition( seg, reg.getName(), reg.getID(), rayData ) );
						}
					}
				}
				break;
		}
		
		return parts;
	}
	
    @Override
	public String toString()
	{
		String leftNode = null;
		String rightNode = null;
		
		if( this.op == Operator.NOT )
		{
			// unary operator
			rightNode = this.right.toString();
			return  this.op.toString() + "( " + rightNode + " )";
		}
		else if( this.op == Operator.LEAF )
		{
			return this.leafName;
		}
		else
		{
			leftNode = this.left.toString();
			rightNode = this.right.toString();
			return (leftNode.contains( " " ) ? "( " + leftNode + " )" : leftNode) +
				" " + this.op.toString() + " " +
				(rightNode.contains( " " ) ? "( " + rightNode + " )" : rightNode);
			
		}
	}

    public Operator getOp() {
        return op;
    }

    public Tree getLeft() {
        return left;
    }
	
    public Tree getRight() {
        return right;
    }

    public Matrix getMat() {
        return mat;
    }

    public String getLeafName() {
        return leafName;
    }

}

