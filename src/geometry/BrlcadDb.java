package geometry;
/**
 * This class holds a BRL-CAD database and provides interfaces to its contents
 *
 * @author Created by Omnicore CodeGuide
 */


import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.io.EOFException;
import numerics.Tolerance;
import java.util.BitSet;
import java.util.Set;
import java.util.TreeSet;

public class BrlcadDb
{
	/** Name of the BRL-CAD db file */
	private String dbFileName;
	
	/** The title of this BRL-CAD db */
	private String title;
	
	/** The RandomAccessFile object associated with this BRL-CAD db */
	private RandomAccessFile dbInput;
	
	/** Directory of BRL-CAD object names and their offset into the BRL-CAD db */
	private Map < String, Long > directory;
	
	/** Magic number (byte) at the start of the BRL-CAD db file */
	public static final byte DB5HDR_MAGIC1 = 'v';
	
	/** Magic number (byte) at the end of each object in the BRL-CAD database */
	public static final byte DB5HDR_MAGIC2 = '5';
	
	/** default tolerance */
	public static final double DEFAULT_TOL_DIST = 0.005;
	public static final double DEFAULT_TOL_PERP = 1.0e-6;
	public static Tolerance tolerance = new Tolerance( DEFAULT_TOL_DIST, DEFAULT_TOL_PERP );;
	
	/** minimum number of faces to trigger use of pieces */
	public static int BOT_MINFACES = 32;
	
	/** number of triangles per Bot piece */
	public static int BOT_FACES_PER_PIECE = 4;
	
	/**
	 * Constructor
	 *
	 * @param    dbFileName          Name of the BRL-CAD DB file
	 *
	 * @exception   FileNotFoundException
	 * @exception   IOException
	 * @exception   DbException
	 *
	 */
	public BrlcadDb ( String dbFileName ) throws FileNotFoundException, IOException, DbException
	{
		this.dbFileName = dbFileName;
		this.dbInput = new RandomAccessFile( this.dbFileName, "r" );
		this.scan();
	}
	
	/**
	 * Sets Tolerance
	 *
	 * @param    Tolerance           a  Tolerance
	 */
	public static void setTolerance(Tolerance tolerance)
	{
		BrlcadDb.tolerance = tolerance;
	}
	
	/**
	 * Returns Tolerance
	 *
	 * @return    a  Tolerance
	 */
	public static Tolerance getTolerance()
	{
		return tolerance;
	}
	
	/**
	 * Get the RandomAccesFile object asociated with this BRL-CAD database
	 *
	 * @return   a RandomAccessFile
	 *
	 */
	public RandomAccessFile getRAM()
	{
		return this.dbInput;
	}
	
	/**
	 * Scan the BRL-CAD database, constructing the directory and extracting the title
	 *
	 * @exception   IOException
	 * @exception   DbException
	 *
	 */
	private void scan() throws IOException, DbException
	{
		// this will hold the 8 byte header at the start of the file
		byte fileHeader[] = new byte[8];
		
		// create a new directory
		this.directory = new HashMap < String, Long > ();
		try
		{
			// make sure we start at the start
			this.dbInput.seek( 0L );
			
			// read the file header
			this.dbInput.readFully( fileHeader );
			
			// verify the file header
			if( !this.fileHeaderIsValid( fileHeader ) )
			{
				throw new IOException( "Invalid file header" );
			}
			
			// scan the rest of the file by importing DbExternal objects
			Long offset;
			while( true )
			{
				offset = this.dbInput.getFilePointer();
				DbExternal dbExt = new DbExternal( this, offset );
				
				// if this object has a name, add it to the directory
				String name = dbExt.getName();
				if( name != null )
				{
					if( this.directory.get( name ) != null )
					{
						System.err.println( "Duplicate name (" + name + ") ignored" );
					}
					else
					{
//						System.out.println( name + ": " + offset
//							+ "; major " + dbExt.getMajorType()
//							+ "; minor " + dbExt.getMinorType());
						this.directory.put( name, offset );
					}
				}
			}
		}
		catch( EOFException e )
		{
			// we reached the end of the file
			
			// get the title from this database
			try
			{
				DbObject global = this.getInternal( "_GLOBAL" );
				this.title = global.getAttribute( "title" );
			}
			catch( Exception e1 )
			{
				throw new DbException( "Db has no GLOBAL object " + e1);
			}
			System.err.println( "Completed scan, " +
								   this.directory.size() +
								   " objects" +
								   ", title = " + this.title);
			return;
		}
		
	}
	
