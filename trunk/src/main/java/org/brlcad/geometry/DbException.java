package org.brlcad.geometry;
/**
 * DbException.java
 *
 * @author Created by Omnicore CodeGuide
 */


public class DbException extends Exception
{
	public DbException( String msg )
	{
		super( msg );
	}
	
	public DbException( String msg, Throwable cause )
	{
		super( msg, cause );
	}
}

