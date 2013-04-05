/**
 * Quaternion.java
 *
 * @author Created by Omnicore CodeGuide
 */

package org.brlcad.numerics;

import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Angle;
import javax.measure.unit.SI;

/**
 * Implement Quaternion algebra. Quaternion algebra is a way to represent
 * three-dimensional orientation, or other rotational quantity, associated with
 * a solid 3D object. For more information, please visit
 * http://www.euclideanspace.com/maths/algebra/realNormedAlgebra/quaternions/
 */
public class Quaternion {

    /**
     * Quaternions have four dimensions (implemented using four scalar numbers),
     * one real dimension and three imaginary dimensions. In the group below, q3
     * is the real dimension, and q0, q1, and q2 are the imaginary dimensions.
     */
    private double q0;

    private double q1;

    private double q2;

    private double q3;

    /**
     * Construct a Quaternion with real part == 1.0 and all imaginary parts ==
     * 0.0.
     */
    public Quaternion() {
        q0 = 0.0;
        q1 = 0.0;
        q2 = 0.0;
        q3 = 1.0;
    }

    /**
     * Copy constructor. Create a new Quaternion from an existing Quaternion.
     *
     * @param in The Quaternion to copy.
     */
    public Quaternion(Quaternion in) {
        if (in == null || !Quaternion.isValidQuaternion(in)) {
            throw new IllegalArgumentException(
                    "Cannot construct Quaternion from a Quaternion that is either null or contains Infinite/NaN values");
        }

        q0 = in.q0;
        q1 = in.q1;
        q2 = in.q2;
        q3 = in.q3;
    }

    /**
     * Construct a Quaternion given a specific angle of rotation and an axis.
     *
     * @param rot  An Angle, the angle of rotation
     * @param axis A Vector3, the axis of rotation
     */
    public Quaternion(Amount<Angle> rot, Vector3 axis) {
        if (rot == null) {
            throw new IllegalArgumentException(
                    "Cannot construct Quaternion from null value for rot");
        }
        if (Double.isInfinite(rot.doubleValue(SI.RADIAN))
                || Double.isNaN(rot.doubleValue(SI.RADIAN))) {
            throw new IllegalArgumentException(
                    "Cannot construct Quaternion from NaN/Infinite value for rot: "
                            + rot.doubleValue(SI.RADIAN));
        }
        if (axis == null || !Vector3.isValidTriple(axis)) {
            throw new IllegalArgumentException(
                    "Vector for Quaternion constructor either null or not valid.");
        }

        Vector3 copy = new Vector3(axis);
        copy.normalize();
        double halfAngle = rot.doubleValue(SI.RADIAN) / 2.0;
        double sin = Math.sin(halfAngle);

        q0 = copy.getX() * sin;
        q1 = copy.getY() * sin;
        q2 = copy.getZ() * sin;
        q3 = Math.cos(halfAngle);
    }

    /**
     * Construct a Quaternion given a set of yaw, pitch, and roll Angles.
     *
     * @param yaw   Left-right angle
     * @param pitch Up-down angle
     * @param roll  Clockwise/counterclockwise rotation
     */
    public Quaternion(Amount<Angle> yaw, Amount<Angle> pitch, Amount<Angle> roll) {
        if (yaw == null) {
            throw new IllegalArgumentException(
                    "Cannot construct Quaternion from null value for yaw");
        }
        if (Double.isInfinite(yaw.doubleValue(SI.RADIAN))
                || Double.isNaN(yaw.doubleValue(SI.RADIAN))) {
            throw new IllegalArgumentException(
                    "Cannot construct Quaternion from NaN/Infinite value for yaw: "
                            + yaw.doubleValue(SI.RADIAN));
        }

        if (pitch == null) {
            throw new IllegalArgumentException(
                    "Cannot construct Quaternion from null value for pitch");
        }
        if (Double.isInfinite(pitch.doubleValue(SI.RADIAN))
                || Double.isNaN(pitch.doubleValue(SI.RADIAN))) {
            throw new IllegalArgumentException(
                    "Cannot construct Quaternion from NaN/Infinite value for pitch: "
                            + pitch.doubleValue(SI.RADIAN));
        }

        if (roll == null) {
            throw new IllegalArgumentException(
                    "Cannot construct Quaternion from null value for roll");
        }
        if (Double.isInfinite(roll.doubleValue(SI.RADIAN))
                || Double.isNaN(roll.doubleValue(SI.RADIAN))) {
            throw new IllegalArgumentException(
                    "Cannot construct Quaternion from NaN/Infinite value for roll: "
                            + roll.doubleValue(SI.RADIAN));
        }

        double halfYaw = yaw.doubleValue(SI.RADIAN) / 2.0;
        double halfPitch = pitch.doubleValue(SI.RADIAN) / 2.0;
        double halfRoll = roll.doubleValue(SI.RADIAN) / 2.0;

        double siny = Math.sin(halfYaw);
        double cosy = Math.cos(halfYaw);
        double sinp = Math.sin(halfPitch);
        double cosp = Math.cos(halfPitch);
        double sinr = Math.sin(halfRoll);
        double cosr = Math.cos(halfRoll);

        q0 = cosy * cosp * sinr - siny * sinp * cosr;
        q1 = cosy * sinp * cosr + siny * cosp * sinr;
        q2 = siny * cosp * cosr - cosy * sinp * sinr;
        q3 = cosy * cosp * cosr + siny * sinp * sinr;
    }

