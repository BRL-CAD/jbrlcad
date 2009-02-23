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
import org.brlcad.geometry.BadGeometryException;
import org.brlcad.geometry.Hit;
import org.brlcad.geometry.Segment;
import org.brlcad.geometry.Tgc;
import org.brlcad.numerics.BoundingBox;
import org.brlcad.numerics.Complex;
import org.brlcad.numerics.Matrix;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Ray;
import org.brlcad.numerics.Vector3;
import org.brlcad.numerics.Constants;
import org.brlcad.numerics.Polynomial;
import org.brlcad.spacePartition.RayData;

/**
 *
 * @author jra
 */
public class PreppedTgc extends PreppedObject {
    private static final double VLARGE = 1000000.0;
    private Point   tgc_V;		/*  center of base of TGC	*/
    private double	tgc_sH;		/*  magnitude of sheared H vector	*/
    private double	tgc_A;		/*  magnitude of A vector		*/
    private double	tgc_B;		/*  magnitude of B vector		*/
    private double	tgc_C;		/*  magnitude of C vector		*/
    private double	tgc_D;		/*  magnitude of D vector		*/
    private double	tgc_CdAm1;	/*  (C/A - 1)				*/
    private double  tgc_DdBm1 ;	/*  (D/B - 1)				*/
    private double	tgc_AAdCC;	/*  (|A|**2)/(|C|**2)			*/
    private double	tgc_BBdDD;	/*  (|B|**2)/(|D|**2)			*/
    private Vector3	tgc_N;		/*  normal at 'top' of cone		*/
    private Matrix	tgc_ScShR;	/*  Scale( Shear( Rot( vect )))		*/
    private Matrix	tgc_invRtShSc;	/*  invRot( trnShear( Scale( vect )))	*/
    private boolean	tgc_AD_CB;	/*  boolean:  A*D == C*B  */

    public PreppedTgc(Tgc tgc, Matrix matrix, Point V, Vector3 H, Vector3 A, Vector3 B, Vector3 C, Vector3 D, Vector3 Hunit, double mag_a, double mag_b, double mag_c, double mag_d, double mag_h) throws BadGeometryException {
		super( tgc.getName() );

        tgc_N = new Vector3(0,0,0);
        double prod_ab = mag_a * mag_b;
        double prod_cd = mag_c * mag_d;
        double magsq_c = mag_c * mag_c;

        if (Math.abs(mag_h) < Constants.RT_LEN_TOL) {
            throw new BadGeometryException(tgc.getName() + " has zero length height vector");
        }

        /* Validate that figure is not two-dimensional			*/
        if (Math.abs(mag_a) < Constants.RT_LEN_TOL &&
                Math.abs(mag_c) < Constants.RT_LEN_TOL) {
            throw new BadGeometryException(tgc.getName() + " has zero length A and C vectors");
        }
        if (Math.abs(mag_b) < Constants.RT_LEN_TOL &&
                Math.abs(mag_d) < Constants.RT_LEN_TOL) {
            throw new BadGeometryException(tgc.getName() + " has zero length B and D vectors");
        }

        Point tip_v;
        Vector3 tip_h;
        Vector3 tip_a;
        Vector3 tip_b;
        Vector3 tip_c;
        Vector3 tip_d;
        /* Validate that both ends are not degenerate */
        if (prod_ab <= Constants.SQRT_SMALL_FASTF) {
                if( prod_cd <= Constants.SQRT_SMALL_FASTF) {
                    throw new BadGeometryException(tgc.getName() + " is degenerate at both ends");
            }
            /* Exchange ends, so that in solids with one degenerate end,
             * the CD end always is always the degenerate one
             */
            tip_v = new Point(V);
            tip_v.join(1.0, H);
            tip_h = Vector3.negate(H);
            tip_a = new Vector3(C);
            tip_b = new Vector3(D);
            tip_c = new Vector3(A);
            tip_d = new Vector3(B);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                    tgc.getName() + ": degenerate end exchange");
        } else {
            tip_v = new Point(V);
            tip_h = new Vector3(H);
            tip_a = new Vector3(A);
            tip_b = new Vector3(B);
            tip_c = new Vector3(C);
            tip_d = new Vector3(D);
        }

