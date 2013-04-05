/*
 * Copyright (C) 2001 De Montfort University, All Rights Reserved. De Montfort
 * University grants to you ("Licensee") a non-exclusive, non-transferable,
 * royalty free, license to use, copy, and modify this software and its
 * documentation. Licensee may redistribute the software in source and binary
 * code form provided that this copyright notice appears in all copies. DE
 * MONTFORT UNIVERSITY MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. THE AUTHORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

package org.brlcad.numerics;

import java.io.Serializable;

/*
 * $Header
 */

/**
 * Plane in 3D space The plane is represented by 4 parameters: a, b, c, d. The
 * first three parameters define a normal to the plane and the third is the
 * distance along that normal from the origin to the plane. The plane is defined
 * as all the points (p) satisfying the equation: VDOT( normal, p) - d = 0. If
 * VDOT( normal, p) - d is greater than zero, then the point is "above" the
 * plane. If VDOT( normal, p ) - d is less than zero, then the point is "below"
 * the plane.
 *
 * @author Paul Cropper Corrected by jra to implement the plane equation
 *         correctly Acquired from
 *         http://www.iesd.dmu.ac.uk/~pcc/dls/docs/intro.htm
 * @version 1.0 March 2001
 */
public class Plane3D implements Serializable {
    /**
     * The plane is represented by 4 parameters: a, b, c, d. The first three
     * parameters define a normal to the plane and the third is the distance
     * along that normal from the origin to the plane. The plane is defined as
     * all the points (p) satisfying the equation: VDOT( normal, p) - d = 0. If
     * VDOT( normal, p) - d is greater than zero, then the point is "above" the
     * plane. If VDOT( normal, p ) - d is less than zero, then the point is
     * "below" the plane.Define points which are the plane
     */
    private double a;

    private double b;

    private double c;

    private double d;

    /**
     * Construct a plane, with the supplied normal, and passing through the
     * origin.
     *
     * @param norm Vector normal to the plane
     */
    public Plane3D(Vector3 norm) {
        if (norm == null || !Vector3.isValidTriple(norm)) {
            throw new IllegalArgumentException(
                    "Vector for Plane3D constructor either null or not valid.");
        }

        a = norm.getX();
        b = norm.getY();
        c = norm.getZ();
        d = 0;
    }

    /**
     * Construct a plane, with the supplied normal, and passing through the
     * supplied point.
     *
     * @param norm Vector normal to the plane
     * @param pt   Point in the plane
     */
    public Plane3D(Vector3 norm, Point pt) {
        if (pt == null || !Point.isValidTriple(pt)) {
            throw new IllegalArgumentException(
                    "Point for Plane3D constructor either null or not valid.");
        }
        if (norm == null || !Vector3.isValidTriple(norm)) {
            throw new IllegalArgumentException(
                    "Vector for Plane3D constructor either null or not valid.");
        }

        a = norm.getX();
        b = norm.getY();
        c = norm.getZ();
        d = (a * pt.getX()) + (b * pt.getY()) + (c * pt.getZ());
    }

    /**
     * Construct a plane which contains the three supplied points
     *
     * @param p1 Point 1
     * @param p2 Point 2
     * @param p3 Point 3
     */
    public Plane3D(Point p1, Point p2, Point p3) {
        if (p1 == null || !Point.isValidTriple(p1)) {
            throw new IllegalArgumentException(
                    "Point 1 for Plane3D constructor either null or not valid.");
        }
        if (p2 == null || !Point.isValidTriple(p2)) {
            throw new IllegalArgumentException(
                    "Point 2 for Plane3D constructor either null or not valid.");
        }
        if (p3 == null || !Point.isValidTriple(p3)) {
            throw new IllegalArgumentException(
                    "Point 3 for Plane3D constructor either null or not valid.");
        }

        Vector3 norm;

        norm = Vector3.crossProduct(Vector3.minus(p1, p2), Vector3
                .minus(p1, p3));

        norm.normalize();

        a = norm.getX();
        b = norm.getY();
        c = norm.getZ();
        d = (a * p1.getX()) + (b * p1.getY()) + (c * p1.getZ());
    }

    /**
     * Does a point lie above the plane?
     *
     * @param pt The point to be tested
     * @return true, if point is above the plane
     */
    public boolean above(Point pt) {
        if (pt == null || !Point.isValidTriple(pt)) {
            throw new IllegalArgumentException(
                    "above() called with Point that is either null or not valid.");
        }

        double result;

        result = (a * pt.getX()) + (b * pt.getY()) + (c * pt.getZ()) - d;

        return (result > 0.0f);
    }

    /**
     * Does a point lie below the plane?
     *
     * @param pt The point to be tested
     * @return true, if point is below the plane
     */
    public boolean below(Point pt) {
        if (pt == null || !Point.isValidTriple(pt)) {
            throw new IllegalArgumentException(
                    "below() called with Point that is either null or not valid.");
        }

        double result;

        result = (a * pt.getX()) + (b * pt.getY()) + (c * pt.getZ()) - d;

        return (result < 0.0f);
    }

