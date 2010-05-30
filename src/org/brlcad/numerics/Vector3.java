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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import javax.measure.quantity.Angle;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

/**
 * Vector in 3-space from point 0,0,0
 *
 * @author Paul Cropper Acquired from
 *         http://www.iesd.dmu.ac.uk/~pcc/dls/docs/intro.htm
 * @version 1.0 March 2001
 */

public class Vector3 implements Serializable {

    public static final Vector3 I = new Vector3(1, 0, 0);
    public static final Vector3 J = new Vector3(0, 1, 0);
    public static final Vector3 K = new Vector3(0, 0, 1);
    public static final Vector3 ZERO = new Vector3(0, 0, 0);

    static final long serialVersionUID = 3335936554470041104L;

    private static final double PI_BY_2 = Math.PI / 2.0;

    private static final double INITIAL_TOLERANCE = 1.0E-11;

    private double tolerance = INITIAL_TOLERANCE;

    private double x = 0.0;

    private double y = 0.0;

    private double z = 0.0;

    /* mag is set to NaN whenever it is no longer up to date */
    private double mag = Double.NaN;

    /**
     * Generate a new Vector with coordinates 0,0,0.
     */
    public Vector3() {
    }

    /**
     * Generate new vector with supplied x, y, and z values.
     *
     * @param x1 Value for x.
     * @param y1 Value for y.
     * @param z1 Value for z.
     * @throws IllegalArgumentException if any input is NaN or Infinite.
     */
    public Vector3(double x1, double y1, double z1) {
//        if (Double.isInfinite(x1) || Double.isNaN(x1)) {
//            throw new IllegalArgumentException(
//                    "Cannot construct Vector3 from NaN/Infinite value for x: "
//                            + x1);
//        }
//        if (Double.isInfinite(y1) || Double.isNaN(y1)) {
//            throw new IllegalArgumentException(
//                    "Cannot construct Vector3 from NaN/Infinite value for y: "
//                            + y1);
//        }
//        if (Double.isInfinite(z1) || Double.isNaN(z1)) {
//            throw new IllegalArgumentException(
//                    "Cannot construct Vector3 from NaN/Infinite value for z: "
//                            + z1);
//        }

        x = x1;
        y = y1;
        z = z1;

        findMagnitude();
    }

    /**
     * Generate new vector with values copied from another vector.
     *
     * @param v Vector to copy.
     * @throws IllegalArgumentException if input is null or any input's
     *                                  coordinates is NaN or Infinite.
     */
    public Vector3(Vector3 v) {
//        if (v == null) {
//            throw new IllegalArgumentException(
//                    "Cannot construct a Vector3 from a null Vector3");
//        }
//        if (!isValidVector(v)) {
//            throw new IllegalArgumentException(
//                    "Cannot construct a Vector3 from Vector3 containing NaN/Infinite coordinates");
//        }

        x = v.x;
        y = v.y;
        z = v.z;

        findMagnitude();
    }

    /**
     * Generate new vector from theta and phi angles.
     *
     * @param theta Angle from Z axis in Radians.
     * @param phi   Angle from X axis in Radians.
     * @throws IllegalArgumentException if either input is NaN or Infinite.
     */
    public static Vector3 fromThetaAndPhi(final double theta, final double phi) {
        if (Double.isInfinite(theta) || Double.isNaN(theta)) {
            throw new IllegalArgumentException(
                    "Cannot construct Vector3 from NaN/Infinite value for theta: "
                            + theta);
        }
        if (Double.isInfinite(phi) || Double.isNaN(phi)) {
            throw new IllegalArgumentException(
                    "Cannot construct Vector3 from NaN/Infinite value for phi: "
                            + phi);
        }

        Vector3 v = new Vector3();
        v.setFieldsFromAngles(theta, phi);
        return v;
    }

    /**
     * Construct a direction vector for firing a threat from the indicated
     * azimuth and elevation angles.
     *
     * @param azimuth   An Angle (left-right).
     * @param elevation An Angle (up-down).
     * @throws IllegalArgumentException if either input is null or either
     *                                  input's value is NaN or Infinite.
     */
    public static Vector3 fromAzimuthAndElevation(Amount<Angle> azimuth, Amount<Angle> elevation) {
        if (azimuth == null) {
            throw new IllegalArgumentException(
                    "Cannot construct Vector3 from null value for azimuth");
        }
        if (elevation == null) {
            throw new IllegalArgumentException(
                    "Cannot construct Vector3 from null value for elevation");
        }

        if (Double.isInfinite(azimuth.doubleValue(SI.RADIAN))
                || Double.isNaN(azimuth.doubleValue(SI.RADIAN))) {
            throw new IllegalArgumentException(
                    "Cannot construct Vector3 from NaN/Infinite value for azimuth: "
                            + azimuth.doubleValue(SI.RADIAN));
        }
        if (Double.isInfinite(elevation.doubleValue(SI.RADIAN))
                || Double.isNaN(elevation.doubleValue(SI.RADIAN))) {
            throw new IllegalArgumentException(
                    "Cannot construct Vector3 from NaN/Infinite value for elevation: "
                            + elevation.doubleValue(SI.RADIAN));
        }

        Vector3 v = new Vector3();
        v.setFieldsFromAngles(
                (Math.PI / 2.0) - elevation.doubleValue(SI.RADIAN),
                azimuth.doubleValue(SI.RADIAN));
        v.negate();
        return v;
    }

