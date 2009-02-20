package org.brlcad.geometry;

/**
 * A Bot (Bag of triangles) object. Based on the BRL-CAD BOT
 */
import java.util.BitSet;
import java.util.Iterator;

import org.brlcad.preppedGeometry.PreppedBot;
import org.brlcad.preppedGeometry.PreppedCombination;
import org.brlcad.preppedGeometry.PreppedTriangle;

import org.brlcad.numerics.Matrix;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Vector3;
import org.brlcad.spacePartition.PreppedDb;

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

        UNORIENTED("unoriented"),
        COUNTER_CLOCKWISE("counter-clockwise"),
        CLOCKWISE("clockwise");
        private final String humanReadable;

        Orientation(String hrs) {
            this.humanReadable = hrs;
        }

        @Override
        public String toString() {
            return this.humanReadable;
        }
    }

    /**
     * The possible modes for a Bot
     *
     */
    public enum Mode {

        SURFACE("Surface"),
        SOLID("Solid"),
        PLATE("Plate"),
        PLATE_NOCOS("Plate (Defined LOS)");
        private final String humanReadable;

        Mode(String hrs) {
            this.humanReadable = hrs;
        }

        @Override
        public String toString() {
            return this.humanReadable;
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
            throw new DbException("Attempted to import an Arb8, but external is " +
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
}

