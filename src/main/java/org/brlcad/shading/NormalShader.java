/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.shading;

import java.awt.Color;
import org.brlcad.geometry.Partition;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Vector3;

/**
 *
 * @author jra
 */
public class NormalShader implements Shader {

    public NormalShader() {
    }


    public Color shade(Partition part, Material mat, Point eye_pt) {
        Point hit_pt = part.getInHit().getHit_pt();
        Vector3 hit_norm = part.getInHitNormal();
        Vector3 toEye = Vector3.minus(eye_pt, hit_pt);
        toEye.normalize();
        float dot = (float) hit_norm.dotProduct(toEye);
        if( dot < 0.0 ) {
            dot = 0.0f;
        }
        if( dot > 1.0 ) {
            dot = 1.0f;
        }
        return new Color( dot, dot, dot );
    }

}