    /**
     * Construct a direction vector for firing a threat in the direction
     * indicated by the specified yaw, pitch.
     *
     * @param yaw   An Angle (nose left-right).
     * @param pitch An Angle (nose up-down).
     * @throws IllegalArgumentException if either yaw or pitch is null or the
     *                                  value for either of these is NaN or Infinite.
     */
    public static Vector3 fromYawAndPitch(Amount<Angle> yaw, Amount<Angle> pitch) {
        if (yaw == null) {
            throw new IllegalArgumentException(
                    "Cannot construct Vector3 from null value for yaw");
        }
        if (pitch == null) {
            throw new IllegalArgumentException(
                    "Cannot construct Vector3 from null value for pitch");
        }

        if (Double.isInfinite(yaw.doubleValue(SI.RADIAN))
                || Double.isNaN(yaw.doubleValue(SI.RADIAN))) {
            throw new IllegalArgumentException(
                    "Cannot construct Vector3 from NaN/Infinite value for yaw: "
                            + yaw.doubleValue(SI.RADIAN));
        }
        if (Double.isInfinite(pitch.doubleValue(SI.RADIAN))
                || Double.isNaN(pitch.doubleValue(SI.RADIAN))) {
            throw new IllegalArgumentException(
                    "Cannot construct Vector3 from NaN/Infinite value for pitch: "
                            + pitch.doubleValue(SI.RADIAN));
        }

        Amount<Angle> elevation = pitch.opposite();
        Amount<Angle> azimuth = yaw.opposite();
        Vector3 v = new Vector3();
        v.setFieldsFromAngles((Math.PI / 2.0) - elevation.doubleValue(SI.RADIAN),
                azimuth.doubleValue(SI.RADIAN));
        return v;
    }

    /**
     * Construct a vector with coordinates read from a DataInput stream.
     *
     * @param in DataInput stream to read from.
     * @throws IOException              if coordinate values cannot be read from DataInput
     *                                  stream.
     * @throws IllegalArgumentException if DataInput stream argument is null.
     */
    public Vector3(DataInput in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException(
                    "Cannot construct Vector3 from null value for DataInput stream");
        }

        double tempx, tempy, tempz;

        try {
            tempx = in.readDouble();
            tempy = in.readDouble();
            tempz = in.readDouble();
        } catch (Exception e) {
            // Catch any exception and convert to IOException
            IOException ioe = new IOException(
                    "Failed to construct Vector3 from inputs read from DataInput stream: \n"
                            + e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }

        if (Double.isInfinite(tempx) || Double.isNaN(tempx)) {
            throw new IllegalArgumentException(
                    "Cannot construct Vector3 from NaN/Infinite value for x: "
                            + tempx);
        }
        if (Double.isInfinite(tempy) || Double.isNaN(tempy)) {
            throw new IllegalArgumentException(
                    "Cannot construct Vector3 from NaN/Infinite value for y: "
                            + tempy);
        }
        if (Double.isInfinite(tempz) || Double.isNaN(tempz)) {
            throw new IllegalArgumentException(
                    "Cannot construct Vector3 from NaN/Infinite value for z: "
                            + tempz);
        }

        x = tempx;
        y = tempy;
        z = tempz;

        findMagnitude();
    }

    public void join(double d, Vector3 v) {
        this.x += d * v.getX();
        this.y += d * v.getY();
        this.z += d * v.getZ();
    }

    /**
     * Private method to calculate and set coordinates of this vector given
     * angles of the vector.
     *
     * @param th Angle (theta) from Z axis in Radians.
     * @param p  Angle (phi) from X axis in Radians.
     */
    private void setFieldsFromAngles(double th, double p) {
        if (p >= (Math.PI + PI_BY_2)) {
            p = p - (Math.PI + PI_BY_2);
            x = Math.sin(th) * Math.sin(p);
            y = -(Math.sin(th) * Math.cos(p));
            z = Math.cos(th);
        } else if (p >= Math.PI) {
            p = p - Math.PI;
            x = -(Math.sin(th) * Math.cos(p));
            y = -(Math.sin(th) * Math.sin(p));
            z = Math.cos(th);
        } else if (p >= PI_BY_2) {
            p = p - PI_BY_2;
            x = -(Math.sin(th) * Math.sin(p));
            y = Math.sin(th) * Math.cos(p);
            z = Math.cos(th);
        } else {
            x = Math.sin(th) * Math.cos(p);
            y = Math.sin(th) * Math.sin(p);
            z = Math.cos(th);
        }

        normalize();
    }

    /**
     * Reviews all coordinates of a vector to ensure that none are Infinity or
     * Not-A-Number (NaN).
     *
     * @param v The input vector to be reviewed.
     * @return true, if input vector is valid.
     */
    public static boolean isValidVector(Vector3 v) {
        return !(v == null || Double.isInfinite(v.x) || Double.isNaN(v.x)
                || Double.isInfinite(v.y) || Double.isNaN(v.y)
                || Double.isInfinite(v.z) || Double.isNaN(v.z));
    }

    /**
     * Sets X coordinate of a vector.
     *
     * @param x The new value for the x coordinate.
     * @throws IllegalArgumentException if argument is a NaN/Infinite.
     */
    public void setX(double x) {
        if (Double.isInfinite(x) || Double.isNaN(x)) {
            throw new IllegalArgumentException(
                    "Cannot set x coordinate value to NaN/Infinite: " + x);
        }

        this.x = x;
        this.mag = Double.NaN;
    }

    /**
     * Sets Y coordinate of a vector
     *
     * @param y The new value for the y coordinate.
     * @throws IllegalArgumentException if argument is a NaN/Infinite.
     */
    public void setY(double y) {
        if (Double.isInfinite(y) || Double.isNaN(y)) {
            throw new IllegalArgumentException(
                    "Cannot set y coordinate value to NaN/Infinite: " + y);
        }

        this.y = y;
        this.mag = Double.NaN;
    }

    /**
     * Sets Z coordinate of a vector
     *
     * @param z The new value for the z coordinate.
     * @throws IllegalArgumentException if argument is a NaN/Infinite.
     */
    public void setZ(double z) {
        if (Double.isInfinite(z) || Double.isNaN(z)) {
            throw new IllegalArgumentException(
                    "Cannot set z coordinate value to NaN/Infinite: " + z);
        }

        this.z = z;
        this.mag = Double.NaN;
    }

    /**
     * Calculate and set the magnitude of a vector.
     */
    private void findMagnitude() {
        mag = Math.sqrt((x * x) + (y * y) + (z * z));
    }

    /**
     * Get the X coordinate relative to 0,0,0.
     *
     * @return The current value for the x coordinate.
     */
    public double getX() {
        return x;
    }

    /**
     * Get the Y coordinate relative to 0,0,0.
     *
     * @return The current value for the y coordinate.
     */
    public double getY() {
        return y;
    }

