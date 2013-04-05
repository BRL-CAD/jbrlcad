/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.numerics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jra
 */
public class PolynomialTest {

    public PolynomialTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDiv1() {
        Polynomial dividend = new Polynomial(3);
        dividend.setCoeff(0, 1.0);
        dividend.setCoeff(1, 9.0);
        dividend.setCoeff(2, 23.0);
        dividend.setCoeff(3, 15.0);

        Polynomial divisor = new Polynomial(2);
        divisor.setCoeff(0, 1.0);
        divisor.setCoeff(1, 4.0);
        divisor.setCoeff(2, 3.0);

        Polynomial remainder = dividend.syntheticDivision(divisor);
        for( int i=0 ; i<=remainder.getDegree() ; i++ ) {
            assertTrue("remainder not zero; " + remainder, remainder.getCoeff(i) == 0.0);
        }
        assertTrue( "expected quotient X+5, but got " + dividend, dividend.getCoeff(0) == 1.0 );
        assertTrue( "expected quotient X+5, but got " + dividend, dividend.getCoeff(1) == 5.0 );
    }

    @Test
    public void testDiv2() {
        Polynomial dividend = new Polynomial(3);
        dividend.setCoeff(0, 1.0);
        dividend.setCoeff(1, 9.0);
        dividend.setCoeff(2, 23.0);
        dividend.setCoeff(3, 15.0);

        Polynomial divisor = new Polynomial(1);
        divisor.setCoeff(0, 1.0);
        divisor.setCoeff(1, 5.0);

        Polynomial remainder = dividend.syntheticDivision(divisor);
        for( int i=0 ; i<=remainder.getDegree() ; i++ ) {
            assertTrue("remainder not zero; " + remainder, remainder.getCoeff(i) == 0.0);
        }
        assertTrue( "expected quotient X^2+4X+3, but got " + dividend, dividend.getCoeff(0) == 1.0 );
        assertTrue( "expected quotient X^2+4X+3, but got " + dividend, dividend.getCoeff(1) == 4.0 );
        assertTrue( "expected quotient X^2+4X+3, but got " + dividend, dividend.getCoeff(2) == 3.0 );
    }

    @Test
    public void testQuadratic1() {
        Polynomial quad = new Polynomial(2);
        quad.setCoeff(0, 1.0);
        quad.setCoeff(1, 5.0);
        quad.setCoeff(2, 6.0);

        Complex[] expectedRoots = new Complex[2];
        expectedRoots[0] = new Complex( -3.0, 0.0 );
        expectedRoots[1] = new Complex( -2.0, 0.0 );

        Complex[] roots = null;
        try {
            roots = quad.roots();
        } catch (IllegalArgumentException ex) {
            fail(ex.getMessage());
        }
        
        assertTrue("expected " + expectedRoots.length + " roots, but found " + roots.length,
                expectedRoots.length == roots.length);

        for( int i=0 ; i<expectedRoots.length ; i++ ) {
            assertTrue("Expected root " + expectedRoots[i] + ", but got " + roots[i],
                    expectedRoots[i].equals(roots[i]));
        }
    }

    @Test
    public void testQuadratic2() {
        Polynomial quad = new Polynomial(2);
        quad.setCoeff(0, 1.0);
        quad.setCoeff(1, 2.0);
        quad.setCoeff(2, 1.0);

        Complex[] expectedRoots = new Complex[2];
        expectedRoots[0] = new Complex( -1.0, 0.0 );
        expectedRoots[1] = new Complex( -1.0, 0.0 );

        Complex[] roots = null;
        try {
            roots = quad.roots();
        } catch (IllegalArgumentException ex) {
            fail(ex.getMessage());
        }

        assertTrue("expected " + expectedRoots.length + " roots, but found " + roots.length,
                expectedRoots.length == roots.length);

        for( int i=0 ; i<expectedRoots.length ; i++ ) {
            assertTrue("Expected root " + expectedRoots[i] + ", but got " + roots[i],
                    expectedRoots[i].equals(roots[i]));
        }
    }

