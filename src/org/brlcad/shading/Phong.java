/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.shading;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import org.brlcad.geometry.Partition;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Vector3;

/**
 *
 * @author jra
 */
public class Phong implements Shader {
    private static final Material defaultMaterial = new Material("default", new Color(200, 200, 200));
    private Set<Light> lights;
    private double ambientIntensity = 0.7;

    public Phong() {
        this.lights = new HashSet<Light>();
    }

    public Phong( Set<Light> lights ) {
        this.lights = new HashSet<Light>();
        this.lights.addAll(lights);
    }

    public Color shade( Partition part, Material mat, Point eye_pt ) {
        Vector3 toEye = Vector3.minus(eye_pt, part.getInHit().getHit_pt());
        toEye.normalize();
        if( mat == null ) {
            mat = defaultMaterial;
        }
        Point hit_pt = part.getInHit().getHit_pt();
        Vector3 hit_normal = part.getInHitNormal();
        double scale = -part.getInHit().getRayData().getTheRay().getDirection().dotProduct(hit_normal);
        if( scale < 0.0 ) {
            scale = 0.0;
        }
        if( scale > 1.0 ) {
            scale = 1.0;
        }
        scale *= ambientIntensity;
        Color color = scale(mat.getColor(), (float)(mat.getKa()*scale));
        for( Light light : lights ) {
            Vector3 toLight = Vector3.minus(light.getLocation(), hit_pt);
            toLight.normalize();
            double cosine = toLight.dotProduct(hit_normal);
            if( cosine < 0.0 ) {
                continue;
            } else if( cosine > 1.0 ) {
                cosine = 1.0;
            }
            color = add( color, scale(light.getDiffuse(), (float)(3.0*mat.getKd()*cosine/lights.size())) );
            Vector3 reflected = Vector3.scale(hit_normal, 2.0*cosine);
            reflected.minus(toLight);
            reflected.normalize();
            cosine = reflected.dotProduct(toEye);
            if( cosine > 1.0 ) {
                cosine = 1.0;
            }
            if( cosine > 0.0 ) {
                double spec = Math.pow(cosine, mat.getAlpha());
                color = add(color, scale(light.getSpecular(), (float) (mat.getKs() * spec)));
            }
        }
        float[] rgb = color.getRGBColorComponents(null);
        for (int i = 0; i < 3; i++) {
            if (rgb[i] > 1.0) {
                rgb[i] = 1.0f;
            }
            if( rgb[i] < 0.0 ) {
                rgb[i] = 0.0f;
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
