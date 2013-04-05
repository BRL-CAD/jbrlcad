/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.utils;

import java.util.ArrayList;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Vector3;

/**
 *
 * @author jra
 */
public class VertexTree {
    private ArrayList<Point> thePoints;
    private VertTreeNode treeRoot;
    private double tolerance;

    public VertexTree(double tol) {
        this.thePoints = new ArrayList<Point>();
        this.treeRoot = null;
        this.tolerance = tol;
    }

    public int addVert(Point pt) {
        int index;
        if (treeRoot == null) {
            index = thePoints.size();
            thePoints.add(index, pt);
            treeRoot = new VertTreeLeafNode(index);
        } else {
            // look for this Point in the tree
            VertTreeCutNode parent = null;
            boolean isUpper = false;
            VertTreeNode node = treeRoot;
            while (!node.isLeaf()) {
                VertTreeCutNode cut = (VertTreeCutNode) node;
                parent = cut;
                if (pt.get(cut.getCutAxis()) >= cut.getCutValue()) {
                    node = cut.getUpper();
                    isUpper = true;
                } else {
                    node = cut.getLower();
                    isUpper = false;
                }
            }
            VertTreeLeafNode leaf = (VertTreeLeafNode) node;
            Point leafPt = thePoints.get(leaf.getIndex());
            if (leafPt.dist(pt) <= tolerance) {
                // use existing Point
                index = leaf.getIndex();
            } else {
                // add a new Point
                index = thePoints.size();
                thePoints.add(index, pt);
                VertTreeLeafNode newLeaf = new VertTreeLeafNode(index);
                Vector3 diff = Vector3.minus(leafPt, pt);
                int cutAxis = -1;
                double cutDiff = Double.NEGATIVE_INFINITY;
                for (int i=0 ; i<3 ; i++) {
                    diff.set(i, Math.abs(diff.get(i)));
                    if (diff.get(i) > cutDiff) {
                        cutAxis = i;
                        cutDiff = diff.get(i);
                    }
                }
                VertTreeCutNode newCut = new VertTreeCutNode(cutAxis, (pt.get(cutAxis) + leafPt.get(cutAxis))/2.0);
                if (pt.get(cutAxis) >= newCut.getCutValue()) {
                    newCut.setUpper(newLeaf);
                    newCut.setLower(leaf);
                } else {
                    newCut.setUpper(leaf);
                    newCut.setLower(newLeaf);
                }
                if (parent == null) {
                    treeRoot = newCut;
                } else {
                    if (isUpper) {
                        parent.setUpper(newCut);
                    } else {
                        parent.setLower(newCut);
                    }
                }
            }
        }
        return index;
    }

    /**
     * @return the thePoints
     */
    public ArrayList<Point> getThePoints() {
        return thePoints;
    }

    /**
     * @return the treeRoot
     */
    public VertTreeNode getTreeRoot() {
        return treeRoot;
    }

    /**
     * @return the tolerance
     */
    public double getTolerance() {
        return tolerance;
    }

    private abstract class VertTreeNode {
        public abstract boolean isLeaf();
    }

    private class VertTreeCutNode extends VertTreeNode {
        private int cutAxis;
        private double cutValue;
        private VertTreeNode upper;
        private VertTreeNode lower;

        private VertTreeCutNode(int cutAxis, double d) {
            this.cutAxis = cutAxis;
            this.cutValue = d;
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        /**
         * @return the cutAxis
         */
        public int getCutAxis() {
            return cutAxis;
        }

        /**
         * @return the cutValue
         */
        public double getCutValue() {
            return cutValue;
        }

        /**
         * @return the upper
         */
        public VertTreeNode getUpper() {
            return upper;
        }

        /**
         * @param upper the upper to set
         */
        public void setUpper(VertTreeNode upper) {
            this.upper = upper;
        }

        /**
         * @return the lower
         */
        public VertTreeNode getLower() {
            return lower;
        }

        /**
         * @param lower the lower to set
         */
        public void setLower(VertTreeNode lower) {
            this.lower = lower;
        }
    }

    private class VertTreeLeafNode extends VertTreeNode {
        private int index;

        private VertTreeLeafNode(int index) {
            this.index = index;
        }

        @Override
        public boolean isLeaf() {
            return true;
        }

        /**
         * @return the index
         */
        public int getIndex() {
            return index;
        }

        /**
         * @param index the index to set
         */
        public void setIndex(int index) {
            this.index = index;
        }
    }
}
