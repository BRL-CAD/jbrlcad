/*
 * Copyright (c) 2007 Your Corporation. All Rights Reserved.
 */

package org.brlcad.numerics;

import org.jscience.physics.amount.Amount;

import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import java.io.Serializable;

/**
 * Specifies a point in 3-space.
 *
 * @author Ronald A. Bowers
 * @version 1.0
 */
public class Position implements Serializable {

    private Amount<Length> positionX;
    private Amount<Length> positionY;
    private Amount<Length> positionZ;
    private Amount<Length> magnitude;
    private Vector3 direction;

    private static Unit myUnits = SI.METER;
    private static UnitConverter pointScaleConverter = Point.pointUnits.getConverterTo(myUnits);
    public static Position ZERO = Position.fromXYZVectors(Amount.valueOf(0, Length.UNIT), Amount.valueOf(0, Length.UNIT), Amount.valueOf(0, Length.UNIT));

    /**
     * Factory that constructs a Position from x, y, and z component distances.
     *
     * @param x X component
     * @param y Y component
     * @param z Z component
     * @return new Position
     */
    public static Position fromXYZVectors(final Amount<Length> x, final Amount<Length> y, final Amount<Length> z) {
        Position pos = new Position();
        Amount<Length> magnitude = (Amount<Length>) Amount.valueOf(Math.sqrt((x.doubleValue(myUnits) * x.doubleValue(myUnits)
                + y.doubleValue(myUnits) * y.doubleValue(myUnits)
                + z.doubleValue(myUnits) * z.doubleValue(myUnits))), myUnits);
        pos.positionX = x;
        pos.positionY = y;
        pos.positionZ = z;
        pos.magnitude = magnitude;
        if (magnitude.approximates(Amount.valueOf(0, Length.UNIT))) {
            pos.direction = new Vector3(0.0, 0.0, 0.0);
        } else {
            double xDir = x.doubleValue(myUnits) / magnitude.doubleValue(myUnits);
            double yDir = y.doubleValue(myUnits) / magnitude.doubleValue(myUnits);
            double zDir = z.doubleValue(myUnits) / magnitude.doubleValue(myUnits);
            pos.direction = new Vector3(xDir, yDir, zDir);
        }
        return pos;
    }

    /**
     * Factory that constructs a Position from a scalar Length and a direction.
     *
     * @param v distance from origin
     * @param d direction
     * @return new Position
     */
    public static Position fromMagnitudeAndDirection(final Amount<Length> v, final Vector3 d) {
        // Vector3 is mutable, copy defensively!
        Vector3 unitVector = new Vector3(d);
        unitVector.normalize();
        Amount<Length> tx = (Amount<Length>) Amount.valueOf(v.doubleValue(myUnits) * unitVector.getX(), myUnits);
        Amount<Length> ty = (Amount<Length>) Amount.valueOf(v.doubleValue(myUnits) * unitVector.getY(), myUnits);
        Amount<Length> tz = (Amount<Length>) Amount.valueOf(v.doubleValue(myUnits) * unitVector.getZ(), myUnits);
        Position pos = new Position();
        pos.positionX = tx;
        pos.positionY = ty;
        pos.positionZ = tz;
        pos.magnitude = v;
        pos.direction = unitVector;
        return pos;

    }

    /**
     * Factory that construct a Position from a Point (the Point must be in millimeters)
     *
     * @param p The Point (in millimeters)
     * @return The Position
     */
    public static Position fromPoint(Point p) {
        return Position.fromXYZVectors(
                (Amount<Length>) Amount.valueOf(pointScaleConverter.convert(p.getX()), myUnits),
                (Amount<Length>) Amount.valueOf(pointScaleConverter.convert(p.getY()), myUnits),
                (Amount<Length>) Amount.valueOf(pointScaleConverter.convert(p.getZ()), myUnits));
    }

    /**
     * Private
     */
    private Position() {
    }


    public Point toPointMillis() {
        Unit unit = SI.MILLI(SI.METER);
        return new Point(positionX.doubleValue(unit), positionY.doubleValue(unit), positionZ.doubleValue(unit));
    }

    /**
     * Returns the X component of the Position.
     *
     * @return the X component
     */
    public Amount<Length> getPositionX() {
        return positionX;
    }

    /**
     * Returns the Y component of the Postion.
     *
     * @return the Y component
     */
    public Amount<Length> getPositionY() {
        return positionY;
    }

    /**
     * Returns the Z component of the Position
     *
     * @return the Z component
     */
    public Amount<Length> getPositionZ() {
        return positionZ;
    }

    private void calculateMagnitude() {
        magnitude = (Amount<Length>) Amount.valueOf(Math.sqrt((positionX.doubleValue(myUnits) * positionX.doubleValue(myUnits)
                + positionY.doubleValue(myUnits) * positionY.doubleValue(myUnits)
                + positionZ.doubleValue(myUnits) * positionZ.doubleValue(myUnits))), myUnits);
    }

    private void calculateDirection() {
        if (magnitude == null) {
            this.calculateMagnitude();
        }

        double xDir = positionX.doubleValue(myUnits) / magnitude.doubleValue(myUnits);
        double yDir = positionY.doubleValue(myUnits) / magnitude.doubleValue(myUnits);
        double zDir = positionZ.doubleValue(myUnits) / magnitude.doubleValue(myUnits);
        direction = new Vector3(xDir, yDir, zDir);
    }

    /**
     * Returns the distance of the point from the origin.
     *
     * @return the magnitude
     */
    public Amount<Length> getMagnitude() {
        if (magnitude == null) {
            this.calculateMagnitude();
        }
        return magnitude;
    }

    /**
     * Returns a unit vector for the direction of the point from the origin.
     *
     * @return direction vector.
     */
    public Vector3 getDirection() {
        if (direction == null) {
            this.calculateDirection();
        }
        return new Vector3(direction);
    }

    /**
     * Creates a new Position that is this Position translated by
     * the given vector.
     *
     * @param v the vector by which to move
     * @return the new Position.
     */
    public Position join(LengthVector v) {
        Amount<Length> newX = positionX.plus(v.getLengthX());
        Amount<Length> newY = positionY.plus(v.getLengthY());
        Amount<Length> newZ = positionZ.plus(v.getLengthZ());
        return fromXYZVectors(newX, newY, newZ);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Position: ");
        sb.append(" X = ").append(positionX.toString());
        sb.append(" Y = ").append(positionY.toString());
        sb.append(" Z = ").append(positionZ.toString());
        sb.append(" Mag = ").append(magnitude.toString());
        sb.append(" Dir = ").append(direction.toString());

        return sb.toString();
    }

}
