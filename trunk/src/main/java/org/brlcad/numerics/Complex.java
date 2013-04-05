/*
 * Representation of a complex number
 */

package org.brlcad.numerics;

/**
 *
 * @author jra
 */
public class Complex {

    private double real;
    private double imaginary;

    public Complex( double real, double imaginary ) {
        this.real = real;
        this.imaginary = imaginary;
    }

    public Complex( Complex c ) {
        this.real = c.real;
        this.imaginary = c.imaginary;
    }

    /**
     * @return the real
     */
    public double getReal() {
        return real;
    }

    /**
     * @return the imaginary
     */
    public double getImaginary() {
        return imaginary;
    }

    public Complex multiply( Complex c ) {
        return new Complex( this.real * c.real - this.imaginary * c.imaginary,
                this.real * c.imaginary + this.imaginary * c.real);
    }

    public static Complex multiply( Complex c1, Complex c2 ) {
        return new Complex( c1.real * c2.real - c1.imaginary * c2.imaginary,
                c1.real * c2.imaginary + c1.imaginary * c2.real);
    }

    public Complex multiply( double s ) {
        return new Complex( this.real * s, this.imaginary * s );
    }

    public Complex add( Complex c ) {
        return new Complex( this.real + c.real, this.imaginary + c.imaginary);
    }

    public Complex add( double d ) {
        return new Complex( this.real + d, this.imaginary);
    }

    public Complex subtract( Complex c ) {
        return new Complex( this.real - c.real, this.imaginary - c.imaginary );
    }

    public Complex subtract( double d ) {
        return new Complex( this.real - d, this.imaginary );
    }

    public Complex divide( Complex bp ) {
        /* Note: classical formula may cause unnecessary overflow */
        double r = bp.real;
        double s = bp.imaginary;
        if (Math.abs(r) >= Math.abs(s)) {
            r = s / r;			/* <= 1 */
            s = 1.0 / (bp.real + r * s);
            return new Complex( (this.real + this.imaginary * r) * s,
                    (this.imaginary - this.real * r) * s);
        } else {
            /* ABS( s ) > ABS( r ) */
            r = r / s;			/* < 1 */
            s = 1.0 / (s + r * bp.real);
            return new Complex( (this.real * r + this.imaginary) * s,
                    (this.imaginary * r - this.real) * s);
        }
    }

    public static Complex conjugate(Complex c) {
        return new Complex( c.real, -c.imaginary );
    }

    public double amplitudeSquared() {
        return (this.real * this.real + this.imaginary * this.imaginary);
    }

    public static Complex subtract( Complex c1, Complex c2 ) {
        return new Complex( c1.real - c2.real, c1.imaginary - c2.imaginary );
    }
    
    private int SIGN( double x ) {
        return ((x) == 0 ? 0 : (x) > 0 ? 1 : -1);
    }

    public Complex sqrt() {
        /* special cases are not necessary; they are here for speed */
        int im_sign = SIGN(this.imaginary);
        int re_sign = SIGN(this.real);
        if (re_sign == 0) {
            if (im_sign == 0) {
                return new Complex( 0.0, 0.0 );
            } else if ( im_sign > 0 ) {
                double val = Math.sqrt(this.imaginary * 0.5);
                return new Complex( val, val );
            } else {			/* im_sign < 0 */
                double val = Math.sqrt(this.imaginary * (-0.5));
                return new Complex( -val, val );
            }
        } else if (im_sign == 0) {
            if (re_sign > 0) {
                return new Complex( Math.sqrt(this.real), 0.0 );
            } else {           /* re_sign < 0 */
                return new Complex( 0.0, Math.sqrt(-this.real));
            }
        } else {
            /* no shortcuts */
            double ampl = this.amplitude();
            double temp = (ampl - this.real) * 0.5;
            double im;
            double re;
            if (temp < 0.0) {
                /* This case happens rather often, when the
                 *  hypot() in bn_cx_ampl() returns an ampl ever so
                 *  slightly smaller than ip->re.  This can easily
                 *  happen when ip->re ~= 10**20.
                 *  Just ignore the imaginary part.
                 */
                im = 0;
            } else {
                im = Math.sqrt(temp);
            }

            if ((temp = (ampl + this.real) * 0.5) < 0.0) {
                re = 0.0;
            } else {
                if (im_sign > 0) {
                    re = Math.sqrt(temp);
                } else	{		/* im_sign < 0 */
                    re = -Math.sqrt(temp);
                }
            }
            return new Complex( re, im );
        }
    }

    public double amplitude() {
        return Math.hypot(real, imaginary);
    }

    public boolean approxEquals( Complex c, double tolerance ) {
        if( Math.abs(this.real - c.real) < tolerance && Math.abs(imaginary - c.imaginary) < tolerance) {
            return true;
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if( o instanceof Double ) {
            Double d = (Double) o;
            if( this.imaginary != 0.0 ) {
                return false;
            }
            if( this.real != d ) {
                return false;
            }
            return true;
        }

        if( o instanceof Complex ) {
            Complex c = (Complex) o;
            if( this.imaginary == c.imaginary && this.real == c.real ) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (int) (Double.doubleToLongBits(this.real) ^ (Double.doubleToLongBits(this.real) >>> 32));
        hash = 83 * hash + (int) (Double.doubleToLongBits(this.imaginary) ^ (Double.doubleToLongBits(this.imaginary) >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        if( this.real == 0.0 && this.imaginary == 0.0 ) {
            return "0";
        }
        
        StringBuilder str = new StringBuilder();

        if( this.real != 0.0 ) {
            str.append(this.real);
        }
        if( this.imaginary > 0.0 ) {
            str.append("+" + this.imaginary + "i");
        } else if( this.imaginary < 0.0 ) {
            str.append(this.imaginary + "i");
        }

        return str.toString();
    }

    /**
     * @param real the real to set
     */
    public void setReal(double real) {
        this.real = real;
    }

    /**
     * @param imaginary the imaginary to set
     */
    public void setImaginary(double imaginary) {
        this.imaginary = imaginary;
    }
}
