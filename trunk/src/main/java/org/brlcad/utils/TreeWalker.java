/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.utils;

import java.io.IOException;
import org.brlcad.geometry.BrlcadDb;
import org.brlcad.geometry.Combination;
import org.brlcad.geometry.DbException;
import org.brlcad.geometry.DbNameNotFoundException;
import org.brlcad.geometry.DbObject;
import org.brlcad.geometry.Operator;
import org.brlcad.geometry.Tree;

/**
 *
 * @author jra
 */
public class TreeWalker {
    private TreeState state;
    private TreeWalkerDelegate delegate;
    private BrlcadDb db;

    public TreeWalker(BrlcadDb db, TreeState s, TreeWalkerDelegate d) {
        this.db = db;
        this.delegate = d;
        this.state = s;
    }

    public void processTree(Tree tree) throws IOException, DbException, DbNameNotFoundException {
        if (tree == null) {
            return;
        } else if (tree.getOp() == Operator.LEAF) {
            DbObject leaf = db.getInternal(tree.getLeafName());
            if (leaf instanceof Combination) {
                Combination comb = (Combination) leaf;
                processTree(comb.getTree());
            } else {
                delegate.processLeaf(leaf, state);
            }
        } else {
            processTree(tree.getLeft());
            processTree(tree.getRight());
        }
    }

    public TreeState getState() {
        return state;
    }
}