    /**
     * Does a point lie in the plane?
     *
     * @param pt The point to be tested
     * @return true, if point is in the plane
     */
    public boolean liesIn(Point pt) {
        if (pt == null || !Point.isValidTriple(pt)) {
            throw new IllegalArgumentException(
                    "liesIn() called with Point that is either null or not valid.");
        }

        double result;

        result = (a * pt.getX()) + (b * pt.getY()) + (c * pt.getZ()) - d;

        return (result == 0.0f);
    }

    public double distToPlane( Point pt ) {
        return (a * pt.getX()) + (b * pt.getY()) + (c * pt.getZ()) - d;
    }

    /**
     * Does a point lie in the plane?
     *
     * @param pt The point to be tested
     * @return true, if point is in the plane
     */
    public boolean liesIn(Point pt, double tolerance) {
        if (pt == null || !Point.isValidTriple(pt)) {
            throw new IllegalArgumentException(
                    "liesIn() called with Point that is either null or not valid.");
        }

        double result;

        result = (a * pt.getX()) + (b * pt.getY()) + (c * pt.getZ()) - d;

        return (Math.abs(result) < tolerance);
    }

    /**
     * Get the normal to the plane
     *
     * @return A new Vector3 which is the normal to the plane
     */
    public Vector3 getNormal() {
        return new Vector3(a, b, c);
    }

    /**
     * Return a String representation of this plane in the form "(a, b, c, d)"
     *
     * @return The output String
     */
    @Override
    public String toString() {
        return "(" + a + ", " + b + ", " + c + ", " + d + ")";
    }

    /**
     * Get the D coordinate of the plane.
     *
     * @return A double which has the value of the D coordinate.
     */
    public double getD() {
        return this.d;
    }

    /**
     * Get the distance between a point and this plane
     *
     * @param pt The point
     * @return the distance between the point and this plane such that pt +
     *         distance * getNormal() is on the plane
     */
    public double distanceTo(Point pt) {
        if (pt == null || !Point.isValidTriple(pt)) {
            throw new IllegalArgumentException(
                    "distanceTo() called with Point that is either null or not valid.");
        }

        return (a * pt.getX()) + (b * pt.getY()) + (c * pt.getZ()) - d;
    }

    /**
     * Intersect a Ray with this Plane
     *
     * @param r The Ray to intersect with this plane
     * @return The distance along the Ray to the intersection such that r.start +
     *         distance * r.direction is the intersection point (infinite if
     *         there is no intersection).
     */
    public double intersect(Ray r) {
        if (r == null || !Ray.isValidRay(r)) {
            throw new IllegalArgumentException(
                    "intersect() called with Ray that is either null or not valid.");
        }

        return (-this.distanceTo(r.getStart()) / this.getNormal()
                .dotProduct(r.getDirection()));
    }

    /**
     * Intersect this plane with another plane
     *
     * @param pl The other plane
     * @return The ray representing the intersection (or null if there is no
     *         intersetcion)
     */
    public Ray intersect(Plane3D pl) {
        if (pl == null || !Plane3D.isValidPlane(pl)) {
            throw new IllegalArgumentException(
                    "intersect() called with Plane that is either null or not valid.");
        }

        Ray retVal = null;
        Vector3 v1 = this.getNormal();
        // double d1 = this.getD();
        Vector3 v2 = pl.getNormal();
        double d2 = pl.getD();

        Vector3 cross = v1.crossProduct(v2);
        double magCross = cross.magnitude();
        if (1.0 / magCross == Double.POSITIVE_INFINITY) {
            return retVal;
        }

        double dot = v1.dotProduct(v2);
        double dot1 = v1.dotProduct(v1);
        double dot2 = v2.dotProduct(v2);

        double determinant = dot1 * dot2 - dot * dot;

        double c1 = (this.d * dot2 - d2 * dot) / determinant;
        double c2 = (d2 * dot1 - this.d * dot) / determinant;

        Point p = new Point(0, 0, 0);
        p.join(c1, v1);
        p.join(c2, v2);

        cross.normalize();

        retVal = new Ray(p, cross);

        return retVal;
    }

    /**
     * Reviews a plane to determine if it is null. If the plane is not null,
     * review all coordinates of the plane to ensure that none are infinity or
     * Not-A-Number (NaN).
     *
     * @param pl Plane to be tested
     * @return true, if the plane and its components are valid
     */
    public static boolean isValidPlane(Plane3D pl) {
        if (pl == null || Double.isInfinite(pl.a) || Double.isNaN(pl.a)
                || Double.isInfinite(pl.b) || Double.isNaN(pl.b)
                || Double.isInfinite(pl.c) || Double.isNaN(pl.c)
                || Double.isInfinite(pl.d) || Double.isNaN(pl.d)) {
            return false;
        }
        return true;
    }

}
