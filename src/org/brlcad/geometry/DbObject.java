package org.brlcad.geometry;
/**
 * This class is the base class for most of the BRL-CAD objects. It is only used directly for objects
 * that are "attribute-only" objects (such as the "GLOBAL" object).
 *
 * @author Created by Omnicore CodeGuide
 */

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.brlcad.preppedGeometry.PreppedCombination;
import org.brlcad.preppedGeometry.PreppedObject;

import org.brlcad.numerics.Matrix;
import org.brlcad.spacePartition.PreppedDb;

public abstract class DbObject implements Serializable
{
	/** the name of this object */
	String name;
	
	/** a map of atributes associated with this object */
	Map < String, String > attributes;
	
	/**
	 * Construct a DbObject from the specified DbExternal object
	 *
	 * @param    dbExt               a  DbExternal
	 *
	 */
	protected DbObject( DbExternal dbExt )
	{
		// get the object name
		this.name = new String( dbExt.getName() );
		
		// create an empty map of attributes
		this.attributes = new HashMap <String, String>();
		
		// Get the attibute bytes from the external object
		byte[] attrBytes = dbExt.getAttributes();
		if( attrBytes == null )
		{
			return;
		}
		
		// Create buffers to hold the attribute names and values as we proces them
		StringBuffer attrName = new StringBuffer();
		StringBuffer attrValue = new StringBuffer();
		
		// process the entire array of bytes
		int i=0;
		while( attrBytes[i] != (byte)0 ) // a zero byte signls the end of the array
		{
			// reset the buffers
			attrName.setLength( 0 );
			attrValue.setLength( 0 );
			
			// read the attribute name (ends with a zero byte)
			while( attrBytes[i] != (byte)0 )
			{
				attrName.append( (char)attrBytes[i] );
				i++;
			}
			i++;
			
			// read the attibute vale (ends with a zero byte)
			while( attrBytes[i] != (byte)0 )
			{
				attrValue.append( (char)attrBytes[i] );
				i++;
			}
			i++;
			
			// if we have something meaningful, save it in the atributes map
			if( attrName.length() > 0 && attrValue.length() > 0 )
			{
				this.attributes.put( attrName.toString(), attrValue.toString() );
			}
		}
	}
	
	/**
	 * Create a String representation of this class
	 *
	 * @return   a String representation of this class
	 *
	 */
    @Override
	public String toString()
	{
		StringBuffer desc = new StringBuffer();
		desc.append( this.name + ":\n" );
		Set <String> keys = this.attributes.keySet();
		for( String key:keys )
		{
			desc.append( "\t" + key + " = " + this.attributes.get( key ) + "\n" );
		}
		return desc.toString();
	}
	
	/**
	 * Get the attribute value corresponding to the specified name (or null if
	 * there is no value fo rthe specified name)
	 *
	 * @param    attrName            the attribute name (a String)
	 *
	 * @return   the attribute value (a String)
	 *
	 */
	public String getAttribute( String attrName )
	{
		return this.attributes.get( attrName );
	}
	
	/**
	 * Get all attribute values for this object
	 *
	 * @return   attribute values (Map<String, String>)
	 *
	 */
	public Map<String, String> getAttributes()
	{
		return new HashMap<String, String>(this.attributes);
	}
	
	/**
	 * Method getName
	 *
	 * @return   a String
	 *
	 */
	public String getName()
	{
		return this.name;
	}
	
	public abstract PreppedObject prep( PreppedCombination reg, PreppedDb preppedDb, Matrix matrix) throws BadGeometryException, DbException, IOException, DbNameNotFoundException;

}

