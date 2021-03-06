/**
 * SpacePartition.java
 *
 * @author Created by Omnicore CodeGuide
 */
package org.brlcad.spacePartition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.logging.Logger;
import org.brlcad.geometry.BadGeometryException;
import org.brlcad.geometry.BrlcadDb;
import org.brlcad.geometry.DbException;
import org.brlcad.geometry.DbNameNotFoundException;
import org.brlcad.geometry.DbObject;
import org.brlcad.geometry.Hit;
import org.brlcad.geometry.OverlapHandler;
import org.brlcad.geometry.Partition;
import org.brlcad.geometry.Segment;
import org.brlcad.numerics.BoundingBox;
import org.brlcad.numerics.Matrix;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Ray;
import org.brlcad.numerics.Vector3;
import org.brlcad.preppedGeometry.PreppedCombination;
import org.brlcad.preppedGeometry.PreppedObject;
import org.brlcad.preppedGeometry.PreppedObjectPiece;

public class PreppedDb {

    private BrlcadDb db;
    private List<String> topLevelObjects;
    private List<PreppedCombination> regions;
    private Map<String, PreppedCombination> combinations;
    private Node spacePartition;
    private BoundingBox boundingBox;
    private BoxNode initialBox;
    private int preppedSolidCount = 0;
    private int preppedRegionCount = 0;
    private final List<PreppedObjectPiece> pieces = new ArrayList<PreppedObjectPiece>();
    public static final String DbBoundingBoxName = "DB BoundingBox";
    private Logger logger;

