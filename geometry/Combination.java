package geometry;
/**
 * Represents a BRL-CAD Combination
 */



import java.util.Stack;
import numerics.Matrix;
import spacePartition.PreppedDb;
import numerics.BoundingBox;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import preppedGeometry.PreppedCombination;
import preppedGeometry.PreppedObject;

public class Combination extends DbObject
{
	private int index;
	private Tree tree;
	
	public static final byte majorType = 1;
	public static final byte minorType = 31;
	
	private static final byte leaf = 1;
	private static final byte union = 2;
	private static final byte intersection = 3;
	private static final byte subtraction = 4;
	private static final byte xor = 5;
	private static final byte not = 6;
	private static final byte identityMatrix = (byte)0377;
	
	public Combination( DbExternal dbExt ) throws DbException
	{
		super( dbExt );
		if( dbExt.getMajorType() != majorType ||dbExt.getMinorType() != minorType )
		{
			throw new DbException( "Attempted to import combination, but external is " +
									  " major type: " + dbExt.getMajorType() +
									  " minor type: " + dbExt.getMinorType() );
		}
		byte[] body = dbExt.getBody();
		int pointer = 0;
		byte wid = body[0];
		pointer += 1;
		int length = DbExternal.fieldLength[ wid ];
		int numMatrices = (int)BrlcadDb.getLong( body, pointer, length );
		pointer += length;
		int numLeaves = (int)BrlcadDb.getLong( body, pointer, length );
		pointer += length;
		int leafBytes = (int)BrlcadDb.getLong( body, pointer, length );
		pointer += length;
		long rpnLength = BrlcadDb.getLong( body, pointer, length );
		pointer += length;
		long maxStackDepth = BrlcadDb.getLong( body, pointer, length );
		pointer += length;
		
		Matrix[] matrices = new Matrix[numMatrices];
		// read the matrices
		for( int i=0 ; i<numMatrices ; i++ )
		{
			matrices[i] = new Matrix( 4, 4 );
			for( int row = 0 ; row < 4 ; row ++ )
			{
				for( int col = 0 ; col < 4 ; col++ )
				{
					matrices[i].set( row, col, BrlcadDb.getDouble( body, pointer ) );
					pointer += 8;
				}
			}
		}
		
		// new pointer that starts at the RPN expresion
		if( rpnLength == 0 )
		{
			List<Tree> list1 = new ArrayList<Tree>();
			List<Tree> list2 = new ArrayList<Tree>();

			// there is no RPN expresion, just union together all the leaves
			for( int i=0 ; i<numLeaves ; i++ )
			{
				int nameStart = pointer;
				int nameEnd = pointer;
				while( body[nameEnd] != (byte)0 )
				{
					nameEnd++;
				}
				String name = new String( body, nameStart, nameEnd - nameStart );
				pointer = nameEnd + 1;
				byte matrixIndex = body[pointer];
				pointer++;
				Matrix mat = null;
				if( matrixIndex != identityMatrix )
				{
					mat = matrices[matrixIndex];
				}
				Tree node = new Tree( name, mat );
				list1.add( node );
			}
			
			// make a balanced tree
			while( true )
			{
				Tree t1;
				Tree t2;
				
				Iterator<Tree> iter = list1.iterator();
				while( iter.hasNext() )
				{
					t1 = iter.next();
					if( iter.hasNext() )
					{
						t2 = iter.next();
						list2.add( new Tree( t1, t2, Operator.UNION ) );
					}
					else
					{
						list2.add( t1 );
					}
				}
				
				if( list2.size() == 1 )
				{
					this.tree = list2.get(0);
					break;
				}
				list1 = list2;
				list2= new ArrayList<Tree>();
			}
		}
		else
		{
			int tmpptr = pointer + leafBytes;
			Stack<Tree> stack = new Stack<Tree>();
			Tree node = null;
			Tree left = null;
			Tree right = null;
			for( int i=tmpptr ; i < tmpptr+rpnLength ; i++ )
			{
				switch( body[i] )
				{
					case leaf:
						// get the leaf from the body array
						int nameStart = pointer;
						int nameEnd = pointer;
						while( body[nameEnd] != (byte)0 )
						{
							nameEnd++;
						}
						String name = new String( body, nameStart, nameEnd - nameStart );
						pointer = nameEnd + 1;
						byte matrixIndex = body[pointer];
						pointer++;
						Matrix mat = null;
						if( matrixIndex != identityMatrix )
						{
							mat = matrices[matrixIndex];
						}
						node = new Tree( name, mat );
						stack.push( node );
						break;
					case union:
						right = (Tree)stack.pop();
						left = (Tree)stack.pop();
						node = new Tree( left, right, Operator.UNION );
						stack.push( node );
						break;
					case intersection:
						right = (Tree)stack.pop();
						left = (Tree)stack.pop();
						node = new Tree( left, right, Operator.INTERSECTION );
						stack.push( node );
						break;
					case subtraction:
						right = (Tree)stack.pop();
						left = (Tree)stack.pop();
						node = new Tree( left, right, Operator.SUBTRACTION );
						stack.push( node );
						break;
					case xor:
						right = (Tree)stack.pop();
						left = (Tree)stack.pop();
						node = new Tree( left, right, Operator.XOR );
						stack.push( node );
						break;
					case not:
						right = (Tree)stack.pop();
						node = new Tree( null, right, Operator.NOT );
						stack.push( node );
						break;
					default:
						throw new DbException( "Unrecognized operator in RPN expression in external form of a Combination" );
				}
			}
			this.tree = (Tree)stack.pop();
		}
	}
	
	/**
	 * Sets Index
	 *
	 * @param    Index               an int
	 */
	public void setIndex(int index)
	{
		this.index = index;
	}
	
	/**
	 * Returns Index
	 *
	 * @return    an int
	 */
	public int getIndex()
	{
		return index;
	}

	/**
	 * Constructor
	 * @param reg	The region that this Combination belongs to (or null)
	 * @param preppedDb	The PreppedDb that this Combination belongs to
	 * @para matrix	The transformation matrix to apply to this Combination
	 * @return A PreppedRegion or a PreppedCombination as appropriate
	 */
	public PreppedObject prep( PreppedCombination reg, PreppedDb preppedDb, Matrix matrix) throws BadGeometryException, DbException, IOException, DbNameNotFoundException
	{
		BoundingBox boundingBox = null;
		PreppedCombination pc =  null;
		boolean isRegion = this.getAttribute( "region" ) != null;
		if( isRegion && reg == null )
		{
			pc =  new PreppedCombination( this );
			boundingBox = this.tree.prep( pc, preppedDb, matrix );
			preppedDb.addPreppedRegion( pc );
		}
		else
		{
			boundingBox = this.tree.prep( reg, preppedDb, matrix );
			pc =  new PreppedCombination( this );
		}
		pc.setBoundingBox( boundingBox );
		
		return pc;
	}
	
	/**
	 * Get the tree that belongs to this Combination
	 * @return	The tree
	 */
	public Tree getTree()
	{
		return this.tree;
	}
	
	/**
	 * Create a String representation of this Combination
	 *
	 * @return   a String
	 *
	 */
	public String toString()
	{
		return super.toString() + " Combination:\n" + this.tree;
	}
}

