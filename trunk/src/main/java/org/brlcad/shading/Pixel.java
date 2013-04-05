/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.shading;

import java.awt.Color;

/**
 *
 * @author jra
 */
public class Pixel {
    private byte red;
    private byte green;
    private byte blue;

    public Pixel( Color color ) {
        this.red = (byte) color.getRed();
        this.green = (byte) color.getGreen();
        this.blue = (byte) color.getBlue();
    }

    /**
     * @return the red
     */
    public byte getRed() {
        return red;
    }

    /**
     * @param red the red to set
     */
    public void setRed(byte red) {
        this.red = red;
    }

    /**
     * @return the green
     */
    public byte getGreen() {
        return green;
    }

    /**
     * @param green the green to set
     */
    public void setGreen(byte green) {
        this.green = green;
    }

    /**
     * @return the blue
     */
    public byte getBlue() {
        return blue;
    }

    /**
     * @param blue the blue to set
     */
    public void setBlue(byte blue) {
        this.blue = blue;
    }

}
