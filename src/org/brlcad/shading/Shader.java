/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.shading;

import java.awt.Color;
import org.brlcad.geometry.Hit;

/**
 *
 * @author jra
 */
public interface Shader {
    public Color shade( Hit hit, Material mat );
}