        /* Ascertain whether H lies in A-B plane 			*/
        Vector3 work = tip_a.crossProduct(tip_b);
        double f = tip_h.dotProduct(work) / ( prod_ab*mag_h );
        if (Math.abs(f) < Constants.RT_DOT_TOL) {
            throw new BadGeometryException(tgc.getName() + ": H lies in A-B plane");
        }

        if (prod_ab > Constants.SQRT_SMALL_FASTF) {
            /* Validate that A.B == 0 */
            f = tip_a.dotProduct(tip_b) / prod_ab;
            if (!(Math.abs(f) < Constants.RT_DOT_TOL)) {
                throw new BadGeometryException(tgc.getName() + ": A not perpendicular to B");
            }
        }
        if (prod_cd > Constants.SQRT_SMALL_FASTF) {
            /* Validate that C.D == 0 */
            f = tip_c.dotProduct(tip_d) / prod_cd;
            if (!(Math.abs(f) < Constants.RT_DOT_TOL)) {
                throw new BadGeometryException(tgc.getName() + ": C not perpendicular to D");
            }
        }

        if (mag_a * mag_c > Constants.SQRT_SMALL_FASTF) {
            /* Validate that  A || C */
            f = 1.0 - tip_a.dotProduct(tip_c) / (mag_a * mag_c);
            if (!(Math.abs(f) < Constants.RT_DOT_TOL)) {
                throw new BadGeometryException(tgc.getName() + ": A not parallel to C");
            }
        }

        if (mag_b * mag_d > Constants.SQRT_SMALL_FASTF) {
            /* Validate that  B || D, for parallel planes	*/
            f = 1.0 - tip_b.dotProduct(tip_d) / (mag_b * mag_d);
            if (!(Math.abs(f) < Constants.RT_DOT_TOL)) {
                throw new BadGeometryException(tgc.getName() + ": B not parallel to D");
            }
        }

        tgc_V = new Point(tip_v);
        tgc_A = tip_a.magnitude();
        tgc_B = tip_b.magnitude();
        tgc_C = tip_c.magnitude();
        tgc_D = tip_d.magnitude();
        double magsq_a = tgc_A * tgc_A;
        double magsq_b = tgc_B * tgc_B;
        magsq_c = tgc_C * tgc_C;
        double magsq_d = tgc_D * tgc_D;

        Logger.getLogger(this.getClass().getName()).log(Level.FINEST, tgc.getName() +
                ": a = " + tgc_A + ", b = " + tgc_B + ", c = " + tgc_C + ", d = " + tgc_D );

        /* Part of computing ALPHA() */
        if( magsq_c < Constants.SQRT_SMALL_FASTF) {
            tgc_AAdCC = VLARGE;
        } else {
            tgc_AAdCC = magsq_a / magsq_c;
        }
        if (magsq_d < Constants.SQRT_SMALL_FASTF) {
            tgc_BBdDD = VLARGE;
        } else {
            tgc_BBdDD = magsq_b / magsq_d;
        }

        /*  If the eccentricities of the two ellipses are the same,
         *  then the cone equation reduces to a much simpler quadratic
         *  form.  Otherwise it is a (gah!) quartic equation.
         */
        f = reldiff((tgc_A * tgc_D), (tgc_C * tgc_B));
        tgc_AD_CB = (f < 0.0001);		/* A*D == C*B */
        Matrix rot = new Matrix(4,4);
        Matrix irot = new Matrix(4,4);
        rt_tgc_rotate(tip_a, tip_b, tip_h, rot, irot, tgc_N);
        Vector3 nH = new Vector3(tip_h);
        rot.mult(nH);
        tgc_sH = nH.getZ();

        tgc_CdAm1 = tgc_C / tgc_A - 1.0;
        tgc_DdBm1 = tgc_D / tgc_B - 1.0;
        if (Math.abs(tgc_CdAm1) < Constants.SQRT_SMALL_FASTF) {
            tgc_CdAm1 = 0.0;
        }
        if (Math.abs(tgc_DdBm1) < Constants.SQRT_SMALL_FASTF) {
            tgc_DdBm1 = 0.0;
        }

