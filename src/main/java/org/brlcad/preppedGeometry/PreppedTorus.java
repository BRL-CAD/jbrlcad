/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brlcad.preppedGeometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.brlcad.geometry.BadGeometryException;
import org.brlcad.geometry.BrlcadDb;
import org.brlcad.geometry.Hit;
import org.brlcad.geometry.Segment;
import org.brlcad.geometry.Torus;
import org.brlcad.numerics.BoundingBox;
import org.brlcad.numerics.Complex;
import org.brlcad.numerics.Matrix;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Polynomial;
import org.brlcad.numerics.Ray;
import org.brlcad.numerics.Vector3;
import org.brlcad.spacePartition.RayData;

/**
 *
 * @author jra
 */
public class PreppedTorus extends PreppedObject {

    double tor_alpha;	/* 0 < (R2/R1) <= 1 */

    double tor_r1;		/* for inverse scaling of k values. */

    double tor_r2;		/* for curvature */

    Point tor_V;		/* center of torus */

    Vector3 tor_N;		/* unit normal to plane of torus */

    Matrix tor_SoR;	/* Scale(Rot(vect)) */

    Matrix tor_invR;	/* invRot(vect') */


    public PreppedTorus(Torus tor, Matrix matrix) throws BadGeometryException {
        super(tor.getName());
        tor_V = new Point(tor.getCenter());
        tor_N = new Vector3(tor.getNormal());

        // apply matrix
        matrix.mult(tor_V);
        matrix.mult(tor_N);
        tor_r1 = tor.getR1() / matrix.get(3, 3);
        tor_r2 = tor.getRNormal() / matrix.get(3, 3);

        /* Calculate two mutually perpendicular vectors, perpendicular to N */
        Vector3 a = Vector3.orthogonalVector(tor_N);
        Vector3 b = tor_N.crossProduct(a);
        a.scale(tor_r1);
        b.scale(tor_r1);


        Matrix R;
        Vector3 P, w1;	/* for RPP calculation */
        double f;

        /* Validate that A.B == 0, B.H == 0, A.H == 0 */
        f = a.dotProduct(b) / (tor_r1 * tor_r2);
        if (Math.abs(f) > BrlcadDb.getTolerance().getDist()) {
            throw new BadGeometryException("tor(" + tor.getName() + "):  A not perpendicular to B, f=" + f);
        }

        f = b.dotProduct(tor_N) / (tor_r2);
        if (Math.abs(f) > BrlcadDb.getTolerance().getDist()) {
            throw new BadGeometryException("tor(" + tor.getName() + "):  B not perpendicular to H, f=" + f);
        }

        f = a.dotProduct(tor_N) / tor_r1;
        if (Math.abs(f) > BrlcadDb.getTolerance().getDist()) {
            throw new BadGeometryException("tor(" + tor.getName() + "):  A not perpendicular to H, f=" + f);
        }

        /* Validate that 0 < r2 <= r1 for alpha computation */
        if (0.0 >= tor_r2 || tor_r2 > tor_r1) {
            throw new BadGeometryException("tor(" + tor.getName() + "): r1 = " + tor_r1 + ", r2 = " + tor_r2 +
                    "  0 < r2 <= r1 is not true");
        }

        /* Solid is OK, compute constant terms now */
        tor_alpha = tor_r2 / tor_r1;

        /* Compute R and invR matrices */
        R = new Matrix(4, 4);
        R.unit();
        for (int i = 0; i < 3; i++) {
            R.set(0, i, a.get(i) / tor_r1);
            R.set(1, i, b.get(i) / tor_r1);
            R.set(2, i, tor_N.get(i));
        }
        tor_invR = Matrix.transpose(R);

        /* Compute SoR.  Here, S = I / r1 */
        tor_SoR = new Matrix(R);
        tor_SoR.set(3, 3, tor_r1 * tor_SoR.get(3, 3));

        boundingRadius = tor_r1 + tor_r2;


        /*
         * Compute the bounding RPP planes for a circular torus.
         *
         * Given a circular torus with vertex V, vector N, and radii r1
         * and r2.  A bounding plane with direction vector P will touch
         * the surface of the torus at the points:
         *
         * V +/- [r2 + r1 * |N x P|] P
         */
        Point min = new Point();
        Point max = new Point();
        /* X */
        P = new Vector3(1, 0, 0);
        w1 = tor_N.crossProduct(P);
        f = tor_r2 + tor_r1 * w1.magnitude();
        w1 = Vector3.scale(P, f);
        f = Math.abs(w1.getX());
        min.setX(tor_V.getX() - f);
        max.setX(tor_V.getX() + f);

        /* Y */
        P = new Vector3(0, 1, 0);
        w1 = tor_N.crossProduct(P);
        f = tor_r2 + tor_r1 * w1.magnitude();
        w1 = Vector3.scale(P, f);
        f = Math.abs(w1.getY());
        min.setY(tor_V.getY() - f);
        max.setY(tor_V.getY() + f);

        /* Z */
        P = new Vector3(0, 0, 1);
        w1 = tor_N.crossProduct(P);
        f = tor_r2 + tor_r1 * w1.magnitude();
        w1 = Vector3.scale(P, f);
        f = Math.abs(w1.getZ());
        min.setZ(tor_V.getZ() - f);
        max.setZ(tor_V.getZ() + f);

        boundingBox = new BoundingBox(min, max);
    }