    /**
     * Get the Z coordinate relative to 0,0,0.
     *
     * @return The current value for the z coordinate.
     */
    public double getZ() {
        return z;
    }

    public double get( int index ) {
        switch( index ) {
            case 0:
                return x;
            case 1:
                return y;
            case 2:
                return z;
            default:
                throw new IllegalArgumentException( "get(int i) called with illegal argument (" + index + ") must be 0, 1, or 2" );
        }
    }

    public void set(int index, double d) {
        switch( index ) {
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
                throw new IllegalArgumentException( "set(int i) called with illegal argument (" + index + ") must be 0, 1, or 2" );
        }
        this.mag = Double.NaN;
    }

    public void squareElements() {
        x = x*x;
        y = y*y;
        z = z*z;

        this.mag = Double.NaN;
    }

    /**
     * Determines if the magnitude of the vector is non-zero.
     *
     * @return true, if magnitude is non-zero.
     */
    public boolean isNonZero() {
        return (magnitude() != 0.0);
    }

    /**
     * Set the tolerance for isEqual().
     *
     * @param t The new value for the tolerance.
     * @throws IllegalArgumentException if argument is a NaN/Infinite.
     */
    public void setTolerance(double t) {
        if (Double.isInfinite(t) || Double.isNaN(t)) {
            throw new IllegalArgumentException(
                    "Cannot set tolerance to NaN/Infinite: " + t);
        }
        tolerance = t;
    }

    /**
     * Get the tolerance for isEqual().
     *
     * @return The current value for tolerance.
     */
    public double getTolerance() {
        return tolerance;
    }

    /**
     * Is this vector equal to another vector?
     *
     * @param other The other vector.
     * @return true, if equal within this vector's tolerance.
     * @throws IllegalArgumentException if the argument is either null or any
     *                                  one of its coordinates is either NaN or Infinite.
     */
    public boolean isEqual(Vector3 other) {
        if (other == null) {
            throw new IllegalArgumentException(
                    "isEqual() called with null Vector3");
        }
        if (!Vector3.isValidVector(other)) {
            throw new IllegalArgumentException(
                    "isEqual() called with Vector3 containing NaN/Infinity coordinates");
        }

        return ((java.lang.Math.abs(x - other.getX()) < tolerance)
                && (java.lang.Math.abs(y - other.getY()) < tolerance) && (java.lang.Math
                .abs(z - other.getZ()) < tolerance));
    }

    /**
     * Tests if the two vectors are equal.
     *
     * @param v1 One vector.
     * @param v2 The other vector.
     * @return true, if they're equal or not (within tolerance).
     * @throws IllegalArgumentException if either argument is null or any one
     *                                  of the arguments' coordinates is either NaN or Infinite.
     */
    public static boolean isEqual(Vector3 v1, Vector3 v2) {
        if (v1 == null) {
            throw new IllegalArgumentException(
                    "isEqual() called with null Vector3 v1");
        }
        if (v2 == null) {
            throw new IllegalArgumentException(
                    "isEqual() called with null Vector3 v2");
        }

        if (!Vector3.isValidVector(v1)) {
            throw new IllegalArgumentException(
                    "isEqual() called with Vector3 v1 containing NaN/Infinity coordinates");
        }
        if (!Vector3.isValidVector(v2)) {
            throw new IllegalArgumentException(
                    "isEqual() called with Vector3 v2 containing NaN/Infinity coordinates");
        }

        return v1.isEqual(v2);
    }

    /**
     * Tests if the two vectors have the same direction. Duplicates both vectors
     * and normalizes them, then calls the normal isEqual() on those.
     *
     * @param other The other vector to compare against.
     * @return true, if they're in the same direction (within tolerance).
     * @throws IllegalArgumentException if the argument is either null or any
     *                                  one of its coordinates is either NaN or Infinite.
     */
    public boolean isEqualDir(Vector3 other) {
        if (other == null) {
            throw new IllegalArgumentException(
                    "isEqualDir() called with null Vector3");
        }
        if (!Vector3.isValidVector(other)) {
            throw new IllegalArgumentException(
                    "isEqualDir() called with Vector3 containing NaN/Infinity coordinates");
        }

        Vector3 v1 = new Vector3(x, y, z);
        Vector3 v2 = new Vector3(other);

        v1.normalize();
        v2.normalize();

        return isEqual(v1, v2);
    }

    /**
     * Test if the two vectors have the same direction. Duplicates both vectors
     * and normalizes them, then calls the normal isEqual() on those.
     *
     * @param v1 One vector.
     * @param v2 The other vector.
     * @return true, if they're in the same direction (within tolerance).
     * @throws IllegalArgumentException if either argument is null or any one
     *                                  of the arguments' coordinates is either NaN or Infinite.
     */
    public static boolean isEqualDir(Vector3 v1, Vector3 v2) {
        if (v1 == null) {
            throw new IllegalArgumentException(
                    "isEqualDir() called with null Vector v1");
        }
        if (v2 == null) {
            throw new IllegalArgumentException(
                    "isEqualDir() called with null Vector v2");
        }

        if (!Vector3.isValidVector(v1)) {
            throw new IllegalArgumentException(
                    "isEqualDir() called with Vector v1 containing NaN/Infinity coordinates");
        }
        if (!Vector3.isValidVector(v2)) {
            throw new IllegalArgumentException(
                    "isEqualDir() called with Vector v2 containing NaN/Infinity coordinates");
        }

        return v1.isEqualDir(v2);
    }

    /**
     * Angle between the vector and Z axis.
     *
     * @return Angle in Radians in the range of 0.0 to Math.PI. Will return NaN
     *         if the operation (z / vector-magnitude) is outside of the range
     *         of -1.0 to +1.0 or if either the z coordinate or the
     *         vector-magnitude is a NaN.
     */
    public double getTheta() {
        return Math.acos((z / magnitude()));
    }

    /**
     * Cosine of the angle between the vector and Z axis.
     *
     * @return Cosine of the angle. Will return Infinity or -Infinity if
     *         magnitude of the vector is 0.0 or -0.0.
     */
    public double getCosTheta() {
        return (z / magnitude());
    }