    /**
     * Determine if the input Quaternion is valid (i.e., is not null and all
     * values are not either Infinite or NaN (Not a Number).
     *
     * @param quat Quaternion to be validated
     * @return true, if input is valid
     */
    public static boolean isValidQuaternion(Quaternion quat) {
        if (quat == null || Double.isInfinite(quat.q0) || Double.isNaN(quat.q0)
                || Double.isInfinite(quat.q1) || Double.isNaN(quat.q1)
                || Double.isInfinite(quat.q2) || Double.isNaN(quat.q2)
                || Double.isInfinite(quat.q3) || Double.isNaN(quat.q3)) {
            return false;
        }
        return true;
    }

    /**
     * Get the value for q0, an imaginary value
     *
     * @return Returns q0
     */
    public double getQ0() {
        return q0;
    }

    /**
     * Set the value for q0, an imaginary value
     *
     * @param q0 The value to set
     */
    public void setQ0(double q0) {
        if (Double.isInfinite(q0) || Double.isNaN(q0)) {
            throw new IllegalArgumentException(
                    "Cannot set q0 coordinate value to NaN/Infinite: " + q0);
        }
        this.q0 = q0;
    }

    /**
     * Get the value for q1, an imaginary value
     *
     * @return Returns q1
     */
    public double getQ1() {
        return q1;
    }

    /**
     * Set the value for q1, an imaginary value
     *
     * @param q1 The value to set
     */
    public void setQ1(double q1) {
        if (Double.isInfinite(q1) || Double.isNaN(q1)) {
            throw new IllegalArgumentException(
                    "Cannot set q1 coordinate value to NaN/Infinite: " + q1);
        }
        this.q1 = q1;
    }

    /**
     * Get the value for q2, an imaginary value
     *
     * @return Returns q2
     */
    public double getQ2() {
        return q2;
    }

    /**
     * Set the value for q2, an imaginary value
     *
     * @param q2 The value to set
     */
    public void setQ2(double q2) {
        if (Double.isInfinite(q2) || Double.isNaN(q2)) {
            throw new IllegalArgumentException(
                    "Cannot set q2 coordinate value to NaN/Infinite: " + q2);
        }
        this.q2 = q2;
    }

    /**
     * Get the value for q3, a real value
     *
     * @return Returns q3
     */
    public double getQ3() {
        return q3;
    }

    /**
     * Set the value for q3, a real value
     *
     * @param q3 The value to set
     */
    public void setQ3(double q3) {
        if (Double.isInfinite(q3) || Double.isNaN(q3)) {
            throw new IllegalArgumentException(
                    "Cannot set q3 coordinate value to NaN/Infinite: " + q3);
        }
        this.q3 = q3;
    }

    /**
     * Set all values of this Quaternion from the input
     *
     * @param quat The Quaternion to copy from
     */
    public void set(Quaternion quat) {
        if (quat == null || !Quaternion.isValidQuaternion(quat)) {
            throw new IllegalArgumentException(
                    "set() called with Quaternion that is null or contains Infinite/NaN values");
        }

        this.q0 = quat.q0;
        this.q1 = quat.q1;
        this.q2 = quat.q2;
        this.q3 = quat.q3;
    }

    /**
     * Perform a Quaternion conjugate operation
     *
     * @param quat Input Quaternion to conjugate this Quaternion
     */
    public void setConjugate(Quaternion quat) {
        if (quat == null || !Quaternion.isValidQuaternion(quat)) {
            throw new IllegalArgumentException(
                    "setConjugate() called with Quaternion that is null or contains Infinite/NaN values");
        }

        this.q0 = -quat.q0;
        this.q1 = -quat.q1;
        this.q2 = -quat.q2;
    }

