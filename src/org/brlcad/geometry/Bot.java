package org.brlcad.geometry;

/**
 * A Bot (Bag of triangles) object. Based on the BRL-CAD BOT
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;

import java.util.List;
import org.brlcad.preppedGeometry.PreppedBot;
import org.brlcad.preppedGeometry.PreppedCombination;
import org.brlcad.preppedGeometry.PreppedTriangle;

import org.brlcad.numerics.Matrix;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Vector3;
import org.brlcad.spacePartition.PreppedDb;
import org.brlcad.utils.VertexTree;

public class Bot extends DbObject {

    private Orientation orientation;
    private Mode mode;
    private byte flags;
    // Array of vertices used in this Bot
    private Point[] points;
    // Array of faces in this Bot (each face has indices into the above array of vertices)
    private Face[] faces;
    // Mode for each face, only used for PLATE or PLATE_NOCOS mode, if bit is set for
    // a face, then its thickness is appended to a shotline at the hit point. Otherwise,
    // the thicknes is centered about the shotline.
    private BitSet faceMode;
    // Thickness of each face (only used for PLATE or PLATE_NOCOS mode)
    private double[] thickness;
    // Array of vertex normals (optional)
    private Vector3[] normals;
    // Array of face normals (each face has indices into the normals array, one-to-one
    // correspondence with faces array)
    private Face[] faceNormals;
    // the major and minor types for a Bot
    public static final byte majorType = 1;
    public static final byte minorType = 30;

    /**
     * The possible orientations for the faces of a Bot
     *
     */
    public enum Orientation {

        UNORIENTED("unoriented", "no"),
        COUNTER_CLOCKWISE("counter-clockwise", "rh"),
        CLOCKWISE("clockwise", "lh");
        private final String humanReadable;
        private final String tclString;

        Orientation(String hrs, String tcl) {
            this.humanReadable = hrs;
            this.tclString = tcl;
        }

        @Override
        public String toString() {
            return this.humanReadable;
        }

        public String toTcl() {
            return this.tclString;
        }
    }

    /**
     * The possible modes for a Bot
     *
     */
    public enum Mode {

        SURFACE("Surface", "surf"),
        SOLID("Solid", "volume"),
        PLATE("Plate", "plate"),
        PLATE_NOCOS("Plate (Defined LOS)", "plate_nocos");
        private final String humanReadable;
        private final String tclString;

        Mode(String hrs, String tcl) {
            this.humanReadable = hrs;
            this.tclString = tcl;
        }

        @Override
        public String toString() {
            return this.humanReadable;
        }

        public String toTcl() {
            return this.tclString;
        }
    }
    // flags
    public static final byte HAS_SURFACE_NORMALS = 0x1;
    public static final byte USE_SURFACE_NORMALS = 0x2;
    public static final byte USE_FLOATS = 0x4;

    /**
     * Constructor - builds a Bot from the body of a dbExt
     * @param dbExt
     * @throws DbException
     */
    public Bot(DbExternal dbExt) throws DbException {
        super(dbExt);

        if (dbExt.getMajorType() != Bot.majorType || dbExt.getMinorType() != Bot.minorType) {
            throw new DbException("Attempted to import a BOT, but external is " +
                    " major type: " + dbExt.getMajorType() +
                    " minor type: " + dbExt.getMinorType());
        }

        // get the body bytes from the DbExternal object
        byte[] body = dbExt.getBody();

        int pointer = 0;

        int numVertices = (int) BrlcadDb.getLong(body, pointer, 4);
        pointer += 4;
        int numFaces = (int) BrlcadDb.getLong(body, pointer, 4);
        pointer += 4;
        int tmp = body[pointer++];
        switch (tmp) {
            case 1:
                this.orientation = Bot.Orientation.UNORIENTED;
                break;
            case 2:
                this.orientation = Bot.Orientation.COUNTER_CLOCKWISE;
                break;
            case 3:
                this.orientation = Bot.Orientation.CLOCKWISE;
                break;
            default:
                throw new DbException("Bot has invalid orientation: " + tmp);
        }

        tmp = body[pointer++];
        switch (tmp) {
            case 1:
                this.mode = Mode.SURFACE;
                break;
            case 2:
                this.mode = Mode.SOLID;
                break;
            case 3:
                this.mode = Mode.PLATE;
                break;
            case 4:
                this.mode = Mode.PLATE_NOCOS;
                break;
            default:
                throw new DbException("Bot has invalid mode: " + tmp);
        }

        this.flags = body[pointer++];
        this.points = new Point[numVertices];
        for (int i = 0; i < numVertices; i++) {
            double x = BrlcadDb.getDouble(body, pointer);
            pointer += 8;
            double y = BrlcadDb.getDouble(body, pointer);
            pointer += 8;
            double z = BrlcadDb.getDouble(body, pointer);
            pointer += 8;
            points[i] = new Point(x, y, z);
        }
        this.faces = new Face[numFaces];
        for (int i = 0; i < numFaces; i++) {
            int v1 = (int) BrlcadDb.getLong(body, pointer, 4);
            pointer += 4;
            int v2 = (int) BrlcadDb.getLong(body, pointer, 4);
            pointer += 4;
            int v3 = (int) BrlcadDb.getLong(body, pointer, 4);
            pointer += 4;
            faces[i] = new Face(v1, v2, v3);
        }

        if (this.mode == Bot.Mode.PLATE || this.mode == Bot.Mode.PLATE_NOCOS) {
            this.thickness = new double[numFaces];
            for (int i = 0; i < numFaces; i++) {
                this.thickness[i] = BrlcadDb.getDouble(body, pointer);
                pointer += 8;
            }

            this.faceMode = BrlcadDb.getBitSet(body, pointer);
            if (this.faceMode == null) {
                throw new DbException("Bad face mode bit vector in database file for " +
                        this.getName());
            }
            pointer += this.faceMode.size() / 8;
        }

        if ((this.flags & Bot.HAS_SURFACE_NORMALS) != 0) {
            int numNormals = (int) BrlcadDb.getLong(body, pointer, 4);
            pointer += 4;
            int numFaceNormals = (int) BrlcadDb.getLong(body, pointer, 4);
            pointer += 4;

            if (numNormals > 0) {
                this.normals = new Vector3[numNormals];
                for (int i = 0; i < numNormals; i++) {
                    double x = BrlcadDb.getDouble(body, pointer);
                    pointer += 8;
                    double y = BrlcadDb.getDouble(body, pointer);
                    pointer += 8;
                    double z = BrlcadDb.getDouble(body, pointer);
                    pointer += 8;
                    normals[i] = new Vector3(x, y, z);
                }
            }
            if (numFaceNormals > 0) {
                this.faceNormals = new Face[numFaceNormals];
                for (int i = 0; i < numFaceNormals; i++) {
                    int v1 = (int) BrlcadDb.getLong(body, pointer, 4);
                    pointer += 4;
                    int v2 = (int) BrlcadDb.getLong(body, pointer, 4);
                    pointer += 4;
                    int v3 = (int) BrlcadDb.getLong(body, pointer, 4);
                    pointer += 4;
                    this.faceNormals[i] = new Face(v1, v2, v3);
                }
            }
        }
    }

    private Bot(Ars ars, DbExternal dbExt) {
        super(dbExt);

        this.mode = Mode.SOLID;
        this.orientation = Orientation.UNORIENTED;
        this.flags = 0;

        // create the vertices
        VertexTree vTree = new VertexTree(BrlcadDb.tolerance.getDist());
        Point[][] curves = ars.getCurves();

        int[][] pts = new int[curves.length][];
        for (int curve=0 ; curve < curves.length ; curve++) {
            pts[curve] = new int[curves[curve].length];
            for (int point=0 ; point < curves[curve].length ; point++) {
                pts[curve][point] = vTree.addVert(curves[curve][point]);
            }
        }
        this.points = vTree.getThePoints().toArray(new Point[0]);

        // now build the faces
        ArrayList<Face> faceList = new ArrayList<Face>();
        for (int curve=0 ; curve < curves.length-1 ; curve++) {
            for (int point=0 ; point < curves[curve].length-1 ; point++) {
                int v1 = pts[curve][point];
                int v2 = pts[curve][point+1];
                int v3 = pts[curve+1][point];
                int v4 = pts[curve+1][point+1];

                if (v1 != v3 && v1 != v4 && v3 != v4) {
                    faceList.add(new Face(v1, v3, v4));
                }
                if (v1 != v2 && v1 != v4 && v2 != v4) {
                    faceList.add(new Face(v1, v2, v4));
                }
            }
        }
        this.faces = faceList.toArray(new Face[0]);

    }

    public void addFace(int v1, int v2, int v3) throws BadGeometryException {
        if (v1 >= points.length || v2 >= points.length || v3 >= points.length) {
            throw new BadGeometryException("Illegal vertex index (" + v1 + "," + v2 + "," + v3 + "), must be " + (points.length-1) + " or less");
        }
        faces = Arrays.copyOf(faces, faces.length+1);
        faces[faces.length-1] = new Face(v1, v2, v3);
    }

    public Point getVertex(int i) {
        return this.points[i];
    }

    public Face getFace(int i) {
        return this.faces[i];
    }

    public boolean hasVertexNormals() {
        return (this.flags & Bot.HAS_SURFACE_NORMALS) != 0;
    }

    public boolean useVertexNormals() {
        return (this.flags & Bot.USE_SURFACE_NORMALS) != 0;
    }

    public Vector3 getVertexNormal(int faceNumber, int vertexNumber) {
        return this.normals[this.faceNormals[faceNumber].v[vertexNumber]];
    }

    public boolean isCCW() {
        return this.orientation == Bot.Orientation.COUNTER_CLOCKWISE;
    }

    public boolean isCW() {
        return this.orientation == Bot.Orientation.CLOCKWISE;
    }

    public boolean isUnOriented() {
        return this.orientation == Bot.Orientation.UNORIENTED;
    }

    public int getFaceCount() {
        return this.faces.length;
    }

    public boolean isSurface() {
        return this.mode == Bot.Mode.SURFACE;
    }

    public boolean isPlate() {
        return this.mode == Bot.Mode.PLATE;
    }

    public boolean isPlateNoCos() {
        return this.mode == Bot.Mode.PLATE_NOCOS;
    }

    public double getFaceThickness(int faceNumber) {
        return this.thickness[faceNumber];
    }

    public boolean isFaceThicknessAppendedtoHit(int faceNumber) {
        return this.faceMode.get(faceNumber);
    }

    public String getOrientationString() {
        return this.orientation.toString();
    }

    public String getModeString() {
        return this.mode.toString();
    }

    public String getFlagsString() {
        StringBuffer sb = new StringBuffer();

        if ((this.flags & Bot.HAS_SURFACE_NORMALS) != 0) {
            sb.append("has surface normals");
        }
        if ((this.flags & Bot.USE_SURFACE_NORMALS) != 0) {
            if (sb.length() > 0) {
                sb.append(" and use surface normals");
            } else {
                sb.append("use surface normals");
            }
        }
        if ((this.flags & Bot.USE_FLOATS) != 0) {
            if (sb.length() > 0) {
                sb.append(" and use floats in prepped version");
            } else {
                sb.append("use floats in prepped version");
            }
        }

        return sb.toString();
    }

    public static Bot fromArs(Ars ars, DbExternal dbExt) {
        return new Bot (ars, dbExt);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getName() + " BOT:\n");
        sb.append("Orientation: " + this.getOrientationString());
        sb.append(", mode: " + this.getModeString());
        sb.append(", flags: " + this.getFlagsString() + "\n");
        sb.append(this.points.length + " vertices, " + this.faces.length + " faces");

        sb.append("\nPoints:\n");
        for (int i = 0; i < this.points.length; i++) {
            sb.append("\t" + i + " " + this.points[i] + "\n");
        }

        sb.append("Faces:\n");
        for (int i = 0; i < this.faces.length; i++) {
            sb.append("\t" + i + " " + this.faces[i] + "\n");
        }

        if (this.mode == Bot.Mode.PLATE || this.mode == Bot.Mode.PLATE_NOCOS) {
            sb.append("Face thickness:\n");
            for (int i = 0; i < this.thickness.length; i++) {
                sb.append("\t" + i + " " + this.thickness[i] + "\n");
            }
            sb.append("Face mode: " + this.faceMode + "\n");
        }

        if ((this.flags & Bot.HAS_SURFACE_NORMALS) != 0) {
            sb.append("Normals:\n");
            for (int i = 0; i < this.normals.length; i++) {
                sb.append("\t" + i + " " + this.normals[i] + "\n");
            }
        }

        return sb.toString();
    }

    /**
     * Prep this Bot
     * @param reg	The region that this Bot belongs to (may be null)
     * @param preppedDb	The PreppedDb that this Bot belongs to
     * @param matrix	The transformation matrix to apply to this Bot
     * @return A PreppedBot
     */
    public PreppedBot prep(PreppedCombination reg, PreppedDb preppedDb, Matrix matrix) {
        PreppedBot prepped = new PreppedBot(this, matrix);
        if (prepped.getTriangleCount() >= BrlcadDb.BOT_MINFACES) {
            boolean newWay = true;
            if (newWay) {
                BotPiece pbp = new BotPiece(this.name, prepped);
                pbp.addTriangles(prepped.getTriangles());
                pbp.subDivide(preppedDb);
            } else {
                // use pieces
                BotPiece pbp = null;
                int triCount = 0;
                Iterator<PreppedTriangle> iter = prepped.getTriangles().iterator();

                while (iter.hasNext()) {
                    if (triCount == 0) {
                        if (pbp != null) {
                            preppedDb.addPreppedObjectPieceToInitialBox(pbp);
                            pbp = null;
                        }
                        pbp = new BotPiece(this.name, prepped);
                    }
                    PreppedTriangle pt = iter.next();
                    pbp.addTriangle(pt);
                    if (triCount++ >= BrlcadDb.BOT_FACES_PER_PIECE) {
                        triCount = 0;
                    }
                }

                if (pbp != null) {
                    preppedDb.addPreppedObjectPieceToInitialBox(pbp);
                }
            }
        } else {
            preppedDb.addPreppedObjectToInitialBox(prepped);
        }

        if (reg != null) {
            // add the region to this objects list of regions
            prepped.addRegion(reg);
        }
        return prepped;
    }

    /**
     * Produce a Tcl string representation of this BoT
     * @return a String containing the Tcl representation of this BoT
     */
    public String toTcl() {
        StringBuilder sb = new StringBuilder();
        sb.append("put {").append(getName()).
                append("} bot mode ").append(mode.toTcl()).
                append(" orient ").append(orientation.toTcl()).
                append(" flags { ");
        if ((flags & Bot.HAS_SURFACE_NORMALS) != 0) {
            sb.append("has_normals ");
        }
        if ((flags & Bot.USE_FLOATS) != 0) {
            sb.append("use_floats ");
        }
        if ((flags & Bot.USE_SURFACE_NORMALS) != 0) {
            sb.append("use_normals ");
        }
        sb.append("} V { ");

        for (Point pt : points) {
            sb.append(" { ").append(pt.getX()).append(" ").append(pt.getY()).append(" ").append(pt.getZ()).append("}");
        }
        sb.append(" } F { ");

        for (Face f : faces) {
            sb.append(" { ").append(f.v[0]).append(" ").append(f.v[1]).append(" ").append(f.v[2]).append(" }");
        }
        sb.append(" }");

        if (mode.equals(Mode.PLATE) || mode.equals(Mode.PLATE_NOCOS)) {
            sb.append(" T {");
            for (int face=0 ; face<thickness.length ; face++) {
                sb.append(" ").append(thickness[face]);
            }
            sb.append(" }");
        }

        if ((flags & Bot.HAS_SURFACE_NORMALS) != 0 ) {
            sb.append(" N { ");
            for( int v=0 ; v<normals.length ; v++) {
                Vector3 norm = normals[v];
                sb.append(" {").append(norm.getX()).append(" ").append(norm.getY()).append(" ").append(norm.getZ()).append(" }");
            }
            for( int fn=0 ; fn<faceNormals.length ; fn++) {
                Face f = faceNormals[fn];
                sb.append(" {").append(f.v[0]).append(" ").append(f.v[1]).append(" ").append(f.v[2]).append(" }");
            }
            sb.append(" }");
        }
        return sb.toString();
    }

    /**
     * Fuse all vertices that are within the specified distance of each other.
     * The remaining vertex, that represents the fusion of a group of two or more
     * vertices, will be translated to the average of the group of vertices.
     * @param dist Any two vertices that are this distance or less apart will be fused.
     * @return The number of vertices that have been deleted (fused).
     */
    public int fuseVerts(double dist) {
        int delCount = 0;
        Point remove = new Point(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        List<Point> pts = new ArrayList<Point>();
        for (Point p : points) {
            pts.add(p);
        }
        int v1 = 0;
        while (v1 < pts.size()-1) {
            Point p1 = pts.get(v1);
            Fusion fuse = new Fusion(p1, v1);
            int v2 = v1+1;
            while (v2 < pts.size()) {
                Point p2 = pts.get(v2);
                if (fuse.center.dist(p2) <= dist) {
                    fuse.addPoint(p2, v2);
                }
                v2++;
            }
            if (fuse.hasDups) {
                pts.set(v1, fuse.center);
                // fuse v1 and v2, replace references to v2 with v1
                // decrease references that are graeter than v2 by one
                int v2Index = fuse.equivs.size()-1;
                while (v2Index >= 0) {
                    v2 = fuse.equivs.get(v2Index);
                    for (Face f : faces) {
                        for (int i = 0; i < 3; i++) {
                            if (f.v[i] == v2) {
                                f.v[i] = v1;
                            } else if (f.v[i] > v2) {
                                f.v[i]--;
                            }
                        }
                    }
                    // mark v2 for removal from the points array
                    pts.set(v2, remove);
                    v2Index--;
                }
                Iterator<Point> iterPt = pts.iterator();
                while (iterPt.hasNext()) {
                    Point pt = iterPt.next();
                    if (pt == remove) {
                        delCount++;
                        iterPt.remove();
                    }
                }
            }
            v1++;
        }
        int deleted = points.length - pts.size();

        // replace the points array
        points = new Point[pts.size()];
        for (int v=0 ; v<pts.size() ; v++) {
            points[v] = pts.get(v);
        }

        return deleted;
    }

    /**
     * A class used to hold a group of vertices that are elegible to be fused into one vertex.
     */
    private class Fusion {
        public Point center;
        public int initial;
        public ArrayList<Integer> equivs;
        public boolean hasDups;

        public Fusion(Point pt, int vertNo) {
            this.center = new Point(pt);
            this.initial = vertNo;
            this.equivs = new ArrayList<Integer>();
            hasDups = false;
        }

        public void addPoint(Point pt, int vertNo) {
            if (vertNo == initial || equivs.contains(vertNo)) {
                return;
            }
            center.scale(equivs.size()+1.0);
            center.plus(pt);
            equivs.add(vertNo);
            center.scale(1.0/(equivs.size()+1.0));
            hasDups = true;
        }
    }
}

