/*
 * Utilities to replace a couple of Arrays utilities that are not available in Java 5
 */

package org.brlcad.utils;

import java.lang.reflect.Array;

/**
 *
 * @author jra
 */
public class ArrayCopy {

    public static <T> T[] copyOfRange(T[] original, int from, int to) {
        int len = to - from;
        if( len < 0 ) {
            throw new IllegalArgumentException( "from cannot be larger than to");
        }
        T[] copy = (T[])Array.newInstance(original.getClass().getComponentType(), len);
        int useableLen = original.length - from;
        if( useableLen < len ) {
            System.arraycopy(original, from, copy, 0, useableLen);
        } else {
            System.arraycopy(original, from, copy, 0, len);
        }
        return copy;
    }

    public static <T> T[] copyOf(T[] original, int newLength) {
        T[] copy = (T[]) Array.newInstance(original.getClass().getComponentType(), newLength);
        if( newLength < original.length ) {
            System.arraycopy(original, 0, copy, 0, newLength);
        } else {
            System.arraycopy(original, 0, copy, 0, original.length);
        }
        return copy;
    }
}
