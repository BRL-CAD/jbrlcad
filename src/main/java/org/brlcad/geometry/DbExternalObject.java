package org.brlcad.geometry;
/**
 * This class contains the external form of a BRL-CAD object consisting of the object name, its offset
 * into the BRL-CAD database file, its major type, its minor type , its total length (bytes) in the
 * BRL-CAD database file, an array of bytes containing the body of the object and another array of bytes
 * containing the attributes of the object.
 *
 * @author Created by Omnicore CodeGuide
 */

import java.io.IOException;
import java.io.RandomAccessFile;

public class DbExternalObject implements DbExternal
{
	// this object's name
	private String name;
	
	// offset into the BRL-CAD database file for this object
	private long offset;
	
	// major type of this object
	private byte majorType;
	
	// minor type of this object
	private byte minorType;
	
	// number of bytes used by this object in the database file
	private long objectLength;
	
	// the attributes associated with this object
	private byte[] attributes;
	
	// the body of the object
	private byte[] body;
	
	// some masks
	private static final byte DLI_MASK = 03;
	private static final byte OBJECT_WID_MASK = (byte)0xc0;
	private static final byte NAME_PRESENT = 0x20;
	private static final byte NAME_WID_MASK = 0x18;
	private static final byte AFLAGS_PRESENT = 0x20;
	private static final byte AFLAGS_WID_MASK = (byte)0xc0;
	private static final byte BFLAGS_PRESENT = 0x20;
	private static final byte BFLAGS_WID_MASK = (byte)0xc0;
	
	// number of bytes used to represent different values in the byte arrays
	public static final int[] fieldLength = { 1, 2, 4, 8 };
	
	/**
	 * Construct a DbExternal by reading bytes from the specified BrlcadDb object starting at the specified offset
	 *
	 * @param    db                  a  BrlcadDb
	 * @param    offset              a  long
	 *
	 * @exception   IOException
	 *
	 */
	public DbExternalObject( BrlcadDb db, long offset ) throws IOException
	{
		// remember our offset
		this.offset = offset;
		
		// get the RandomAccessFile
		RandomAccessFile dbInput = db.getRAM();
		
		// seek to the specified offset
		dbInput.seek( offset );
		
		// storage for the header
		byte header[] = new byte[6];
		
		// a counter of the number of bytes read so far
		int used = 0;
		
		// read the object header
		dbInput.readFully( header );
		
		// check the first byte in the header
		if( header[0] != BrlcadDb.DB5HDR_MAGIC1 )
		{
			throw new IOException( "Bad magic number in object header" );
		}
		
		// get the major and minor types
		this.majorType = header[4];
		this.minorType = header[5];
		
		// the header was 6 bytes long
		used += 6;
		
		// get the index into the fieldLength array for the object length
		int objectWidIndex = (byte)((header[1] & OBJECT_WID_MASK & 0xFF) >> 6);
		
		// get the number of bytes used to store the object length
		int objectLengthWidth = fieldLength[objectWidIndex];
		
		// read the object length
		this.objectLength = db.getUnsignedLong( objectLengthWidth );
		
		// add the number of bytes read to our used count
		used += objectLengthWidth;
		
		// the object length is specified in 8 byte chunks, so multiply by 8 to get bytes
		this.objectLength = this.objectLength << 3;
		
		// check if we have a name
		if( (header[1] & NAME_PRESENT) != 0 )
		{
			// get the index into the fieldLength array for the name length
			int nameWidIndex = (byte)((header[1] & NAME_WID_MASK & 0xFF) >> 3);
			// get the number of bytes used to store the name length
			int nameLengthWidth = fieldLength[nameWidIndex];
			// read the name length
			int nameLength = (int)db.getUnsignedLong( nameLengthWidth );
			// update our used count
			used += nameLengthWidth;
			// create an array of bytes to hold the actual name
			byte nameBytes[] = new byte[nameLength];
			// read the name
			dbInput.readFully( nameBytes );
			// update our used count
			used += nameLength;
			// convert name bytes into a String and save it
			this.name = new String( nameBytes );
			// eliminate trailing null
			this.name = this.name.substring( 0, this.name.length() - 1 );
		}
		
		// check if we have attributes
		if( (header[2] & AFLAGS_PRESENT) != 0 )
		{
			// get the index into the fieldLength array for the attributes length
			int attWidIndex = (byte)((header[2] & AFLAGS_WID_MASK & 0xFF) >> 6);
			// get the number of bytes used to store the attribute length
			int attLengthWidth = fieldLength[attWidIndex];
			// read the attribute length
			int attLength = (int)db.getUnsignedLong( attLengthWidth );
			// update our used count
			used += attLengthWidth;
			// create an array of bytes to hold the actual array of bytes for attributes
			this.attributes = new byte[attLength];
			// read the attribute bytes
			dbInput.readFully( this.attributes );
			// update our used count
			used += attLength;
		}
		
		// check if we have a body
		if( (header[3] & BFLAGS_PRESENT) != 0 )
		{
			// get the index into the fieldLength array for the body length
			int bodyWidIndex = (byte)((header[3] & BFLAGS_WID_MASK & 0xFF) >> 6);
			// get the number of bytes used to store the body length
			int bodyLengthWidth = fieldLength[bodyWidIndex];
			// read the body length
			int bodyLength = (int)db.getUnsignedLong( bodyLengthWidth );
			// update our used count
			used += bodyLengthWidth;
			// create an array of bytes to hold the actual array of bytes for the body
			this.body = new byte[bodyLength];
			// read the body
			dbInput.readFully( this.body );
			// update our used count
			used += bodyLength;
		}
//			The dli bits indicate what type of object we are reading,
//			but since all objects are the same format, we really don't need
//			to pay any attention to them.
//		byte dli = (byte)(header[1] & DLI_MASK);
//		if( dli == 0x1 )
//		{
//		}
//		else if( dli == 0x0 )
//		{
//		}
		
		// skip over unused bytes (except for the last one)
		dbInput.skipBytes( (int)(objectLength - used - 1) );
		// read the last byte in this object
		byte lastByte = dbInput.readByte();
		// verify the last byte
		if( lastByte != BrlcadDb.DB5HDR_MAGIC2 )
		{
			throw new IOException( "Corrupted file (bad magic2)" );
		}
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public byte getMajorType()
	{
		return this.majorType;
	}

	public byte getMinorType()
	{
		return this.minorType;
	}
	
	public byte[] getBody()
	{
		return this.body;
	}
	
	public byte[] getAttributes()
	{
		return this.attributes;
	}
}

