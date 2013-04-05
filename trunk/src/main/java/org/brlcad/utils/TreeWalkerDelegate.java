/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.utils;

import org.brlcad.geometry.DbObject;

/**
 *
 * @author jra
 */
public interface TreeWalkerDelegate {
    public void processLeaf(DbObject leaf, TreeState state);
}
