package org.brlcad.geometry;

/**
 * Hit.java
 *
 * @author Created by Omnicore CodeGuide
 */
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Vector3;
import org.brlcad.spacePartition.RayData;

public class Hit implements Comparable, Externalizable {

    private double hit_dist;
    private Point hit_pt;
    private Vector3 hit_normal;
    private int hit_surfno;
    private RayData rayData;
    private String primitiveName;
    
    /*
     * No-arg constructor for use by Externalization. DO NOT USE THIS CONSTRUCTOR
     */
    public Hit() {}

    public Hit(double dist, Point pt, Vector3 norm, int surfno, RayData rayData, String primName) {
        this.hit_dist = dist;
        this.hit_pt = pt;
        this.hit_normal = norm;
        this.hit_surfno = surfno;
        this.rayData = rayData;
        this.primitiveName = primName;
    }

    public Hit(Hit hit) {
        this.hit_dist = hit.getHit_dist();
        this.hit_normal = hit.getHit_normal();
        this.hit_pt = hit.getHit_pt();
        this.hit_surfno = hit.getHit_surfno();
        this.rayData = hit.getRayData();
        this.primitiveName = hit.getPrimitiveName();
    }

    /**
     * Sets Hit_dist
     *
     * @param    Hit_dist            a  double
     */
    public void setHit_dist(double hit_dist) {
        this.hit_dist = hit_dist;
    }

    /**
     * Returns Hit_dist
     *
     * @return    a  double
     */
    public double getHit_dist() {
        return hit_dist;
    }

    /**
     * Sets Hit_pt
     *
     * @param    Hit_pt              a  Point
     */
    public void setHit_pt(Point hit_pt) {
        this.hit_pt = hit_pt;
    }

    /**
     * Returns Hit_pt
     *
     * @return    a  Point
     */
    public Point getHit_pt() {
        return hit_pt;
    }

    /**
     * Sets Hit_normal
     *
     * @param    Hit_normal          a  Vector3
     */
    public void setHit_normal(Vector3 hit_normal) {
        this.hit_normal = hit_normal;
    }

    /**
     * Returns Hit_normal
     *
     * @return    a  Vector3
     */
    public Vector3 getHit_normal() {
        return hit_normal;
    }

    /**
     * Sets Hit_surfno
     *
     * @param    Hit_surfno          an int
     */
    public void setHit_surfno(int hit_surfno) {
        this.hit_surfno = hit_surfno;
    }

    /**
     * Returns Hit_surfno
     *
     * @return    an int
     */
    public int getHit_surfno() {
        return hit_surfno;
    }

    @Override
    public String toString() {
        return "Hit: dist_pt=" + hit_dist +
                ", point=" + hit_pt +
                ", norm=" + hit_normal +
                ", surf=" + hit_surfno +
                ", on " + this.primitiveName;
    }

    public String toString(boolean flipNormal) {
        Vector3 norm = new Vector3(hit_normal);
        if (flipNormal) {
            norm.negate();
        }
        return "Hit: dist_pt=" + hit_dist +
                ", point=" + hit_pt +
                ", norm=" + norm +
                ", surf=" + hit_surfno +
                ", on " + this.primitiveName;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.<p>
     *
     * In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of <i>expression</i>
     * is negative, zero or positive.
     *
     * The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)<p>
     *
     * The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.<p>
     *
     * Finally, the implementer must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.<p>
     *
     * It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * @param   o the Object to be compared.
     * @return  a negative integer, zero, or a positive integer as this object
     *		is less than, equal to, or greater than the specified object.
     *
     * @throws ClassCastException if the specified object's type prevents it
     *         from being compared to this Object.
     */
    public int compareTo(Object o) {
        if (!(o instanceof Hit)) {
            throw new ClassCastException("Attempt to compare Hit to a " + o.getClass().getName());
        }

        Hit h = (Hit) o;
        double dist2 = h.getHit_dist();
        double diff = this.hit_dist - dist2;

        if (diff < -this.rayData.getTolerance().getDist()) {
            return -1;
        }

        if (diff > this.getRayData().getTolerance().getDist()) {
            return 1;
        }

        double thisDot = this.hit_normal.dotProduct(this.getRayData().getTheRay().getDirection());
        double hDot = h.hit_normal.dotProduct(this.getRayData().getTheRay().getDirection());

        if (thisDot < 0.0 && hDot > 0.0) {
            return -1;
        }

        if (thisDot > 0.0 && hDot < 0.0) {
            return 1;
        }

        return 0;
    }

    /**
     * @return the rayData
     */
    public RayData getRayData() {
        return rayData;
    }

    /**
     * @param rayData the rayData to set
     */
    public void setRayData(RayData rayData) {
        this.rayData = rayData;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Hit other = (Hit) obj;
        if (this.hit_dist != other.hit_dist) {
            return false;
        }
        if (this.hit_pt != other.hit_pt && (this.hit_pt == null || !this.hit_pt.equals(other.hit_pt))) {
            return false;
        }
        if (this.hit_normal != other.hit_normal && (this.hit_normal == null || !this.hit_normal.equals(other.hit_normal))) {
            return false;
        }
        if (this.hit_surfno != other.hit_surfno) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (int) (Double.doubleToLongBits(this.hit_dist) ^ (Double.doubleToLongBits(this.hit_dist) >>> 32));
        hash = 53 * hash + (this.hit_pt != null ? this.hit_pt.hashCode() : 0);
        hash = 53 * hash + (this.hit_normal != null ? this.hit_normal.hashCode() : 0);
        hash = 53 * hash + this.hit_surfno;
        return hash;
    }

    /**
     * @return the primitiveName
     */
    public String getPrimitiveName() {
        return primitiveName;
    }

    /**
     * @param primitiveName the primitiveName to set
     */
    public void setPrimitiveName(String primitiveName) {
        this.primitiveName = primitiveName;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(hit_surfno);
        out.writeDouble(hit_dist);
        out.writeUTF(primitiveName);
        if (hit_normal == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeDouble(hit_normal.getX());
            out.writeDouble(hit_normal.getY());
            out.writeDouble(hit_normal.getZ());
        }
        if (hit_pt == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeDouble(hit_pt.getX());
            out.writeDouble(hit_pt.getY());
            out.writeDouble(hit_pt.getZ());
        }
        if (rayData == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(rayData);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        hit_surfno = in.readInt();
        hit_dist = in.readDouble();
        primitiveName = in.readUTF();

        if (in.readBoolean()) {
            hit_normal = new Vector3(in.readDouble(), in.readDouble(), in.readDouble());
        } else {
            hit_normal = null;
        }

        if (in.readBoolean()) {
            hit_pt = new Point(in.readDouble(), in.readDouble(), in.readDouble());
        } else {
            hit_pt = null;
        }

        if (in.readBoolean()) {
            rayData = (RayData) in.readObject();
        } else {
            rayData = null;
        }
    }
}

