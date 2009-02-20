package org.brlcad.geometry;

/**
 * Partition.java
 *
 * @author Created by Omnicore CodeGuide
 */
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.brlcad.numerics.Vector3;
import org.brlcad.preppedGeometry.PreppedCombination;
import org.brlcad.spacePartition.RayData;

public class Partition implements Comparable,Serializable {

    private static final  Logger logger = Logger.getLogger(Partition.class.getName());
    private Hit in_hit;
    private boolean flipInNormal;
    private float inObliquity;
    private Hit out_hit;
    private boolean flipOutNormal;
    private float outObliquity;
    private String fromRegion;
    private double los;

    public Partition(Segment seg, String reg, RayData rayData) {
        this.in_hit = seg.getInHit();
        this.out_hit = seg.getOutHit();
        this.fromRegion = reg;
        this.flipInNormal = false;
        this.flipOutNormal = false;
        this.los = out_hit.getHit_pt().dist(in_hit.getHit_pt());
        this.calcObliquities(rayData);
    }

    public Partition(Hit inHit, boolean inFlip, Hit outHit, boolean outFlip,
            float enterObl, float exitObl, String reg) {
        this.in_hit = inHit;
        this.flipInNormal = inFlip;
        this.inObliquity = enterObl;
        this.out_hit = outHit;
        this.flipOutNormal = outFlip;
        this.outObliquity = exitObl;
        this.fromRegion = reg;
        this.los = out_hit.getHit_pt().dist(in_hit.getHit_pt());
    }

    public Partition(Partition part) {
        this.in_hit = part.in_hit;
        this.out_hit = part.out_hit;
        this.fromRegion = part.fromRegion;
        this.flipInNormal = part.flipInNormal;
        this.flipOutNormal = part.flipOutNormal;
        this.inObliquity = part.inObliquity;
        this.outObliquity = part.outObliquity;
        this.los = part.los;
    }

    private void calcObliquities( RayData rayData ) {
        Vector3 rayDir = rayData.getTheRay().getDirection();
        double dot = -in_hit.getHit_normal().dotProduct(rayDir);
        if( dot < -1.0 ) {
            dot = -1.0;
        } else if( dot > 1.0 ) {
            dot = 1.0;
        }
        this.inObliquity = (float) Math.acos(dot);
        dot = out_hit.getHit_normal().dotProduct(rayDir);
        if( dot < -1.0 ) {
            dot = -1.0;
        } else if( dot > 1.0 ) {
            dot = 1.0;
        }
        this.outObliquity = (float) Math.acos(dot);
    }

    /**
     * Sets Out_hit
     *
     * @param    Out_hit             a  Hit
     */
    public void setOutHit(Hit out_hit) {
        this.out_hit = out_hit;
    }

    /**
     * Returns Out_hit
     *
     * @return    a  Hit
     */
    public Hit getOutHit() {
        return out_hit;
    }

    /**
     * Sets In_hit
     *
     * @param    In_hit              a  Hit
     */
    public void setInhit(Hit in_hit) {
        this.in_hit = in_hit;
    }

    /**
     * Returns In_hit
     *
     * @return    a  Hit
     */
    public Hit getInHit() {
        return in_hit;
    }

    /**
     * Sets FlipOutNormal
     *
     * @param    FlipOutNormal       a  boolean
     */
    public void setFlipOutNormal(boolean flipOutNormal) {
        this.flipOutNormal = flipOutNormal;
    }

    /**
     * Returns FlipOutNormal
     *
     * @return    a  boolean
     */
    public boolean isFlipOutNormal() {
        return flipOutNormal;
    }

    /**
     * Sets FlipInNormal
     *
     * @param    FlipInNormal        a  boolean
     */
    public void setFlipInNormal(boolean flipInNormal) {
        this.flipInNormal = flipInNormal;
    }

    /**
     * Returns FlipInNormal
     *
     * @return    a  boolean
     */
    public boolean isFlipInNormal() {
        return flipInNormal;
    }

    /**
     * Sets FromRegion
     *
     * @param    FromRegion          a  PreppedCombination
     */
    public void setFromRegion(PreppedCombination fromRegion) {
        this.fromRegion = fromRegion.getName();
    }

    /**
     * Returns FromRegion
     *
     * @return    a  PreppedCombination
     */
    public String getFromRegion() {
        return fromRegion;
    }

    public void setInHit(Hit hit, boolean flip) {
        this.in_hit = hit;
        if (flip) {
            this.flipInNormal = !this.flipInNormal;
        }
    }

    public void setOutHit(Hit hit, boolean flip) {
        this.out_hit = hit;
        if (flip) {
            this.flipOutNormal = !this.flipOutNormal;
        }
    }

    public Vector3 getOutHitNormal() {
        if( this.flipOutNormal ) {
            return Vector3.negate(this.out_hit.getHit_normal());
        } else {
            return new Vector3(this.out_hit.getHit_normal());
        }
    }