    /**
     * Angle between the vector and X axis, positive towards the Y axis.
     *
     * @return angle in Radians
     */
    public double getPhi() {
        double phi = 0.0;

        if (magnitude() < Constants.RT_LEN_TOL) {
            return 0.0;
        }

        if ((x != 0.0) && (y != 0.0)) {
            phi = Math.asin(Math.abs(y) / Math.sqrt((x * x) + (y * y)));

            if ((x < 0.0) && (y > 0.0)) {
                phi = Math.PI - phi;
            } else if ((x < 0.0) && (y < 0.0)) {
                phi = Math.PI + phi;
            } else if ((x > 0.0) && (y < 0.0)) {
                phi = (2.0 * Math.PI) - phi;
            }
        } else {
            // for x or y values that are exactly zero, return the exact phi
            if ((x == 0.0) && (y > 0.0)) {
                phi = PI_BY_2;
            } else if ((x == 0.0) && (y < 0.0)) {
                phi = Math.PI + PI_BY_2;
            } else if ((x > 0.0) && (y == 0.0)) {
                phi = 0.0;
            } else if ((x < 0.0) && (y == 0.0)) {
                phi = Math.PI;
            }
        }

        return phi;
    }

    /**
     * Sets x, y, and z coordinates of vector in one method call.
     *
     * @param x1 The new value for the x coordinate.
     * @param y1 The new value for the y coordinate.
     * @param z1 The new value for the z coordinate.
     * @throws IllegalArgumentException if any argument is a NaN/Infinite.
     */
    public void update(double x1, double y1, double z1) {
        if (Double.isInfinite(x1) || Double.isNaN(x1)) {
            throw new IllegalArgumentException(
                    "update() called with illegal value for x: " + x1);
        }
        if (Double.isInfinite(y1) || Double.isNaN(y1)) {
            throw new IllegalArgumentException(
                    "update() called with illegal value for y: " + y1);
        }
        if (Double.isInfinite(z1) || Double.isNaN(z1)) {
            throw new IllegalArgumentException(
                    "update() called with illegal value for z: " + z1);
        }

        x = x1;
        y = y1;
        z = z1;

        mag = Double.NaN;
    }

    /**
     * Sets x, y, and z coordinates of vector in one method call.
     *
     * @param v Vector containing new values for x, y, and z.
     * @throws IllegalArgumentException if the argument is either null or any
     *                                  one of its coordinates is either NaN or Infinite.
     */
    public void update(Vector3 v) {
        if (v == null) {
            throw new IllegalArgumentException(
                    "update() called with null Vector");
        }
        if (!Vector3.isValidVector(v)) {
            throw new IllegalArgumentException(
                    "update() called with Vector containing NaN/Infinity coordinates");
        }

        x = v.x;
        y = v.y;
        z = v.z;

        mag = Double.NaN;
    }

    /**
     * Sets x, y, and z coordinates with values read from a DataInput stream.
     *
     * @param in DataInput stream to read from.
     * @throws IOException              if coordinate values cannot be read from DataInput
     *                                  stream.
     * @throws IllegalArgumentException if DataInput stream argument is null.
     */
    public void update(DataInput in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException(
                    "update() called with null DataInput");
        }

        double tempx, tempy, tempz;

        try {
            tempx = in.readDouble();
            tempy = in.readDouble();
            tempz = in.readDouble();
        } catch (Exception e) {
            // Catch any exception and convert to IOException
            IOException ioe = new IOException(
                    "Failed to read updates from DataInput stream: \n"
                            + e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }

        if (Double.isInfinite(tempx) || Double.isNaN(tempx)) {
            throw new IllegalArgumentException(
                    "Cannot construct Vector3 from NaN/Infinite value for x: "
                            + tempx);
        }
        if (Double.isInfinite(tempy) || Double.isNaN(tempy)) {
            throw new IllegalArgumentException(
                    "Cannot construct Vector3 from NaN/Infinite value for y: "
                            + tempy);
        }
        if (Double.isInfinite(tempz) || Double.isNaN(tempz)) {
            throw new IllegalArgumentException(
                    "Cannot construct Vector3 from NaN/Infinite value for z: "
                            + tempz);
        }

        x = tempx;
        y = tempy;
        z = tempz;

        mag = Double.NaN;
    }

    /**
     * Add vector operation.
     *
     * @param v Vector to add.
     * @throws IllegalArgumentException if the argument is either null or any
     *                                  one of its coordinates is either NaN or Infinite.
     */
    public void plus(Vector3 v) {
//        if (v == null) {
//            throw new IllegalArgumentException("plus() called with null Vector");
//        }
//        if (!Vector3.isValidVector(v)) {
//            throw new IllegalArgumentException(
//                    "plus() called with Vector containing NaN/Infinity coordinates");
//        }

        x += v.x;
        y += v.y;
        z += v.z;

        mag = Double.NaN;
    }

    /**
     * Subtract vector operation.
     *
     * @param v Vector to subtract.
     * @throws IllegalArgumentException if the argument is either null or any
     *                                  one of its coordinates is either NaN or Infinite.
     */
    public void minus(Vector3 v) {
//        if (v == null) {
//            throw new IllegalArgumentException(
//                    "minus() called with null Vector");
//        }
//        if (!Vector3.isValidVector(v)) {
//            throw new IllegalArgumentException(
//                    "minus() called with Vector containing NaN/Infinity coordinates");
//        }

        x -= v.x;
        y -= v.y;
        z -= v.z;

        mag = Double.NaN;
    }

    /**
     * Get the magnitude of the vector.
     *
     * @return Magnitude of the vector.
     */
    public double magnitude() {
        if (Double.isNaN(mag)) {
            this.findMagnitude();
        }
        return mag;
    }

    /**
     * Find the dot product of this vector and the supplied vector.
     *
     * @param v Input vector.
     * @return Dot product value.
     * @throws IllegalArgumentException if the argument is either null or any
     *                                  one of its coordinates is either NaN or Infinite.
     */
    public double dotProduct(Vector3 v) {
//        if (v == null) {
//            throw new IllegalArgumentException(
//                    "dotProduct() called with null Vector");
//        }
//        if (!Vector3.isValidVector(v)) {
//            throw new IllegalArgumentException(
//                    "dotProduct() called with Vector containing NaN/Infinity coordinates");
//        }

        return (x * v.x) + (y * v.y) + (z * v.z);
    }

