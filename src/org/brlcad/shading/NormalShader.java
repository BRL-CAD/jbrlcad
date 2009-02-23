/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.shading;

import java.awt.Color;
import org.brlcad.geometry.Hit;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Vector3;

/**
 *
 * @author jra
 */
public class NormalShader implements Shader {

    private Point eye_pt;

    public NormalShader( Point eye_pt ) {
        this.eye_pt = new Point( eye_pt );
    }


    public Color shade(Hit hit, Material mat) {
        Vector3 toEye = Vector3.minus(eye_pt, hit.getHit_pt());
        toEye.normalize();
        Vector3 norm = hit.getHit_normal();
        float dot = (float) norm.dotProduct(toEye);
        if( dot < 0.0 ) {
            dot = 0.0f;
        }
        if( dot > 1.0 ) {
            dot = 1.0f;
        }
        return new Color( dot, dot, dot );
    }

}
