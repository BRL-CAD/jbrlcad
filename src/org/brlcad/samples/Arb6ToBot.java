
package org.brlcad.samples;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.brlcad.geometry.Arb8;
import org.brlcad.geometry.BadGeometryException;
import org.brlcad.geometry.Bot;
import org.brlcad.geometry.BrlcadDb;
import org.brlcad.geometry.Combination;
import org.brlcad.geometry.DbException;
import org.brlcad.geometry.DbExternal;
import org.brlcad.geometry.DbNameNotFoundException;
import org.brlcad.geometry.DbObject;
import org.brlcad.geometry.Tree;
import org.brlcad.numerics.Point;
import org.brlcad.preppedGeometry.PreppedArb8;
import org.brlcad.preppedGeometry.PreppedArb8.ArbInfo;
import org.brlcad.utils.TreeState;
import org.brlcad.utils.TreeWalker;
import org.brlcad.utils.TreeWalkerDelegate;
import org.brlcad.utils.VertexTree;

/**
 * This class is an attempt to provide a utility for converting BRL-CAD models that are built entirely as
 * unions of ARB6 primitives to BOT primitives. Some of the assumptions that are made by this utility:
 * <ul>
 * <li>
 *    The ARB6 primitives are arranged to approximate a skin or a shell type structure, i.e., the
 * triangular faces of each ARB6 primitive represent the interior and exterior boundaries of a skin.
 * </li>
 * <li>
 *    The thickness of the skin is always less than the shortest edge of of any triangular ARB6 face.
 * </li>
 * <li>
 *    No Boolean operations other than union are used in the model.
 * </li>
 * </ul>
 * To run this utility, use the <b>Arb6ToBot</b> script in the <b>bin</b> directory of this distribution.
 * The syntax for the script is:
 * <pre>
 * Arb6ToBot [-a] tolerance outputFile file.g obj1 [obj2 obj3 ...]
 *            where '-a' specifies that the output file should be appropriate for use by asc2g
 *                   tolerance is the maximum distance (mm) bewteen ARB6 vertices that should be fused
 *                   outputFile is the name of the output file
 *                   file.g is the name of the input BRL-CAD database file
 *                   obj1, obj2, ... are combinations, in file.g, that contain regions that contain only Arb6 primitives
 * </pre>
 * For best results, the tolerance should be set to the largest value that will not lead to unwanted vertex fusing.
 * It must be less than the thickness of the thinnest ARB6, less than the shortest edge of any triangular face
 * of any ARB6, but larger than the distance between vertices of adjacent ARB6 primitives that should correspond to
 * a single vertex. Each execution of this utility will result in one BOT primitive that consists of all the
 * ARB6 primitives referenced by the specified input objects. If the <b>-a</b> option is specified, the output
 * will be suitable for use with <b>asc2g</b> (from the brlcad distribution), otherwise, the output will be suitable
 * for use with the <b>source</b> command in <b>mged</b>.
 * 
 * @author jra
 */
public class Arb6ToBot {
    private static final String usage = "Arb6ToBot [-a] tolerance outputFile file.g obj1 [obj2 obj3 ...] \n"
            + "\twhere \'-a\' specifies that the output file should be appropriate for use by asc2g\n"
            + "\t       tolerance is the maximum distance (mm) bewteen ARB6 vertices that should be fused\n"
            + "\t       outputFile is the name of the output file\n"
            + "\t       file.g is the name of theinput BRL-CAD database file\n"
            + "\t       obj1, obj2, ... are combinations, in file.g, that contain regions that contain only Arb6 primitives\n";
    private BrlcadDb brlcadDb = null;
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private List<Bot>bots = new ArrayList<Bot>();
    private FileWriter output = null;
    private boolean makeAsciiFile = false;
    private double toleranceDist;

    /**
     * Constructor
     * @param makeAscii If true, the output is suitable for use by asc2g, otherwise the header information is left out.
     * @param tol The maximum distance (mm) between two ARB6 vertices that should be fused
     * @param outFile The file to hold to output
     * @param dbFile The input BRL-CAD ".g" file
     */
    public Arb6ToBot(boolean makeAscii, double tol, String outFile, String dbFile) {
        this.makeAsciiFile = makeAscii;
        this.toleranceDist = tol;
        try {
            output = new FileWriter(outFile);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            System.err.println(usage);
            System.exit(1);
        }
        try {
            brlcadDb = new BrlcadDb(dbFile);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            System.err.println(usage);
            System.exit(1);
        }
    }

