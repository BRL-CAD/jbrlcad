/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.preppedGeometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.brlcad.geometry.Hit;
import org.brlcad.geometry.Segment;
import org.brlcad.geometry.Tgc;
import org.brlcad.numerics.BoundingBox;
import org.brlcad.numerics.Constants;
import org.brlcad.numerics.Matrix;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Ray;
import org.brlcad.numerics.Vector3;
import org.brlcad.spacePartition.RayData;

/**
 *
 * @author jra
 */
public class PreppedRec extends PreppedObject {
    
    private Point	rec_V;		/* center of base of cylinder  */
    private Vector3	rec_A;		/* One axis of ellipse */
    private Vector3	rec_B;		/* Other axis of ellipse */
    private Vector3	rec_Hunit;	/* Unit H vector */
    private Matrix	rec_SoR;	/* Scale(Rot(vect)) */
    private Matrix	rec_invRoS;	/* invRot(Scale(vect)) */

    public PreppedRec(Tgc tgc, Matrix matrix, Point V, Vector3 A, Vector3 B, Vector3 Hunit, double mag_a, double mag_b, double mag_c, double mag_d, double mag_h) {
        // this constructor assumes that the passed in Tgc is actually an Rec
        super( tgc.getName() );
        name = tgc.getName();
        rec_Hunit = Hunit;
        rec_V = V;
        rec_A = A;
        rec_B = B;
        
        double magsq_a = mag_a * mag_a;
        double magsq_b = mag_b * mag_b;
        double magsq_c = mag_c * mag_c;
        double magsq_d = mag_d * mag_d;

        /* Compute R and Rinv matrices */
        Matrix R = new Matrix(4,4);
        R.unit();
        double f = 1.0 / mag_a;
        for( int i=0 ; i<3 ; i++ ) {
            R.set(0, i, rec_A.get(i) * f);
        }
        f = 1.0 / mag_b;
        for( int i=0 ; i<3 ; i++ ) {
            R.set(1, i, rec_B.get(i) * f);
        }
        for( int i=0 ; i<3 ; i++ ) {
            R.set(2, i, rec_Hunit.get(i));
        }
        Matrix Rinv = Matrix.transpose(R);			/* inv of rot mat is trn */

        /* Compute S */
        Matrix S = new Matrix(4,4);
        S.unit();
        S.set(0, 0, 1.0/mag_a);
        S.set(1, 1, 1.0/mag_b);
        S.set(2, 2, 1.0/mag_h);

        /* Compute SoR and invRoS */
        rec_SoR = new Matrix(4,4);
        rec_SoR.add(S);
        rec_SoR.mult(R);
        rec_invRoS = new Matrix(4,4);
        rec_invRoS.add(Rinv);
        rec_invRoS.mult(S);

        /* Compute bounding sphere and RPP */
        {
            double dx, dy, dz;	/* For bounding sphere */
            Vector3 P, w1;
            double tmp, z;

            /* X */
            w1 = new Vector3(1.0, 0.0, 0.0)		/* bounding plane normal */;
            R.mult(w1);		/* map plane into local coord syst */
            /* 1st end ellipse (no Z part) */
            tmp = magsq_a * w1.getX() * w1.getX() + magsq_b * w1.getY() * w1.getY();
            if (tmp > Constants.SQRT_SMALL_FASTF) {
                f = Math.sqrt(tmp);		/* XY part */
            } else {
                f = 0;
            }
            Point st_min = new Point();
            Point st_max = new Point();
            st_min.setX(rec_V.getX() - f);	/* V.P +/- f */
            st_max.setX(rec_V.getX() + f);
            /* 2nd end ellipse */
            z = w1.getZ() * mag_h;		/* Z part */
            tmp = magsq_c * w1.getX() * w1.getX() + magsq_d * w1.getY() * w1.getY();
            if (tmp > Constants.SQRT_SMALL_FASTF) {
                f = Math.sqrt(tmp);		/* XY part */
            } else {
                f = 0;
            }
            if (rec_V.getX() - f + z < st_min.getX()) {
                st_min.setX( rec_V.getX() - f + z );
            }
            if (rec_V.getX() + f + z > st_max.getX()) {
                st_max.setX( rec_V.getX() + f + z );
            }

            /* Y */
            w1 = new Vector3( 0.0, 1.0, 0.0 ) /* bounding plane normal */;
            R.mult(w1);		/* map plane into local coord syst */
            /* 1st end ellipse (no Z part) */
            tmp = magsq_a * w1.getX() * w1.getX() + magsq_b * w1.getY() * w1.getY();
            if (tmp > Constants.SQRT_SMALL_FASTF) {
                f = Math.sqrt(tmp);		/* XY part */
            } else {
                f = 0;
            }
            st_min.setY(rec_V.getY() - f);	/* V.P +/- f */
            st_max.setY(rec_V.getY() + f);
            /* 2nd end ellipse */
            z = w1.getZ() * mag_h;		/* Z part */
            tmp = magsq_c * w1.getX() * w1.getX() + magsq_d * w1.getY() * w1.getY();
            if (tmp > Constants.SQRT_SMALL_FASTF) {
                f = Math.sqrt(tmp);		/* XY part */
            } else {
                f = 0;
            }
            if (rec_V.getY() - f + z < st_min.getY()) {
                st_min.setY( rec_V.getY() - f + z );
            }
            if (rec_V.getY() + f + z > st_max.getY()) {
                st_max.setY( rec_V.getY() + f + z );
            }

            /* Z */
            w1 = new Vector3( 0.0, 0.0, 1.0 );		/* bounding plane normal */
            R.mult(w1);		/* map plane into local coord syst */
            /* 1st end ellipse (no Z part) */
            tmp = magsq_a * w1.getX() * w1.getX() + magsq_b * w1.getY() * w1.getY();
            if (tmp > Constants.SQRT_SMALL_FASTF) {
                f = Math.sqrt(tmp);		/* XY part */
            } else {
                f = 0;
            }
            st_min.setZ(rec_V.getZ() - f);	/* V.P +/- f */
            st_max.setZ(rec_V.getZ() + f );
            /* 2nd end ellipse */
            z = w1.getZ() * mag_h;		/* Z part */
            tmp = magsq_c * w1.getX() * w1.getX() + magsq_d * w1.getY() * w1.getY();
            if (tmp > Constants.SQRT_SMALL_FASTF) {
                f = Math.sqrt(tmp);		/* XY part */
            } else {
                f = 0;
            }
            if (rec_V.getZ() - f + z < st_min.getZ()) {
                st_min.setZ( rec_V.getZ() - f + z );
            }
            if (rec_V.getZ() + f + z > st_max.getZ()) {
                st_max.setZ(rec_V.getZ() + f + z);
            }

            dx = (st_max.getX() - st_min.getX()) / 2;
            f = dx;
            dy = (st_max.getY() - st_min.getY()) / 2;
            if (dy > f) {
                f = dy;
            }
            dz = (st_max.getZ() - st_min.getZ()) / 2;
            if (dz > f) {
                f = dz;
            }
            this.boundingRadius = f;
            this.boundingBox = new BoundingBox(st_min, st_max);
            this.center = new Point( this.boundingBox.getMin() );
            this.center.plus(this.boundingBox.getMax());
            this.center.scale(0.5);
        }
    }

