/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.shading;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.util.HashSet;
import java.util.Set;
import org.brlcad.geometry.Hit;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Vector3;

/**
 *
 * @author jra
 */
public class Phong {
//    public static final ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
    private static final Material defaultMaterial = new Material("default", new Color(55, 55, 55));
    private Point eye_pt;
    private Set<Light> lights;

    public Phong( Point eye_pt ) {
        this.eye_pt = new Point( eye_pt );
        this.lights = new HashSet<Light>();
    }

    public Phong( Point eye_pt, Set<Light> lights ) {
        this.eye_pt = new Point( eye_pt );
        this.lights = new HashSet<Light>();
        this.lights.addAll(lights);
    }

    public Color shade( Hit hit, Material mat ) {
        Vector3 toEye = Vector3.minus(eye_pt, hit.getHit_pt());
        toEye.normalize();
        if( mat == null ) {
            mat = defaultMaterial;
        }
        Color color = scale(mat.getColor(), (float)mat.getKa());
        for( Light light : lights ) {
            Vector3 toLight = Vector3.minus(light.getLocation(), eye_pt);
            toLight.normalize();
            double cosine = toLight.dotProduct(hit.getHit_normal());
            color = add( color, scale(light.getDiffuse(), (float)(mat.getKd()*cosine)) );
            Vector3 reflected = Vector3.scale(hit.getHit_normal(), 2.0*cosine);
            reflected.minus(toLight);
            reflected.normalize();
            cosine = reflected.dotProduct(toEye);
            double spec = Math.pow(cosine, mat.getAlpha());
            color = add( color, scale(light.getSpecular(), (float)(mat.getKs()*spec)));
        }
        float[] rgb = color.getRGBColorComponents(null);
        for (int i = 0; i < 3; i++) {
            if (rgb[i] > 1.0) {
                rgb[i] = 1.0f;
            }
        }
        return new Color(rgb[0], rgb[1], rgb[2]);
    }

    private Color scale( Color color, float scale ) {
        float[] rgb = color.getRGBColorComponents(null);

        for( int i=0 ; i<3 ; i++ ) {
            rgb[i] = clamp(rgb[i] * scale);
        }

        return new Color(rgb[0], rgb[1], rgb[2]);
    }

    private float clamp( float c ) {
        if( c > 1.0 ) {
            c = 1.0f;
        } else if( c < 0.0 ) {
            c = 0.0f;
        }

        return c;
    }

    private Color add( Color c1, Color c2 ) {
        float[] rgb1 = c1.getRGBColorComponents(null);
        float[] rgb2 = c2.getRGBColorComponents(null);

        for( int i=0 ; i<3 ; i++ ) {
            rgb1[i] = clamp(rgb1[i] + rgb2[i]);
        }

        return new Color(rgb1[0], rgb1[1], rgb1[2]);
    }

    public void addLight( Light l ) {
        this.lights.add(l);
    }

    /**
     * @return the eye_pt
     */
    public Point getEye_pt() {
        return eye_pt;
    }

    /**
     * @param eye_pt the eye_pt to set
     */
    public void setEye_pt(Point eye_pt) {
        this.eye_pt = eye_pt;
    }

    /**
     * @return the lights
     */
    public Set<Light> getLights() {
        return lights;
    }

    /**
     * @param lights the lights to set
     */
    public void setLights(Set<Light> lights) {
        this.lights = lights;
    }
}