        /*
         *	Added iShr parameter to tgc_shear().
         *	Changed inverse transformation of normal vectors of std.
         *		solid intersection to include shear inverse
         *		(tgc_invRtShSc).
         *	Fold in scaling transformation into the transformation to std.
         *		space from target space (tgc_ScShR).
         */
        Matrix Shr = new Matrix(4,4);
        Matrix tShr = new Matrix(4,4);
        Matrix iShr = new Matrix(4,4);
        tgc_shear(nH, 2, Shr, tShr, iShr);
        Matrix Scl = new Matrix(4,4);
        Matrix iScl = new Matrix(4,4);
        tgc_scale(tgc_A, tgc_B, tgc_sH, Scl, iScl);

        Matrix tmp = new Matrix(Shr);
        tmp.mult(rot);
        tgc_ScShR = new Matrix(Scl);
        tgc_ScShR.mult(tmp);

        tmp = new Matrix(tShr);
        tmp.mult(Scl);
        tgc_invRtShSc = new Matrix(irot);
        tgc_invRtShSc.mult(tmp);

        /* Compute bounding sphere and RPP */
        {
            double dx, dy, dz;	/* For bounding sphere */
            Point temp;
            this.boundingBox = new BoundingBox();

            /* There are 8 corners to the bounding RPP */
            /* This may not be minimal, but does fully contain the TGC */

            temp = new Point( tgc_V );
            temp.plus(tip_a);
            temp.plus(tip_b);
            this.boundingBox.extend(temp);
            temp = new Point( tgc_V );
            temp.plus(tip_a);
            temp.subtract(tip_b);
            this.boundingBox.extend(temp);
            temp = new Point( tgc_V );
            temp.subtract(tip_a);
            temp.plus(tip_b);
            this.boundingBox.extend(temp);
            temp = new Point( tgc_V );
            temp.subtract(tip_a);
            temp.subtract(tip_b);
            this.boundingBox.extend(temp);

            temp = new Point( tgc_V );
            temp.plus(tip_h);
            temp.plus(tip_c);
            temp.plus(tip_d);
            this.boundingBox.extend(temp);
            temp = new Point( tgc_V );
            temp.plus(tip_h);
            temp.plus(tip_c);
            temp.subtract(tip_d);
            this.boundingBox.extend(temp);
            temp = new Point( tgc_V );
            temp.plus(tip_h);
            temp.subtract(tip_c);
            temp.plus(tip_d);
            this.boundingBox.extend(temp);
            temp = new Point( tgc_V );
            temp.plus(tip_h);
            temp.subtract(tip_c);
            temp.subtract(tip_d);
            this.boundingBox.extend(temp);

            this.center = this.boundingBox.getMin();
            this.center.plus(this.boundingBox.getMax());
            this.center.scale(0.5);

            this.boundingRadius = this.boundingBox.getDiameter().magnitude()/2.0;
        }
    }

    @Override
    public List<Segment> shoot(Ray ray, RayData rayData) {
        Set<Hit> hits = new HashSet<Hit>();
        Vector3 pprime;
        Vector3 dprime;
        Vector3 work;
        double[] k = new double[6];
        int[] hit_type = new int[6];
        int npts = 0;
        double t, b, zval, dir;
        double t_scale;
        double alf1, alf2;
        int intersect;
        Vector3 cor_pprime;	/* corrected P prime */
        double cor_proj = 0;	/* corrected projected dist */
        int i;
        Polynomial C;	/*  final equation	*/
        Polynomial Xsqr, Ysqr;
        Polynomial R, Rsqr;
        Vector3 vpriv;

        /* find rotated point and direction */
        dprime = new Vector3(ray.getDirection());
        tgc_ScShR.mult(dprime);

        /*
         *  A vector of unit length in model space (r_dir) changes length in
         *  the special unit-tgc space.  This scale factor will restore
         *  proper length after hit points are found.
         */
        t_scale = dprime.magnitude();
        if (t_scale < Constants.SMALL_FASTF) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
                    "tgc(" + this.name + ") dprime=" + dprime + ", t_scale=" + t_scale + ", miss.\n");
            return new ArrayList<Segment>();
        }
        t_scale = 1 / t_scale;
        dprime.scale(t_scale);	/* VUNITIZE( dprime ); */

        if (Math.abs(dprime.getZ()) < Constants.RT_PCOEF_TOL) {
            dprime.setZ(0.0);	/* prevent rootfinder heartburn */
        }

        pprime = Vector3.minus(ray.getStart(), tgc_V);
        tgc_ScShR.mult(pprime);

        /* Translating ray origin along direction of ray to closest
         * pt. to origin of solids coordinate system, new ray origin
         * is 'cor_pprime'.
         */
        cor_proj = -pprime.dotProduct(dprime);
        cor_pprime = new Vector3(pprime);
        cor_pprime.join(cor_proj, dprime);

        /*
         * The TGC is defined in "unit" space, so the parametric distance
         * from one side of the TGC to the other is on the order of 2.
         * Therefore, any vector/point coordinates that are very small
         * here may be considered to be zero,
         * since double precision only has 18 digits of significance.
         * If these tiny values were left in, then as they get
         * squared (below) they will cause difficulties.
         */
        for (i = 0; i < 3; i++) {
            /* Direction cosines */
            if (Math.abs(dprime.get(i)) < 1e-10) {
                dprime.set(i, 0.0);
            }
            /* Position in -1..+1 coordinates */
            if (Math.abs(cor_pprime.get(i)) < 1e-20) {
                cor_pprime.set(i, 0.0);
            }
        }

        /*
         *  Given a line and the parameters for a standard cone, finds
         *  the roots of the equation for that cone and line.
         *  Returns the number of real roots found.
         *
         *  Given a line and the cone parameters, finds the equation
         *  of the cone in terms of the variable 't'.
         *
         *  The equation for the cone is:
         *
         *      X**2 * Q**2  +  Y**2 * R**2  -  R**2 * Q**2 = 0
         *
         *  where	R = a + ((c - a)/|H'|)*Z
         *		Q = b + ((d - b)/|H'|)*Z
         *
         *  First, find X, Y, and Z in terms of 't' for this line, then
         *  substitute them into the equation above.
         *
         *  Express each variable (X, Y, and Z) as a linear equation
         *  in 'k', eg, (dprime[X] * k) + cor_pprime[X], and
         *  substitute into the cone equation.
         */
        double[] cf = new double[5];
        cf[0] = dprime.getX() * dprime.getX();
        cf[1] = 2.0 * dprime.getX() * cor_pprime.getX();
        cf[2] = cor_pprime.getX() * cor_pprime.getX();
        Xsqr = new Polynomial(2, cf);

        cf[0] = dprime.getY() * dprime.getY();
        cf[1] = 2.0 * dprime.getY() * cor_pprime.getY();
        cf[2] = cor_pprime.getY() * cor_pprime.getY();
        Ysqr = new Polynomial(2, cf);

        cf[0] = dprime.getZ() * tgc_CdAm1;
        /* A vector is unitized (tgc->tgc_A == 1.0) */
        cf[1] = (cor_pprime.getZ() * tgc_CdAm1) + 1.0;
        R = new Polynomial(1, cf);

        /* (void) rt_poly_mul(&Rsqr, &R, &R); */
        double[] cfsq = new double[3];
        cfsq[0] = cf[0] * cf[0];
        cfsq[1] = cf[0] * cf[1] * 2;
        cfsq[2] = cf[1] * cf[1];
        Rsqr = new Polynomial(2, cfsq);

        /*
         *  If the eccentricities of the two ellipses are the same,
         *  then the cone equation reduces to a much simpler quadratic
         *  form.  Otherwise it is a (gah!) quartic equation.
         *
         *  this can only be done when C.cf[0] is not too small!!!! (JRA)
         */
        cf[0] = Xsqr.getCoeff(0) + Ysqr.getCoeff(0) - Rsqr.getCoeff(0);
        if (tgc_AD_CB && !(Math.abs(cf[0]) < 1.0e-10)) {
            double roots;

            /*
             *  (void) bn_poly_add( &sum, &Xsqr, &Ysqr );
             *  (void) bn_poly_sub( &C, &sum, &Rsqr );
             */
            cf[1] = Xsqr.getCoeff(1) + Ysqr.getCoeff(1) - Rsqr.getCoeff(1);
            cf[2] = Xsqr.getCoeff(2) + Ysqr.getCoeff(2) - Rsqr.getCoeff(2);
            C = new Polynomial(2, cf);

            /* Find the real roots the easy way.  C.dgr==2 */
            if ((roots = C.getCoeff(1) * C.getCoeff(1) - 4 * C.getCoeff(0) * C.getCoeff(2)) >= 0) {
                double f = 0.5 / C.getCoeff(0);
                roots = Math.sqrt(roots);
                k[0] = (roots - C.getCoeff(1)) * f;
                hit_type[0] = Tgc.BODY;

                k[1] = (roots + C.getCoeff(1)) * -f;
                hit_type[1] = Tgc.BODY;
                npts = 2;
            }
        } else {
            Polynomial Q, Qsqr;
            Complex[] val;	/* roots of final equation */
            int l;

            cf[0] = dprime.getZ() * tgc_DdBm1;
            /* B vector is unitized (tgc->tgc_B == 1.0) */
            cf[1] = (cor_pprime.getZ() * tgc_DdBm1) + 1.0;
            Q = new Polynomial(1, cf);

            /* (void) bn_poly_mul( &Qsqr, &Q, &Q ); */
            cf[0] = Q.getCoeff(0) * Q.getCoeff(0);
            cf[1] = Q.getCoeff(0) * Q.getCoeff(1) * 2;
            cf[2] = Q.getCoeff(1) * Q.getCoeff(1);
            Qsqr = new Polynomial(2, cf);

            /*
             * (void) bn_poly_mul( &T1, &Qsqr, &Xsqr );
             * (void) bn_poly_mul( &T2 &Rsqr, &Ysqr );
             * (void) bn_poly_mul( &T1, &Rsqr, &Qsqr );
             * (void) bn_poly_add( &sum, &T1, &T2 );
             * (void) bn_poly_sub( &C, &sum, &T3 );
             */
            cf[0] = Qsqr.getCoeff(0) * Xsqr.getCoeff(0) +
                    Rsqr.getCoeff(0) * Ysqr.getCoeff(0) -
                    (Rsqr.getCoeff(0) * Qsqr.getCoeff(0));
            cf[1] = Qsqr.getCoeff(0) * Xsqr.getCoeff(1) + Qsqr.getCoeff(1) * Xsqr.getCoeff(0) +
                    Rsqr.getCoeff(0) * Ysqr.getCoeff(1) + Rsqr.getCoeff(1) * Ysqr.getCoeff(0) -
                    (Rsqr.getCoeff(0) * Qsqr.getCoeff(1) + Rsqr.getCoeff(1) * Qsqr.getCoeff(0));
            cf[2] = Qsqr.getCoeff(0) * Xsqr.getCoeff(2) + Qsqr.getCoeff(1) * Xsqr.getCoeff(1) +
                    Qsqr.getCoeff(2) * Xsqr.getCoeff(0) +
                    Rsqr.getCoeff(0) * Ysqr.getCoeff(2) + Rsqr.getCoeff(1) * Ysqr.getCoeff(1) +
                    Rsqr.getCoeff(2) * Ysqr.getCoeff(0) -
                    (Rsqr.getCoeff(0) * Qsqr.getCoeff(2) + Rsqr.getCoeff(1) * Qsqr.getCoeff(1) +
                    Rsqr.getCoeff(2) * Qsqr.getCoeff(0));
            cf[3] = Qsqr.getCoeff(1) * Xsqr.getCoeff(2) + Qsqr.getCoeff(2) * Xsqr.getCoeff(1) +
                    Rsqr.getCoeff(1) * Ysqr.getCoeff(2) + Rsqr.getCoeff(2) * Ysqr.getCoeff(1) -
                    (Rsqr.getCoeff(1) * Qsqr.getCoeff(2) + Rsqr.getCoeff(2) * Qsqr.getCoeff(1));
            cf[4] = Qsqr.getCoeff(2) * Xsqr.getCoeff(2) +
                    Rsqr.getCoeff(2) * Ysqr.getCoeff(2) -
                    (Rsqr.getCoeff(2) * Qsqr.getCoeff(2));
            C = new Polynomial(4, cf);

            /*  The equation is 4th order, so we expect 0 to 4 roots */
            val = C.roots();

            /* bn_pr_roots("roots", val, nroots); */

            /*  Only real roots indicate an intersection in real space.
             *
             *  Look at each root returned; if the imaginary part is zero
             *  or sufficiently close, then use the real part as one value
             *  of 't' for the intersections
             */
            for (l = 0; l < val.length; l++) {
                if( val[l] == null ) {
                    continue;
                }
                if (Math.abs(val[l].getImaginary()) < 1e-2) {
                    hit_type[npts] = Tgc.BODY;
                    k[npts] = val[l].getReal();
                    npts++;
                }
            }
            /* bu_log("npts rooted is %d; ", npts); */

            /* Here, 'npts' is number of points being returned */
            if (npts != 0 && npts != 2 && npts != 4 && npts > 0) {
                StringBuilder str = new StringBuilder("tgc:  reduced " + val.length +
                        " to " + npts + " roots:\n");
                for (int j = 0; j < val.length; j++) {
                    str.append("\t" + val[j] + "\n");
                }
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, str.toString() );
            } else if (val.length < 0) {
                boolean reported = false;
                StringBuilder str = new StringBuilder("The root solver failed to converge on a solution for " +
                        this.name + "\n");
                if (!reported) {
                    str.append("while shooting from:\t" + ray.getStart());
                    str.append("while shooting at:\t" + ray.getDirection());
                    str.append("Additional truncated general cone convergence failure details will be suppressed.\n");
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, str.toString());
                    reported = true;
                }
            }
        }

        /*
         * Reverse above translation by adding distance to all 'k' values.
         */
        for (i = 0; i < npts; ++i) {
            k[i] += cor_proj;
        }

        /* bu_log("npts before elimination is %d; ", npts); */
        /*
         * Eliminate hits beyond the end planes
         */
        i = 0;
        while (i < npts) {
            zval = k[i] * dprime.getZ() + pprime.getZ();
            /* Height vector is unitized (tgc->tgc_sH == 1.0) */
            if (zval >= 1.0 || zval <= 0.0) {
                int j;
                /* drop this hit */
                npts--;
                for (j = i; j < npts; j++) {
                    hit_type[j] = hit_type[j + 1];
                    k[j] = k[j + 1];
                }
            } else {
                i++;
            }
        }

        /*
         * Consider intersections with the end ellipses
         */
        /* bu_log("npts before base is %d; ", npts); */
        dir = tgc_N.dotProduct(ray.getDirection());
        if (!(Math.abs(dprime.getZ()) < Constants.SMALL_FASTF) &&
                !(Math.abs(dir) < Constants.RT_DOT_TOL)) {
            b = (-pprime.getZ()) / dprime.getZ();
            /*  Height vector is unitized (tgc->tgc_sH == 1.0) */
            t = (1.0 - pprime.getZ()) / dprime.getZ();

            /* the top end */
            work = new Vector3(pprime);
            work.join(b, dprime);
            /* A and B vectors are unitized (tgc->tgc_A == _B == 1.0) */
            /* alf1 = ALPHA(work[X], work[Y], 1.0, 1.0 ) */
            alf1 = work.getX() * work.getX() + work.getY() * work.getY();

            /* the bottom end */
            work = new Vector3(pprime);
            work.join(t, dprime);
            /* Must scale C and D vectors */
            alf2 = this.alpha(work.getX(), work.getY(), tgc_AAdCC, tgc_BBdDD);

            /*
            bu_log("alf1 is %f, alf2 is %f\n", alf1, alf2);
            bu_log("work[x]=%f, work[y]=%f, aadcc=%f, bbddd=%f\n", work[X], work[Y], tgc->tgc_AAdCC, tgc->tgc_BBdDD);
             */
            if (alf1 <= 1.0) {
                hit_type[npts] = Tgc.BOTTOM;
                k[npts++] = b;
            }
            if (alf2 <= 1.0) {
                hit_type[npts] = Tgc.TOP;
                k[npts++] = t;
            }
        }

        /* bu_log("npts FINAL is %d\n", npts); */
        for( i=0 ; i<npts ; i++ ) {
            Point hit_pt = null;
            Vector3 norm = null;
            switch( hit_type[i] ) {
                case Tgc.BODY:
                    vpriv = new Vector3(pprime);
                    vpriv.join(k[i], dprime);
                    hit_pt = new Point(ray.getStart());
                    k[i] *= t_scale;
                    hit_pt.join(k[i], ray.getDirection());
                    norm = normal( vpriv );
                    break;
                case Tgc.TOP:
                    hit_pt = new Point(ray.getStart());
                    k[i] *= t_scale;
                    hit_pt.join(k[i], ray.getDirection());
                    norm = new Vector3(tgc_N);
                    break;
                case Tgc.BOTTOM:
                    hit_pt = new Point(ray.getStart());
                    k[i] *= t_scale;
                    hit_pt.join(k[i], ray.getDirection());
                    norm = new Vector3(tgc_N);
                    norm.negate();
                    break;
            }
            Hit hit = new Hit(k[i], hit_pt, norm, hit_type[i], rayData);
            hits.add(hit);
        }
        List<Segment> segs = this.makeSegs(hits, ray, rayData);

        return segs;
    }

    private Vector3 normal( Vector3 vpriv ) {

	    /* Compute normal, given hit point on standard (unit) cone */
	    double R = 1 + tgc_CdAm1 * vpriv.getZ();
	    double Q = 1 + tgc_DdBm1 * vpriv.getZ();
        Vector3 stdnorm = new Vector3();
	    stdnorm.setX( vpriv.getX() * Q * Q );
	    stdnorm.setY( vpriv.getY() * R * R );
	    stdnorm.setZ( (vpriv.getX()*vpriv.getX() - R*R) * Q * tgc_DdBm1 +
                (vpriv.getY()*vpriv.getY() - Q*Q)	* R * tgc_CdAm1);
        tgc_invRtShSc.mult(stdnorm);
	    /*XXX - save scale */
        stdnorm.normalize();
	    return stdnorm;
    }

    @Override
    public List<Segment> makeSegs(Set<Hit> hitSet, Ray ray, RayData rayData) {
        List<Segment> segs = new ArrayList<Segment>();

        /* Sort Most distant to least distant: rt_pt_sort( k, npts ) */
        List<Hit> hits = new ArrayList(hitSet);
        Collections.sort(hits);

        if (hits.size() % 2 != 0) {
            /* odd number of hits!!!
             * perhaps we got two hits on an edge
             * check for duplicate hit distances
             */

            for (int i = hits.size() - 1; i > 0; i--) {
                double diff;

                diff = hits.get(i - 1).getHit_dist() - hits.get(i).getHit_dist();	/* non-negative due to sorting */
                if (diff < rayData.getTolerance().getDist()) {
                    /* remove this duplicate hit */
                    hits.remove(i);

                    /* now have even number of hits */
                    break;
                }
            }
        }

        if (hits.size() != 0 && hits.size() != 2 && hits.size() != 4) {
            StringBuilder str = new StringBuilder("tgc(" + this.name + "):  " +
                    hits.size() + " intersects != {0, 2, 4}\n");
            str.append("\tray: pt = " + ray);
            for (int i = 0; i < hits.size(); i++) {
                str.append("\t" + hits.get(i) + "\n");
            }
            return segs;			/* No hit */
        }

        for (int i = 0; i < hits.size(); i += 2) {
            Segment seg = new Segment(hits.get(i), hits.get(i+1));
            segs.add(seg);
        }

        return segs;
    }

    private double alpha( double x, double y, double c, double d ) {
        return( (x)*(x)*(c) + (y)*(y)*(d) );
    }

    private double reldiff(double a, double b) {
        double d;
        double diff;

        /* d = Max(Abs(a), Abs(b)) */
        d = (a >= 0.0) ? a : -a;
        if (b >= 0.0) {
            if (b > d) {
                d = b;
            }
        } else {
            if ((-b) > d) {
                d = (-b);
            }
        }
        if (d == 0.0) {
            return (0.0);
        }
        if ((diff = a - b) < 0.0) {
            diff = -diff;
        }
        return (diff / d);
    }
    
    private void rt_tgc_rotate(Vector3 A, Vector3 B, Vector3 Hv, Matrix Rot, Matrix Inv, Vector3 tgc_N)
{
    Vector3	uA, uB, uC;	/*  unit vectors		*/
    double	mag_ha;		/*  magnitude of H in the	*/
	double  mag_hb;		/*    A and B directions	*/

    /* copy A and B, then 'unitize' the results			*/
    uA = new Vector3(A);
    uA.normalize();
    uB = new Vector3(B);
    uB.normalize();

    /*  Find component of H in the A direction			*/
    mag_ha = Hv.dotProduct(uA);
    /*  Find component of H in the B direction			*/
    mag_hb = Hv.dotProduct(uB);

    /*  Subtract the A and B components of H to find the component
     *  perpendicular to both, then 'unitize' the result.
     */
    uC = new Vector3(Hv);
    uC.join( -mag_ha, uA );
    uC.join(-mag_hb, uB);
    uC.normalize();
    for( int i=0 ; i<3 ; i++ ) {
        tgc_N.set(i, uC.get(i));
    }

    Rot.unit();
    Inv.unit();

    for( int i=0 ; i<3 ; i++ ) {
        Rot.set(0, i, uA.get(i));
        Rot.set(1, i, uB.get(i));
        Rot.set(2, i, uC.get(i));
        Inv.set(i, 0, uA.get(i));
        Inv.set(i, 1, uB.get(i));
        Inv.set(i, 2, uC.get(i));
    }
}