    /**
     * Check for intersections with the top and bottom plates of the REC
     *
     * @param hits The Set of hits where newly discovered intersections will be added
     * @param pprime Ray start point rotated into the coords of the unit cylinder
     * @param dprime Ray direction vector rotated into the coords of the unit cylinder
     * @param rayData Information about the ray that is being intersected with this REC
     */
    private void checkPlates(Set<Hit> hits, Vector3 pprime, Vector3 dprime, RayData rayData) {
        Ray ray = rayData.getTheRay();
        double k1 = -pprime.getZ() / dprime.getZ();		/* bottom plate */
        double k2 = (1.0 - pprime.getZ()) / dprime.getZ();	/* top plate */

        if( !Double.isInfinite(k1) && !Double.isNaN(k1) ) {
            Vector3 vpriv = Vector3.plus(pprime, Vector3.scale(dprime, k1));
            if (vpriv.getX() * vpriv.getX() + vpriv.getY() * vpriv.getY() <= 1.0) {
                Point hitPoint = new Point(ray.getStart());
                hitPoint.translate(Vector3.scale(ray.getDirection(), k1));
                Vector3 norm = Vector3.negate(rec_Hunit);
                Hit hit = new Hit(k1, hitPoint, norm, Tgc.BOTTOM, rayData);
                hits.add(hit);
            }
        }

        if( !Double.isInfinite(k2) && !Double.isNaN(k2) ) {
            Vector3 vpriv = Vector3.plus(pprime, Vector3.scale(dprime, k2));
            if (vpriv.getX() * vpriv.getX() + vpriv.getY() * vpriv.getY() <= 1.0) {
                Point hitPoint = new Point(ray.getStart());
                hitPoint.translate(Vector3.scale(ray.getDirection(), k2));
                Vector3 norm = new Vector3(rec_Hunit);
                Hit hit = new Hit(k2, hitPoint, norm, Tgc.TOP, rayData);
                hits.add(hit);
            }
        }
    }