    /**
     * Find the cross product of this vector and the supplied vector.
     *
     * @param v Input vector.
     * @return Cross product vector.
     * @throws IllegalArgumentException if the argument is either null or any
     *                                  one of its coordinates is either NaN or Infinite.
     */
    public Vector3 crossProduct(Vector3 v) {
//        if (v == null) {
//            throw new IllegalArgumentException(
//                    "crossProduct() called with null Vector");
//        }
//        if (!Vector3.isValidVector(v)) {
//            throw new IllegalArgumentException(
//                    "crossProduct() called with Vector containing NaN/Infinity coordinates");
//        }

        Vector3 result = new Vector3();

        result.x = (y * v.z) - (z * v.y);
        result.y = (z * v.x) - (x * v.z);
        result.z = (x * v.y) - (y * v.x);

        return result;
    }

    /**
     * Normalize the x, y, and z coordinates of the vector so that the magnitude
     * of the vector is 1.0. If the magnitude is 0.0 (the Vector is 0,0,0), then
     * no normalization is performed. 
     */
    public void normalize() {
        findMagnitude();

        if (mag == 0.0) {
            return;
        }

        x = x / mag;
        y = y / mag;
        z = z / mag;
        mag = 1.0;
    }

    /**
     * Find the angle between this vector and the supplied vector
     *
     * @param v Input vector.
     * @return Angle in Radians in the range of 0.0 to Math.PI. Will return NaN
     *         if the operation (dot_product / (this_vector_magnitude *
     *         v_magnitude)) is outside of the range of -1.0 to +1.0 or if
     *         either of the magnitudes of the two vectors is 0.0 or NaN.
     * @throws IllegalArgumentException if the argument is either null or any
     *                                  one of its coordinates is either NaN or Infinite.
     */
    public double angleBetween(Vector3 v) {
        if (v == null) {
            throw new IllegalArgumentException(
                    "angleBetween() called with null Vector");
        }
        if (!Vector3.isValidVector(v)) {
            throw new IllegalArgumentException(
                    "angleBetween() called with Vector containing NaN/Infinity coordinates");
        }

        return Math.acos((dotProduct(v) / (magnitude() * v.magnitude())));
    }

    /**
     * Return a new vector mid-way between this vector and the supplied vector,
     * in the same plane.
     *
     * @param v Input vector.
     * @return Vector between this vector and the input vector.
     * @throws IllegalArgumentException if the argument is either null or any
     *                                  one of its coordinates is either NaN or Infinite.
     */
    public Vector3 vectorBetween(Vector3 v) {
        if (v == null) {
            throw new IllegalArgumentException(
                    "vectorBetween() called with null Vector");
        }
        if (!Vector3.isValidVector(v)) {
            throw new IllegalArgumentException(
                    "vectorBetween() called with Vector containing NaN/Infinity coordinates");
        }

        Vector3 result = new Vector3();

        result.x = (x + v.x) / 2.0;
        result.y = (y + v.y) / 2.0;
        result.z = (z + v.z) / 2.0;

        result.normalize();

        return result;
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
     * Write x,y,z coordinates to a DataOutput stream
     *
     * @param out DataOutput stream to write to.
     * @throws IOException              if coordinate values cannot be written to DataInput
     *                                  stream.
     * @throws IllegalArgumentException if DataOutpput stream argument is null.
     */
    public void write(DataOutput out) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException(
                    "write() called with null DataOutput");
        }
        try {
            out.writeDouble(x);
            out.writeDouble(y);
            out.writeDouble(z);
        } catch (Exception e) {
            // Catch any exception and convert to IOException
            IOException ioe = new IOException(
                    "Failed to write outputs to DataOutput stream: \n"
                            + e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }
    }

    /**
     * Return a string containing x,y,z coordinates
     *
     * @return String of the format "(x.xx, y.yy, z.zz)"
     */
    @Override
    public String toString() {
        return ("(" + x + ", " + y + ", " + z + ")");
    }

    /**
     * Angle between the vector and Z axis
     *
     * @param v Input vector.
     * @return Angle in Radians in the range of 0.0 to Math.PI. Will return NaN
     *         if the operation (z / vector_magnitude) is outside of the range
     *         of -1.0 to +1.0 or if the magnitude of the vector is 0.0 or NaN.
     * @throws IllegalArgumentException if the argument is either null or any
     *                                  one of its coordinates is either NaN or Infinite.
     */
    public static double getTheta(Vector3 v) {
        if (v == null) {
            throw new IllegalArgumentException(
                    "getTheta() called with null Vector");
        }
        if (!Vector3.isValidVector(v)) {
            throw new IllegalArgumentException(
                    "getTheta() called with Vector containing NaN/Infinity coordinates");
        }

        return Math.acos((v.z / v.magnitude()));
    }

    /**
     * Get elevation angle
     *
     * @param v A direction vector (not necessarily unit).
     * @return Angle between the specified vector and the XY-plane (reversed to
     *         agree with the common ARL understanding of an azimuth/elevation
     *         view, i.e, the vector is pointing toward the origin, not away).
     * @throws IllegalArgumentException if the argument is either null or any
     *                                  one of its coordinates is either NaN or Infinite.
     */
    public static Amount<Angle> getElevation(Vector3 v) {
        if (v == null) {
            throw new IllegalArgumentException(
                    "getElevation() called with null Vector");
        }
        if (!Vector3.isValidVector(v)) {
            throw new IllegalArgumentException(
                    "getElevation() called with Vector containing NaN/Infinity coordinates");
        }

        double ang;

        if (v.z != 0.0) {
            ang = Math.atan2(-v.z, Math.sqrt((v.x * v.x) + (v.y * v.y)));
        } else {
            ang = 0.0;
        }

        return Amount.valueOf(ang, Math.ulp(ang) * 4.0, SI.RADIAN);
    }