	/**
	 * Get the DbObject that has the specified name
	 *
	 * @param    name                The name of the desired object
	 *
	 * @return   a DbObject
	 *
	 * @exception   IOException
	 * @exception   DbException
	 * @exception   DbNameNotFoundException
	 *
	 */
	public DbObject getInternal( String name ) throws IOException, DbException, DbNameNotFoundException
	{
		// Lookup this name in the directory
		Long offset = this.directory.get( name );
		
		if( offset == null )
		{
			// no such object
			throw new DbNameNotFoundException( "Error: " + name + " not found" );
		}
		
		// Read the object in external form
		DbExternal dbExt = new DbExternal( this, offset );
		
		// Import this object into its internal form (this will usually be a subclass of DbObject)
		DbObject dbObj = this.importObj( dbExt );
		return dbObj;
	}
	
	/**
	 * Import a DbExternal object into its internal form
	 *
	 * @param    dbExt               a  DbExternal object
	 *
	 * @return   a  dbObject or, more likely, a subclass of DbObject
	 */
	private DbObject importObj(DbExternal dbExt) throws DbException
	{
		switch( dbExt.getMajorType() )
		{
			case 0:
				throw new DbException( "Illegal major type number (0)" );
			case 1:
				switch( dbExt.getMinorType() )
				{
					case Ellipsoid.minorType:
						return new Ellipsoid( dbExt );
					case Arb8.minorType:
						return new Arb8( dbExt );
					case Bot.minorType:
						return new Bot( dbExt );
					case Combination.minorType:
						return new Combination( dbExt );
					default:
						throw new DbException( "Unrecognized minor type (" +
												  dbExt.getMinorType() + ")" );
				}
			case 2:
				return new DbObject( dbExt );
				
			default:
				throw new DbException( "Unrecognized major type (" +
										  dbExt.getMajorType() + ")" );
		}
	}
	
	/**
	 * Get the description of an object with the specified name
	 *
	 * @param    name                a  String
	 *
	 * @return   a description of the specified object
	 *
	 * @exception   DbException
	 * @exception   IOException
	 * @exception   DbNameNotFoundException
	 *
	 */
	public String describe( String name ) throws DbException, IOException, DbNameNotFoundException
	{
		DbObject obj = this.getInternal( name );
		return obj.toString();
	}
	
	/**
	 * Read an unsigned integral number from the database using the specified number of bytes
	 *
	 * @param    numBytes            the number of bytes to read (must be 1, 2, 4, or 8)
	 *
	 * @return   a long
	 *
	 * @exception   IOException
	 *
	 */
	public long getUnsignedLong( int numBytes ) throws IOException
	{
		long longNum;
		
		switch( numBytes )
		{
			case 1:
				return (long)this.dbInput.readUnsignedByte();
			case 2:
				return (long)this.dbInput.readUnsignedShort();
			case 4:
				byte[] bytes = new byte[4];
				this.dbInput.readFully( bytes );
				longNum = (long)(bytes[0] & 0xff);
				for( int i=1 ; i<4 ; i++ )
				{
					longNum = longNum << 8;
					longNum |= (long)(bytes[i] & 0xff);
				}
				return longNum;
			case 8:
				longNum = this.dbInput.readLong();
				if( longNum < 0 )
				{
					throw new IOException( "Database contains an unsigned long that we cannot read in Java!!");
				}
				return longNum;
			default:
				throw new IOException( "BrlcadDb.getLong(): Illegal length (" + numBytes +
										  ")" );
		}
	}
	
