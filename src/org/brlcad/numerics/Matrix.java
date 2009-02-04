/*
 * Copyright (C) 2001 De Montfort University, All Rights Reserved. De Montfort
 * University grants to you ("Licensee") a non-exclusive, non-transferable,
 * royalty free, license to use, copy, and modify this software and its
 * documentation. Licensee may redistribute the software in source and binary
 * code form provided that this copyright notice appears in all copies. DE
 * MONTFORT UNIVERSITY MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. THE AUTHORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

package org.brlcad.numerics;

import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Angle;
import javax.measure.unit.SI;
import java.io.Serializable;

/**
 * Two dimensional Matrix (of 'double')
 *
 * @author Paul Cropper <p/> Acquired from
 *         http://www.iesd.dmu.ac.uk/~pcc/dls/docs/intro.htm
 * @version 1.0 March 2001
 */
public class Matrix implements Serializable {

    static final long serialVersionUID = -6454670196025290674L;

    private double mat[][]; // matrix defined as 2D Java array

    private int rows; // number of rows in matrix

    private int columns; // number of columns in matrix

    /**
     * Create new matrix r rows by c columns, all values set to 0.0.
     *
     * @param r Number of rows
     * @param c Number of columns
     * @throws IllegalArgumentException if r <= 0 or c <= 0
     */
    public Matrix(int r, int c) {
        if (r <= 0 || c <= 0) {
            throw new IllegalArgumentException(
                    "Cannot construct Matrix of size (" + r + "," + c + ")");
        }

        rows = r;
        columns = c;

        mat = new double[rows][columns];

        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                mat[i][j] = 0.0;
            }
        }
    }

    /**
     * Copy constructor. Create new Matrix from an existing Matrix.
     *
     * @param m Matrix to copy
     * @throws IllegalArgumentException if m is null or not valid
     */
    public Matrix(Matrix m) {
        if (m == null || !Matrix.isValidMatrix(m)) {
            throw new IllegalArgumentException(
                    "Cannot construct new Matrix from null or invalid Matrix");
        }

        this.rows = m.rows;
        this.columns = m.columns;
        this.mat = new double[rows][columns];
        for (int row = 0; row < rows; row++) {
            System.arraycopy(m.mat[row], 0, this.mat[row], 0, columns);
        }
    }

    /**
     * Constructor for a 4x4 matrix with each element specified
     *
     * @param a m[0][0]
     * @param b m[0][1]
     * @param c m[0][2]
     * @param d m[0][3]
     * @param e m[1][0]
     * @param f m[1][1]
     * @param g m[1][2]
     * @param h m[1][3]
     * @param i m[2][0]
     * @param j m[2][1]
     * @param k m[2][2]
     * @param l m[2][3]
     * @param m m[3][0]
     * @param n m[3][1]
     * @param o m[3][2]
     * @param p m[3][3]
     * @throws IllegalArgumentException if any input is Infinite or NaN
     */
    public Matrix(double a, double b, double c, double d, double e, double f,
                  double g, double h, double i, double j, double k, double l,
                  double m, double n, double o, double p) {
        if (Double.isInfinite(a) || Double.isInfinite(b)
                || Double.isInfinite(c) || Double.isInfinite(d)
                || Double.isInfinite(e) || Double.isInfinite(f)
                || Double.isInfinite(g) || Double.isInfinite(h)
                || Double.isInfinite(i) || Double.isInfinite(j)
                || Double.isInfinite(k) || Double.isInfinite(l)
                || Double.isInfinite(m) || Double.isInfinite(n)
                || Double.isInfinite(o) || Double.isInfinite(p)
                || Double.isNaN(a) || Double.isNaN(b) || Double.isNaN(c)
                || Double.isNaN(d) || Double.isNaN(e) || Double.isNaN(f)
                || Double.isNaN(g) || Double.isNaN(h) || Double.isNaN(i)
                || Double.isNaN(j) || Double.isNaN(k) || Double.isNaN(l)
                || Double.isNaN(m) || Double.isNaN(n) || Double.isNaN(o)
                || Double.isNaN(p)) {
            throw new IllegalArgumentException(
                    "Cannot construct new Matrix if a value is Infinite or NaN");
        }

        this.rows = 4;
        this.columns = 4;
        this.mat = new double[4][4];

        mat[0][0] = a;
        mat[0][1] = b;
        mat[0][2] = c;
        mat[0][3] = d;
        mat[1][0] = e;
        mat[1][1] = f;
        mat[1][2] = g;
        mat[1][3] = h;
        mat[2][0] = i;
        mat[2][1] = j;
        mat[2][2] = k;
        mat[2][3] = l;
        mat[3][0] = m;
        mat[3][1] = n;
        mat[3][2] = o;
        mat[3][3] = p;
    }

    /**
     * Creat a 4x4 Matrix based on direction vectors and a scale factor
     *
     * @param col0  x-direction vector
     * @param col1  y-direction vector
     * @param col2  z-direction vector
     * @param col3  translation vector
     * @param scale global scale factor
     * @throws IllegalArgumentException if any input vector is null or not
     *                                  valid or if the scale factor is Infinite or NaN
     */
    public Matrix(Vector3 col0, Vector3 col1, Vector3 col2, Vector3 col3,
                  double scale) {
        if (col0 == null || !Vector3.isValidVector(col0)) {
            throw new IllegalArgumentException(
                    "Cannot construct new Matrix from null or invalid Vector3 col0");
        }
        if (col1 == null || !Vector3.isValidVector(col1)) {
            throw new IllegalArgumentException(
                    "Cannot construct new Matrix from null or invalid Vector3 col1");
        }
        if (col2 == null || !Vector3.isValidVector(col2)) {
            throw new IllegalArgumentException(
                    "Cannot construct new Matrix from null or invalid Vector3 col2");
        }
        if (col3 == null || !Vector3.isValidVector(col3)) {
            throw new IllegalArgumentException(
                    "Cannot construct new Matrix from null or invalid Vector3 col3");
        }
        if (Double.isInfinite(scale) || Double.isNaN(scale)) {
            throw new IllegalArgumentException(
                    "Cannot construct new Matrix if 'scale' is Infinite or NaN");
        }

        this.rows = 4;
        this.columns = 4;
        this.mat = new double[4][4];

        this.mat[0][0] = col0.getX();
        this.mat[1][0] = col0.getY();
        this.mat[2][0] = col0.getZ();
        this.mat[0][1] = col1.getX();
        this.mat[1][1] = col1.getY();
        this.mat[2][1] = col1.getZ();
        this.mat[0][2] = col2.getX();
        this.mat[1][2] = col2.getY();
        this.mat[2][2] = col2.getZ();
        this.mat[3][0] = 0.0;
        this.mat[3][1] = 0.0;
        this.mat[3][2] = 0.0;
        this.mat[0][3] = col3.getX();
        this.mat[1][3] = col3.getY();
        this.mat[2][3] = col3.getZ();
        this.mat[3][3] = scale;
    }

    /**
     * Create a 4 x 1 matrix from a Point, where x is (0,0), y is (1,0), z is
     * (2,0), and (3,0) is set to 1.0.
     *
     * @param v Point to convert to Matrix
     * @throws IllegalArgumentException if v is null or not valid
     */
    public Matrix(Point v) {
        if (v == null || !Point.isValidPoint(v)) {
            throw new IllegalArgumentException(
                    "Cannot construct new Matrix from null or invalid Point");
        }

        rows = 4;
        columns = 1;
        mat = new double[rows][columns];

        mat[0][0] = v.getX();
        mat[1][0] = v.getY();
        mat[2][0] = v.getZ();
        mat[3][0] = 1.0;
    }

    /**
     * Constructor to build a Matrix thats transforms coordinates into a rotated
     * and translated coordinate system. The roll rotation is performed first,
     * then the pitch rotation, then the yaw rotation, finally the translation.
     *
     * @param yaw      The yaw angle of the transformed coordinate system
     * @param pitch    The pitch angle of the transformed coordinate system
     * @param roll     The roll angle of the transformed coordinate system
     * @param location The location of the origin of the transformed coordinate
     *                 system
     * @throws IllegalArgumentException if any angle or the location is null or
     *                                  not valid
     */
    public Matrix(Amount<Angle> yaw, Amount<Angle> pitch, Amount<Angle> roll, Point location) {
        if (yaw == null) {
            throw new IllegalArgumentException(
                    "Cannot construct Matrix from null value for yaw");
        }
        if (pitch == null) {
            throw new IllegalArgumentException(
                    "Cannot construct Matrix from null value for pitch");
        }
        if (roll == null) {
            throw new IllegalArgumentException(
                    "Cannot construct Matrix from null value for roll");
        }

        if (Double.isInfinite(yaw.doubleValue(SI.RADIAN))
                || Double.isNaN(yaw.doubleValue(SI.RADIAN))) {
            throw new IllegalArgumentException(
                    "Cannot construct Matrix from NaN/Infinite value for yaw: "
                            + yaw.doubleValue(SI.RADIAN));
        }
        if (Double.isInfinite(pitch.doubleValue(SI.RADIAN))
                || Double.isNaN(pitch.doubleValue(SI.RADIAN))) {
            throw new IllegalArgumentException(
                    "Cannot construct Matrix from NaN/Infinite value for pitch: "
                            + pitch.doubleValue(SI.RADIAN));
        }
        if (Double.isInfinite(roll.doubleValue(SI.RADIAN))
                || Double.isNaN(roll.doubleValue(SI.RADIAN))) {
            throw new IllegalArgumentException(
                    "Cannot construct Matrix from NaN/Infinite value for roll: "
                            + roll.doubleValue(SI.RADIAN));
        }

        if (location == null || !Point.isValidPoint(location)) {
            throw new IllegalArgumentException(
                    "Cannot construct Matrix from null or invalid Point");
        }

        rows = 4;
        columns = 4;
        mat = new double[rows][columns];

        double cosy = Math.cos(yaw.doubleValue(SI.RADIAN));
        double siny = Math.sin(yaw.doubleValue(SI.RADIAN));
        double cosp = Math.cos(pitch.doubleValue(SI.RADIAN));
        double sinp = Math.sin(pitch.doubleValue(SI.RADIAN));
        double cosr = Math.cos(roll.doubleValue(SI.RADIAN));
        double sinr = Math.sin(roll.doubleValue(SI.RADIAN));

        mat[0][0] = cosy * cosp;
        mat[0][1] = cosy * sinp * sinr - siny * cosr;
        mat[0][2] = cosy * sinp * cosr + siny * sinr;
        mat[1][0] = siny * cosp;
        mat[1][1] = siny * sinp * sinr + cosy * cosr;
        mat[1][2] = siny * sinp * cosr - cosy * sinr;
        mat[2][0] = -sinp;
        mat[2][1] = cosp * sinr;
        mat[2][2] = cosp * cosr;
        mat[2][3] = 0.0;
        mat[3][0] = 0.0;
        mat[3][1] = 0.0;
        mat[3][2] = 0.0;
        mat[3][3] = 1.0;

        mat[0][3] = location.getX();
        mat[1][3] = location.getY();
        mat[2][3] = location.getZ();
    }

    /**
     * Constructor to build a Matrix thats transforms coordinates into a rotated
     * and translated coordinate system. The roll rotation is performed first,
     * then the pitch rotation, then the yaw rotation.
     *
     * @param yaw   The yaw angle of the transformed coordinate system
     * @param pitch The pitch angle of the transformed coordinate system
     * @param roll  The roll angle of the transformed coordinate system
     * @throws IllegalArgumentException if any angle is null or not valid
     */
    public Matrix(Amount<Angle> yaw, Amount<Angle> pitch, Amount<Angle> roll) {
        this(yaw, pitch, roll, new Point(0.0, 0.0, 0.0));
    }

    /**
     * Create matrix from Quaternion and Vector.
     *
     * @param q     Quaternion to be used to create matrix.
     * @param xlate The vector to translate the quaternion as matrix is created.
     * @throws IllegalArgumentException if either input is null or not valid
     */
    public Matrix(Quaternion q, Vector3 xlate) {
        if (q == null || !Quaternion.isValidQuaternion(q)) {
            throw new IllegalArgumentException(
                    "Cannot construct new Matrix from null or invalid Quaternion");
        }
        if (xlate == null || !Vector3.isValidVector(xlate)) {
            throw new IllegalArgumentException(
                    "Cannot construct new Matrix from null or invalid Vector3");
        }

        rows = 4;
        columns = 4;
        mat = new double[4][4];

        mat[0][0] = q.getQ0() * q.getQ0() - q.getQ1() * q.getQ1() - q.getQ2()
                * q.getQ2() + q.getQ3() * q.getQ3();
        mat[1][0] = 2.0 * (q.getQ0() * q.getQ1() + q.getQ3() * q.getQ2());
        mat[2][0] = 2.0 * (q.getQ0() * q.getQ2() - q.getQ3() * q.getQ1());
        mat[0][3] = xlate.getX();
        mat[0][1] = 2.0 * (q.getQ0() * q.getQ1() - q.getQ3() * q.getQ2());
        mat[1][1] = -q.getQ0() * q.getQ0() + q.getQ1() * q.getQ1() - q.getQ2()
                * q.getQ2() + q.getQ3() * q.getQ3();
        mat[2][1] = 2.0 * (q.getQ1() * q.getQ2() + q.getQ3() * q.getQ0());
        mat[1][3] = xlate.getY();
        mat[0][2] = 2.0 * (q.getQ0() * q.getQ2() + q.getQ3() * q.getQ1());
        mat[1][2] = 2.0 * (q.getQ1() * q.getQ2() - q.getQ3() * q.getQ0());
        mat[2][2] = -q.getQ0() * q.getQ0() - q.getQ1() * q.getQ1() + q.getQ2()
                * q.getQ2() + q.getQ3() * q.getQ3();
        mat[2][3] = xlate.getZ();
        mat[3][0] = 0.0;
        mat[3][1] = 0.0;
        mat[3][2] = 0.0;
        mat[3][3] = 1.0;
    }

    /**
     * Get the number of rows in this matrix
     *
     * @return The number of rows in this matrix
     */
    public int getRows() {
        return rows;
    }

    /**
     * Get the number of columns in this matrix
     *
     * @return The number of columns in this matrix
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Get the matrix element at a specified row, column. Matrix is zero-based
     * (i.e., first element is at (0,0).
     *
     * @param row The row of the desired element
     * @param col The column of the desireed element
     * @return The value of the element at the specified row and column
     * @throws IllegalArgumentException if row or col is out of bounds
     */
    public double get(int row, int col) {
        if (row < 0 || col < 0 || row >= this.rows || col >= this.columns) {
            throw new IllegalArgumentException("Cannot get(" + row + "," + col
                    + ") when acceptable range is (0 thru " + (this.rows - 1)
                    + ",0 thru " + (this.columns - 1) + ")");
        }

        return mat[row][col];
    }

    public void set( int row, int col, double val ) {
        this.mat[row][col] = val;
    }

    /**
     * Determine if Matrix and its contents are valid.
     *
     * @param m Matrix to be reviewed
     * @return true, if Matrix is valid; false if not valid
     */
    public static boolean isValidMatrix(Matrix m) {
        return (!(m == null || m.rows <= 0 || m.columns <= 0 || m.mat == null));
    }

    /**
     * Build a Matrix that transforms coordinates from a rotated and translated
     * coordinate system into the unrotated system.
     *
     * @param yaw   The yaw angle of the transformed coordinate system
     * @param pitch The pitch angle of the transformed coordinate system
     * @param roll  The roll angle of the transformed coordinate system
     * @return A matrix that contains the coordinates in an unrotated system
     * @throws IllegalArgumentException if any angle is null or not valid
     */
    public static Matrix inverseYPR(Amount<Angle> yaw, Amount<Angle> pitch, Amount<Angle> roll) {
        if (yaw == null) {
            throw new IllegalArgumentException(
                    "Cannot construct Matrix from null value for yaw");
        }
        if (pitch == null) {
            throw new IllegalArgumentException(
                    "Cannot construct Matrix from null value for pitch");
        }
        if (roll == null) {
            throw new IllegalArgumentException(
                    "Cannot construct Matrix from null value for roll");
        }

        if (Double.isInfinite(yaw.doubleValue(SI.RADIAN))
                || Double.isNaN(yaw.doubleValue(SI.RADIAN))) {
            throw new IllegalArgumentException(
                    "Cannot construct Matrix from NaN/Infinite value for yaw: "
                            + yaw.doubleValue(SI.RADIAN));
        }
        if (Double.isInfinite(pitch.doubleValue(SI.RADIAN))
                || Double.isNaN(pitch.doubleValue(SI.RADIAN))) {
            throw new IllegalArgumentException(
                    "Cannot construct Matrix from NaN/Infinite value for pitch: "
                            + pitch.doubleValue(SI.RADIAN));
        }
        if (Double.isInfinite(roll.doubleValue(SI.RADIAN))
                || Double.isNaN(roll.doubleValue(SI.RADIAN))) {
            throw new IllegalArgumentException(
                    "Cannot construct Matrix from NaN/Infinite value for roll: "
                            + roll.doubleValue(SI.RADIAN));
        }

        Matrix my = new Matrix((Amount<Angle>) yaw.opposite(), Amount.valueOf(0, Angle.UNIT), Amount.valueOf(0, Angle.UNIT));
        Matrix mp = new Matrix(Amount.valueOf(0, Angle.UNIT), (Amount<Angle>) pitch.opposite(), Amount.valueOf(0, Angle.UNIT));
        Matrix mr = new Matrix(Amount.valueOf(0, Angle.UNIT), Amount.valueOf(0, Angle.UNIT), (Amount<Angle>) roll.opposite());

        mr.mult(mp);
        mr.mult(my);

        return mr;
    }

    /**
     * Transpose a matrix
     *
     * @param in The matrix to be transposed
     * @return The transposed matrix
     * @throws IllegalArgumentException if the input matrix is null or not
     *                                  valid
     */
    public static Matrix transpose(Matrix in) {
        if (in == null || !Matrix.isValidMatrix(in)) {
            throw new IllegalArgumentException(
                    "Cannot transpose null or invalid Matrix");
        }

        Matrix out = new Matrix(in.columns, in.rows);

        for (int row = 0; row < out.rows; row++) {
            for (int col = 0; col < out.columns; col++) {
                out.mat[row][col] = in.mat[col][row];
            }
        }

        return out;
    }

    /**
     * Make matrix a unit matrix by setting its values to 1.0 where r == c
     * (i.e., along the diagonal) and 0.0 everywhere else.
     */
    public void unit() {
        for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < this.columns; c++) {
                if (r == c) {
                    mat[r][c] = 1.0;
                } else {
                    mat[r][c] = 0.0;
                }
            }
        }
    }

    /**
     * Multiply by supplied matrix, note that applying the resulting matrix is
     * equivalent to applying the specified multiplier transformation first then
     * applying the original matrix
     *
     * @param multiplier Matrix
     * @throws IllegalArgumentException if multiplier is null or not valid or
     *                                  the number of columns in the multiplicand != number of rows
     *                                  in the multiplier
     */
    public void mult(Matrix multiplier) {
        if (multiplier == null || !Matrix.isValidMatrix(multiplier)) {
            throw new IllegalArgumentException(
                    "mult() called with null or invalid Matrix");
        }
        if (columns != multiplier.rows) {
            throw new IllegalArgumentException(
                    "Cannot multiply this matrix of size (" + this.rows + ","
                            + this.columns + ") with matrix of size ("
                            + multiplier.rows + "," + multiplier.columns + ").");
        }

        double result[][] = new double[rows][multiplier.columns];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < multiplier.columns; c++) {
                result[r][c] = 0.0;
                for (int i = 0; i < columns; i++) {
                    result[r][c] += (this.mat[r][i] * multiplier.mat[i][c]);
                }
            }
        }
        mat = result;
        columns = multiplier.columns;
    }

    /**
     * Multiply supplied vertex by matrix
     *
     * @param v Point
     * @throws IllegalArgumentException if v is null or not valid or the number
     *                                  of rows < 3 or the number of columns < 4 in this object
     */
    public void mult(Point v) {
        if (v == null || !Point.isValidPoint(v)) {
            throw new IllegalArgumentException(
                    "Cannot multiply null or invalid Point");
        }
        if ((rows < 3) || (columns < 4)) {
            throw new IllegalArgumentException(
                    "mult(Point): Matrix too small (" + this.rows + ","
                            + this.columns + "); must be at least (3,4)");
        }

        double x = (mat[0][0] * v.getX()) + (mat[0][1] * v.getY())
                + (mat[0][2] * v.getZ()) + mat[0][3];
        double y = (mat[1][0] * v.getX()) + (mat[1][1] * v.getY())
                + (mat[1][2] * v.getZ()) + mat[1][3];
        double z = (mat[2][0] * v.getX()) + (mat[2][1] * v.getY())
                + (mat[2][2] * v.getZ()) + mat[2][3];
        v.setX(x);
        v.setY(y);
        v.setZ(z);
    }

    /**
     * Multiply supplied vector by this matrix (note that translations do not
     * get applied to vectors)
     *
     * @param v a Vector3
     * @throws IllegalArgumentException if v is null or not valid or the number
     *                                  of rows < 3 or the number of columns < 4 in this object
     */
    public void mult(Vector3 v) {
        if (v == null || !Vector3.isValidVector(v)) {
            throw new IllegalArgumentException(
                    "Cannot multiply null or invalid Vector");
        }
        if ((rows < 3) || (columns < 4)) {
            throw new IllegalArgumentException(
                    "mult(Point): Matrix too small (" + this.rows + ","
                            + this.columns + "); must be at least (3,4)");
        }

        double x = (mat[0][0] * v.getX()) + (mat[0][1] * v.getY())
                + (mat[0][2] * v.getZ());
        double y = (mat[1][0] * v.getX()) + (mat[1][1] * v.getY())
                + (mat[1][2] * v.getZ());
        double z = (mat[2][0] * v.getX()) + (mat[2][1] * v.getY())
                + (mat[2][2] * v.getZ());
        v.setX(x);
        v.setY(y);
        v.setZ(z);
    }

    /**
     * Subtract supplied matrix. This matrix and the supplied matrix must be the
     * same size or an IllegalArgumentException is thrown.
     *
     * @param s Matrix
     * @throws IllegalArgumentException if s is null or not valid or the number
     *                                  of rows and columns in s is not the same as for this object
     */
    public void sub(Matrix s) {
        if (s == null || !Matrix.isValidMatrix(s)) {
            throw new IllegalArgumentException(
                    "Cannot subtract null or invalid Matrix");
        }
        if (this.columns != s.columns || this.rows != s.rows) {
            throw new IllegalArgumentException(
                    "Cannot subtract input matrix whose size (" + s.rows + ","
                            + s.columns + ") is different from this ("
                            + this.rows + "," + this.columns + ")");

        }

        for (int c = 0; c < columns; c++) {
            for (int r = 0; r < rows; r++) {
                mat[r][c] -= s.mat[r][c];
            }
        }
    }

    /**
     * Add supplied matrix. This matrix and the supplied matrix must be the same
     * size or an IllegalArgumentException is thrown.
     *
     * @param a Matrix
     * @throws IllegalArgumentException if a is null or not valid or the number
     *                                  of rows and columns in a is not the same as for this object
     */
    public void add(Matrix a) {
        if (a == null || !Matrix.isValidMatrix(a)) {
            throw new IllegalArgumentException(
                    "Cannot add null or invalid Matrix");
        }
        if (this.columns != a.columns || this.rows != a.rows) {
            throw new IllegalArgumentException(
                    "Cannot add input matrix whose size (" + a.rows + ","
                            + a.columns + ") is different from this ("
                            + this.rows + "," + this.columns + ")");

        }

        for (int c = 0; c < columns; c++) {
            for (int r = 0; r < rows; r++) {
                mat[r][c] += a.mat[r][c];
            }
        }
    }

    /**
     * Translate this matrix by a given vector. This matrix must be a 4x4
     * matrix; else, an IllegalArgumentException is thrown.
     *
     * @param v The input vector
     * @throws IllegalArgumentException if v is null or not valid or the number
     *                                  of rows and columns in this object is not 4
     */
    public void translate(Vector3 v) {
        if (v == null || !Vector3.isValidVector(v)) {
            throw new IllegalArgumentException(
                    "Cannot translate using null or invalid Vector");
        }
        if ((this.columns != 4) || (this.rows != 4)) {
            throw new IllegalArgumentException(
                    "Matrix to translate is wrong size (" + this.rows + ","
                            + this.columns + "); must be (4,4)");
        }

        mat[0][3] += v.getX();
        mat[1][3] += v.getY();
        mat[2][3] += v.getZ();
    }

    /**
     * Print matrix in a multi-line format, each line ending with a newline
     *
     * @return String containing the matrix with the format "[ r1c1 r1c2 ...
     *         r1cy ]\n[r2c1 r2c2 ... r2cy]\n[ rxc1 rxc2 ... rxcy ]\n"
     */
    public String toPrettyString() {
        StringBuffer out = new StringBuffer(this.getRows() * this.getColumns()
                * 10);

        for (int r = 0; r < this.getRows(); r++) {
            out.append("[");
            for (int c = 0; c < this.getColumns(); c++) {
                out.append(" ").append(this.get(r, c)).append(" ");
            }
            out = out.append("]\n");
        }
        return out.toString();
    }

    /**
     * Print matrix as a single String
     *
     * @return String containing the matrix in the format "[ r1c1 r1c2 r1c3 ...
     *         rxcy ]"
     */
    public String toString() {
        StringBuffer str = new StringBuffer(this.getRows() * this.getColumns()
                * 10);

        str.append("[");
        for (int row = 0; row < this.getRows(); row++) {
            for (int col = 0; col < this.getColumns(); col++) {
                str.append(" ").append(this.get(row, col));
            }
        }
        str.append(" ]");

        return str.toString();
    }

}