    /**
     * Get azimuth angle
     *
     * @param v A direction vector (not necessarily unit)
     * @return Angle between the specified direction and the positive X-axis
     *         (reversed to agree with the common ARL understanding of an
     *         azimuth/elevation view, i.e, the vector is pointing toward the
     *         origin, not away).
     * @throws IllegalArgumentException if the argument is either null or any
     *                                  one of its coordinates is either NaN or Infinite.
     */
    public static Amount<Angle> getAzimuth(Vector3 v) {
        if (v == null) {
            throw new IllegalArgumentException(
                    "getAzimuth() called with null Vector");
        }
        if (!Vector3.isValidVector(v)) {
            throw new IllegalArgumentException(
                    "getAzimuth() called with Vector containing NaN/Infinity coordinates");
        }

        Vector3 revDir = Vector3.negate(v);
        double ang = Vector3.getPhi(revDir);

        return Amount.valueOf(ang, Math.ulp(ang) * 2.0, SI.RADIAN);
    }

    /**
     * Angle between the vector and X axis, positive towards the Y axis.
     *
     * @param v Input vector
     * @return Angle in Radians
     * @throws IllegalArgumentException if the argument is either null or any
     *                                  one of its coordinates is either NaN or Infinite.
     */
    public static double getPhi(Vector3 v) {
        if (v == null) {
            throw new IllegalArgumentException(
                    "getPhi() called with null Vector");
        }
        if (!Vector3.isValidVector(v)) {
            throw new IllegalArgumentException(
                    "getPhi() called with Vector containing NaN/Infinity coordinates");
        }

        return v.getPhi();
    }

    /**
     * Return a new vector mid-way between vector v1 and vector v2, in the same
     * plane
     *
     * @param v1 Input vector
     * @param v2 Input vector
     * @return A new vector midway between the two inputs
     * @throws IllegalArgumentException if the either argument is null or any
     *                                  one of either argument's coordinates is either NaN or
     *                                  Infinite.
     */
    public static Vector3 vectorBetween(Vector3 v1, Vector3 v2) {
        if (v1 == null) {
            throw new IllegalArgumentException(
                    "vectorBetween() called with null Vector v1");
        }
        if (v2 == null) {
            throw new IllegalArgumentException(
                    "vectorBetween() called with null Vector v2");
        }
        if (!Vector3.isValidVector(v1)) {
            throw new IllegalArgumentException(
                    "vectorBetween() called with Vector v1 containing NaN/Infinity coordinates");
        }
        if (!Vector3.isValidVector(v2)) {
            throw new IllegalArgumentException(
                    "vectorBetween() called with Vector v2 containing NaN/Infinity coordinates");
        }

        Vector3 result = new Vector3();

        result.x = (v1.x + v2.x) / 2.0;
        result.y = (v1.y + v2.y) / 2.0;
        result.z = (v1.z + v2.z) / 2.0;
        result.normalize();

        return result;
    }

    /**
     * Find the angle between vector v1 and vector v2
     *
     * @param v1 Input vector
     * @param v2 Input vector
     * @return Angle in Radians in the range of 0.0 to Math.PI. Will return NaN
     *         if the operation (dot_product(v1,v2) / (v1_magnitude *
     *         v2_magnitude)) is outside of the range of -1.0 to +1.0 or if the
     *         magnitude of either vector is 0.0 or NaN.
     * @throws IllegalArgumentException if the either argument is null or any
     *                                  one of either argument's coordinates is either NaN or
     *                                  Infinite.
     */
    public static double angleBetween(Vector3 v1, Vector3 v2) {
        if (v1 == null) {
            throw new IllegalArgumentException(
                    "angleBetween() called with null Vector v1");
        }
        if (v2 == null) {
            throw new IllegalArgumentException(
                    "angleBetween() called with null Vector v2");
        }
        if (!Vector3.isValidVector(v1)) {
            throw new IllegalArgumentException(
                    "angleBetween() called with Vector v1 containing NaN/Infinity coordinates");
        }
        if (!Vector3.isValidVector(v2)) {
            throw new IllegalArgumentException(
                    "angleBetween() called with Vector v2 containing NaN/Infinity coordinates");
        }

        return Math.acos((Vector3.dotProduct(v1, v2) / (v1.magnitude() * v2
                .magnitude())));
    }

    /**
     * Find the angle between vector v1 and vector v2
     *
     * @param v1 Input vector
     * @param v2 Input vector
     * @return Cosine of the angle. Will return Infinity or -Infinity if the
     *         magnitude of either vector is 0.0 or -0.0.
     * @throws IllegalArgumentException if the either argument is null or any
     *                                  one of either argument's coordinates is either NaN or
     *                                  Infinite.
     */
    public static double cosOfAngleBetween(Vector3 v1, Vector3 v2) {
        if (v1 == null) {
            throw new IllegalArgumentException(
                    "cosOfAngleBetween() called with null Vector v1");
        }
        if (v2 == null) {
            throw new IllegalArgumentException(
                    "cosOfAngleBetween() called with null Vector v2");
        }
        if (!Vector3.isValidVector(v1)) {
            throw new IllegalArgumentException(
                    "cosOfAngleBetween() called with Vector v1 containing NaN/Infinity coordinates");
        }
        if (!Vector3.isValidVector(v2)) {
            throw new IllegalArgumentException(
                    "cosOfAngleBetween() called with Vector v2 containing NaN/Infinity coordinates");
        }

        return (Vector3.dotProduct(v1, v2) / (v1.magnitude() * v2.magnitude()));
    }

    /**
     * Find the cross product of vector v1 and vector v2
     *
     * @param v1 Input vector
     * @param v2 Input vector
     * @return A new vector which is the cross product of the two inputs
     * @throws IllegalArgumentException if the either argument is null or any
     *                                  one of either argument's coordinates is either NaN or
     *                                  Infinite.
     */
    public static Vector3 crossProduct(Vector3 v1, Vector3 v2) {
        if (v1 == null) {
            throw new IllegalArgumentException(
                    "crossProduct() called with null Vector v1");
        }
        if (v2 == null) {
            throw new IllegalArgumentException(
                    "crossProduct() called with null Vector v2");
        }
//        if (!Vector3.isValidVector(v1)) {
//            throw new IllegalArgumentException(
//                    "crossProduct() called with Vector v1 containing NaN/Infinity coordinates");
//        }
//        if (!Vector3.isValidVector(v2)) {
//            throw new IllegalArgumentException(
//                    "crossProduct() called with Vector v2 containing NaN/Infinity coordinates");
//        }

        Vector3 result = new Vector3();

        result.x = (v1.y * v2.z) - (v1.z * v2.y);
        result.y = (v1.z * v2.x) - (v1.x * v2.z);
        result.z = (v1.x * v2.y) - (v1.y * v2.x);

        return result;
    }