    public Vector3 getInHitNormal() {
        if( this.flipInNormal ) {
            return Vector3.negate(this.in_hit.getHit_normal());
        } else {
            return new Vector3(this.in_hit.getHit_normal());
        }
    }

    /**
     * Method not
     *
     * @param    partsL              a  SortedSet<Partition>
     *
     * @return   a  SortedSet<Partition>
     */
    public static SortedSet<Partition> not(SortedSet<Partition> partsL) {
        // TODO
        return null;
    }

    /**
     * Method intersect
     *
     * @param    partsL              a  SortedSet<Partition>
     * @param    partsR              a  SortedSet<Partition>
     *
     * @return   a  SortedSet<Partition> representing the intesection of the input Partitions
     */
    public static SortedSet<Partition> intersect(SortedSet<Partition> partsL, SortedSet<Partition> partsR) {
        if (partsL == null || partsR == null) {
            return null;
        }

        if( partsL.size() == 0 || partsR.size() == 0 ) {
            return null;
        }

        SortedSet<Partition> parts = new TreeSet<Partition>();

        for (Partition part1 : partsL) {
            double inDist1 = part1.in_hit.getHit_dist();
            double outDist1 = part1.out_hit.getHit_dist();
            float inObliquity1 = part1.getInObliquity();
            float outObliquity1 = part1.getOutObliquity();
            Hit inHit;
            Hit outHit;
            float inObliquity;
            float outObliquity;

            logger.log(Level.FINEST, "Starting seg: <" + inDist1 + " - " + outDist1 + ">");

            for (Partition part2 : partsR) {
                inHit = null;
                outHit = null;
                double inDist2 = part2.in_hit.getHit_dist();
                double outDist2 = part2.out_hit.getHit_dist();
                float inObliquity2 = part2.getInObliquity();
                float outObliquity2 = part2.getOutObliquity();

                logger.log(Level.FINEST, "intersect seg: <" + inDist2 + " - " + outDist2 + ">");

                if (inDist2 > outDist1 || outDist2 < inDist1) {
                    continue;
                }

                if (inDist2 > inDist1 && outDist2 < outDist1) {
                    inHit = part2.in_hit;
                    outHit = part2.out_hit;
                    inObliquity = inObliquity2;
                    outObliquity = outObliquity2;
                } else if (inDist2 > inDist1) {
                    inHit = part2.in_hit;
                    outHit = part1.out_hit;
                    inObliquity = inObliquity2;
                    outObliquity = outObliquity1;
                } else if (outDist2 < outDist1) {
                    inHit = part1.in_hit;
                    outHit = part2.out_hit;
                    inObliquity = inObliquity1;
                    outObliquity = outObliquity2;
                } else {
                    inHit = part1.in_hit;
                    outHit = part1.out_hit;
                    inObliquity = inObliquity1;
                    outObliquity = outObliquity1;
                }
                if (inHit != null && outHit != null) {
                    if (inHit.getHit_dist() < outHit.getHit_dist()) {
                        parts.add(new Partition(inHit, false, outHit, false,
                                inObliquity, outObliquity, part1.fromRegion));
                    }
                }
            }
        }
        return parts;
    }

    /**
     * Method subtract
     *
     * @param    partsL              a  SortedSet<Partition>
     * @param    partsR              a  SortedSet<Partition>
     *
     * @return   a  SortedSet<Partition>
     */
    public static SortedSet<Partition> subtract(SortedSet<Partition> partsL, SortedSet<Partition> partsR) {
        if (partsL == null || partsL.size() == 0) {
            return null;
        }

        if (partsR == null || partsR.size() == 0) {
            return partsL;
        }

        List<Partition> result = new ArrayList<Partition>();
        Iterator<Partition> iter = partsL.iterator();
        while( iter.hasNext() ) {
            result.add(new Partition(iter.next()));
        }

        int index = 0;
        boolean done = false;
        while ( !done ) {
            Partition part1 = result.get(index);
            double inDist1 = part1.in_hit.getHit_dist();
            double outDist1 = part1.out_hit.getHit_dist();

            logger.log(Level.FINEST, "Starting seg: <" + inDist1 + " - " + outDist1 + ">");

            for (Partition part2 : partsR) {
                double inDist2 = part2.in_hit.getHit_dist();
                double outDist2 = part2.out_hit.getHit_dist();

                logger.log(Level.FINEST, "Subtract seg: <" + inDist2 + " - " + outDist2 + ">");

                if (inDist2 >= outDist1) {
                    continue;
                }
                if (outDist2 <= inDist1) {
                    continue;
                }

                if( inDist2 <= inDist1 && outDist2 >= outDist1 ) {
                    // part1 goes away completely
                    result.remove(index);
                    index--;
                    break;
                }

                if (inDist2 > inDist1 && outDist2 < outDist1) {
                    Partition newPart = new Partition(part1);
                    newPart.setInHit(part2.out_hit, true);
                    result.add(index+1, newPart);
                    part1.setOutHit(part2.in_hit, true);
                    break;
                }

                if (inDist2 > inDist1) {
                    part1.setOutHit(part2.in_hit, true);
                }

                if (outDist2 < outDist1) {
                    part1.setInHit(part2.out_hit, true);
                }
            }
            index++;
            if( index >= result.size() ) {
                done = true;
            }
        }

        return new TreeSet<Partition>(result);
    }

