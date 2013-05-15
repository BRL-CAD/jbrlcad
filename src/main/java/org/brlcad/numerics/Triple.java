/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.numerics;

import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import java.io.Serializable;

/**
 * @author jra
 */
public abstract class Triple implements Serializable {

    private static final long serialVersionUID = -4880011665146636047L;
    public static final double INITIAL_TOLERANCE = 1e-7; // revised to match BRL-CAD's VUNITIZE_TOL

    // units must always be  millimeters
    public static final Unit units = SI.MILLI(SI.METER);

    protected double tolerance = INITIAL_TOLERANCE;

    protected double x = 0.0;

    protected double y = 0.0;

    protected double z = 0.0;

    protected double mag = Double.NaN;

    /**
     * Set the X coordinate of the Triple
     *
     * @param x The value of the X coordinate
     */
    public void setX(double x) {
        if (Double.isInfinite(x) || Double.isNaN(x)) {
            throw new IllegalArgumentException(
                    "Cannot set Triple coordinate to illegal value: " + x);
        }
        this.x = x;
        mag = Double.NaN;
    }

    /**
     * Set the Y coordinate of the Triple
     *
     * @param y The value of the Y coordinate
     */
    public void setY(double y) {
        if (Double.isInfinite(y) || Double.isNaN(y)) {
            throw new IllegalArgumentException(
                    "Cannot set Triple coordinate to illegal value: " + y);
        }
        this.y = y;
        mag = Double.NaN;
    }

    /**
     * Set the Z coordinate of the Triple
     *
     * @param z The value of the Z coordinate
     */
    public void setZ(double z) {
        if (Double.isInfinite(z) || Double.isNaN(z)) {
            throw new IllegalArgumentException(
                    "Cannot set Triple coordinate to illegal value: " + z);
        }
        this.z = z;
        mag = Double.NaN;
    }

    /**
     * Get the X coordinate of this Triple
     *
     * @return The X coordinate of this Triple
     */
    public double getX() {
        return (x);
    }

    /**
     * Get the Y coordinate of this Triple
     *
     * @return The Y coordinate of this Triple
     */
    public double getY() {
        return (y);
    }

    /**
     * Get the Z coordinate of this Triple
     *
     * @return The Z coordinate of this Triple
     */
    public double getZ() {
        return (z);
    }

    public double get(int index) {
        switch (index) {
            case 0:
                return x;
            case 1:
                return y;
            case 2:
                return z;
            default:
                throw new IllegalArgumentException("get(int i) called with illegal argument (" + index + ") must be 0, 1, or 2");
        }
    }

    public void set(int index, double d) {
        switch (index) {
            case 0:
                this.x = d;
                break;
            case 1:
                this.y = d;
                break;
            case 2:
                this.z = d;
                break;
            default:
                throw new IllegalArgumentException("set(int i) called with illegal argument (" + index + ") must be 0, 1, or 2");
        }
        mag = Double.NaN;
    }

    /**
     * Add Triple operation.
     *
     * @param v Triple to add.
     * @throws IllegalArgumentException if the argument is either null or any
     *                                  one of its coordinates is either NaN or Infinite.
     */
    public void plus(Triple v) {
        if (v == null) {
            throw new IllegalArgumentException("plus() called with null Triple");
        }

        x += v.getX();
        y += v.getY();
        z += v.getZ();
        mag = Double.NaN;
    }

    /**
     * Subtract a Triple
     *
     * @param p The Triple to subtract
     */
    public void subtract(Triple p) {
        this.x -= p.getX();
        this.y -= p.getY();
        this.z -= p.getZ();
        mag = Double.NaN;
    }

    /**
     * Subtract a Triple
     *
     * @param p The Triple to subtract
     */
    public void minus(Triple p) {
        this.x -= p.getX();
        this.y -= p.getY();
        this.z -= p.getZ();
        mag = Double.NaN;
    }

    /**
     * Scale this Triple by the amount specified by the supplied scaleFactor
     *
     * @param scaleFactor The supplied scaling factor
     */
    public void scale(double scaleFactor) {
        if (Double.isInfinite(scaleFactor) || Double.isNaN(scaleFactor)) {
            throw new IllegalArgumentException("Illegal value of scaleFactor: "
                    + scaleFactor);
        }
        x *= scaleFactor;
        y *= scaleFactor;
        z *= scaleFactor;
        mag = Double.NaN;
    }

    /**
     * Find the dot product of this Triple and the supplied Triple.
     *
     * @param v Input Triple.
     * @return Dot product value.
     * @throws IllegalArgumentException if the argument is either null or any
     *                                  one of its coordinates is either NaN or Infinite.
     */
    public double dotProduct(Triple v) {

        return (x * v.x) + (y * v.y) + (z * v.z);
    }