/**
 *			T G C _ S H E A R
 *
 *  To shear the H vector to the Z axis, every point must be shifted
 *  in the X direction by  -(Hx/Hz)*z, and in the Y direction by
 *  -(Hy/Hz)*z .  This operation makes the equation for the standard
 *  cone much easier to work with.
 *
 *  NOTE:  This computes the TRANSPOSE of the shear matrix rather than
 *  the inverse.
 *
 * Begin changes GSM, EOD -- Added INVERSE (Inv) calculation.
 */
private void tgc_shear(Vector3 vect, int axis, Matrix Shr, Matrix Trn, Matrix Inv)
{
    Shr.unit();
    Trn.unit();
    Inv.unit();

    if ( Math.abs( vect.get(axis))  < Constants.SMALL_FASTF ) {
        throw new ArithmeticException("tgc_shear() divide by zero\n");
    }

    double val;
    if ( axis == 0 ) { // X axis
        val = -vect.getY() / vect.getX();
        Inv.set(1, 0, -val);
        Shr.set(1, 0, val);
        Trn.set(0, 1, val);
        val = -vect.getZ() / vect.getX();
        Inv.set(2, 0, -val);
        Shr.set(2, 0, val);
        Trn.set(0, 2, val);
    } else if ( axis == 1 ) { //Y axis
        val = -vect.getX() / vect.getY();
        Inv.set(0, 1, -val);
        Shr.set(0, 1, val);
        Trn.set(1, 0, val);
        val = -vect.getZ() / vect.getY();
        Inv.set(2, 1, -val);
        Shr.set(2, 1, val);
        Trn.set(1, 2, val);
    } else if ( axis == 2 ) { //Z axis
        val = -vect.getX() / vect.getZ();
        Inv.set(0, 2, -val);
        Shr.set(0, 2, val);
        Trn.set(2, 0, val);
        val = -vect.getY() / vect.getZ();
        Inv.set(1, 2, -val);
        Shr.set(1, 2, val);
        Trn.set(2, 1, val);
    }
}

/**
 *			R T _ T G C _ S C A L E
 */
private void tgc_scale(double a, double b, double h, Matrix Scl, Matrix Inv) throws IllegalArgumentException
{
    Scl.unit();
    Inv.unit();
    Scl.set(0, 0, Scl.get(0, 0)/a);
    Scl.set(1, 1, Scl.get(1, 1)/b);
    Scl.set(2, 2, Scl.get(2, 2)/h);
    Inv.set(0, 0, a);
    Inv.set(1, 1, b);
    Inv.set(2, 2, h);
    return;
}

}
