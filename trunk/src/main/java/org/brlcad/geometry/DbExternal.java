/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.geometry;

/**
 *
 * @author jra
 */
public interface DbExternal {

	public String getName();

	public byte getMajorType();

	public byte getMinorType();

	public byte[] getBody();

	public byte[] getAttributes();

}