    /**
     * Find the dot product of Triple v1 and Triple v2
     *
     * @param v1 Input Triple
     * @param v2 Input Triple
     * @return Dot product of the two Triples
     * @throws IllegalArgumentException if the either argument is null or any
     *                                  one of either argument's coordinates is either NaN or
     *                                  Infinite.
     */
    public static double dotProduct(Triple v1, Triple v2) {
        if (v1 == null) {
            throw new IllegalArgumentException(
                    "dotProduct() called with null Triple v1");
        }
        if (v2 == null) {
            throw new IllegalArgumentException(
                    "dotProduct() called with null Triple v2");
        }

        return (v1.x * v2.x) + (v1.y * v2.y) + (v1.z * v2.z);
    }

    /**
     * Scale the Triple by another Triple
     *
     * @param dist Distance
     * @param dir  Direction
     */
    public void join(double dist, Triple dir) {
        if (dir == null) {
            throw new IllegalArgumentException(
                    "Cannot join with a null direction vector");
        }
//        if (!Vector3.isValidTriple(dir)) {
//            throw new IllegalArgumentException(
//                    "Cannot join using Vector3 with illegal coordinates");
//        }

        if (Double.isInfinite(dist) || Double.isNaN(dist)) {
            throw new IllegalArgumentException("Illegal value of dist: " + dist);
        }

        x += dist * dir.getX();
        y += dist * dir.getY();
        z += dist * dir.getZ();
        mag = Double.NaN;
    }

    public void squareElements() {
        x = x * x;
        y = y * y;
        z = z * z;

        this.mag = Double.NaN;
    }

    /**
     * Calculate and set the magnitude of a Triple.
     */
    protected void findMagnitude() {
        mag = Math.sqrt((x * x) + (y * y) + (z * z));
    }

    /**
     * Get the magnitude of the Triple.
     *
     * @return Magnitude of the Triple.
     */
    public double magnitude() {
        if (Double.isNaN(mag)) {
            this.findMagnitude();
        }
        return mag;
    }

    /**
     * Negate the vector
     */
    public void negate() {
        x = -x;
        y = -y;
        z = -z;
    }

    /**
     * Report the current tolerance (epsilon).
     *
     * @return The comparison tolerance for this Triple
     */
    public double getTolerance() {
        return tolerance;
    }

    /**
     * Set the tolerance of this Triple
     *
     * @param t Tolerance value to set
     */
    public void setTolerance(double t) {
        if (Double.isInfinite(t) || Double.isNaN(t)) {
            throw new IllegalArgumentException("Illegal value for tolerance: "
                    + t);
        }
        tolerance = Math.abs(t);
    }

    /**
     * Implement hash calculation for this Triple.
     *
     * @return An int which is the hashcode.
     */
    @Override
    public int hashCode() {

        int result;

        result = 7 * DoubleHash.hashCode(this.x);
        result = 13 * DoubleHash.hashCode(this.y) + result;
        result = 17 * DoubleHash.hashCode(this.z) + result;

        return result;
    }

    /**
     * Test if two Triples are in the same place (within epsilon). The tolerance
     * aspect applies to half-edge length of a bounding box.
     *
     * @param p Triple to compare against
     * @return Boolean indicating if the Triples are 'close' enough to call
     *         equal.
     */
    public boolean isEqual(Triple p) {
        if (p == null || !isValidTriple(p)) {
            return false;
        }
        return (Math.abs(p.x - x) < tolerance) && (Math.abs(p.y - y) < tolerance)
                && (Math.abs(p.z - z) < tolerance);
    }

    /**
     * Tests if the two Triples are equal.
     *
     * @param v1 One Triple.
     * @param v2 The other Triple.
     * @return true, if they're equal or not (within tolerance).
     * @throws IllegalArgumentException if either argument is null or any one
     *                                  of the arguments' coordinates is either NaN or Infinite.
     */
    public static boolean isEqual(Triple v1, Triple v2) {
        if (v1 == null) {
            throw new IllegalArgumentException(
                    "isEqual() called with null Triple v1");
        }
        if (v2 == null) {
            throw new IllegalArgumentException(
                    "isEqual() called with null Triple v2");
        }

        if (!Triple.isValidTriple(v1)) {
            throw new IllegalArgumentException(
                    "isEqual() called with Triple v1 containing NaN/Infinity coordinates");
        }
        if (!Triple.isValidTriple(v2)) {
            throw new IllegalArgumentException(
                    "isEqual() called with Triple v2 containing NaN/Infinity coordinates");
        }

        return v1.isEqual(v2);
    }

    /**
     * Reviews all coordinates of a Triple to ensure that none are Infinity or
     * Not-A-Number (NaN).
     *
     * @param v The input Triple to be reviewed.
     * @return true, if input Triple is valid.
     */
    public static boolean isValidTriple(Triple v) {
        return !(v == null || Double.isInfinite(v.x) || Double.isNaN(v.x)
                || Double.isInfinite(v.y) || Double.isNaN(v.y)
                || Double.isInfinite(v.z) || Double.isNaN(v.z));
    }

    /**
     * Format a String representation of this Triple in the form "(x, y, z)"
     *
     * @return The output String
     */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

}
