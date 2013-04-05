/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */

package org.brlcad.numerics;

/**
 * A static hashcode function for doubles that insures that the hashcode for
 * -0.0 is the same as that for +0.0
 *
 * @author John R. Anderson
 */
public class DoubleHash {

    /**
     * Initial value for hash calculations
     */
    private static final int INITIAL_VALUE = 19 * 17;

    /**
     * Method hashCode
     *
     * @param val a double
     * @return an int
     */
    public static int hashCode(double val) {
        if (Double.isInfinite(val) || Double.isNaN(val)) {
            throw new IllegalArgumentException(
                    "hashCode() called with illegal value: " + val);
        }

        long d = 0;
        int result = 0;

        // if the double is -0.0 use the value 0.0
        if (val == -0.0) {
            d = Double.doubleToLongBits(0.0);
        } else {
            d = Double.doubleToLongBits(val);
        }
        result = INITIAL_VALUE + (int) (d ^ d >>> 32);

        return result;
    }
}