    /**
     * Set Quaternian at an angle from a normalized axis
     *
     * @param rot   Amount to rotate
     * @param naxis Normal axis
     */
    public void setFromNormalizedAxisAngle(double rot, Vector3 naxis) {
        if (Double.isInfinite(rot) || Double.isNaN(rot)) {
            throw new IllegalArgumentException(
                    "setFromNormalizedAxisAngle() called with 'rot' that is Infinite/NaN");
        }
        if (naxis == null || !Vector3.isValidTriple(naxis)) {
            throw new IllegalArgumentException(
                    "setFromNormalizedAxisAngle() called with naxis that is either null or not valid.");
        }

        double halfAngle = ((rot / 180.0f) * Math.PI) / 2.0;
        double sin = Math.sin(halfAngle);

        q0 = naxis.getX() * sin;
        q1 = naxis.getY() * sin;
        q2 = naxis.getZ() * sin;
        q3 = Math.cos(halfAngle);
    }

    /**
     * Normalize this Quaternion.
     */
    public void normalize() {
        double len = Math.sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);

        if (len == 0.0) {
            return;
        }

        q0 = q0 / len;
        q1 = q1 / len;
        q2 = q2 / len;
        q3 = q3 / len;
    }

    /**
     * Negate this Quaternion.
     */
    public void negate() {
        q0 = -q0;
        q1 = -q1;
        q2 = -q2;
    }

    /**
     * Multiply this Quaternion by a Vector but return a new vector which
     * contains the result.
     *
     * @param in The input vector
     * @return The new vector
     */
    public Vector3 mult(Vector3 in) {
        if (in == null || !Vector3.isValidTriple(in)) {
            throw new IllegalArgumentException(
                    "mult() called with 'in' that is either null or contains Infinite/NaN values");
        }

        Vector3 qvec = new Vector3(q0, q1, q2);
        Vector3 out = Vector3.crossProduct(qvec, in);
        out.scale(2.0 * q3);
        out.plus(Vector3.scale(qvec, 2.0 * Vector3.dotProduct(qvec, in)));
        out.plus(Vector3.scale(in, 2.0 * q3 * q3 - 1.0));

        return out;
    }

    /**
     * Multiply two Quaternions and save the result in this Quaternion.
     *
     * @param b Input Quaternion 1
     * @param c Input Quaternion 2
     */
    public void multiplyAndSet(Quaternion b, Quaternion c) {
        if (b == null || !Quaternion.isValidQuaternion(b)) {
            throw new IllegalArgumentException(
                    "multiplyAndSet() called with Quaternion 'b' that is null or contains Infinite/NaN values");
        }
        if (c == null || !Quaternion.isValidQuaternion(c)) {
            throw new IllegalArgumentException(
                    "multiplyAndSet() called with Quaternion 'c' that is null or contains Infinite/NaN values");
        }

        q3 = b.q3 * c.q3 - b.q0 * c.q0 - b.q1 * c.q1 - b.q2 * c.q2;
        q0 = b.q3 * c.q0 + b.q0 * c.q3 + b.q1 * c.q2 - b.q2 * c.q1;
        q1 = b.q3 * c.q1 + b.q1 * c.q3 + b.q2 * c.q0 - b.q0 * c.q2;
        q2 = b.q3 * c.q2 + b.q2 * c.q3 + b.q0 * c.q1 - b.q1 * c.q0;
    }

    /**
     * Multiply two Quaternions and return the result in a new Quaternion.
     *
     * @param b Input Quaternion 1
     * @param c Input Quaternion 2
     * @return The new Quaternion which is the multiplication of the two.
     */
    public static Quaternion multiply(Quaternion b, Quaternion c) {
        if (b == null || !Quaternion.isValidQuaternion(b)) {
            throw new IllegalArgumentException(
                    "multiply() called with Quaternion 'b' that is null or contains Infinite/NaN values");
        }
        if (c == null || !Quaternion.isValidQuaternion(c)) {
            throw new IllegalArgumentException(
                    "multiply() called with Quaternion 'c' that is null or contains Infinite/NaN values");
        }

        Quaternion a = new Quaternion();

        a.q0 = b.q3 * c.q0 + b.q0 * c.q3 + b.q1 * c.q2 - b.q2 * c.q1;
        a.q1 = b.q3 * c.q1 + b.q1 * c.q3 + b.q2 * c.q0 - b.q0 * c.q2;
        a.q2 = b.q3 * c.q2 + b.q2 * c.q3 + b.q0 * c.q1 - b.q1 * c.q0;
        a.q3 = b.q3 * c.q3 - b.q0 * c.q0 - b.q1 * c.q1 - b.q2 * c.q2;

        return a;
    }

    /**
     * Override toString()
     *
     * @return A String of the form "[ m.nnn m.nnn m.nnn m.nnn ]" where m.nnn is
     *         a double w
     */
    @Override
    public String toString() {
        return "[" + q0 + ", " + q1 + ", " + q2 + ", " + q3 + "]";
    }
}
