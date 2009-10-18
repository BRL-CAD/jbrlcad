/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.numerics;

/**
 *
 * @author jra
 */
public enum Coord {
    X,Y,Z;
    public static Coord withOrdinal (int i) {
        switch (i) {
            case 0:
                return X;
            case 1:
                return Y;
            case 2:
                return Z;
        }
        throw new IllegalArgumentException("ordinal must be 0 through 2");
    }
}
