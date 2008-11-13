package org.brlcad.geometry;
/**
 * Operators.java
 *
 */


public enum Operator
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
	
	public String toString()
	{
		return this.name;
	}
}

