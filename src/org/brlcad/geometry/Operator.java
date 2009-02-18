package org.brlcad.geometry;

import java.io.Serializable;

/**
 * Operators.java
 *
 */


public enum Operator implements Serializable
{
		LEAF ( "l" ),
		UNION ( "u" ),
		INTERSECTION ( "+" ),
		SUBTRACTION ( "-" ),
		XOR ( "^" ),
		NOT ( "!" );
	
	private final String name;
		
	private Operator( String name )
	{
		this.name = name;
	}
	
    @Override
	public String toString()
	{
		return this.name;
	}
}

