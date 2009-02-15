/*
 * Represents a polynomial and operations that can be performed on it.
 * Based on BRL-CAD libbn/poly.c
 */

package org.brlcad.numerics;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jra
 */
public class Polynomial {
    /**
     * original degree
     */
    private int degree;

    /**
     * original coefficients
     */
    private double[] coeff;

    /**
     * copy of degree (for destructive algorithm)
     */
    private int dgr;

    /**
     * copy of coefficients (for destructive algorithm)
     */
    private double c[];

    public Polynomial() {
        this.degree = 0;
        this.coeff = new double[1];
        this.coeff[0] = 0.0;
        this.copyData();
    }

    public Polynomial( int degree ) {
        this.degree = degree;
        this.coeff = new double[degree+1];
        for( int i=0 ; i<=degree ; i++ ) {
            this.coeff[i] = 0.0;
        }
        this.copyData();
    }

    public Polynomial( int degree, double[] coeffs ) throws IllegalArgumentException {
        if( coeffs.length < (degree + 1) ) {
            throw new IllegalArgumentException( "Polynomial of degree " + degree +
                    " must have " + (degree+1) + " coefficients, but " + coeffs.length +
                    " were specified" );
        } else if( coeffs.length > (degree + 1) ) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
                    "Polynomial of degree " + degree + " was provided too many coefficients (" +
                    coeffs.length + "), ignoring excess" );
        }
        this.degree = degree;
        this.coeff = coeffs;
        this.copyData();
    }

    public Polynomial( Polynomial p ) {
        this.degree = p.degree;
        this.coeff = new double[p.coeff.length];
        for( int i=0 ; i<p.coeff.length ; i++ ) {
            this.coeff[i] = p.coeff[i];
        }
        this.copyData();
    }

    public void setCoeff( int index, double value ) {
        this.coeff[index] = value;
        this.c[index] = value;
    }

    public int getDegree() {
        return this.dgr;
    }

    public double getCoeff( int index ) {
        return this.c[index];
    }

    public Complex[] roots() throws IllegalArgumentException {
        Complex[] roots;

        /* Remove leading coefficients which are too close to zero,
         * to prevent the polynomial factoring from blowing up, below.
         */
        this.eliminateLeadingZeroes();
        if (this.dgr == 0) {
            roots = new Complex[1];
            roots[0] = new Complex(-this.c[0], 0.0);
            return roots;
        }

        /* Factor the polynomial so the first coefficient is one
         * for ease of handling.
         */
        double factor = 1.0 / this.c[0];
        scale( factor );

        /* A trailing coefficient of zero indicates that zero
         * is a root of the equation.
         */
        roots = new Complex[this.degree];
        int rootsFound = 0;
        while (Math.abs(this.c[this.dgr]) < Constants.SQRT_SMALL_FASTF) {
            roots[rootsFound] = new Complex(0.0, 0.0);
            rootsFound++;
            this.dgr--;
        }

        while( this.dgr > 0 ) {
            Complex[] newRoots = null;
            switch( this.dgr ) {
                case 1:
                    roots[rootsFound] = new Complex( -this.c[1], 0.0 );
                    rootsFound++;
                    return roots;
                case 2:
                    newRoots = quadraticRoots();
                    if( newRoots != null ) {
                        for( int i=0 ; i<newRoots.length ; i++ ) {
                            roots[rootsFound] = newRoots[i];
                            rootsFound++;
                        }
                    }
                    return roots;
                case 3:
                    newRoots = cubicRoots();
                    if( newRoots != null ) {
                        for( int i=0 ; i<newRoots.length ; i++ ) {
                            roots[rootsFound] = newRoots[i];
                            rootsFound++;
                        }
                        if( checkRoots(roots) ) {
                            return roots;
                        }
                    }
                    break;
                case 4:
                    newRoots = quarticRoots();
                    if( newRoots != null ) {
                        for( int i=0 ; i<newRoots.length ; i++ ) {
                            roots[rootsFound] = newRoots[i];
                            rootsFound++;
                        }
                        if( checkRoots(roots) ) {
                            return roots;
                        }
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Cannot solve polynomials with degree greater than 4, " +
                            "this polynomial has degree " + this.dgr + ":\n" +
                            this.toString() );
            }

            /*
             *  Set initial guess for root to almost zero.
             *  This method requires a small nudge off the real axis.
             */
            roots[rootsFound] = new Complex(0.0, Constants.SQRT_SMALL_FASTF);
            boolean found = findRoot(roots[rootsFound]);
            if( !found ) {
                roots[rootsFound] = null;
                return roots;
            }

            if (Math.abs(roots[rootsFound].getImaginary()) > 1.0e-5 * Math.abs(roots[rootsFound].getReal())) {
                /* If root is complex, its complex conjugate is
                 * also a root since complex roots come in con-
                 * jugate pairs when all coefficients are real.
                 */
                rootsFound++;
                roots[rootsFound] = Complex.conjugate(roots[rootsFound - 1]);
            } else {
                /* Change 'practically real' to real		*/
                roots[rootsFound] = new Complex( roots[rootsFound].getReal(), 0.0 );
            }
            this.deflate(roots[rootsFound]);
            rootsFound++;
        }

        return roots;
    }

    private boolean checkRoots( Complex[] roots ) {
        for (int m = 0; m < roots.length; ++m) {
            /* Select value of Z to evaluate at */
            double zr = roots[m].getReal();
            double zi = roots[m].getImaginary();

            /* Initialize */
            double er = this.c[0];
            /* ei = 0.0; */

            /* n=1 step.  Broken out because ei = 0.0 */
            double ei = er * zi;		/* must come before er= */
            er = er * zr + this.c[1];

            /* Remaining steps */
            for (int n = 2; n <= this.dgr; ++n) {
                double tr = er * zr - ei * zi + this.c[n];
                double ti = er * zi + ei * zr;
                er = tr;
                ei = ti;
            }
            if (Math.abs(er) > 1.0e-5 || Math.abs(ei) > 1.0e-5) {
                return false;	/* FAIL */
            }
        }
        /* Evaluating polynomial for all Z values gives zero */
        return true;			/* OK */
    }

    private void copyData() {
        this.dgr = this.degree;
        this.c = new double[this.coeff.length];
        for( int i=0 ; i<this.coeff.length ; i++ ) {
            this.c[i] = this.coeff[i];
        }
    }

    private void copyPolynomial(Polynomial quotient) {
        this.degree = quotient.degree;
        this.coeff = new double[quotient.coeff.length];
        for( int i=0 ; i<this.coeff.length ; i++ ) {
            this.coeff[i] = quotient.coeff[i];
        }
        copyData();
    }

    private Complex[] cubicRoots() {
        Complex[] roots = null;

        double c1 = this.c[1];
        if (Math.abs(c1) > Constants.SQRT_MAX_FASTF) {
            return null;	/* FAIL */
        }

        double c1_3rd = c1 * Constants.THIRD;
        double a = this.c[2] - c1 * c1_3rd;
        if (Math.abs(a) > Constants.SQRT_MAX_FASTF) {
            return null;	/* FAIL */
        }
        double b = (2.0 * c1 * c1 * c1 - 9.0 * c1 * this.c[2] +
                27.0 * this.c[3]) * Constants.INV_TWENTYSEVEN;
        if (Math.abs(b) > Constants.SQRT_MAX_FASTF) {
            return null;	/* FAIL */
        }

        double delta = a * a;
        if (delta > Constants.SQRT_MAX_FASTF) {
            return null;	/* FAIL */
        }
        delta = b * b * 0.25 + delta * a * Constants.INV_TWENTYSEVEN;

        if (delta > 0.0) {
            double r_delta = Math.sqrt(delta);
            double B = -0.5 * b;
            double A = B + r_delta;
            B -= r_delta;

            A = Math.cbrt(A);
            B = Math.cbrt(B);

            roots = new Complex[3];
            roots[0] = new Complex( A+B, 0.0 );
            roots[1] = new Complex( -0.5*roots[0].getReal(), (A-B)*Constants.SQRT3*0.5);
            roots[2] = new Complex( roots[1].getReal(), -( roots[1].getImaginary()));
        } else if (delta == 0.0) {
            double b_2 = -0.5 * b;
            roots = new Complex[3];
            roots[0] = new Complex( 2.0*Math.cbrt(b_2), 0.0 );
            roots[1] = new Complex( -0.5*roots[0].getReal(), 0.0);
            roots[2] = new Complex( roots[1].getReal(), 0.0);
        } else {
            double phi, fact;
            double cs_phi, sn_phi_s3;

            if (a >= 0.0) {
                fact = 0.0;
                phi = 0.0;
                cs_phi = 1.0;		/* cos( phi ); */
                sn_phi_s3 = 0.0;	/* sin( phi ) * SQRT3; */
            } else {
                double f;
                a *= -Constants.THIRD;
                fact = Math.sqrt(a);
                if ((f = b * (-0.5) / (a * fact)) >= 1.0) {
                    phi = 0.0;
                    cs_phi = 1.0;		/* cos( phi ); */
                    sn_phi_s3 = 0.0;	/* sin( phi ) * SQRT3; */
                } else if (f <= -1.0) {
                    phi = Constants.PI_DIV_3;
                    cs_phi = Math.cos(phi);
                    sn_phi_s3 = Math.sin(phi) * Constants.SQRT3;
                } else {
                    phi = Math.acos(f) * Constants.THIRD;
                    cs_phi = Math.cos(phi);
                    sn_phi_s3 = Math.sin(phi) * Constants.SQRT3;
                }
            }

            roots = new Complex[3];
            double re = 2.0*fact*cs_phi;
            roots[0] = new Complex(re, 0.0);
            re = fact*(  sn_phi_s3 - cs_phi);
            roots[1] = new Complex(re, 0.0);
            re = fact*( -sn_phi_s3 - cs_phi);
            roots[2] = new Complex(re, 0.0);
        }
        for( int i=0 ; i<3 ; i++ ) {
            roots[i] = roots[i].subtract(c1_3rd);
        }

    return roots;		/* OK */
    }

    private Complex[] quarticRoots() {
        double small = 1.0e-8;
        Complex[] roots = null;
        Polynomial cube = new Polynomial(3);
        cube.setCoeff(0, 1.0);
        cube.setCoeff(1, -this.c[2]);
        cube.setCoeff(2, this.c[3]*this.c[1]- 4.0*this.c[4]);
        cube.setCoeff(3, -this.c[3]*this.c[3] - this.c[4]*this.c[1]*this.c[1] + 4.0*this.c[4]*this.c[2] );

        Complex[] u = cube.cubicRoots();
        if( u == null ) {
            return null;
        }
        
        double U;
        if (u[1].getImaginary() != 0.0) {
            U = u[0].getReal();
        } else {
            U = Math.max(u[0].getReal(), Math.max(u[1].getReal(), u[2].getReal()));
        }

        double p = this.c[1] * this.c[1] * 0.25 + U - this.c[2];
        U *= 0.5;
        double q = U * U - this.c[4];

        if (p < 0) {
            if (p < -small) {
                return null;	/* FAIL */
            }
            p = 0;
        } else {
            p = Math.sqrt(p);
        }
        if (q < 0) {
            if (q < -small) {
                return null;	/* FAIL */
            }
            q = 0;
        } else {
            q = Math.sqrt(q);
        }
        
        Polynomial quad1 = new Polynomial(2);
        Polynomial quad2 = new Polynomial(2);
        quad1.setCoeff(0, 1.0);
        quad2.setCoeff(0, 1.0);
        quad1.setCoeff(1, this.c[1]*0.5 - p);
        quad2.setCoeff(1, this.c[1]*0.5 + p);


        double q1 = U - q;
        double q2 = U + q;

        p = quad1.getCoeff(1) * q2 + quad2.getCoeff(1) * q1 - this.c[3];
        if (Math.abs(p) < small) {
            quad1.setCoeff(2, q1);
            quad2.setCoeff(2, q2);
        } else {
            q = quad1.getCoeff(1) * q1 + quad2.getCoeff(1) * q2 - this.c[3];
            if (Math.abs(q) < small) {
                quad1.setCoeff(2, q2);
                quad2.setCoeff(2, q1);
            } else {
                return null;	/* FAIL */
            }
        }

        Complex[] qroots1 = quad1.quadraticRoots();
        Complex[] qroots2 = quad2.quadraticRoots();
        roots = new Complex[4];
        roots[0] = qroots1[0];
        roots[1] = qroots1[1];
        roots[2] = qroots2[0];
        roots[3] = qroots2[1];

        return roots;		/* SUCCESS */
    }

    private void eliminateLeadingZeroes() {
        /* Remove leading coefficients which are too close to zero,
         * to prevent the polynomial factoring from blowing up, below.
         */
        while( this.dgr > 0 && Math.abs(this.c[0]) < Constants.SQRT_SMALL_FASTF ) {
            double[] oldCoeffs = this.c;
            this.c = new double[this.dgr];
            for (int n = 0; n < this.dgr; n++) {
                this.c[n] = oldCoeffs[n + 1];
            }
            this.dgr--;
        }

    }

    private Complex[] quadraticRoots() {
        Complex[] roots = null;
        if( Math.abs(this.c[0]) < Constants.SQRT_SMALL_FASTF ) {
            if( Math.abs(this.c[1]) < Constants.SQRT_SMALL_FASTF ) {
                return roots;
            }
            roots = new Complex[2];
            roots[0] = new Complex(-this.c[2]/this.c[1], 0.0);
            roots[1] = new Complex(roots[0]);
            return roots;
        }

        roots = new Complex[2];
        double discrim = this.c[1] * this.c[1] - 4.0 * this.c[0] * this.c[2];
        double denom = 0.5 / this.c[0];
        if( discrim > 0.0 ) {
            double rad = Math.sqrt(discrim);

            if (Math.abs(this.c[1]) < Constants.SQRT_SMALL_FASTF) {
                double r = Math.abs(rad * denom);
                roots[0] = new Complex(r,0.0);
                roots[1] = new Complex(-r, 0.0);
            } else {
                double t, r1, r2;

                if (this.c[1] > 0.0) {
                    t = -0.5 * (this.c[1] + rad);
                } else {
                    t = -0.5 * (this.c[1] - rad);
                }
                r1 = t / this.c[0];
                r2 = this.c[2] / t;

                if (r1 < r2) {
                    roots[0] = new Complex(r1,0.0);
                    roots[1] = new Complex(r2,0.0);
                } else {
                    roots[0] = new Complex(r2,0.0);
                    roots[1] = new Complex(r1,0.0);
                }
            }
        } else if (Math.abs(discrim) < Constants.SQRT_SMALL_FASTF) {
            roots[0] = new Complex( -this.c[1] * denom, 0.0);
            roots[1] = new Complex( -this.c[1] * denom, 0.0);
        } else {
            roots[0] = new Complex( -this.c[1] * denom, Math.sqrt(-discrim) * denom);
            roots[1] = new Complex( -this.c[1] * denom, -roots[0].getImaginary());
        }

        return roots;
    }

    private Complex[] evalWith2Deriviative(Complex cZ) {
        Complex[] results = new Complex[3]; // 0->evaluation, 1-> 1st deriviative, 2->2nd deriviative
        results[0] = new Complex( this.c[0], 0.0 );
        results[1] = new Complex(results[0]);
        results[2] = new Complex(results[0]);
        int m = 0;
        for (int n = 1; (m = this.dgr - n) >= 0; ++n) {
            results[0] = results[0].multiply(cZ).add(this.c[n]);
            if (m > 0) {
                results[1] = results[1].multiply(cZ).add(results[0]);
            }
            if (m > 1) {
                results[2] = results[2].multiply(cZ).add(results[1]);
            }
        }
        return results;
    }

    private boolean findRoot(Complex nxZ) {
        Complex[] p = null;
        Complex cH = null;
        Complex T = null;
        for (int i = 0; i < 20; i++) {
            Complex cZ = new Complex(nxZ);
            p = evalWith2Deriviative(cZ);

            /* Compute H for Laguerre's method. */
            int n = this.dgr - 1;
            cH = Complex.multiply(p[1], p[1]).multiply(n*n);
            T = Complex.multiply(p[2], p[0]).multiply(this.dgr + n);
            cH = cH.subtract(T);

            /* Calculate the next iteration for Laguerre's method.
             * Test to see whether addition or subtraction gives the
             * larger denominator for the next 'Z', and use the
             * appropriate value in the formula.
             */
            cH = cH.sqrt();
            Complex p1_H = Complex.subtract(p[1], cH);
            p[1] = p[1].add(cH);		/* p1 <== p1+H */
            p[0] = p[0].multiply(this.dgr);
            if( p1_H.amplitude() > p[1].amplitude() ) {
                p[0] = p[0].divide(p1_H);
                nxZ = nxZ.subtract(p[0]);
            } else {
                p[0] = p[0].divide(p[1]);
                nxZ = nxZ.subtract(p[0]);
            }

            /* Use proportional convergence test to allow very small
             * roots and avoid wasting time on large roots.
             * The original version used bn_cx_ampl(), which requires
             * a square root.  Using bn_cx_amplsq() saves lots of cycles,
             * but changes loop termination conditions somewhat.
             *
             * diff is |p0|**2.  nxZ = Z - p0.
             *
             * SGI XNS IRIS 3.5 compiler fails if following 2 assignments
             * are imbedded in the IF statement, as before.
             */
            double b = nxZ.amplitudeSquared();
            double diff = p[0].amplitudeSquared();
            if (b < diff)
                continue;
            if ((b - diff) == b)
                return true;		/* OK -- can't do better */
            if (diff > (b - diff) * 1.0e-5)
                continue;
            return (true);			/* OK */
        }

        /* If the thing hasn't converged yet, it probably won't. */
//        bu_log("rt_poly_findroot: solving for %s didn't converge in %d iterations, b=%g, diff=%g\n",
//                str, i, b, diff);
//        bu_log("nxZ=%gR+%gI, p0=%gR+%gI\n", nxZ ->  re, nxZ ->  im, p0.re, p0.im);
        return (false);		/* ERROR */
    }

    private void scale(double factor) {
        for (int cnt = 0; cnt <= this.dgr; ++cnt) {
            this.c[cnt] *= factor;
        }
    }

    private void deflate( Complex root ) {
        Polynomial div = null;

        /* Make a polynomial out of the given root:  Linear for a real
         * root, Quadratic for a complex root (since they come in con-
         * jugate pairs).
         */
        if (Math.abs(root.getImaginary()) < Constants.SQRT_SMALL_FASTF) {
            /*  root is real		*/
            div = new Polynomial(1);
            div.setCoeff(0, 1.0);
            div.setCoeff(1, -root.getReal());
        } else {
            /*  root is complex		*/
            div = new Polynomial(2);
            div.setCoeff(0, 1.0);
            div.setCoeff(1, -2.0 * root.getReal());
            div.setCoeff(2, root.amplitude());
        }

        /* Use synthetic division to find the quotient (new polynomial)
         * and the remainder (should be zero if the root was really a
         * root).
         */
        Polynomial remainder = syntheticDivision( div );

    }

    /**
     * Divide this Polynomial by another Polynomial
     * 
     * @param divisor The divisor
     * @return the remainder
     */
    public Polynomial syntheticDivision(Polynomial divisor) {
        Polynomial remainder = new Polynomial(this);
        Polynomial quotient = new Polynomial(this.dgr - divisor.dgr);

        for (int i = 0; i <= quotient.dgr; i++) {
            quotient.setCoeff(i, remainder.c[i] / divisor.coeff[0] );
            for( int j=i ; j<=divisor.dgr+i ; j++ ) {
                remainder.setCoeff(j, remainder.c[j] - divisor.c[j-i] * quotient.c[i]);
            }
        }

        copyPolynomial(quotient);

        return remainder;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        int zeros = 0;

        for( int i=0 ; i<=this.dgr ; i++ ) {
            int exp = this.dgr - i;
            if( this.c[i] == 0.0 ) {
                zeros++;
                continue;
            }
            if( this.c[i] != 1.0 ) {
                if( this.c[i] < 0.0 ) {
                    str.append("-" + Math.abs(this.c[i]));
                } else {
                    str.append("+" + Math.abs(this.c[i]));
                }
            }
            if (exp > 1) {
                str.append("X^" + exp);
            } else if( exp == 1 ) {
                str.append("X");
            }
        }

        if( zeros == this.dgr+1 ) {
            str.append("0");
        }

        return str.toString();
    }
}