    /**
     * Method xor
     *
     * @param    partsL              a  SortedSet<Partition>
     * @param    partsR              a  SortedSet<Partition>
     *
     * @return   a  SortedSet<Partition>
     */
    public static SortedSet<Partition> xor(SortedSet<Partition> partsL, SortedSet<Partition> partsR) {
        // TODO
        return null;
    }

    /**
     * Method union
     *
     * @param    partsL              a  SortedSet<Partition>
     * @param    partsR              a  SortedSet<Partition>
     *
     * @return   a  SortedSet<Partition>
     */
    public static SortedSet<Partition> union(SortedSet<Partition> partsL, SortedSet<Partition> partsR) {
        logger.log(Level.FINEST, "Partition.union():");
        if (partsL == null && partsR == null) {
            return null;
        }

        if( partsL == null ) {
            return partsR;
        }

        if( partsR == null ) {
            return partsL;
        }

        SortedSet<Partition> result = new TreeSet<Partition>();
        result.addAll(partsL);
        result.addAll(partsR);

        Iterator<Partition> iter = result.iterator();
        if (!iter.hasNext()) {
            return null;
        }

        Partition part1 = iter.next();
        logger.log(Level.FINEST, "Starting Partition: " + part1);
         while (iter.hasNext()) {
            Partition part2 = iter.next();
            logger.log(Level.FINEST, "  unioning :" + part2);
            if (part2.in_hit.getHit_dist() <= part1.out_hit.getHit_dist()) {
                if (part2.out_hit.getHit_dist() > part1.out_hit.getHit_dist()) {
                    part1.setOutHit(part2.out_hit, false);
                    logger.log(Level.FINEST, "   new part1 = " + part1);
                }
                iter.remove();
                logger.log(Level.FINEST, "After remove, parts has " + result.size() + " Partitions");
                continue;
            }
            part1 = part2;
        }
        logger.log(Level.FINEST, "At return, parts has " + result.size() + " Partitions");
        return result;
    }

    public int compareTo(Object o) {
        if (!(o instanceof Partition)) {
            throw new ClassCastException("Cannot compare Partition to " + o.getClass().getName());
        }
        Partition p = (Partition) o;
        int compare = Double.compare(this.in_hit.getHit_dist(), p.in_hit.getHit_dist());

        if (compare == 0) {
            compare = Double.compare(this.out_hit.getHit_dist(), p.out_hit.getHit_dist());
        }

        if (compare == 0) {
            compare = String.CASE_INSENSITIVE_ORDER.compare(this.fromRegion, p.fromRegion);
        }

        return compare;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Partition other = (Partition) obj;
        if (this.in_hit != other.in_hit && (this.in_hit == null || !this.in_hit.equals(other.in_hit))) {
            return false;
        }
        if (this.flipInNormal != other.flipInNormal) {
            return false;
        }
        if (this.out_hit != other.out_hit && (this.out_hit == null || !this.out_hit.equals(other.out_hit))) {
            return false;
        }
        if (this.flipOutNormal != other.flipOutNormal) {
            return false;
        }
        if (this.fromRegion != other.fromRegion && (this.fromRegion == null || !this.fromRegion.equals(other.fromRegion))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + (this.in_hit != null ? this.in_hit.hashCode() : 0);
        hash = 73 * hash + (this.flipInNormal ? 1 : 0);
        hash = 73 * hash + (this.out_hit != null ? this.out_hit.hashCode() : 0);
        hash = 73 * hash + (this.flipOutNormal ? 1 : 0);
        hash = 73 * hash + (this.fromRegion != null ? this.fromRegion.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return this.fromRegion +
                "\n\tin hit: " + this.in_hit.toString(this.flipInNormal) +
                "\n\tout hit: " + this.out_hit.toString(this.flipOutNormal);
    }

    /**
     * @return the inObliquity
     */
    public float getInObliquity() {
        return inObliquity;
    }

    /**
     * @param inObliquity the inObliquity to set
     */
    public void setInObliquity(float inObliquity) {
        this.inObliquity = inObliquity;
    }

    /**
     * @return the outObliquity
     */
    public float getOutObliquity() {
        return outObliquity;
    }

    /**
     * @param outObliquity the outObliquity to set
     */
    public void setOutObliquity(float outObliquity) {
        this.outObliquity = outObliquity;
    }

    /**
     * @return the los
     */
    public double getLos() {
        return los;
    }

    /**
     * @param los the los to set
     */
    public void setLos(double los) {
        this.los = los;
    }
}

