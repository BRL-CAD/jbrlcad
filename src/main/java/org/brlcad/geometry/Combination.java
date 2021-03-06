package org.brlcad.geometry;

/**
 * Represents a BRL-CAD Combination
 */
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.brlcad.numerics.BoundingBox;
import org.brlcad.numerics.Matrix;
import org.brlcad.preppedGeometry.PreppedCombination;
import org.brlcad.preppedGeometry.PreppedObject;
import org.brlcad.shading.Material;
import org.brlcad.spacePartition.PreppedDb;

public class Combination extends DbObject {

    private Material material;
    private int index;
    private Tree tree;
    public static final byte majorType = 1;
    public static final byte minorType = 31;
    private static final byte leaf = 1;
    private static final byte union = 2;
    private static final byte intersection = 3;
    private static final byte subtraction = 4;
    private static final byte xor = 5;
    private static final byte not = 6;
    // minus one stored as differnt length ints (indexed by wid)
    private static final long[] identMatrix = {0377L,
        0177777L,
        037777777777L,
        01777777777777777777777L};

    public Combination(DbExternal dbExt) throws DbException {
        super(dbExt);
        if (dbExt.getMajorType() != majorType || dbExt.getMinorType() != minorType) {
            throw new DbException("Attempted to import combination, but external is " +
                    " major type: " + dbExt.getMajorType() +
                    " minor type: " + dbExt.getMinorType());
        }
        byte[] body = dbExt.getBody();
        int pointer = 0;
        byte wid = body[0];
        pointer += 1;
        int length = DbExternalObject.fieldLength[wid];
        int numMatrices = (int) BrlcadDb.getLong(body, pointer, length);
        pointer += length;
        int numLeaves = (int) BrlcadDb.getLong(body, pointer, length);
        pointer += length;
        int leafBytes = (int) BrlcadDb.getLong(body, pointer, length);
        pointer += length;
        long rpnLength = BrlcadDb.getLong(body, pointer, length);
        pointer += length;
        long maxStackDepth = BrlcadDb.getLong(body, pointer, length);
        pointer += length;

        Matrix[] matrices = new Matrix[numMatrices];
        // read the matrices
        for (int i = 0; i < numMatrices; i++) {
            matrices[i] = new Matrix(4, 4);
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 4; col++) {
                    matrices[i].set(row, col, BrlcadDb.getDouble(body, pointer));
                    pointer += 8;
                }
            }
        }

        // new pointer that starts at the RPN expresion
        if (rpnLength == 0 && numLeaves > 0) {
            List<Tree> list1 = new ArrayList<Tree>();
            List<Tree> list2 = new ArrayList<Tree>();

            // there is no RPN expresion, just union together all the leaves
            for (int i = 0; i < numLeaves; i++) {
                int nameStart = pointer;
                int nameEnd = pointer;
                while (body[nameEnd] != (byte) 0) {
                    nameEnd++;
                }
                String nodeName = new String(body, nameStart, nameEnd - nameStart);
                pointer = nameEnd + 1;
                long matrixInd = BrlcadDb.getLong(body, pointer, length);
                pointer += length;
                Matrix mat = null;
                if (matrixInd != identMatrix[wid]) {
                    mat = matrices[(int) matrixInd];
                }
                Tree node = new Tree(nodeName, mat);
                list1.add(node);
            }

            // make a balanced tree
            while (true) {
                Tree t1;
                Tree t2;

                Iterator<Tree> iter = list1.iterator();
                while (iter.hasNext()) {
                    t1 = iter.next();
                    if (iter.hasNext()) {
                        t2 = iter.next();
                        list2.add(new Tree(t1, t2, Operator.UNION));
                    } else {
                        list2.add(t1);
                    }
                }

                if (list2.size() == 1) {
                    this.tree = list2.get(0);
                    break;
                }
                list1 = list2;
                list2 = new ArrayList<Tree>();
            }
        } else if (rpnLength == 0 && numLeaves == 0) {
            // this combination has no tree
            tree = null;
        } else {
            int tmpptr = pointer + leafBytes;
            Stack<Tree> stack = new Stack<Tree>();
            Tree node = null;
            Tree left = null;
            Tree right = null;
            for (int i = tmpptr; i < tmpptr + rpnLength; i++) {
                switch (body[i]) {
                    case leaf:
                        // get the leaf from the body array
                        int nameStart = pointer;
                        int nameEnd = pointer;
                        while (body[nameEnd] != (byte) 0) {
                            nameEnd++;
                        }
                        String nodeName = new String(body, nameStart, nameEnd - nameStart);
                        pointer = nameEnd + 1;
                        long matrixInd = BrlcadDb.getLong(body, pointer, length);
                        pointer += length;
                        Matrix mat = null;
                        if (matrixInd != identMatrix[wid]) {
                            mat = matrices[(int) matrixInd];
                        }
                        node = new Tree(nodeName, mat);
                        stack.push(node);
                        break;
                    case union:
                        right = stack.pop();
                        left = stack.pop();
                        node = new Tree(left, right, Operator.UNION);
                        stack.push(node);
                        break;
                    case intersection:
                        right = stack.pop();
                        left = stack.pop();
                        node = new Tree(left, right, Operator.INTERSECTION);
                        stack.push(node);
                        break;
                    case subtraction:
                        right = stack.pop();
                        left = stack.pop();
                        node = new Tree(left, right, Operator.SUBTRACTION);
                        stack.push(node);
                        break;
                    case xor:
                        right = stack.pop();
                        left = stack.pop();
                        node = new Tree(left, right, Operator.XOR);
                        stack.push(node);
                        break;
                    case not:
                        right = stack.pop();
                        node = new Tree(null, right, Operator.NOT);
                        stack.push(node);
                        break;
                    default:
                        throw new DbException("Unrecognized operator in RPN expression in external form of a Combination");
                }
            }
            this.tree = stack.pop();
        }

        String oshader = this.getAttribute("oshader");
        String rgbString = this.getAttribute("rgb");
        if (rgbString != null) {
            String[] rgbs = rgbString.split("/");
            if (rgbs.length == 3) {
                float[] rgb = new float[3];
                for (int i = 0; i < 3; i++) {
                    rgb[i] = Float.parseFloat(rgbs[i]) / 255.0f;
                }
                Color color = new Color(rgb[0], rgb[1], rgb[2]);
                this.material = new Material(oshader, color);
            }
        }
    }

    /**
     * Sets Index
     *
     * @param    Index               an int
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Returns Index
     *
     * @return    an int
     */
    public int getIndex() {
        return index;
    }

    /**
     * Constructor
     * @param reg	The region that this Combination belongs to (or null)
     * @param preppedDb	The PreppedDb that this Combination belongs to
     * @para matrix	The transformation matrix to apply to this Combination
     * @return A PreppedRegion or a PreppedCombination as appropriate
     */
    @Override
    public PreppedObject prep(PreppedCombination reg, PreppedDb preppedDb, Matrix matrix) throws BadGeometryException, DbException, IOException, DbNameNotFoundException {
        BoundingBox boundingBox = null;
        PreppedCombination pc = new PreppedCombination(this);
        boolean isRegion = this.getAttribute("region") != null;
        if (this.tree != null) {
            if (isRegion && reg == null) {
                boundingBox = this.tree.prep(pc, preppedDb, matrix);
                preppedDb.addPreppedRegion(pc);
                pc.setBoundingBox(boundingBox);
            } else {
                boundingBox = this.tree.prep(reg, preppedDb, matrix);
                preppedDb.addPreppedCombination(pc);
                pc.setBoundingBox(boundingBox);
            }
        }

        return pc;
    }

    /**
     * Get the tree that belongs to this Combination
     * @return	The tree
     */
    public Tree getTree() {
        return this.tree;
    }

    /**
     * Create a String representation of this Combination
     *
     * @return   a String
     *
     */
    @Override
    public String toString() {
        return super.toString() + " Combination:\n" + this.tree;
    }

    /**
     * @return the material
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * @param material the material to set
     */
    public void setMaterial(Material material) {
        this.material = material;
    }
}