    /**
     * Find the dot product of vector v1 and vector v2
     *
     * @param v1 Input vector
     * @param v2 Input vector
     * @return Dot product of the two vectors
     * @throws IllegalArgumentException if the either argument is null or any
     *                                  one of either argument's coordinates is either NaN or
     *                                  Infinite.
     */
    public static double dotProduct(Vector3 v1, Vector3 v2) {
        if (v1 == null) {
            throw new IllegalArgumentException(
                    "dotProduct() called with null Vector v1");
        }
        if (v2 == null) {
            throw new IllegalArgumentException(
                    "dotProduct() called with null Vector v2");
        }
//        if (!Vector3.isValidVector(v1)) {
//            throw new IllegalArgumentException(
//                    "dotProduct() called with Vector v1 containing NaN/Infinity coordinates");
//        }
//        if (!Vector3.isValidVector(v2)) {
//            throw new IllegalArgumentException(
//                    "dotProduct() called with Vector v2 containing NaN/Infinity coordinates");
//        }

        return (v1.x * v2.x) + (v1.y * v2.y) + (v1.z * v2.z);
    }

    /**
     * Return a new vector, the sum of vector v1 and vector v2
     *
     * @param v1 Input vector
     * @param v2 Input vector
     * @return A new vector which is the two inputs added together
     * @throws IllegalArgumentException if the either argument is null or any
     *                                  one of either argument's coordinates is either NaN or
     *                                  Infinite.
     */
    public static Vector3 plus(Vector3 v1, Vector3 v2) {
        if (v1 == null) {
            throw new IllegalArgumentException(
                    "plus() called with null Vector v1");
        }
        if (v2 == null) {
            throw new IllegalArgumentException(
                    "plus() called with null Vector v2");
        }
//        if (!Vector3.isValidVector(v1)) {
//            throw new IllegalArgumentException(
//                    "plus() called with Vector v1 containing NaN/Infinity coordinates");
//        }
//        if (!Vector3.isValidVector(v2)) {
//            throw new IllegalArgumentException(
//                    "plus() called with Vector v2 containing NaN/Infinity coordinates");
//        }

        Vector3 result = new Vector3();

        result.x = v1.x + v2.x;
        result.y = v1.y + v2.y;
        result.z = v1.z + v2.z;

        return result;
    }

    /**
     * Return a new vector, vector v1 minus vector v2
     *
     * @param v1 Input vector
     * @param v2 Input vector
     * @return A new vector which is v1 minus v2
     * @throws IllegalArgumentException if the either argument is null or any
     *                                  one of either argument's coordinates is either NaN or
     *                                  Infinite.
     */
    public static Vector3 minus(Vector3 v1, Vector3 v2) {
        if (v1 == null) {
            throw new IllegalArgumentException(
                    "minus() called with null Vector v1");
        }
        if (v2 == null) {
            throw new IllegalArgumentException(
                    "minus() called with null Vector v2");
        }
//        if (!Vector3.isValidVector(v1)) {
//            throw new IllegalArgumentException(
//                    "minus() called with Vector v1 containing NaN/Infinity coordinates");
//        }
//        if (!Vector3.isValidVector(v2)) {
//            throw new IllegalArgumentException(
//                    "minus() called with Vector v2 containing NaN/Infinity coordinates");
//        }

        Vector3 result = new Vector3();

        result.x = v1.x - v2.x;
        result.y = v1.y - v2.y;
        result.z = v1.z - v2.z;

        return result;
    }

    /**
     * Return a new vector, the negative of vector v
     *
     * @param v Input vector
     * @return A new vector which is the negative of the input
     * @throws IllegalArgumentException if the argument is either null or any
     *                                  one of the argument's coordinates is either NaN or Infinite.
     */
    public static Vector3 negate(Vector3 v) {
        if (v == null) {
            throw new IllegalArgumentException(
                    "negate() called with null Vector");
        }
//        if (!Vector3.isValidVector(v)) {
//            throw new IllegalArgumentException(
//                    "negate() called with Vector containing NaN/Infinity coordinates");
//        }

        Vector3 result = new Vector3();

        result.x = -v.x;
        result.y = -v.y;
        result.z = -v.z;

        return result;
    }

    /**
     * Return a new vector which is the vector between two points
     *
     * @param p1 First point
     * @param p2 Second point
     * @return A new vector from p1 to p2
     * @throws IllegalArgumentException if the either argument is null or any
     *                                  one of either argument's coordinates is either NaN or
     *                                  Infinite.
     */
    public static Vector3 minus(Point p1, Point p2) {
        if (p1 == null) {
            throw new IllegalArgumentException(
                    "minus() called with null Point p1");
        }
        if (p2 == null) {
            throw new IllegalArgumentException(
                    "minus() called with null Point p2");
        }
//        if (!Point.isValidPoint(p1)) {
//            throw new IllegalArgumentException(
//                    "minus() called with Point p1 containing NaN/Infinity coordinates");
//        }
//        if (!Point.isValidPoint(p2)) {
//            throw new IllegalArgumentException(
//                    "minus() called with Point p2 containing NaN/Infinity coordinates");
//        }

        return new Vector3(p1.getX() - p2.getX(), p1.getY() - p2.getY(), p1
                .getZ()
                - p2.getZ());
    }

    /**
     * Scale this vector
     *
     * @param factor Scaling factor
     * @throws IllegalArgumentException if the argument is either NaN or
     *                                  Infinite.
     */
    public void scale(double factor) {
        if (Double.isInfinite(factor) || Double.isNaN(factor)) {
            throw new IllegalArgumentException("Illegal value for factor: "
                    + factor);
        }

        this.x *= factor;
        this.y *= factor;
        this.z *= factor;
        this.mag = Double.NaN;
    }

