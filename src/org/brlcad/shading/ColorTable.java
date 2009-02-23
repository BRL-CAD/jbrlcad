/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.shading;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jra
 */
public class ColorTable {
    private List<ColorTableEntry> entries;

    public ColorTable( String definition ) {
        this.entries = new ArrayList<ColorTableEntry>();
        String[] tokens = definition.split(" *\\} *\\{ *| *\\} *| *\\{ *");
        for( String token : tokens ) {
            if( token.length() == 0 ) {
                continue;
            }
            ColorTableEntry cte = new ColorTableEntry(token);
            if( cte != null ) {
                this.entries.add(cte);
            }
        }
    }

    public Color getColor( int ident ) {
        for( ColorTableEntry cte : this.entries ) {
            if( ident >= cte.getStart() && ident <= cte.getEnd() ) {
                return cte.getColor();
            }
        }
        return null;
    }

    private class ColorTableEntry {
        private int start;
        private int end;
        private Color color;

        public ColorTableEntry( String definition ) {
            String[] tokens = definition.split(" +");
            if( tokens.length != 5 ) {
                throw new IllegalArgumentException( "Color Table entry must have exactly 5 numbers found: " + definition );
            }
            this.start = Integer.valueOf(tokens[0]);
            this.end = Integer.valueOf(tokens[1]);
            float r = Float.valueOf(tokens[2]) / 255.0f;
            float g = Float.valueOf(tokens[3]) / 255.0f;
            float b = Float.valueOf(tokens[4]) / 255.0f;
            this.color = new Color(r, g, b);
        }

        /**
         * @return the start
         */
        public int getStart() {
            return start;
        }

        /**
         * @return the end
         */
        public int getEnd() {
            return end;
        }

        /**
         * @return the color
         */
        public Color getColor() {
            return color;
        }
    }
}