    @Test
    public void testQuadratic3() {
        Polynomial quad = new Polynomial(2);
        quad.setCoeff(0, 0.5);
        quad.setCoeff(1, 1.0);
        quad.setCoeff(2, 5.0);

        Complex[] expectedRoots = new Complex[2];
        expectedRoots[0] = new Complex( -1.0, 3.0 );
        expectedRoots[1] = new Complex( -1.0, -3.0 );

        Complex[] roots = null;
        try {
            roots = quad.roots();
        } catch (IllegalArgumentException ex) {
            fail(ex.getMessage());
        }

        assertTrue("expected " + expectedRoots.length + " roots, but found " + roots.length,
                expectedRoots.length == roots.length);

        for( int i=0 ; i<expectedRoots.length ; i++ ) {
            assertTrue("Expected root " + expectedRoots[i] + ", but got " + roots[i],
                    expectedRoots[i].equals(roots[i]));
        }
    }

    @Test
    public void testCubic1() {
        Polynomial cubic = new Polynomial(3);
        cubic.setCoeff(0, 1.0);
        cubic.setCoeff(1, -6.0);
        cubic.setCoeff(2, 11.0);
        cubic.setCoeff(3, -6.0);

        Complex[] expectedRoots = new Complex[3];
        expectedRoots[0] = new Complex( 3.0, 0.0 );
        expectedRoots[1] = new Complex( 2.0, 0.0 );
        expectedRoots[2] = new Complex( 1.0, 0.0 );

        Complex[] roots = null;
        try {
            roots = cubic.roots();
        } catch (IllegalArgumentException ex) {
            fail(ex.getMessage());
        }

        assertTrue("expected " + expectedRoots.length + " roots, but found " + roots.length,
                expectedRoots.length == roots.length);

        for( int i=0 ; i<expectedRoots.length ; i++ ) {
            assertTrue("Expected root #" + i + ": " + expectedRoots[i] + ", but got " + roots[i],
                    expectedRoots[i].approxEquals(roots[i], 0.0000000005));
        }
    }

    @Test
    public void testCubic2() {
        Polynomial cubic = new Polynomial(3);
        cubic.setCoeff(0, 1.0);
        cubic.setCoeff(1, 2.0);
        cubic.setCoeff(2, -1.0);
        cubic.setCoeff(3, -2.0);

        Complex[] expectedRoots = new Complex[3];
        expectedRoots[0] = new Complex( 1.0, 0.0 );
        expectedRoots[1] = new Complex( -1.0, 0.0 );
        expectedRoots[2] = new Complex( -2.0, 0.0 );

        Complex[] roots = null;
        try {
            roots = cubic.roots();
        } catch (IllegalArgumentException ex) {
            fail(ex.getMessage());
        }

        assertTrue("expected " + expectedRoots.length + " roots, but found " + roots.length,
                expectedRoots.length == roots.length);

        for( int i=0 ; i<expectedRoots.length ; i++ ) {
            assertTrue("Expected root #" + i + ": " + expectedRoots[i] + ", but got " + roots[i],
                    expectedRoots[i].approxEquals(roots[i], 0.0000000005));
        }
    }

    @Test
    public void testCubic3() {
        Polynomial cubic = new Polynomial(3);
        cubic.setCoeff(0, 1.0);
        cubic.setCoeff(1, -9.0);
        cubic.setCoeff(2, 19.0);
        cubic.setCoeff(3, 29.0);

        Complex[] expectedRoots = new Complex[3];
        expectedRoots[0] = new Complex( -1.0, 0.0 );
        expectedRoots[1] = new Complex( 5.0, 2.0 );
        expectedRoots[2] = new Complex( 5.0, -2.0 );

        Complex[] roots = null;
        try {
            roots = cubic.roots();
        } catch (IllegalArgumentException ex) {
            fail(ex.getMessage());
        }

        assertTrue("expected " + expectedRoots.length + " roots, but found " + roots.length,
                expectedRoots.length == roots.length);

        for( int i=0 ; i<expectedRoots.length ; i++ ) {
            assertTrue("Expected root #" + i + ": " + expectedRoots[i] + ", but got " + roots[i],
                    expectedRoots[i].approxEquals(roots[i], 0.0000000005));
        }
    }