    @Override
    public List<Segment> shoot(Ray ray, RayData rayData) {
        Vector3 dprime;		/* D' */
        Vector3 pprime;		/* P' */
        double k1, k2;		/* distance constants of solution */
        Set<Hit> hits = new HashSet<Hit>();

        /* out, Mat, vect */
        dprime = new Vector3(ray.getDirection());
        rec_SoR.mult(dprime);
        pprime = Vector3.minus(ray.getStart(), rec_V);
        rec_SoR.mult(pprime);

        checkPlates(hits, pprime, dprime, rayData);

        /* Find roots of the equation, using forumla for quadratic w/ a=1 */
        {
            double b;		/* coeff of polynomial */
            double root;		/* root of radical */
            double dx2dy2;

            dx2dy2 =  1 / (dprime.getX() * dprime.getX() + dprime.getY() * dprime.getY());
            b = 2 * (dprime.getX() * pprime.getX() + dprime.getY() * pprime.getY()) * dx2dy2;
            root = b * b - 4 * dx2dy2 *
                    (pprime.getX() * pprime.getX() + pprime.getY() * pprime.getY() - 1);
            if (root <= 0 ) {
                checkPlates(hits, pprime, dprime, rayData);
            }
            root = Math.sqrt(root);

            k1 = (root - b) * 0.5;
            k2 = (root + b) * (-0.5);
        }

        /*
         *  k1 and k2 are potential solutions to intersection with side.
         *  See if they fall in range.
         */
        Vector3 vpriv;
        if( !Double.isInfinite(k1) && !Double.isNaN(k1) ) {
            vpriv = Vector3.plus(pprime, Vector3.scale(dprime, k1));
            if (vpriv.getZ() >= 0.0 && vpriv.getZ() <= 1.0) {
                Point hitPoint = new Point(ray.getStart());
                hitPoint.translate(Vector3.scale(ray.getDirection(), k1));
                vpriv.setZ(0.0);
                rec_invRoS.mult(vpriv);
                vpriv.normalize();
                Hit hit = new Hit(k1, hitPoint, vpriv, Tgc.BODY, rayData);
                hits.add(hit);
            }
        }

        if( !Double.isInfinite(k2) && !Double.isNaN(k2) ) {
            vpriv = Vector3.plus(pprime, Vector3.scale(dprime, k2));
            if (vpriv.getZ() >= 0.0 && vpriv.getZ() <= 1.0) {
                Point hitPoint = new Point(ray.getStart());
                hitPoint.translate(Vector3.scale(ray.getDirection(), k2));
                vpriv.setZ(0.0);
                rec_invRoS.mult(vpriv);
                vpriv.normalize();
                Hit hit = new Hit(k2, hitPoint, vpriv, Tgc.BODY, rayData);
                hits.add(hit);
            }
        }

        List<Segment> segs = makeSegs(hits, ray, rayData);
        rayData.addSegs(this, segs);

        return segs;
    }

    @Override
    public List<Segment> makeSegs(Set<Hit> hits, Ray ray, RayData rayData) {
        List<Segment> segs = new ArrayList<Segment>();
        if( hits.size() == 0 ) {
            return segs;
        }

        List<Hit> sortedHits = new ArrayList(hits);

        if( sortedHits.size() > 1 ) {
            Collections.sort(sortedHits);

            // eliminate duplicate hits
            int i = 0;
            while( i < sortedHits.size()-1 ) {
                Hit hit1 = sortedHits.get(i);
                Hit hit2 = sortedHits.get(i+1);
                double diff = Math.abs(hit2.getHit_dist() - hit1.getHit_dist());
                if( diff < rayData.getTolerance().getDist() ) {
                    // duplicate - remove one of them
                    sortedHits.remove(i);
                } else {
                    i++;
                }
            }
        }

        if (sortedHits.size() == 1) {
            Hit hit = hits.iterator().next();
            int surfno = hit.getHit_surfno();
            if (surfno == Tgc.BODY) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
                        "One intersection with body of primitive " + this.name);
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
                        "One intersection with top or bottom of primitive " + this.name);
            }
            /*
             *  Ray is tangent to body of cylinder,
             *  or a single hit on on an end plate (??)
             *  This could be considered a MISS,
             *  but to signal the condition, return 0-thickness hit.
             */
            Hit hit2 = new Hit(hit);
            hits.add(hit2);

            sortedHits = new ArrayList<Hit>();
            sortedHits.add(hit);
            sortedHits.add(hit2);
        }

        // if there is more than two hits, just use the first two
        Segment seg = new Segment(sortedHits.get(0), sortedHits.get(1));
        segs.add(seg);
        return segs;
    }

}
