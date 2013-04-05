/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.shading;

import java.awt.Color;
import java.io.Serializable;

/**
 *
 * @author jra
 */
public class Material implements Serializable {
    private String name;
    private Color color;
    private double ks;  // specular reflection constant, the ratio of reflection of the specular term of incoming light
    private double kd;  // diffuse reflection constant, the ratio of reflection of the diffuse term of incoming light (Lambertian reflectance)
    private double ka;  // ambient reflection constant, the ratio of reflection of the ambient term present in all points in the scene rendered
    private int alpha;  // is a shininess constant for this material, which decides how "evenly" light is reflected from a shiny spot, and is very large for most surfaces, on the order of 50, getting larger the more mirror-like they are.

    public Material( String name, Color color ) {
        this.name = name;
        this.color = new Color(color.getRGB());
        this.ks = 0.7;
        this.kd = 0.2;
        this.ka = 1.0;
        this.alpha = 10;
    }

    /**
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * @return the ks
     */
    public double getKs() {
        return ks;
    }

    /**
     * @param ks the ks to set
     */
    public void setKs(double ks) {
        this.ks = ks;
    }

    /**
     * @return the kd
     */
    public double getKd() {
        return kd;
    }

    /**
     * @param kd the kd to set
     */
    public void setKd(double kd) {
        this.kd = kd;
    }

    /**
     * @return the ka
     */
    public double getKa() {
        return ka;
    }

    /**
     * @param ka the ka to set
     */
    public void setKa(double ka) {
        this.ka = ka;
    }

    /**
     * @return the alpha
     */
    public int getAlpha() {
        return alpha;
    }

    /**
     * @param alpha the alpha to set
     */
    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

}
