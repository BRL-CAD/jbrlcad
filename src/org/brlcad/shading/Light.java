/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.shading;

import java.awt.Color;
import org.brlcad.numerics.Point;

/**
 *
 * @author jra
 */
public class Light {
    private Point location;
    private Color specular;
    private Color diffuse;

    public Light( Point location, Color specular, Color diffuse ) {
        this.location = new Point( location );
        this.specular = new Color( specular.getRGB() );
        this.diffuse = new Color( diffuse.getRGB() );
    }

    /**
     * @return the specular
     */
    public Color getSpecular() {
        return specular;
    }

    /**
     * @param specular the specular to set
     */
    public void setSpecular(Color specular) {
        this.specular = specular;
    }

    /**
     * @return the diffuse
     */
    public Color getDiffuse() {
        return diffuse;
    }

    /**
     * @param diffuse the diffuse to set
     */
    public void setDiffuse(Color diffuse) {
        this.diffuse = diffuse;
    }

    /**
     * @return the location
     */
    public Point getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(Point location) {
        this.location = location;
    }
}
