/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.brlcad.geometry;

import java.io.IOException;
import org.brlcad.numerics.Matrix;
import org.brlcad.numerics.Point;
import org.brlcad.preppedGeometry.PreppedCombination;
import org.brlcad.preppedGeometry.PreppedObject;
import org.brlcad.spacePartition.PreppedDb;

/**
 *
 * @author jra
 */
public class Ars extends DbObject {
    // the major and minor types for an ARS
    public static final byte majorType = 1;
    public static final byte minorType = 5;

    // the curves
    private Point[][] curves;

    // the equivalent Bot
    private Bot bot;

    public Ars(DbExternal dbExt) throws DbException {
        super(dbExt);

        if (dbExt.getMajorType() != Ars.majorType || dbExt.getMinorType() != Ars.minorType) {
            throw new DbException("Attempted to import an ARS, but external is " +
                    " major type: " + dbExt.getMajorType() +
                    " minor type: " + dbExt.getMinorType());
        }

        // get the body bytes from the DbExternal object
        byte[] body = dbExt.getBody();

        int pointer = 0;

        int numCurves = (int) BrlcadDb.getLong(body, pointer, 4);
        pointer += 4;
        int ptsPerCurve = (int) BrlcadDb.getLong(body, pointer, 4);
        pointer += 4;
        curves = new Point[numCurves][ptsPerCurve+1];
        for (int curve=0 ; curve<numCurves ; curve++) {
            for (int pt=0 ; pt<ptsPerCurve ; pt++) {
                double x = BrlcadDb.getDouble(body, pointer);
                pointer += 8;
                double y = BrlcadDb.getDouble(body, pointer);
                pointer += 8;
                double z = BrlcadDb.getDouble(body, pointer);
                pointer += 8;
                curves[curve][pt] = new Point(x, y, z);
            }
            // duplicate first point on each curve
            curves[curve][ptsPerCurve] = new Point(curves[curve][0]);
        }

        this.bot = Bot.fromArs(this, dbExt);
    }

    /**
     * Prepping an ARS produces a PreppedBot object
     *
     * @param reg The region containing this ARS (or null)
     * @param preppedDb The PreppedDb containing this ARS
     * @param matrix The transformation matrix to be applied to this ARS
     * @return A PreppedBot
     * @throws BadGeometryException
     * @throws DbException
     * @throws IOException
     * @throws DbNameNotFoundException
     */
    @Override
    public PreppedObject prep(PreppedCombination reg, PreppedDb preppedDb, Matrix matrix) throws BadGeometryException, DbException, IOException, DbNameNotFoundException {
        return this.bot.prep(reg, preppedDb, matrix);
    }

    /**
     * @return the curves
     */
    public Point[][] getCurves() {
        return curves;
    }

    public Point[] getCurve(int curve) {
        return curves[curve];
    }

    public Point getPoint(int curve, int point) {
        return curves[curve][point];
    }

    /**
     * @return the bot
     */
    public Bot getBot() {
        return bot;
    }

}
