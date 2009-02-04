/* Generated by Together */

package org.brlcad.numerics;

import java.io.Serializable;


/**
 * Ray.
 * <p>
 * <b>Source:</b>
 * <p>
 * The U. S. Army Research Laboratory Aberdeen Proving Ground, Maryland 21005
 * USA
 * </p>
 * <p>
 * <b>Copyright Notice:</b>
 * <p>
 * This software is Copyright (C) 2004 by the United States Army. All rights
 * reserved.
 * </p>
 * <p/>
 * <b>Responsibilities:</b>
 * <ul>
 * <li>Represents a 3D ray.</li>
 * </ul>
 * <p/>
 * <b>Collaboration:</b>
 * <ul>
 * <li>Uses a Point and Vector3.</li>
 * </ul>
 */
public class Ray implements Serializable {

    static final long serialVersionUID = -6058668611916514230L;

    private Point start;

    private Vector3 direction;

    /**
     * Construct a Ray from a Point and a Vector3. New Point and Vector3 are
     * created to keep this Ray independent
     *
     * @param s The starting Point of the Ray
     * @param d The direction vector of the Ray
     */
    public Ray(Point s, Vector3 d) {
        if (s == null || !Point.isValidPoint(s)) {
            throw new IllegalArgumentException(
                    "Point for Ray constructor either null or not valid.");
        }
        if (d == null || !Vector3.isValidVector(d)
                || d.magnitude() <= Double.MIN_VALUE) {
            throw new IllegalArgumentException(
                    "Vector for Ray constructor either null, not valid, or vector magnitude is zero.");
        }
        start = new Point(s);
        direction = new Vector3(d);
    }

    /**
     * Produce a String representation of the Ray of the form "start: (x, y, z),
     * direction: (x, y, z)"
     *
     * @return A String with the expected values
     */
    public String toString() {
        return "start: " + start + ", direction: " + direction;
    }

    /**
     * Get the start point of this Ray
     *
     * @return The start Point of this Ray
     */
    public Point getStart() {
        return (start);
    }

    /**
     * Get the direction vector for this Ray
     *
     * @return The direction Vector3 for this Ray
     */
    public Vector3 getDirection() {
        return (direction);
    }

    /**
     * Test equality of rays.
     *
     * @param r Ray to compare against.
     * @return if they're equal.
     */
    public boolean isEqual(Ray r) {
        if (r == null || !Ray.isValidRay(r)) {
            throw new IllegalArgumentException(
                    "isEqual() called with Ray that is either null or not valid");
        }
        return (start.isEqual(r.getStart()) && direction.isEqual(r
                .getDirection()));
    }

    /**
     * Test equality of rays.
     *
     * @param p1 A ray.
     * @param p2 The ray to compare p1 against.
     * @return if they're equal.
     */
    public static boolean isEqual(Ray p1, Ray p2) {
        if (p1 == null && p2 == null) {
            return true;
        }
        if (p1 == null || p2 == null) {
            return false;
        }

        return p1.isEqual(p2);
    }

    /**
     * Reviews the point and vector portions of a ray to ensure that none are
     * null. Also reviews the internal values of the point and vector to ensure
     * none are Infinity or Not-A-Number (NaN).
     *
     * @param r Input ray
     * @return true, if the ray is valid
     */
    public static boolean isValidRay(Ray r) {
        if (r == null || r.start == null || !Point.isValidPoint(r.start)
                || r.direction == null || !Vector3.isValidVector(r.direction)) {
            return false;
        }
        return true;
    }
}