    public PreppedDb(BrlcadDb db, String... objs) throws BadGeometryException, DbException, IOException, DbNameNotFoundException {
        logger = Logger.getLogger(this.getClass().getPackage().getName());

        this.boundingBox = new BoundingBox();
        this.regions = new ArrayList<PreppedCombination>();
        this.initialBox = new BoxNode();
        Matrix m = new Matrix(4, 4);
        this.db = db;
        this.topLevelObjects = new ArrayList<String>();
        this.combinations = new HashMap<String, PreppedCombination>();
        logger.info("Prepping " + db.getDbFileName() + "...");
        for (String obj : objs) {
            logger.info( "\tPrepping " + obj);
            this.topLevelObjects.add(obj);
            DbObject dbObject = null;
            try {
                dbObject = db.getInternal(obj);
                m.unit();
                PreppedObject po = dbObject.prep(null, this, m);
                BoundingBox bb = po.getBoundingBox();
                if (bb != null) {
                    this.boundingBox.extend(po.getBoundingBox());
                }
            } catch (DbException e) {
                logger.severe(e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                logger.severe(e.getMessage());
                e.printStackTrace();
            } catch (DbNameNotFoundException e) {
                logger.severe(e.getMessage());
                e.printStackTrace();
            }

        }

        //start cutting initialBox
        logger.info("Starting space partitioning..");
        long startTime = System.currentTimeMillis();
        this.spacePartition = this.cut(this.initialBox);
        logger.info("Completed space partitioning in " + (System.currentTimeMillis()-startTime) + "ms (Prep complete)");
//        AtomicInteger boxCount = new AtomicInteger(0);
//        plotBoxes(this.spacePartition, boxCount);
    }

    private void plotBoxes(Node n, AtomicInteger boxCount) {
        if (n instanceof BoxNode) {
            BoxNode bn = (BoxNode) n;
            String name = "box." + boxCount.incrementAndGet();
            System.out.println("in " + name + " rpp " +
                    bn.getBoundingBox().getMin().getX() + " " +
                    bn.getBoundingBox().getMax().getX() + " " +
                    bn.getBoundingBox().getMin().getY() + " " +
                    bn.getBoundingBox().getMax().getY() + " " +
                    bn.getBoundingBox().getMin().getZ() + " " +
                    bn.getBoundingBox().getMax().getZ());
        } else if (n instanceof CutNode) {
            CutNode cn = (CutNode) n;
            plotBoxes(cn.getGteCutValue(), boxCount);
            plotBoxes(cn.getLtCutValue(), boxCount);
        }
    }

    protected PreppedDb() {
    }

    private Node cut(BoxNode box) {
        logger.finest("Cutting BoxNode: " + box);
        if (box.size() < 15) {
            logger.finest("\tnode is small enough, do not cut");
            return box;
        }

        Point max = box.getBoundingBox().getMax();
        Point min = box.getBoundingBox().getMin();
        Vector3 diff = Vector3.minus(max, min);

        double x = Math.abs(diff.getX());
        double y = Math.abs(diff.getY());
        double z = Math.abs(diff.getZ());

        int cutAxis = -1;
        double cutValue = 0.0;

        // select largest dimension to cut
        String axis = null;
        if (x > y) {
            if (x > z) {
                // cut along X-axis
                cutAxis = 0;
                axis = "X";
            } else {
                // cut along Z-axis
                cutAxis = 2;
                axis = "Z";
            }
        } else {
            if (y > z) {
                // cut along Y-axis
                cutAxis = 1;
                axis = "Y";
            } else {
                // cut along Z-axis
                cutAxis = 2;
                axis = "Z";
            }
        }

        BoxNode lower = new BoxNode();
        BoxNode upper = new BoxNode();

        BoundingBox upperBB = new BoundingBox(box.getBoundingBox());
        BoundingBox lowerBB = new BoundingBox(box.getBoundingBox());
        if (max.get(cutAxis) - min.get(cutAxis) <= BoxNode.MIN_BOX_WIDTH) {
            logger.finest("Not cutting node because it is already small");
            return box;
        }
        cutValue = (max.get(cutAxis) + min.get(cutAxis)) / 2.0;

        logger.finest("Cutting node along the " + axis + " axis, at " + cutValue);
        upperBB.setMin(cutAxis, cutValue);
        lowerBB.setMax(cutAxis, cutValue);

        lower.setBoundingBox(lowerBB);
        upper.setBoundingBox(upperBB);

        lower.populate(box);
        upper.populate(box);

        if (lower.size() == box.size() && upper.size() == box.size()) {
            return box;
        }

        return new CutNode(cutAxis, cutValue, this.cut(lower), this.cut(upper));
    }

    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    public void addPreppedObjectToInitialBox(PreppedObject preppedObject) {
        this.initialBox.addPreppedObjectAndExtendBB(preppedObject);
        preppedObject.setIndex(this.preppedSolidCount++);
    }

    /**
     * Method addPreppedObjectPieceToInitialBox
     *
     * @param    pbp                 a  BotPiece
     *
     */
    public void addPreppedObjectPieceToInitialBox(PreppedObjectPiece pbp) {
        this.initialBox.addPreppedObjectPieceAndExtendBB(pbp);
        pbp.setIndex(this.preppedSolidCount++);
        this.pieces.add(pbp);
    }

    public List<PreppedObjectPiece> getPieces() {
        return this.pieces;
    }

    public void addPreppedRegion(PreppedCombination reg) {
        this.regions.add(this.preppedRegionCount, reg);
        reg.setIndex(this.preppedRegionCount++);
        this.combinations.put(reg.getName(), reg);
    }

    public void addPreppedCombination(PreppedCombination pc) {
        this.combinations.put(pc.getName(), pc);
    }

    public PreppedCombination getCombination(String name) {
        return this.combinations.get(name);
    }

    public BrlcadDb getDb() {
        return this.db;
    }

    public Segment shootBoundingBox(Ray ray, BoundingBox bb) {
        double rmin = Double.NEGATIVE_INFINITY;
        double rmax = Double.POSITIVE_INFINITY;

        double dist1;
        double dist2;
        double min;
        double max;
        int imax = -1;
        int imin = -1;

        for (int i = 0; i < 3; i++) {
            dist1 = (bb.getMin().get(i) - ray.getStart().get(i)) / ray.getDirection().get(i);
            dist2 = (bb.getMax().get(i) - ray.getStart().get(i)) / ray.getDirection().get(i);
            min = Math.min(dist1, dist2);
            max = Math.max(dist1, dist2);
            if (min > rmin) {
                imin = i;
                rmin = min;
            }
            if (max < rmax) {
                imax = i;
                rmax = max;
            }
        }

        if (rmin >= rmax || rmax < 0.0) {
            return null;
        }

        if (rmin < 0.0) {
            rmin = 0.0;
        }

        Point inHitPoint = new Point(ray.getStart());
        inHitPoint.join(rmin, ray.getDirection());
        Vector3 norm1 = new Vector3(0.0, 0.0, 0.0);
        norm1.set(imin, 1.0);
        if (norm1.dotProduct(ray.getDirection()) > 0.0) {
            norm1.negate();
        }
        Point outHitPoint = new Point(ray.getStart());
        outHitPoint.join(rmax, ray.getDirection());
        Hit inHit = new Hit(rmin, inHitPoint, norm1, imin, null, PreppedDb.DbBoundingBoxName);
        Vector3 norm2 = Vector3.negate(norm1);
        Hit outHit = new Hit(rmax, outHitPoint, norm2, imax, null, PreppedDb.DbBoundingBoxName);

        return new Segment(inHit, outHit);
    }

    public SortedSet<Partition> shootRay(Ray ray, OverlapHandler overlapHandler) {
        logger.finest("Shooting ray: " + ray);

        SortedSet<Partition> parts = new TreeSet<Partition>();
        ray.getDirection().normalize();


        // first intersect with model bounding box
        double[] hits = null;

        if (this.boundingBox != null) {
            if (this.boundingBox.getMin() != null && this.boundingBox.getMax() != null) {
                hits = this.boundingBox.isect2(ray);
            }
        }

        if (hits == null) {
            logger.finest("\tMissed BoundingBox");
            return parts;
        }

        double maxDist = hits[1];
        Point locator = new Point(ray.getStart());
        locator.join(hits[0] + BoxNode.MIN_BOX_WIDTH / 10.0, ray.getDirection());
        BitSet regbits = new BitSet(this.preppedRegionCount);
        BitSet solidBits = new BitSet(this.preppedSolidCount);
        RayData rayData = new RayData(locator, hits[0] + BoxNode.MIN_BOX_WIDTH / 10.0, solidBits, regbits, BrlcadDb.getTolerance(), ray);
        logger.finest("Pushing ray through space partitions");
        while (rayData.getDist() < maxDist) {
            this.spacePartition.shootRay(this, ray, rayData);
        }

        // make segments from hits on pieces
        rayData.makeSegs();

        for (int i = regbits.nextSetBit(0); i > -1; i = regbits.nextSetBit(i + 1)) {
            PreppedCombination region = this.regions.get(i);
            SortedSet<Partition> regParts = region.evaluate(region, rayData);
            if (regParts != null && regParts.size() > 0) {
                parts.addAll(regParts);
            }
        }

        parts = overlapHandler.handleOverlaps(parts, ray);

        return parts;
    }

    private void printNodes(Node n) {
        if (n == null) {
            return;
        }
        if (n instanceof CutNode) {
            CutNode c = (CutNode) n;
            printNodes(c.getLtCutValue());
            printNodes(c.getGteCutValue());
        } else {
            System.out.println(n.toString());
        }
    }
}

