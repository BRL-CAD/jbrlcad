
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

    private BBPlane[] bbPlanes;

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
        if (bb != null) {
            if (bb.getMin() != null && bb.getMax() != null) {
                this.max = new Point(bb.getMax());
                this.min = new Point(bb.getMin());
            }
        }
    }

    public void extend(BoundingBox bb) {
        if( bb == null ) {
            return;
        }

        if (bb.getMax() == null || bb.getMin() == null ) {
            return;
        }

        bbPlanes = null;
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
               bbPlanes = null;
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
        bbPlanes = null;
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
        if (max == null) {
            return null;
        }
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
        bbPlanes = null;
        this.max = max;
    }

    /**
     * Return the min value of the bounding box.
     *
     * @return The min value
     */
    public Point getMin() {
        if (min == null) {
            return null;
        }
        return new Point( min );
    }

    public void setMax(int i, double cutValue) {
        bbPlanes = null;
        this.max.set(i, cutValue);
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
        bbPlanes = null;
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

        double tmpMin = Double.POSITIVE_INFINITY;
        double tmpMax = Double.NEGATIVE_INFINITY;
        List<Double> ret = new ArrayList<Double>();

        // Check all 8 vertices of the bounding box

        // (xmin, ymin, zmin)
        Vector3 vt = new Vector3(this.min.getX(), this.min.getY(), this.min
                .getZ());
        double dot = vt.dotProduct(dir);
        if (dot > tmpMax) {
            tmpMax = dot;
        }
        if (dot < tmpMin) {
            tmpMin = dot;
        }

        // (xmax, ymin, zmin)
        vt.setX(this.max.getX());
        dot = vt.dotProduct(dir);
        if (dot > tmpMax) {
            tmpMax = dot;
        }
        if (dot < tmpMin) {
            tmpMin = dot;
        }

        // (xmax, ymax, zmin )
        vt.setY(this.max.getY());
        dot = vt.dotProduct(dir);
        if (dot > tmpMax) {
            tmpMax = dot;
        }
        if (dot < tmpMin) {
            tmpMin = dot;
        }

        // (xmin, ymax, zmin )
        vt.setX(this.min.getX());
        dot = vt.dotProduct(dir);
        if (dot > tmpMax) {
            tmpMax = dot;
        }
        if (dot < tmpMin) {
            tmpMin = dot;
        }

        // (xmin, ymax, zmax )
        vt.setZ(this.max.getZ());
        dot = vt.dotProduct(dir);
        if (dot > tmpMax) {
            tmpMax = dot;
        }
        if (dot < tmpMin) {
            tmpMin = dot;
        }

        // (xmax, ymax, zmax )
        vt.setX(this.max.getX());
        dot = vt.dotProduct(dir);
        if (dot > tmpMax) {
            tmpMax = dot;
        }
        if (dot < tmpMin) {
            tmpMin = dot;
        }

        // (xmax, ymin, zmax )
        vt.setY(this.min.getY());
        dot = vt.dotProduct(dir);
        if (dot > tmpMax) {
            tmpMax = dot;
        }
        if (dot < tmpMin) {
            tmpMin = dot;
        }

        // (xmin, ymin, zmax )
        vt.setX(this.min.getX());
        dot = vt.dotProduct(dir);
        if (dot > tmpMax) {
            tmpMax = dot;
        }
        if (dot < tmpMin) {
            tmpMin = dot;
        }

        ret.add(Double.valueOf(tmpMin));
        ret.add(Double.valueOf(tmpMax));

        return ret;
    }

    public void setMin(int i, double cutValue) {
        bbPlanes = null;
        this.min.set(i, cutValue);
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

    public double[] isect2(Ray r) throws IllegalArgumentException {
        if (r == null || !Ray.isValidRay(r)) {
            throw new IllegalArgumentException("intersect() called with Ray that is either null or not valid");
        }

        double inHit = Double.NEGATIVE_INFINITY;
        double outHit = Double.POSITIVE_INFINITY;

        for (int axis=0 ; axis<3 ; axis++) {
            double d;
            double dirComp;
            double dist;

            d = min.get(axis);
            dirComp = r.getDirection().get(axis);
            dist = (d - r.getStart().get(axis)) / dirComp;
            if (Double.isInfinite(dist)) {
                // ray is parallel to this plane, check if it is outside the BB
                if (r.getStart().get(axis) < d) {
                    // ray is outside Box
                    return null;
                }
            }
            if (dirComp < 0.0) {
                if (dist < outHit) {
                    outHit = dist;
                }
            } else if (dirComp > 0.0) {
                if (dist > inHit) {
                    inHit = dist;
                }
            }

            d = max.get(axis);
            dirComp = r.getDirection().get(axis);
            dist = (d - r.getStart().get(axis)) / dirComp;
            if (Double.isInfinite(dist)) {
                // ray is parallel to this plane, check if it is outside the BB
                if (r.getStart().get(axis) > d) {
                    // ray is outside Box
                    return null;
                }
            }
            if (dirComp < 0.0) {
                if (dist > inHit) {
                    inHit = dist;
                }
            } else if (dirComp > 0.0) {
                if (dist < outHit) {
                    outHit = dist;
                }
            }
        }

        if (inHit >= outHit) {
            return null;
        } else {
            double[] hits = new double[2];
            hits[0] = inHit;
            hits[1] = outHit;
            return hits;
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
                    && dist != Double.POSITIVE_INFINITY
                    && !Double.isNaN(dist)) {
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

    public boolean doesIntersect( Ray ray ) {
        return (intersect(ray) != null);
    }

    public String showPlanes() {
        StringBuilder sb = new StringBuilder();
        if (bbPlanes == null) {
            createBBPlanes();
        }
        for (BBPlane pl : bbPlanes) {
            sb.append(pl.toString());
        }
        return sb.toString();
    }

    public boolean intersectsCone (Ray r, double cosHalfAngle) {
        if (r == null || !Ray.isValidRay(r)) {
            throw new IllegalArgumentException("intersectsCone() called with Ray that is either null or not valid");
        }

        if (cosHalfAngle > 1.0 || cosHalfAngle < -1.0) {
            throw new IllegalArgumentException("intersectsCone() called with illegal value (" +
                    cosHalfAngle + ") for cosine of half angle");
        }

        double inHit = Double.NEGATIVE_INFINITY;
        double outHit = Double.POSITIVE_INFINITY;

        if (bbPlanes == null) {
            createBBPlanes();
        }

        boolean miss = false;
        double hitDist[] = new double[bbPlanes.length];
        for (int i=0 ; i<bbPlanes.length ; i++ ) {
            BBPlane pl = bbPlanes[i];
            int axis = pl.getCoord().ordinal();
            double d = pl.getValue();
            double dirComp = r.getDirection().get(axis);
            double dist = (d - r.getStart().get(axis)) / dirComp;
            hitDist[i] = dist;
            if (d == min.get(axis)) {
                if (Double.isInfinite(dist)) {
                    // ray is parallel to this plane, check if it is outside the BB
                    if (r.getStart().get(axis) < d) {
                        // ray is outside Box
                        miss = true;
                        continue;
                    }
                }
                if (dirComp < 0.0) {
                    if (dist < outHit) {
                        outHit = dist;
                    }
                } else if (dirComp > 0.0) {
                    if (dist > inHit) {
                        inHit = dist;
                    }
                }
            } else {
                if (Double.isInfinite(dist)) {
                    // ray is parallel to this plane, check if it is outside the BB
                    if (r.getStart().get(axis) > d) {
                        // ray is outside Box
                        miss = true;
                        continue;
                    }
                }
                if (dirComp < 0.0) {
                    if (dist > inHit) {
                        inHit = dist;
                    }
                } else if (dirComp > 0.0) {
                    if (dist < outHit) {
                        outHit = dist;
                    }
                }
            }
        }

        if ( !miss && !Double.isInfinite(inHit) && !Double.isInfinite(outHit) && inHit < outHit) {
            // center ray intersects BondingBox
            // make sure BB is not behind Ray start
            if (outHit > 0.0) {
                return true;
            }
        }

        if (cosHalfAngle == 1.0) {
            // this is not a cone, just a ray
            return false;
        }

        // Now check PCA's of each BBPlane
        for (int i=0 ; i<bbPlanes.length ; i++ ) {
            BBPlane pl = bbPlanes[i];
            double dist = hitDist[i];
            if (Double.isInfinite(dist) || Double.isNaN(dist)) {
                continue;
            }
            Point hit = new Point(r.getStart());
            hit.join(dist, r.getDirection());
            Point pca = pl.getPCA(hit);
            Vector3 toPca = Vector3.minus(pca, r.getStart());
            toPca.normalize();
            if (toPca.dotProduct(r.getDirection()) > cosHalfAngle) {
                return true;
            }
        }

        return false;
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

    private void createBBPlanes() {
        bbPlanes = new BBPlane[6];
        for (int i=0 ; i<3 ; i++) {
            bbPlanes[i*2] = new BBPlane(Coord.withOrdinal(i), true);
            bbPlanes[i*2+1] = new BBPlane(Coord.withOrdinal(i), false);
        }

    }

    private class BBEdge {
        private Coord changingCoord;
        private Point start;
        private Point end;

        public BBEdge (Coord changing, Point start, Point end) {
            this.changingCoord = changing;
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return start.toString() + "->" + end.toString() + ", " + changingCoord + " is changing";
        }
    }

    private class BBPlane {
        private Coord coord;
        private double value;
        private BBEdge[] edges;

        public BBPlane (Coord c, boolean isMax) {
            coord = c;
            if (isMax) {
                value = max.get(c.ordinal());
            } else {
                value = min.get(c.ordinal());
            }

            edges = new BBEdge[4];
            Point edgeStart;
            Point edgeEnd;
            Coord fixedCoord;
            Coord changingCoord;
            fixedCoord = Coord.withOrdinal((c.ordinal() + 1) % 3);
            changingCoord = Coord.withOrdinal((c.ordinal() + 2) % 3);
            edgeStart = new Point();
            edgeStart.set(c.ordinal(), value);
            edgeStart.set(fixedCoord.ordinal(), min.get(fixedCoord.ordinal()));
            edgeStart.set(changingCoord.ordinal(), min.get(changingCoord.ordinal()));
            edgeEnd = new Point(edgeStart);
            edgeEnd.set(changingCoord.ordinal(), max.get(changingCoord.ordinal()));
            edges[0] = new BBEdge(changingCoord, edgeStart, edgeEnd);

            edgeStart = new Point();
            edgeStart.set(c.ordinal(), value);
            edgeStart.set(fixedCoord.ordinal(), max.get(fixedCoord.ordinal()));
            edgeStart.set(changingCoord.ordinal(), min.get(changingCoord.ordinal()));
            edgeEnd = new Point(edgeStart);
            edgeEnd.set(changingCoord.ordinal(), max.get(changingCoord.ordinal()));
            edges[1] = new BBEdge(changingCoord, edgeStart, edgeEnd);

            fixedCoord = Coord.withOrdinal((c.ordinal() + 2) % 3);
            changingCoord = Coord.withOrdinal((c.ordinal() + 1) % 3);
            edgeStart = new Point();
            edgeStart.set(c.ordinal(), value);
            edgeStart.set(fixedCoord.ordinal(), min.get(fixedCoord.ordinal()));
            edgeStart.set(changingCoord.ordinal(), min.get(changingCoord.ordinal()));
            edgeEnd = new Point(edgeStart);
            edgeEnd.set(changingCoord.ordinal(), max.get(changingCoord.ordinal()));
            edges[2] = new BBEdge(changingCoord, edgeStart, edgeEnd);

            edgeStart = new Point();
            edgeStart.set(c.ordinal(), value);
            edgeStart.set(fixedCoord.ordinal(), max.get(fixedCoord.ordinal()));
            edgeStart.set(changingCoord.ordinal(), min.get(changingCoord.ordinal()));
            edgeEnd = new Point(edgeStart);
            edgeEnd.set(changingCoord.ordinal(), max.get(changingCoord.ordinal()));
            edges[3] = new BBEdge(changingCoord, edgeStart, edgeEnd);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("BBPlane at " + coord + " = " + value + ":\n");
            for (BBEdge e : edges) {
                sb.append("\t" + e.toString() + "\n");
            }
            return sb.toString();
        }

        /**
         * @return the coord
         */
        public Coord getCoord() {
            return coord;
        }

        /**
         * @return the value
         */
        public double getValue() {
            return value;
        }

        /**
         * @return the edges
         */
        public BBEdge[] getEdges() {
            return edges;
        }

        private Point getPCA(Point hit) {
            double nearest_sq = Double.MAX_VALUE;
            Point pca = null;
            Point pcaCanididate = new Point();

            for (BBEdge edge : edges) {
                double tmp = 0.0;
                double d;
                for (Coord cr : Coord.values()) {
                    int ordinal = cr.ordinal();
                    double t;
                    if (cr == coord) {
                        pcaCanididate.set(ordinal, value);
                    } else if (cr == edge.changingCoord) {
                        if (hit.get(ordinal) <= edge.start.get(ordinal)) {
                            pcaCanididate.set(ordinal, edge.start.get(ordinal));
                            d = pcaCanididate.get(ordinal) - hit.get(ordinal);
                            tmp += d * d;
                        } else if (hit.get(ordinal) >= edge.end.get(ordinal)) {
                            pcaCanididate.set(ordinal, edge.end.get(ordinal));
                            d = pcaCanididate.get(ordinal) - hit.get(ordinal);
                            tmp += d * d;
                        } else {
                            pcaCanididate.set(ordinal, hit.get(ordinal));
                        }
                    } else {
                        pcaCanididate.set(ordinal, edge.start.get(ordinal));
                        d = pcaCanididate.get(ordinal) - hit.get(ordinal);
                        tmp += d * d;
                    }
                }
                if (tmp < nearest_sq) {
                    nearest_sq = tmp;
                    pca = new Point(pcaCanididate);
                }
            }

            return pca;
        }
    }
}