    /**
     * The main routine
     * @param args
     */
    public static void main(String[] args) {
        int arg = 0;
        boolean makeAscii = false;
        double tol;
        if (args.length < 4) {
            System.err.println(usage);
            System.exit(1);
        }
        if ( "-a".equals(args[arg])) {
            makeAscii = true;
            arg++;
        }

        tol = Double.parseDouble(args[arg]);
        arg++;
        Arb6ToBot atb = new Arb6ToBot(makeAscii, tol, args[arg], args[arg+1] );
        try {
            atb.toBots(Arrays.copyOfRange(args, arg+2, args.length));
        } catch (BadGeometryException ex) {
            Logger.getLogger(Arb6ToBot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DbNameNotFoundException ex) {
            Logger.getLogger(Arb6ToBot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Arb6ToBot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Arb6ToBot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DbException ex) {
            Logger.getLogger(Arb6ToBot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This is the primary entrance into the Arb6 to BoT method. Converts all the specified
     * objects into a single BoT, and writes the ascii BoT to the output file.
     * @param regions a list of Combinations to be processed
     * @throws IOException
     * @throws DbException
     * @throws DbNameNotFoundException
     * @throws BadGeometryException
     */
    private void toBots(String[] regions) throws IOException, DbException, DbNameNotFoundException, BadGeometryException {
        Combination[] combs = new Combination[regions.length];
        int combCount = 0;
        int errors = 0;
        System.err.println(  "processing " + regions.length + " regions:");
        for(String reg : regions) {
            System.err.println( "\t" + reg);
            DbObject dbo = this.brlcadDb.getInternal(reg);
            if (!(dbo instanceof Combination)) {
                System.err.println( reg + " is not a region");
                errors++;
            } else {
                combs[combCount++] = (Combination) dbo;
            }
        }
        if (errors > 0) {
            System.exit(errors);
        }
        if (this.makeAsciiFile) {
            output.write("title {" + this.brlcadDb.getTitle() + " BoT's (by Arb6ToBot)}\n");
            output.write("units mm\n");
            output.write("attr set {_GLOBAL}\n");
        }
        for (Combination c : combs) {
            List<DbObject> prims = removeDups(c);
            primsToBot(c,prims);
        }

        int ed = 0;
        for (Bot bot : bots) {
            bot.fuseVerts(this.toleranceDist);
            List<Edge> freeEdges = findFreeEdges(bot);
            closeFreeEdges(bot, freeEdges);
            output.write(bot.toTcl() + "\n");
        }
        output.close();
    }

    /**
     * Create a list of unique primitives that are referenced in the specified Combination
     *
     * @param c The Combination
     * @return a List of DbbObjects (primitives)
     * @throws IOException
     * @throws DbException
     * @throws DbNameNotFoundException
     */
    private List<DbObject> removeDups(Combination c) throws IOException, DbException, DbNameNotFoundException {
        Tree tree = c.getTree();
        List<DbObject> prims = getPrims(tree);

        Iterator<DbObject> iter = prims.iterator();
        while (iter.hasNext()) {
            DbObject obj = iter.next();
            Iterator<DbObject> iter2 = prims.iterator();
            while (iter2.hasNext()) {
                DbObject obj2 = iter2.next();
                if (obj.getName().equals(obj2.getName())) {
                    continue;
                }
                if (obj.equals(obj2)) {
                    iter.remove();
                    break;
                }
            }
        }
        return prims;
    }

    /**
     * Get a list of all the primitives that are referenced by the specified Tree
     * @param tree The Tree
     * @return a List of DbObjects (primitives)
     * @throws IOException
     * @throws DbException
     * @throws DbNameNotFoundException
     */
    private List<DbObject> getPrims(Tree tree) throws IOException, DbException, DbNameNotFoundException {
        ATBTreeState state = new ATBTreeState();
        ATBDelegate delegate = new ATBDelegate();
        TreeWalker tw = new TreeWalker(brlcadDb, state, delegate);
        tw.processTree(tree);
        return state.primitives;
    }

    /**
     * Produce BoTs from all the Arb6 primitives in the specified List
     * @param c The combination where these Arb6 primitives are from
     * @param prims The list of Arb6 primitives from the above Combination
     * @throws BadGeometryException
     * @throws DbException
     */
    private void primsToBot(Combination c, List<DbObject> prims) throws BadGeometryException, DbException {
        List<Face> faces = new ArrayList<Face>();
        ByteBuffer bb = ByteBuffer.allocate(24);
        VertexTree vTree = new VertexTree(toleranceDist);
        Point[] triangle1 = new Point[3];
        Point[] triangle2 = new Point[3];
        int faceCount = 0;
        for (DbObject obj : prims) {
            if (!(obj instanceof Arb8)) {
                System.err.println( obj.getName() + " is not an ARB6, ignoring");
                continue;
            }
            Arb8 arb8 = (Arb8) obj;
            int triCount = 0;
            for (ArbInfo ai : PreppedArb8.arbInfo) {
                Point[] pts = new Point[4];
                int ptCount = 0;
                for (int v : ai.getFaceVertices()) {
                    Point pt = arb8.getVertex(v);
                    if (ptCount == 0) {
                        pts[ptCount++] = pt;
                    } else {
                        boolean dup = false;
                        for (int j=0 ; j<ptCount ; j++) {
                            if (pts[j].isEqual(pt)) {
                                dup = true;
                                break;
                            }
                        }
                        if (!dup) {
                            pts[ptCount++] = pt;
                        }
                    }
                }
                if (ptCount == 3) {
                    if (triCount == 1) {
                        Face face = new Face();
                        for (int i=0 ; i<3 ; i++) {
                            triangle2[i] = pts[i];
                            int v = vTree.addVert(triangle2[i]);
                            face.v[i] = v;
                        }
                        faces.add(face);
                    } else if (triCount == 0) {
                        Face face = new Face();
                        for (int i=0 ; i<3 ; i++) {
                            triangle1[i] = pts[i];
                            int v = vTree.addVert(triangle1[i]);
                            face.v[i] = v;
                        }
                        faces.add(face);
                    } else {
                        System.err.println(arb8.getName() + " has too many triangular faces, ignoring extras");
                    }
                    triCount++;
                    faceCount++;
                }
            }
        }
        baos.reset();
        ArrayList<Point> pts = vTree.getThePoints();
        bb.putInt(pts.size());
        baos.write(bb.array(), 0, 4);
        bb.clear();
        bb.putInt(faces.size());
        baos.write(bb.array(), 0, 4);
        bb.clear();
        baos.write(1); // unoriented
        baos.write(2); // volume mode
        baos.write(0); // flags
        for (int i=0 ; i<pts.size() ; i++) {
            Point pt = pts.get(i);
            bb.clear();
            bb.putDouble(pt.getX());
            bb.putDouble(pt.getY());
            bb.putDouble(pt.getZ());
            baos.write(bb.array(), 0, 3*8);
        }
        for (Face f : faces) {
            bb.clear();
            for (int i=0 ; i<3 ; i++) {
                bb.putInt(f.v[i]);
            }
            baos.write(bb.array(), 0, 3*4);
        }
        BotExternal be = new BotExternal(c.getName(), baos.toByteArray());
        Bot bot = new Bot(be);
        bots.add(bot);
    }

    /**
     * Find all the free edges (edges that appear only once) in the specified BoT
     * @param bot The Bot to be processed
     * @return a List of Edges where each edge on the list appears only once in the specified BoT
     */
    private List<Edge> findFreeEdges(Bot bot) {
        List<Edge> freeEdges = new ArrayList<Edge>();
        int totalEdges = 0;

        for (int fNo1=0 ; fNo1<bot.getFaceCount() ; fNo1++) {
            totalEdges += 3;
            org.brlcad.geometry.Face f1 = bot.getFace(fNo1);
            boolean e01free = true;
            boolean e12free = true;
            boolean e20free = true;
            for (int fNo2=0; fNo2 < bot.getFaceCount(); fNo2++) {
                if (fNo2 == fNo1) {
                    continue;
                }
                org.brlcad.geometry.Face f2 = bot.getFace(fNo2);
                if (e01free) {
                    if (faceHasEdge(f2, f1.v[0], f1.v[1])) {
                        e01free = false;
                    } 
                }
                if (e12free) {
                    if (faceHasEdge(f2, f1.v[1], f1.v[2])) {
                        e12free = false;
                    }
                }
                if (e20free) {
                    if (faceHasEdge(f2, f1.v[2], f1.v[0])) {
                        e20free = false;
                    }
                }
                if (!e01free && !e12free && !e20free) {
                    break;
                }
            }
            if (e01free) {
                freeEdges.add(new Edge(f1.v[0], f1.v[1]));
            }
            if (e12free) {
                freeEdges.add(new Edge(f1.v[1], f1.v[2]));
            }
            if (e20free) {
                freeEdges.add(new Edge(f1.v[2], f1.v[0]));
            }
        }
        return freeEdges;
    }

    /**
     * Determine if the specified face has an edge from vertex v1 to v2 (or v2 to v1)
     * @param f The Face
     * @param v1 vertex at one edge of the edge
     * @param v2 vertex at the other end of the edge
     * @return true, if the Face contains the edge, false otherwise
     */
    public boolean faceHasEdge( org.brlcad.geometry.Face f, int v1, int v2) {
        if ( f.v[0] == v1 && f.v[1] == v2) {
            return true;
        }
        if (f.v[1] == v1 && f.v[0] == v2) {
            return true;
        }

        if ( f.v[1] == v1 && f.v[2] == v2) {
            return true;
        }
        if (f.v[2] == v1 && f.v[1] == v2) {
            return true;
        }

        if ( f.v[2] == v1 && f.v[0] == v2) {
            return true;
        }
        if (f.v[0] == v1 && f.v[2] == v2) {
            return true;
        }

        return false;
    }

    /**
     * Add faces to the specified BoT to close the spaces between free edges
     * @param bot The BoT
     * @param freeEdges a List of free Edges in the above BoT
     * @throws BadGeometryException
     */
    private void closeFreeEdges(Bot bot, List<Edge> freeEdges) throws BadGeometryException {
        while (freeEdges.size()>1) {
            Edge e1 = freeEdges.get(0);
            Edge e2 = findNearestEdge( bot, freeEdges, e1);
            addFaces(bot, e1, e2);
            freeEdges.remove(e1);
            freeEdges.remove(e2);
        }
    }

    /**
     * Find the free Edge that is nearest the specified Edge
     * @param bot The BoT being processed
     * @param freeEdges A list of free Edges in the above BoT
     * @param e A free edge (from the above list)
     * @return a free Edge from the specified list that is closest to the specified Edge
     */
    private Edge findNearestEdge(Bot bot, List<Edge> freeEdges, Edge e) {
        Point v1 = bot.getVertex(e.v1);
        Point v2 = bot.getVertex(e.v2);
        double dist = Double.MAX_VALUE;
        Edge nearest = null;
        for (Edge edge : freeEdges) {
            if (edge == e) {
                continue;
            }
            Point v3 = bot.getVertex(edge.v1);
            Point v4 = bot.getVertex(edge.v2);
            double dist1 = Math.min(v1.dist(v3), v1.dist(v4));
            double dist2 = Math.min(v2.dist(v3), v2.dist(v4));
            double tmp = dist1 + dist2;
            if (tmp < dist) {
                dist = tmp;
                nearest = edge;
            }
        }
        return nearest;
    }

    /**
     * Add faces to the specified BoT to close the area between two free Edges
     * @param bot The BoT being processed
     * @param e1 one free Edge
     * @param e2 another free Edge
     * @throws BadGeometryException
     */
    private void addFaces(Bot bot, Edge e1, Edge e2) throws BadGeometryException {
        bot.addFace(e1.v1, e1.v2, e2.v1);
        bot.addFace(e2.v1, e2.v2, e1.v1);
    }

    /**
     * Tree state class used in the TreeWalker to find primitives referenced in a Tree
     */
    public class ATBTreeState implements TreeState {
        private List<DbObject> primitives = new ArrayList<DbObject>();

        public void addPrimitive(DbObject prim) {
            this.primitives.add(prim);
        }

        public List<DbObject> getPrimitives() {
            return this.primitives;
        }
    }

    /**
     * TreeWalkerDelegate for finding primitives referenced in a Tree
     */
    public class ATBDelegate implements TreeWalkerDelegate {

        public void processLeaf(DbObject leaf, TreeState state) {

            ((ATBTreeState)state).addPrimitive(leaf);
        }

    }

    /**
     * Simple class representing a triangular face (three vertices)
     */
    private class Face {
        public int[] v = new int[3];
    }

    /**
     * Simple class to represent an Edge (two vertices)
     */
    private class Edge {
        public int v1;
        public int v2;

        private Edge(int i1, int i2) {
            v1 = i1;
            v2 = i2;
        }
    }

    /**
     * Private class to hold the external representation of a BoT
     */
    private class BotExternal implements DbExternal {
        private String name;
        private byte majorType;
        private byte minorType;
        private byte[] body;

        public BotExternal (String name, byte[] body) {
            this.name = name;
            this.body = body;
            this.majorType = Bot.majorType;
            this.minorType = Bot.minorType;
        }

        public String getName() {
            return name;
        }

        public byte getMajorType() {
            return majorType;
        }

        public byte getMinorType() {
            return minorType;
        }

        public byte[] getBody() {
            return body;
        }

        public byte[] getAttributes() {
            return null;
        }
    }
}
