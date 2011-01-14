/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.numerics;

/**
 *
 * @author jra
 */
public class Constants {
    // SMALL_FASTF from BRL-CAD
    public static final double SMALL_FASTF = 1.0e-77;

    // SQRT_SMALL_FASTF from BRL-CAD
    public static final double SQRT_SMALL_FASTF = 1.0e-39;

    // MAX_FASTF from BRL-CAD
    public static final double MAX_FASTF = 1.0e73;

    // SQRT_MAX_FASTF from BRL-CAD
    public static final double SQRT_MAX_FASTF = 1.0e36;

    // Constants from BRL-CAD libbn/poly.c
    public static final double SQRT3 = 1.732050808;
    public static final double THIRD = 0.333333333333333333333333333;
    public static final double INV_TWENTYSEVEN = 0.037037037037037037037037037;
    public static final  double PI_DIV_3 = Math.PI/3.0;
    
    // Constants from BRL-CAD include/raytrace.h
    public static final double RT_LEN_TOL = 1.0e-8;
    public static final double RT_DOT_TOL = 0.001;
    public static final double RT_PCOEF_TOL = 1.0e-10;
}