	/**
	 * Read a signed integral number from the database using the specified number of bytes
	 *
	 * @param    numBytes            The number of bytes to read
	 *
	 * @return   a long
	 *
	 * @exception   IOException
	 *
	 */
	public long getLong( int numBytes ) throws IOException
	{
		switch( numBytes )
		{
			case 1:
				return (long)this.dbInput.readByte();
			case 2:
				return (long)this.dbInput.readShort();
			case 4:
				return (long)this.dbInput.readInt();
			case 8:
				return this.dbInput.readLong();
			default:
				throw new IOException( "BrlcadDb.getLong(): Illegal length (" + numBytes +
										  ")" );
		}
	}
	
	/**
	 * Extract a long value using bytes from the provided input array
	 * 
	 * @param bytes	The array containing the bytes
	 * @param pointer	The location in the above array to start converting
	 * @param length	The number of bytes to convert
	 * @return	A long
	 */
	public static long getLong( byte[] bytes, int pointer, int length )
	{
		long longBytes = (long)(bytes[pointer] & 0xff);
		for( int i=1 ; i<length ; i++ )
		{
			longBytes = longBytes << 8;
			longBytes |= ((long)bytes[pointer+i] & 0xff);
		}
		
		return longBytes;
	}
	
	/**
	 * Extract a double value using 8 bytes from the provided input array starting with the
	 * array element at index "pointer"
	 *
	 * @param    bytes               a  byte[]
	 * @param    pointer             the starting index in the above array
	 *
	 * @return   a double
	 *
	 */
	public static double getDouble( byte[] bytes, int pointer )
	{
		long longBytes = (long)(bytes[pointer] & 0xff);
		for( int i=1 ; i<8 ; i++ )
		{
			longBytes = longBytes << 8;
			longBytes |= ((long)bytes[pointer+i] & 0xff);
		}
		return Double.longBitsToDouble( longBytes );
	}

	/**
	 * Extract a BitSet from an input array of bytes. The end of the BitSet is
	 * the first 0 value byte after the staring point. 
	 * @param bytes	The array of bytes
	 * @param pointer	The starting point of the BitSet in the above array
	 * @return	A BitSet
	 */
	public static BitSet getBitSet( byte[] bytes, int pointer )
	{
		BitSet bitset;
		
		// find start of bit vector
		while( Character.isWhitespace(bytes[pointer]) )
		{
			pointer++;
		}
		// count number of hex digits in bit vector
		int index = pointer;
		while( bytes[index] != 0 )
		{
			index++;
		}
		int length = index - pointer;
		if( length < 2 || (length % 2) != 0 )
		{
			return null;
		}
		bitset = new BitSet( length * 4 );
		
		// set the bits
		int bitNum = length*4 - 1;
		while( bytes[pointer] != 0 )
		{
			byte mask = (byte)0x8;
			byte b = Byte.decode( "0x" + (char)bytes[pointer] );
			for( int i=0 ; i<4 ; i++ )
			{
				if( (b & mask) != 0 )
				{
					bitset.set( bitNum, true );
				}
				mask = (byte)(mask >> 1);
				bitNum--;
			}
			pointer++;
		}
		return bitset;
	}
	
	/**
	 * Method fileHeaderIsValid
	 *
	 * @param    fileHeader          a  byte[]
	 *
	 * @return   a  boolean
	 */
	private boolean fileHeaderIsValid(byte[] h )
	{
		if( h[0] != DB5HDR_MAGIC1 ) return false;
		if( h[7] != DB5HDR_MAGIC2 ) return false;
		return true;
	}
    
    
    /**
     * Method getObjectNames
     * 
     * @return  Set of object names in the file in alphabetical order
     */
    public Set<String> getObjectNames() {
        return new TreeSet<String>(directory.keySet());
    }
    
    /**
     * Method getDbExternal
     * 
     * @param   String which contains the name of the object to return
     * 
     * @return  DbExternal object for given name; null if it doesn't exist
     */
    public DbExternal getDbExternal(String name) {
	Long offset = this.directory.get( name );
	if( offset == null ) return null;
        try {
	    return new DbExternal( this, offset );
        } catch (IOException ioe) {
            return null;
        }
    }
    
    
}
