
package org.brlcad.numerics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bounding box calculations
 *
 * @author jra
 */
public class BoundingBox implements Serializable {

    static final long serialVersionUID = -1776036366449361312L;

    private Point min;

    private Point max;

    public BoundingBox() {
    }

    /**
     * Create a new bounding box. The two parameters are opposite corners of the
     * box.
     *
     * @param a Point describing corner of the box, opposite b.
     * @param b Point describing corner of the box, opposite a.
     */
    public BoundingBox(Point a, Point b) throws IllegalArgumentException {
        if (a == null || !Point.isValidPoint(a)) {
            throw new IllegalArgumentException(
                    "Point a for BoundingBox constructor either null or not valid.");
        }
        if (b == null || !Point.isValidPoint(b)) {
            throw new IllegalArgumentException(
                    "Point b for BoundingBox constructor either null or not valid.");
        }
        min = new Point(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b
                .getY()), Math.min(a.getZ(), b.getZ()));
        max = new Point(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b
                .getY()), Math.max(a.getZ(), b.getZ()));
    }

    /**
     * Copy Constructor
     *
     * @param bb Another BoundingBox
     */
    public BoundingBox( BoundingBox bb ) {
        this.max = new Point( bb.getMax() );
        this.min = new Point( bb.getMin() );
    }

    public void extend(BoundingBox bb) {
        if( bb == null ) {
            return;
        }

        if( this.max == null ) {
            this.max = new Point( bb.getMax() );
        } else {
            for (int i = 0; i < 3; i++) {
                if (bb.getMax().get(i) > this.max.get(i)) {
                    this.max.set(i, bb.getMax().get(i));
                }
            }
        }

        if( this.min == null ) {
            this.min = new Point( bb.getMin() );
        } else {
            for (int i = 0; i < 3; i++) {
                if (bb.getMin().get(i) < this.min.get(i)) {
                    this.min.set(i, bb.getMin().get(i));
                }
            }
        }
    }

       /**
        * Intersect this BoundingBox with another. This BoundingBox is
        * adjusted to represent the intersection. A null intersection is
        * represented by null min and null max
        *
        * @param bb    Another BoundingBox
        */
       public void intersect( BoundingBox bb )
       {
               if( this.min == null )
               {
                       return;
               }
               Point minOther = bb.getMin();
               Point maxOther = bb.getMax();
               if( minOther == null )
               {
                       this.min = null;
                       this.max = null;
                       return;
               }

               if( minOther.getX() > this.max.getX() ||
                   minOther.getY() > this.max.getY() ||
                   minOther.getZ() > this.max.getZ() )
               {
                       this.min = null;
                       this.max = null;
                       return;
               }
        Double min = new Double(Double.POSITIVE_INFINITY);
        Double max = new Double(Double.NEGATIVE_INFINITY);
        List<Double> ret = new ArrayList<Double>();

               if( maxOther.getX() < this.min.getX() ||
                   maxOther.getY() < this.min.getY() ||
                   maxOther.getZ() < this.min.getZ() )
               {
                       this.min = null;
                       this.max = null;
                       return;
               }

               if( minOther.getX() > this.min.getX() )
               {
                       this.min.setX( minOther.getX() );
               }
               if( minOther.getY() > this.min.getY() )
               {
                       this.min.setY( minOther.getY() );
               }
               if( minOther.getZ() > this.min.getZ() )
               {
                       this.min.setZ( minOther.getZ() );
               }
               if( maxOther.getX() < this.max.getX() )
               {
                       this.max.setX( maxOther.getX() );
               }
               if( maxOther.getY() < this.max.getY() )
               {
                       this.max.setY( maxOther.getY() );
               }
               if( maxOther.getZ() < this.max.getZ() )
               {
                       this.max.setZ( maxOther.getZ() );
               }
       }


       /**
        * Does this BoundingBox overlap another BoundingBox??
        *
        * @param    boundingBox         another  BoundingBox
        *
        * @return   True if BoundingBoxes overlap, false otherwise
        */
       public boolean overlaps( BoundingBox bb )
       {
               if( this.min == null )
               {
                       return false;
               }
               Point minOther = bb.getMin();
               Point maxOther = bb.getMax();
               if( minOther == null )
               {
                       return false;
               }

               if( minOther.getX() > this.max.getX() ||
                   minOther.getY() > this.max.getY() ||
                   minOther.getZ() > this.max.getZ() )
               {
                       return false;
               }

               if( maxOther.getX() < this.min.getX() ||
                   maxOther.getY() < this.min.getY() ||
                   maxOther.getZ() < this.min.getZ() )
               {
                       return false;
               }
               
               return true;
       }

    public void extend(Point p) {
        if( min == null ) {
            min = new Point(p);
        } else {
            for( int i=0 ; i<3 ; i++ ) {
                if( p.get(i) < min.get(i)) {
                    min.set(i, p.get(i));
                }
            }
        }

        if( max == null ) {
            max = new Point(p);
        } else {
            for( int i=0 ; i<3 ; i++ ) {
                if( p.get(i) > max.get(i) ) {
                    max.set(i, p.get(i));
                }
            }
        }
    }

    public Vector3 getDiameter() {
        return Vector3.minus(max, min);
    }

    /**
     * Return the max value of the bounding box.
     *
     * @return The max value
     */
    public Point getMax() {
        return new Point( max );
    }

    /**
     * Set the max value of the bounding box.
     *
     * @param max The value to set
     */
    public void setMax(Point max) throws IllegalArgumentException {
        if (max == null || !Point.isValidPoint(max)) {
            throw new IllegalArgumentException(
                    "setMax() called with Point that is either null or not valid");
        }
        this.max = max;
    }

    /**
     * Return the min value of the bounding box.
     *
     * @return The min value
     */
    public Point getMin() {
        return new Point( min );
    }

    /**
     * Set the min value of the bounding box.
     *
     * @param min The value to set
     */
    public void setMin(Point min) throws IllegalArgumentException {
        if (min == null || !Point.isValidPoint(min)) {
            throw new IllegalArgumentException(
                    "setMin() called with Point that is either null or not valid");
        }
        this.min = min;
    }

    /**
     * Test if the point is constrained inside of the bounding box. Points on
     * the surface are not considered to be 'bound'.
     *
     * @param p Point to test
     * @return true, if point is constrained inside of the bounding box
     */
    public boolean bound(Point p) throws IllegalArgumentException {
        if (p == null || !Point.isValidPoint(p)) {
            throw new IllegalArgumentException(
                    "bound() called with Point that is either null or not valid");
        }
        if (p.getX() < max.getX() && p.getX() > min.getX()
                && p.getY() < max.getY() && p.getY() > min.getY()
                && p.getZ() < max.getZ() && p.getZ() > min.getZ()) {
            return true;
        }
        return false;
    }

    /**
     * Calculate the extent of the bounding box in the specified direction
     *
     * @param dir A unit vector in the desired direction
     * @return A List of two Doubles, the minimum and maximum (in that order) of
     *         the extent of this bounding box along a vector in the specified
     *         direction
     */
    public List<Double> getExtentsInDirection(Vector3 dir) throws IllegalArgumentException {
        if (dir == null || !Vector3.isValidVector(dir)) {
            throw new IllegalArgumentException(
                    "getExtentsInDirection() called with Vector that is either null or not valid");
        }

        Double min = new Double(Double.POSITIVE_INFINITY);
        Double max = new Double(Double.NEGATIVE_INFINITY);
        List<Double> ret = new ArrayList<Double>();

        // Check all 8 vertices of the bounding box

        // (xmin, ymin, zmin)
        Vector3 vt = new Vector3(this.min.getX(), this.min.getY(), this.min
                .getZ());
        double dot = vt.dotProduct(dir);
        if (dot > max) {
            max = dot;
        }
        if (dot < min) {
            min = dot;
        }

        // (xmax, ymin, zmin)
        vt.setX(this.max.getX());
        dot = vt.dotProduct(dir);
        if (dot > max) {
            max = dot;
        }
        if (dot < min) {
            min = dot;
        }

        // (xmax, ymax, zmin )
        vt.setY(this.max.getY());
        dot = vt.dotProduct(dir);
        if (dot > max) {
            max = dot;
        }
        if (dot < min) {
            min = dot;
        }

        // (xmin, ymax, zmin )
        vt.setX(this.min.getX());
        dot = vt.dotProduct(dir);
        if (dot > max) {
            max = dot;
        }
        if (dot < min) {
            min = dot;
        }

        // (xmin, ymax, zmax )
        vt.setZ(this.max.getZ());
        dot = vt.dotProduct(dir);
        if (dot > max) {
            max = dot;
        }
        if (dot < min) {
            min = dot;
        }

        // (xmax, ymax, zmax )
        vt.setX(this.max.getX());
        dot = vt.dotProduct(dir);
        if (dot > max) {
            max = dot;
        }
        if (dot < min) {
            min = dot;
        }

        // (xmax, ymin, zmax )
        vt.setY(this.min.getY());
        dot = vt.dotProduct(dir);
        if (dot > max) {
            max = dot;
        }
        if (dot < min) {
            min = dot;
        }

        // (xmin, ymin, zmax )
        vt.setX(this.min.getX());
        dot = vt.dotProduct(dir);
        if (dot > max) {
            max = dot;
        }
        if (dot < min) {
            min = dot;
        }

        ret.add(min);
        ret.add(max);

        return ret;
    }

    /**
     * Determines if a ray is in a plane.
     *
     * @param r  The ray
     * @param pl The plane
     * @return true, if the ray is in the plane.
     */
    private boolean isInHit(Ray r, Plane3D pl) throws IllegalArgumentException {
        if (r == null || !Ray.isValidRay(r)) {
            throw new IllegalArgumentException("isInHit() called with Ray that is either null or not valid");
        }
        if (pl == null || !Plane3D.isValidPlane(pl)) {
            throw new IllegalArgumentException("isInHit() called with Plane3D that is either null or not valid");
        }

        double dot = pl.getNormal().dotProduct(r.getDirection());
        if (dot > 0.0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Computes intersections of this bounding box and a ray
     *
     * @param r The ray
     * @return Either null (no intersections) or an array of two doubles which
     *         are the intersections.
     */
    public double[] intersect(Ray r) throws IllegalArgumentException {
        if (r == null || !Ray.isValidRay(r)) {
            throw new IllegalArgumentException("intersect() called with Ray that is either null or not valid");
        }

        Plane3D[] planes = new Plane3D[6];
        double[] widths = new double[6];
        double inhit = -Double.MAX_VALUE;
        double outhit = Double.MAX_VALUE;
        double dist;

        // intersect the Ray with each of the six faces of the BoundingBox
        // the largest inhit and the smallest outhit define the extent of the
        // intersection

        // faces
        planes[0] = new Plane3D(new Vector3(0, 0, 1), this.getMax());
        planes[1] = new Plane3D(new Vector3(0, 1, 0), this.getMax());
        planes[2] = new Plane3D(new Vector3(1, 0, 0), this.getMax());
        planes[3] = new Plane3D(new Vector3(0, 0, -1), this.getMin());
        planes[4] = new Plane3D(new Vector3(0, -1, 0), this.getMin());
        planes[5] = new Plane3D(new Vector3(-1, 0, 0), this.getMin());

        widths[0] = (this.getMax().getZ() - this.getMin().getZ()) / 2.0;
        widths[1] = (this.getMax().getY() - this.getMin().getY()) / 2.0;
        widths[2] = (this.getMax().getX() - this.getMin().getX()) / 2.0;
        widths[3] = widths[0];
        widths[4] = widths[1];
        widths[5] = widths[2];

        for (int i = 0; i < 6; i++) {
            Plane3D pl = planes[i];
            dist = pl.intersect(r);
            if (dist != Double.NEGATIVE_INFINITY
                    && dist != Double.POSITIVE_INFINITY) {
                Point hitPoint = new Point(r.getStart());
                hitPoint.join(dist, r.getDirection());
                // move hitPoint below plane (inside BoundingBox)
                hitPoint.join(-widths[i], pl.getNormal());
                if (this.bound(hitPoint)) {
                    if (this.isInHit(r, pl)) {
                        if (dist > inhit) {
                            inhit = dist;
                        }
                    } else {
                        if (dist < outhit) {
                            outhit = dist;
                        }
                    }
                }
            }
        }

        if (inhit == -Double.MAX_VALUE || outhit == Double.MAX_VALUE) {
            return null;
        } else {
            double[] retval = new double[2];
            retval[0] = inhit;
            retval[1] = outhit;
            return retval;
        }
    }

    /**
     * toString() for a BoundingBox
     *
     * @return Returns String containing "BoundingBox: min=(m.n, m.n, m.n),
     *         max=(m.n, m.n, m.n)" where n is any number of significant digits.
     */
    @Override
    public String toString() {
        return "BoundingBox: min=" + this.min + ", max=" + this.max;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BoundingBox other = (BoundingBox) obj;
        if (this.min != other.min && (this.min == null || !this.min.equals(other.min))) {
            return false;
        }
        if (this.max != other.max && (this.max == null || !this.max.equals(other.max))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.min != null ? this.min.hashCode() : 0);
        hash = 23 * hash + (this.max != null ? this.max.hashCode() : 0);
        return hash;
    }
}