    @Override
    public List<Segment> shoot(Ray ray, RayData rayData) {
        List<Segment> segments = new ArrayList<Segment>();
        Vector3 dprime;		/* D' */
        Vector3 pprime;		/* P' */
        Vector3 work;		/* temporary vector */
        Polynomial C;		/* The final equation */
        Complex[] val = new Complex[4];		/* The complex roots */
        double[] k = new double[4];		/* The real roots */
        int i;
        int j;
        Polynomial A, Asqr;
        Polynomial X2_Y2;		/* X**2 + Y**2 */
        Vector3 cor_pprime;	/* new ray origin */
        double cor_proj;

        if (!boundingBox.doesIntersect(ray)) {
            return segments;
        }

        /* Convert vector into the space of the unit torus */
        dprime = new Vector3(ray.getDirection());
        tor_SoR.mult(dprime);
        dprime.normalize();

        pprime = Vector3.minus(ray.getStart(), tor_V);
        tor_SoR.mult(pprime);

        /* normalize distance from torus.  substitute corrected pprime
         * which contains a translation along ray direction to closest
         * approach to vertex of torus.  Translating ray origin along
         * direction of ray to closest pt. to origin of solid's coordinate
         * system, new ray origin is 'cor_pprime'.
         */
        cor_proj = pprime.dotProduct(dprime);
        cor_pprime = Vector3.scale(dprime, cor_proj);
        cor_pprime = Vector3.minus(pprime, cor_pprime);

        /* Given a line and a ratio, alpha, finds the equation of the unit
         * torus in terms of the variable 't'.
         *
         * The equation for the torus is:
         *
         * [ X**2 + Y**2 + Z**2 + (1 - alpha**2) ]**2 - 4*(X**2 + Y**2) = 0
         *
         * First, find X, Y, and Z in terms of 't' for this line, then
         * substitute them into the equation above.
         *
         * Wx = Dx*t + Px
         *
         * Wx**2 = Dx**2 * t**2  +  2 * Dx * Px  +  Px**2
         *		[0]                [1]           [2]    dgr=2
         */
        X2_Y2 = new Polynomial(2);
        X2_Y2.setCoeff(0, dprime.getX() * dprime.getX() + dprime.getY() * dprime.getY());
        X2_Y2.setCoeff(1, 2.0 * (dprime.getX() * cor_pprime.getX() +
                dprime.getY() * cor_pprime.getY()));
        X2_Y2.setCoeff(2, cor_pprime.getX() * cor_pprime.getX() +
                cor_pprime.getY() * cor_pprime.getY());

        /* A = X2_Y2 + Z2 */
        A = new Polynomial(2);
        A.setCoeff(0, X2_Y2.getCoeff(0) + dprime.getZ() * dprime.getZ());
        A.setCoeff(1, X2_Y2.getCoeff(1) + 2.0 * dprime.getZ() * cor_pprime.getZ());
        A.setCoeff(2, X2_Y2.getCoeff(2) + cor_pprime.getZ() * cor_pprime.getZ() +
                1.0 - tor_alpha * tor_alpha);

        /* Inline expansion of (void) bn_poly_mul(&Asqr, &A, &A) */
        /* Both polys have degree two */
        Asqr = new Polynomial(4);
        Asqr.setCoeff(0, A.getCoeff(0) * A.getCoeff(0));
        Asqr.setCoeff(1, A.getCoeff(0) * A.getCoeff(1) + A.getCoeff(1) * A.getCoeff(0));
        Asqr.setCoeff(2, A.getCoeff(0) * A.getCoeff(2) + A.getCoeff(1) * A.getCoeff(1) + A.getCoeff(2) * A.getCoeff(0));
        Asqr.setCoeff(3, A.getCoeff(1) * A.getCoeff(2) + A.getCoeff(2) * A.getCoeff(1));
        Asqr.setCoeff(4, A.getCoeff(2) * A.getCoeff(2));

        /* Inline expansion of bn_poly_scale(&X2_Y2, 4.0) and
         * bn_poly_sub(&C, &Asqr, &X2_Y2).
         */
        C = new Polynomial(4);
        C.setCoeff(0, Asqr.getCoeff(0));
        C.setCoeff(1, Asqr.getCoeff(1));
        C.setCoeff(2, Asqr.getCoeff(2) - X2_Y2.getCoeff(0) * 4.0);
        C.setCoeff(3, Asqr.getCoeff(3) - X2_Y2.getCoeff(1) * 4.0);
        C.setCoeff(4, Asqr.getCoeff(4) - X2_Y2.getCoeff(2) * 4.0);

        /* It is known that the equation is 4th order.  Therefore, if the
         * root finder returns other than 4 roots, error.
         */
        try {
        val = C.roots();
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.err.println( "ray = " + ray);
            ex.printStackTrace();
        }
        if (val.length != 4) {
            if (val.length > 0) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
                        "tor:  Poynomial.roots() did not find 4 roots!! (got " + val.length + ")\n");
            }
            return segments;
        }

        /* Only real roots indicate an intersection in real space.
         *
         * Look at each root returned; if the imaginary part is zero or
         * sufficiently close, then use the real part as one value of 't'
         * for the intersections
         */
        i = 0;
        for (j = 0, i = 0; j < 4; j++) {
            if (val[j] != null) {
                if (Math.abs(val[j].getImaginary()) < BrlcadDb.getTolerance().getDist()) {
                    k[i++] = val[j].getReal();
                }
            }
        }

        /* reverse above translation by adding distance to all 'k' values.
         */
        for (j = 0; j < i; ++j) {
            k[j] -= cor_proj;
        }

        /* Here, 'i' is number of points found */
        switch (i) {
            case 0:
                return segments;		/* No hit */

            default:
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
                        "PreppedTorfus.shoot: reduced 4 to " + i + " roots\n");
                return segments;		/* No hit */

            case 2:
                 {
                     /* Sort most distant to least distant. */
                     double u;
                     if ((u = k[0]) < k[1]) {
                         /* bubble larger towards [0] */
                         k[0] = k[1];
                         k[1] = u;
                     }
                }
                break;
            case 4:
                 {
                     int n;
                     int lim;

                     /*  Inline rt_pt_sort().  Sorts k[] into descending order. */
                     for (lim = i - 1; lim > 0; lim--) {
                         for (n = 0; n < lim; n++) {
                             double u;
                             if ((u = k[n]) < k[n + 1]) {
                                 /* bubble larger towards [0] */
                                 k[n] = k[n + 1];
                                 k[n + 1] = u;
                             }
                         }
                     }
                }
                break;
        }

        /* Now, t[0] > t[npts-1] */
        /* k[1] is entry point, and k[0] is farthest exit point */
        double dist = k[1]*tor_r1;
        Point inPoint = new Point(ray.getStart());
        inPoint.join(dist, ray.getDirection());
        Vector3 inNorm = normal(pprime, dprime, k[1], inPoint, tor_alpha, tor_invR);
        Hit in = new Hit(dist, inPoint, inNorm, 0, rayData, name);
        dist = k[0]*tor_r1;
        Point outPoint = new Point(ray.getStart());
        outPoint.join(dist, ray.getDirection());
        Vector3 outNorm = normal(pprime, dprime, k[0], outPoint, tor_alpha, tor_invR);
        Hit out = new Hit(dist, outPoint, outNorm, 0, rayData, name);
        Segment seg = new Segment(in, out);
        segments.add(seg);

        if (i == 2)
            return segments;			/* HIT */

        /* 4 points */
        /* k[3] is entry point, and k[2] is exit point */
        dist = k[3]*tor_r1;
        inPoint = new Point(ray.getStart());
        inPoint.join(dist, ray.getDirection());
        inNorm = normal(pprime, dprime, k[3], inPoint, tor_alpha, tor_invR);
        in = new Hit(dist, inPoint, inNorm, 0, rayData, name);
        dist = k[2]*tor_r1;
        outPoint = new Point(ray.getStart());
        outPoint.join(dist, ray.getDirection());
        outNorm = normal(pprime, dprime, k[2], outPoint, tor_alpha, tor_invR);
        out = new Hit(dist, outPoint, outNorm, 0, rayData, name);
        seg = new Segment(in, out);
        segments.add(seg);
        return segments;
    }

    private Vector3 normal(Vector3 pprime, Vector3 dprime, double dist, Point hitPt, double tor_alpha, Matrix tor_invR)
    {
        Vector3 hit_vpriv = new Vector3(pprime);
        hit_vpriv.join(dist, dprime);

        double w = hit_vpriv.getX() * hit_vpriv.getX() +
                hit_vpriv.getY() * hit_vpriv.getY() +
                hit_vpriv.getZ() * hit_vpriv.getZ() +
                1.0 - tor_alpha * tor_alpha;
        Vector3 norm = new Vector3((w - 2.0) * hit_vpriv.getX(),
                (w - 2.0) * hit_vpriv.getY(),
                w * hit_vpriv.getZ());
        norm.normalize();

        tor_invR.mult(norm);

        return norm;

    }

    @Override
    public List<Segment> makeSegs(Set<Hit> hits, Ray ray, RayData rayData) {
		// this should never get called
		return null;
    }
}