    /**
     * Scale a vector
     *
     * @param v1    Vector to scale
     * @param scale Scaling factor
     * @return A new vector scaled to appropriate value
     * @throws IllegalArgumentException if the the vector is null, any one of
     *                                  the vector's coordinates is either NaN or Infinite, or the
     *                                  scale factor is either NaN or Infinite.
     */
    public static Vector3 scale(Vector3 v1, double scale) {
        if (v1 == null) {
            throw new IllegalArgumentException(
                    "scale() called with null Vector");
        }
//        if (!Vector3.isValidVector(v1)) {
//            throw new IllegalArgumentException(
//                    "scale() called with Vector containing NaN/Infinity coordinates");
//        }

        if (Double.isInfinite(scale) || Double.isNaN(scale)) {
            throw new IllegalArgumentException(
                    "scale() called with illegal scale value: " + scale);
        }

        return new Vector3(v1.getX() * scale, v1.getY() * scale, v1.getZ()
                * scale);
    }

    /**
     * Rotate this vector around the specified up vector.
     *
     * @param upVector The up vector to perform rotation around.
     * @param angle    The angle (in degrees) to rotate the vector.
     * @throws IllegalArgumentException if the the vector is null, any one of
     *                                  the vector's coordinates is either NaN or Infinite, or the
     *                                  rotation angle is either NaN or Infinite.
     */
    public void rotate(Vector3 upVector, double angle) {
        if (upVector == null) {
            throw new IllegalArgumentException(
                    "rotate() called with null upVector");
        }
        if (!Vector3.isValidVector(upVector)) {
            throw new IllegalArgumentException(
                    "rotate() called with upVector containing NaN/Infinity coordinates");
        }

        if (Double.isInfinite(angle) || Double.isNaN(angle)) {
            throw new IllegalArgumentException(
                    "rotate() called with illegal angle: " + angle);
        }

        // create basis vectors with upVector as one of the directions
        Vector3 v3 = new Vector3(upVector);
        v3.normalize();

        Vector3 v1 = this.crossProduct(v3);
        v1.normalize();

        if (Double.isNaN(v1.getX()) || Double.isNaN(v1.getY())
                || Double.isNaN(v1.getZ())) {
            // this vector is parallel to the upvector, no need to change it
            return;
        }

        Vector3 v2 = Vector3.crossProduct(v3, v1);
        v2.normalize();

        double radians = angle / 180.0 * java.lang.Math.PI;
        double sine = Math.sin(radians);
        double cosine = Math.cos(radians);

        double x1 = this.dotProduct(v1);
        double y1 = this.dotProduct(v2);
        double z1 = this.dotProduct(v3);

        double x2 = x1 * cosine - y1 * sine;
        double y2 = x1 * sine + y1 * cosine;

        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;

        this.plus(scale(v1, x2));
        this.plus(scale(v2, y2));
        this.plus(scale(v3, z1));

        this.mag = Double.NaN;
    }

    /**
     * Given a vector, create a new unit vector that is orthogonal to the
     * original.
     *
     * @param in A vector
     * @return A unit vector that is orthogonal to the input vector. If the
     *         input vector has zero length, a zero length vector will be
     *         returned.
     * @throws IllegalArgumentException if the input vector is null or any one
     *                                  of its's coordinates is either NaN or Infinite.
     */
    public static Vector3 orthogonalVector(Vector3 in) {
        if (in == null) {
            throw new IllegalArgumentException(
                    "orthogonalVector() called with null Vector");
        }
        if (!Vector3.isValidVector(in)) {
            throw new IllegalArgumentException(
                    "orthogonalVector() called with Vector containing NaN/Infinity coordinates");
        }

        double xcoord = Math.abs(in.x);
        double ycoord = Math.abs(in.y);
        double zcoord = Math.abs(in.z);

        double hypot;
        double scale;

        if (xcoord < ycoord) {
            if (xcoord < zcoord) {
                // x-coord is the smallest
                hypot = Math.sqrt(ycoord * ycoord + zcoord * zcoord);
                scale = 1.0 / hypot;
                if (Double.isInfinite(scale)) {
                    return new Vector3(0.0, 0.0, 0.0);
                }
                return new Vector3(0.0, -in.z * scale, in.y * scale);
            } else {
                // z-coord is the smallest
                hypot = Math.sqrt(ycoord * ycoord + xcoord * xcoord);
                scale = 1.0 / hypot;
                if (Double.isInfinite(scale)) {
                    return new Vector3(0.0, 0.0, 0.0);
                }
                return new Vector3(-in.y * scale, in.x * scale, 0.0);
            }
        } else {
            // Y < X
            if (ycoord < zcoord) {
                // y-coord is the smallest
                hypot = Math.sqrt(xcoord * xcoord + zcoord * zcoord);
                scale = 1.0 / hypot;
                if (Double.isInfinite(scale)) {
                    return new Vector3(0.0, 0.0, 0.0);
                }
                return new Vector3(-in.z * scale, 0.0, in.x * scale);
            } else {
                // z-coord is the smallest
                hypot = Math.sqrt(xcoord * xcoord + ycoord * ycoord);
                scale = 1.0 / hypot;
                if (Double.isInfinite(scale)) {
                    return new Vector3(0.0, 0.0, 0.0);
                }
                return new Vector3(-in.y * scale, in.x * scale, 0.0);
            }
        }
    }

    /**
     * Test equality of points.
     *
     * @param o Object that may or may not be a point.
     * @return true, if 'o' is a point and the points are in the same place,
     *         within the tolerance of 'this'.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Vector3) {
            return isEqual((Vector3) o);
        }
        return false;
    }

    /**
     * Generate hash code for this object.
     *
     * @return An int containing the hash code.
     */
    @Override
    public int hashCode() {

        int result = 11;

        result = 7 * DoubleHash.hashCode(this.x) + result;
        result = 13 * DoubleHash.hashCode(this.y) + result;
        result = 17 * DoubleHash.hashCode(this.z) + result;

        return result;
    }

}
