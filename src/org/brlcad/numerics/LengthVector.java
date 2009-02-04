/*
 * Copyright (c) 2007 Your Corporation. All Rights Reserved.
 */

package org.brlcad.numerics;

import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

/**
 * @author Ronald A. Bowers
 * @version 1.0
 */
public class LengthVector {

    private Amount<Length> lengthX;
    private Amount<Length> lengthY;
    private Amount<Length> lengthZ;
    private Amount<Length> magnitude;
    private Vector3 direction;

    private static Unit myUnits = SI.METER;
    public static LengthVector ZERO = LengthVector.fromXYZVectors(Amount.valueOf(0, Length.UNIT), Amount.valueOf(0, Length.UNIT), Amount.valueOf(0, Length.UNIT));

    /**
     * Factory that constructs velocity vectors from x, y, and z component vectors.
     *
     * @param x X component
     * @param y Y component
     * @param z Z component
     * @return new VelocityVector
     */
    public static LengthVector fromXYZVectors(final Amount<Length> x, final Amount<Length> y, final Amount<Length> z) {
        LengthVector nv = new LengthVector();
        Amount<Length> magnitude = (Amount<Length>) Amount.valueOf(Math.sqrt((x.doubleValue(myUnits) * x.doubleValue(myUnits)
                + y.doubleValue(myUnits) * y.doubleValue(myUnits)
                + z.doubleValue(myUnits) * z.doubleValue(myUnits))), myUnits);
        nv.lengthX = x;
        nv.lengthY = y;
        nv.lengthZ = z;
        nv.magnitude = magnitude;
        if (magnitude.approximates(Amount.valueOf(0, Length.UNIT))) {
            nv.direction = new Vector3(0.0, 0.0, 0.0);
        } else {
            double xDir = x.doubleValue(myUnits) / magnitude.doubleValue(myUnits);
            double yDir = y.doubleValue(myUnits) / magnitude.doubleValue(myUnits);
            double zDir = z.doubleValue(myUnits) / magnitude.doubleValue(myUnits);
            nv.direction = new Vector3(xDir, yDir, zDir);
        }
        return nv;
    }

    /**
     * Factory that constructs a LengthVector from a scalar Length and a direction.
     *
     * @param v Length
     * @param d
     * @return new LengthVector
     */
    public static LengthVector fromMagnitudeAndDirection(final Amount<Length> v, final Vector3 d) {
        // Vector3 is mutable, copy defensively!
        Vector3 unitVector = new Vector3(d);
        unitVector.normalize();
        Amount<Length> tx = (Amount<Length>) Amount.valueOf(v.doubleValue(myUnits) * unitVector.getX(), myUnits);
        Amount<Length> ty = (Amount<Length>) Amount.valueOf(v.doubleValue(myUnits) * unitVector.getY(), myUnits);
        Amount<Length> tz = (Amount<Length>) Amount.valueOf(v.doubleValue(myUnits) * unitVector.getZ(), myUnits);
        LengthVector nv = new LengthVector();
        nv.lengthX = tx;
        nv.lengthY = ty;
        nv.lengthZ = tz;
        nv.magnitude = v;
        nv.direction = unitVector;
        return nv;

    }

    /**
     * Private
     */
    private LengthVector() { }


    /**
     * Returns the X component of the Velocity.
     *
     * @return the X component
     */
    public Amount<Length> getLengthX() {
        return lengthX;
    }

    /**
     * Returns the Y component of the Velocity.
     *
     * @return the Y component
     */
    public Amount<Length> getLengthY() {
        return lengthY;
    }

    /**
     * Returns the Z component of the Velocity
     *
     * @return the Z component
     */
    public Amount<Length> getLengthZ() {
        return lengthZ;
    }

    /**
     * Returns the magnitude of the velocity vector.
     *
     * @return the magnitude
     */
    public Amount<Length> getMagnitude() {
        return magnitude;
    }

    /**
     * Returns a unit vector for the direction of the velocity.
     *
     * @return direction vector.
     */
    public Vector3 getDirection() {
        return new Vector3(direction);
    }

    /**
     * Adds this VelocityVector to another to produce a new VelocityVector.
     *
     * @param v the vector to be added.
     * @return the vector sum.
     */
    public LengthVector add(LengthVector v) {
        Amount<Length> newX = lengthX.plus(v.getLengthX());
        Amount<Length> newY = lengthY.plus(v.getLengthY());
        Amount<Length> newZ = lengthZ.plus(v.getLengthZ());
        return fromXYZVectors(newX, newY, newZ);
    }

    /**
     * Subtracts a VelocityVector from this vector and produces a new VelocityVector
     *
     * @param v the second vector
     * @return the vector difference
     */
    public LengthVector subtract(LengthVector v) {
        Amount<Length> newX = lengthX.minus(v.getLengthX());
        Amount<Length> newY = lengthY.minus(v.getLengthY());
        Amount<Length> newZ = lengthZ.minus(v.getLengthZ());
        return fromXYZVectors(newX, newY, newZ);
    }

}
