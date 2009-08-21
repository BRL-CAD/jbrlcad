/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brlcad.geometry;

import java.io.IOException;
import org.brlcad.numerics.Matrix;
import org.brlcad.preppedGeometry.PreppedCombination;
import org.brlcad.preppedGeometry.PreppedObject;
import org.brlcad.spacePartition.PreppedDb;

/**
 *
 * @author jra
 */
public class DbAttributeOnly extends DbObject {
    public static final byte majorType = 2;
    public static final byte minorType = 0;

    public DbAttributeOnly(DbExternal dbExt) {
        super(dbExt);
    }

    @Override
    public PreppedObject prep(PreppedCombination reg, PreppedDb preppedDb, Matrix matrix) throws BadGeometryException, DbException, IOException, DbNameNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
