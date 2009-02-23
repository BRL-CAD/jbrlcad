/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.shading;

import java.awt.Color;
import org.brlcad.geometry.Partition;
import org.brlcad.numerics.Point;

/**
 *
 * @author jra
 */
public interface Shader {
    public Color shade( Partition part, Material mat, Point eye_pt );
}