    @Test
    public void testQuartic1() {
        Polynomial quartic = new Polynomial(4);
        quartic.setCoeff(0, 1.0);
        quartic.setCoeff(1, 6.0);
        quartic.setCoeff(2, -5.0);
        quartic.setCoeff(3, -10.0);
        quartic.setCoeff(4, -3.0);

        Complex[] expectedRoots = new Complex[4];
        expectedRoots[0] = new Complex( (-7.0 + Math.sqrt(37.0))/2.0, 0.0 );
        expectedRoots[1] = new Complex( (1.0 + Math.sqrt(5.0))/2.0, 0.0 );
        expectedRoots[2] = new Complex( (-7.0 - Math.sqrt(37.0))/2.0, 0.0 );
        expectedRoots[3] = new Complex( (1.0 - Math.sqrt(5.0))/2.0, 0.0 );

        Complex[] roots = null;
        try {
            roots = quartic.roots();
        } catch (IllegalArgumentException ex) {
            fail(ex.getMessage());
        }

        assertTrue("expected " + expectedRoots.length + " roots, but found " + roots.length,
                expectedRoots.length == roots.length);

        for( int i=0 ; i<expectedRoots.length ; i++ ) {
            assertTrue("Expected root #" + i + ": " + expectedRoots[i] + ", but got " + roots[i],
                    expectedRoots[i].approxEquals(roots[i], 0.0000000005));
        }
    }

    @Test
    public void testQuartic2() {
        Polynomial quartic = new Polynomial(4);
        quartic.setCoeff(0, 1.0);
        quartic.setCoeff(1, 6.0);
        quartic.setCoeff(2, 7.0);
        quartic.setCoeff(3, -7.0);
        quartic.setCoeff(4, -12.0);

        Complex[] expectedRoots = new Complex[4];
        expectedRoots[0] = new Complex( -4.0, 0.0 );
        expectedRoots[1] = new Complex( 1.1478990357, 0.0 );
        expectedRoots[2] = new Complex( -1.5739495179, 0.3689894075 );
        expectedRoots[3] = new Complex( -1.5739495179, -0.3689894075 );

        Complex[] roots = null;
        try {
            roots = quartic.roots();
        } catch (IllegalArgumentException ex) {
            fail(ex.getMessage());
        }

        assertTrue("expected " + expectedRoots.length + " roots, but found " + roots.length,
                expectedRoots.length == roots.length);

        for( int i=0 ; i<expectedRoots.length ; i++ ) {
            assertTrue("Expected root #" + i + ": " + expectedRoots[i] + ", but got " + roots[i],
                    expectedRoots[i].approxEquals(roots[i], 0.0000000005));
        }

        quartic = new Polynomial(4);
        quartic.setCoeff(0, 1.0);
        quartic.setCoeff(1, 6.0);
        quartic.setCoeff(2, 7.0);
        quartic.setCoeff(3, -7.0);
        quartic.setCoeff(4, -12.0);

        try {
            roots = quartic.roots();
        } catch (IllegalArgumentException ex) {
            fail( ex.getMessage() );
        }

        assertTrue("expected " + expectedRoots.length + " roots, but found " + roots.length,
                expectedRoots.length == roots.length);

        for( int i=0 ; i<expectedRoots.length ; i++ ) {
            assertTrue("Expected root #" + i + ": " + expectedRoots[i] + ", but got " + roots[i],
                    expectedRoots[i].approxEquals(roots[i], 0.0000000005));
        }

    }
}