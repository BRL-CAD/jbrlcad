/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jra
 */
public class ArrayCopyTest {
    public ArrayCopyTest() {}

    @Test
    public void TestCopyOf() {
        Integer[] orig = new Integer[5];
        for( int i=0 ; i<orig.length ; i++ ) {
            orig[i] = new Integer(i);
        }

        // test copy length = 0
        int copyLength = 0;
        Integer[] copy = ArrayCopy.copyOf(orig, copyLength);
        assertEquals("Expected copy length of " + copyLength + ", but got " + copy.length, copyLength, copy.length);

        // test copy less than entire array
        copyLength = 3;
        copy = ArrayCopy.copyOf(orig, copyLength);
        assertEquals("Expected copy length of " + copyLength + ", but got " + copy.length, copyLength, copy.length);
        for( int i=0 ; i<copyLength ; i++ ) {
            assertEquals( "Expected copy element at " + i + " to be " + orig[i]+ ", but got " + copy[i], orig[i], copy[i] );
        }

        // test copy entire array
        copyLength = orig.length;
        copy = ArrayCopy.copyOf(orig, copyLength);
        assertEquals("Expected copy length of " + copyLength + ", but got " + copy.length, copyLength, copy.length);
        for( int i=0 ; i<copyLength ; i++ ) {
            assertEquals( "Expected copy element at " + i + " to be " + orig[i] + ", but got " + copy[i], orig[i], copy[i] );
        }

        // test copy more than entire array
        copyLength = orig.length + 2;
        copy = ArrayCopy.copyOf(orig, copyLength);
        assertEquals("Expected copy length of " + copyLength + ", but got " + copy.length, copyLength, copy.length);
        for( int i=0 ; i<copyLength ; i++ ) {
            if (i < orig.length ) {
                assertEquals("Expected copy element at " + i + " to be " + orig[i] + ", but got " + copy[i], orig[i], copy[i]);
            } else {
                assertNull("Expected copy element at " + i + " to be null, but got " + copy[i], copy[i]);
            }
        }

        // test negative length
        boolean gotException = false;
        copyLength = -5;
        try {
            copy = ArrayCopy.copyOf(orig, copyLength);
        } catch( NegativeArraySizeException nas ) {
            gotException = true;
        }
        if( !gotException ) {
            fail("Failed to get NegativeArraySizeException");
        }

    }

    @Test
    public void TestCopyOfRange() {

        Integer[] orig = new Integer[5];
        for( int i=0 ; i<orig.length ; i++ ) {
            orig[i] = new Integer(i);
        }


        // test copy from = 0 to = 0
        int from = 0;
        int to = 0;
        Integer[] copy = ArrayCopy.copyOfRange(orig, from, to);
        assertEquals("Expected copy length of " + 0 + ", but got " + copy.length, 0, copy.length);

        // test copy from 0 to orig.length
        from = 0;
        to = orig.length - 2;
        copy = ArrayCopy.copyOfRange(orig, from, to);
        assertEquals("Expected copy length of " + (to-from) + ", but got " + copy.length, (to-from), copy.length);
        for( int i=0 ; i<copy.length ; i++ ) {
            assertEquals( "Expected copy element at " + i + " to be " + orig[i]+ ", but got " + copy[i], orig[i], copy[i] );
        }

        // test copy entire array
        from = 0;
        to = orig.length;
        copy = ArrayCopy.copyOfRange(orig, from, to);
        assertEquals("Expected copy length of " + orig.length + ", but got " + copy.length, orig.length, copy.length);
        for( int i=0 ; i<copy.length ; i++ ) {
            assertEquals( "Expected copy element at " + i + " to be " + orig[i] + ", but got " + copy[i], orig[i], copy[i] );
        }

        // test copy more than entire array
        from = 0;
        to = orig.length + 2;
        copy = ArrayCopy.copyOfRange(orig, from, to);
        assertEquals("Expected copy length of " + (to-from) + ", but got " + copy.length, (to-from), copy.length);
        for( int i=0 ; i<copy.length ; i++ ) {
            if (i < orig.length ) {
                assertEquals("Expected copy element at " + i + " to be " + orig[i] + ", but got " + copy[i], orig[i], copy[i]);
            } else {
                assertEquals("Expected copy element at " + i + " to be " + null + ", but got " + copy[i], null, copy[i]);
            }
        }

        // test illegal from/to values
        boolean gotException = false;
        from = -1;
        to = orig.length -1;
        try {
            copy = ArrayCopy.copyOfRange(orig, from, to);
        } catch( ArrayIndexOutOfBoundsException nas ) {
            gotException = true;
        }
        if( !gotException ) {
            fail("Failed to get ArrayIndexOutOfBoundsException");
        }

        gotException = false;
        from = orig.length + 2;
        to = orig.length -1;
        try {
            copy = ArrayCopy.copyOfRange(orig, from, to);
        } catch( IllegalArgumentException nas ) {
            gotException = true;
        }
        if( !gotException ) {
            fail("Failed to get IllegalArgumentException");
        }

        gotException = false;
        from = 2;
        to = 1;
        try {
            copy = ArrayCopy.copyOfRange(orig, from, to);
        } catch( IllegalArgumentException nas ) {
            gotException = true;
        }
        if( !gotException ) {
            fail("Failed to get IllegalArgumentException");
        }

        // check for NPE
        gotException = false;
        from = 0;
        to = 1;
        try {
            copy = ArrayCopy.copyOfRange(null, from, to);
        } catch( NullPointerException nas ) {
            gotException = true;
        }
        if( !gotException ) {
            fail("Failed to get NullPointerException");
        }

    }

}
