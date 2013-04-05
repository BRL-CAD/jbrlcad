/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brlcad.samples;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import org.brlcad.geometry.BadGeometryException;
import org.brlcad.geometry.BrlcadDb;
import org.brlcad.geometry.DbAttributeOnly;
import org.brlcad.geometry.DbException;
import org.brlcad.geometry.DbNameNotFoundException;
import org.brlcad.geometry.DbObject;
import org.brlcad.geometry.OverlapHandler;
import org.brlcad.geometry.Partition;
import org.brlcad.geometry.SimpleOverlapHandler;
import org.brlcad.numerics.BoundingBox;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Ray;
import org.brlcad.numerics.Vector3;
import org.brlcad.shading.ColorTable;
import org.brlcad.shading.Light;
import org.brlcad.shading.Material;
import org.brlcad.shading.Shader;
import org.brlcad.shading.Phong;
import org.brlcad.spacePartition.PreppedDb;
import org.jscience.physics.amount.Amount;

/**
 *
 * @author jra
 */
public class Rt {

    private static final String usage = "Usage: Rt [-R] [-b # #] [-s size] [-a azimuth] [-e elevation] [-o output_file] [-F frame_buffer_port] dbfile.g object1 [object2 object3 ...]";

    @SuppressWarnings("empty-statement")
    public static void main(String[] args) {
        int cpus =  Runtime.getRuntime().availableProcessors();
        int size = 512;
        Amount<Angle> az = Amount.valueOf(35, NonSI.DEGREE_ANGLE);
        Amount<Angle> el = Amount.valueOf(25, NonSI.DEGREE_ANGLE);
        String outputFileName = null;
        String dbFileName = null;
        String[] tlos = null;
        int fbPort = -1;
        boolean reportOverlaps = true;

        if (args.length < 2) {
            System.err.println(usage);
            return;
        }

        boolean endOfOptions = false;
        int argNo = 0;
        int objNo = -1;
        Integer xPixelNo = -1;
        Integer yPixelNo = -1;
        while (argNo < args.length) {
            String arg = args[argNo];
            if (!endOfOptions) {
                if ("-s".equals(arg)) {
                    argNo++;
                    size = Integer.valueOf(args[argNo]);
                } else if ("-o".equals(arg)) {
                    argNo++;
                    outputFileName = args[argNo];
                } else if( "-F".equals(arg)) {
                    argNo++;
                    fbPort = Integer.valueOf(args[argNo]) + 5559;
                } else if( "-a".equals(arg)) {
                    argNo++;
                    az = Amount.valueOf(Double.valueOf(args[argNo]), NonSI.DEGREE_ANGLE);
                } else if( "-e".equals(arg)) {
                    argNo++;
                    el = Amount.valueOf(Double.valueOf(args[argNo]), NonSI.DEGREE_ANGLE);
                } else if( "-r".equals(arg)) {
                    reportOverlaps = true;
                } else if( "-R".equals(arg)) {
                    reportOverlaps = false;
                } else if( "-b".equals(arg)) {
                    argNo++;
                    xPixelNo = Integer.valueOf(args[argNo]);
                    argNo++;
                    yPixelNo = Integer.valueOf(args[argNo]);
                } else {
                    endOfOptions = true;
                    continue;
                }
            } else {
                if( objNo == -1 ) {
                    dbFileName = arg;
                    System.out.println( "dbFileName = " + dbFileName);
                    objNo++;
                    tlos = new String[args.length - argNo - 1];
                } else {
                    tlos[objNo] = arg;
                    objNo++;
                }
            }
            argNo++;
        }
        if( fbPort < 0 && outputFileName == null ) {
            outputFileName = "out.pix";
        }
        Vector3 rayDir = Vector3.fromAzimuthAndElevation(az, el);
        Vector3 xDir = null;
        Vector3 yDir = null;
        FileOutputStream outputFile = null;
        OutputStream fbOs = null;
        BrlcadDb brlcadDb = null;
        PreppedDb prepped = null;
        Socket sock = null;
        try {
            if( fbPort > 0 ) {
                    sock = new Socket((String) null, fbPort);
                    fbOs = sock.getOutputStream();
            }
            if( outputFileName != null ) {
                outputFile = new FileOutputStream(outputFileName);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Rt.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Rt.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Rt.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            brlcadDb = new BrlcadDb(dbFileName);
			prepped = new PreppedDb( brlcadDb, tlos );
            BoundingBox bb = prepped.getBoundingBox();
            if (bb == null) {
                System.err.println( "Bounding Box is null (perhaps the object you specified is empty)");
                System.exit(1);
            }

            if (bb.getMin() == null || bb.getMax() == null) {
                System.err.println( "Bounding Box is null (perhaps the object you specified is empty)");
                System.exit(1);
            }
            Vector3 tmp = new Vector3(0, 0, 1);
            xDir = rayDir.crossProduct(tmp);;
            xDir.normalize();
            yDir = xDir.crossProduct(rayDir);
            yDir.normalize();
            Point center = new Point(bb.getMin());
            center.plus(bb.getMax());
            center.scale(0.5);
            Point gridCenter = new Point(center);
            double radius = bb.getDiameter().magnitude() / 2.0;
            gridCenter.join(-radius, rayDir);
            Point gridStart = new Point( gridCenter );
            gridStart.join(-radius, yDir);
            gridStart.join(-radius, xDir);
            double delta = radius * 2.0 / size;
            Set<Light> lights = new HashSet<Light>();
            Light light = new Light( gridCenter, new Color(255, 255, 255), new Color(255, 255, 255) );
            lights.add(light);
            Color backGround = new Color( 200, 200, 200 );

            // get colortable from _GLOBAL
            DbObject dbo = brlcadDb.getInternal("_GLOBAL");
            ColorTable colorTable = null;
            if( dbo instanceof DbAttributeOnly ) {
                DbAttributeOnly global = (DbAttributeOnly) dbo;
                String colortab = global.getAttribute(BrlcadDb.COLOR_TABLE_KEY);
                if( colortab != null ) {
                    colorTable = new ColorTable(colortab);
                }
            }
            OverlapHandler oh = new SimpleOverlapHandler();
            oh.setQuiet(!reportOverlaps);
            ByteBuffer buffer = null;
            if( outputFile != null ) {
                buffer = ByteBuffer.allocate(3 * size * size);
            }
            if( xPixelNo != -1 && yPixelNo != -1 ) {
                // just do one pixel
                Point start = new Point(gridStart);
                start.join(delta * xPixelNo, xDir);
                start.join(delta * yPixelNo, yDir);
                Ray ray = new Ray(start, rayDir);
                Shader shader = new Phong(lights);
//                Shader shader = new NormalShader();
                SortedSet<Partition> parts = prepped.shootRay(ray, oh);
                Color color = null;
                if (parts.size() > 0) {
                    Partition first = parts.first();
                    Material material = prepped.getCombination(first.getFromRegion()).getMaterial();
                    if( material == null && colorTable != null ) {
                        Color matColor = colorTable.getColor(first.getRegionID());
                        material = new Material("dummy", matColor);
//                        System.out.println( "Setting color of " + first.getFromRegion() + " to " + matColor);
                    }
                    color = shader.shade(first, material, start);
                } else {
                    color = backGround;
                }
                byte[] bytes = new byte[3];
                bytes[0] = (byte) color.getRed();
                bytes[1] = (byte) color.getGreen();
                bytes[2] = (byte) color.getBlue();
                Rt.writePixelToFrameBuffer(fbOs, xPixelNo, yPixelNo, bytes);
                return;
            }
            ExecutorService executor = Executors.newFixedThreadPool(cpus);
            Object lock = new Object();
            for( int row = 0 ; row < size ; row++ ) {
                executor.submit(new RowTask(row, size, gridStart, xDir, yDir, rayDir,
                        delta, lights, oh, backGround, prepped, colorTable, buffer, fbOs, lock));
            }
            executor.shutdown();
            while( !executor.awaitTermination(10, TimeUnit.SECONDS));
            if( outputFile != null ) {
                outputFile.write(buffer.array());
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Rt.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadGeometryException ex) {
            Logger.getLogger(Rt.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DbNameNotFoundException ex) {
            Logger.getLogger(Rt.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Rt.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Rt.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DbException ex) {
            Logger.getLogger(Rt.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if( outputFile != null ) {
                    outputFile.close();
                }
                if( fbOs != null ) {
                    sock.close();
                    fbOs.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(Rt.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static class RowTask implements Runnable {
        private int row;
        private int size;
        private Point gridStart;
        private Vector3 xDir;
        private Vector3 yDir;
        private double delta;
        private Set<Light> lights;
        private ByteBuffer buffer;
        private Vector3 rayDir;
        private Color backGround;
        private ColorTable colorTable;
        private PreppedDb prepped;
        private final Object lock;
        private OutputStream fbOs;
        private OverlapHandler oh;

        public RowTask(int row, int size, Point gridStart, Vector3 xDir, Vector3 yDir, Vector3 rayDir,
                double delta, Set<Light> lights, OverlapHandler oh, Color backGround, PreppedDb prepped,
                ColorTable colorTable, ByteBuffer buffer, OutputStream fbOs, Object lock) {
            this.row = row;
            this.size = size;
            this.gridStart = gridStart;
            this.xDir = xDir;
            this.yDir = yDir;
            this.rayDir = rayDir;
            this.delta = delta;
            this.lights = lights;
            this.oh = oh;
            this.backGround = backGround;
            this.buffer = buffer;
            this.prepped = prepped;
            this.colorTable = colorTable;
            this.lock = lock;
            this.fbOs = fbOs;
        }

        public void run() {
            try {
            byte[] bytes = new byte[3*size];
            for (int col = 0; col < size; col++) {
                Point start = new Point(gridStart);
                start.join(delta * row, yDir);
                start.join(delta * col, xDir);
                Ray ray = new Ray(start, rayDir);
                Shader shader = new Phong(lights);
//                Shader shader = new NormalShader();
                SortedSet<Partition> parts = prepped.shootRay(ray, oh);
                Color color = null;
                if (parts.size() > 0) {
                    Partition first = parts.first();
                    Material material = prepped.getCombination(first.getFromRegion()).getMaterial();
                    if( material == null && colorTable != null ) {
                        Color matColor = colorTable.getColor(first.getRegionID());
                        if (matColor != null) {
                            material = new Material("dummy", matColor);
//                          System.out.println( "Setting color of " + first.getFromRegion() + " to " + matColor);
                        }
                    }
                    color = shader.shade(first, material, start);
                } else {
                    color = backGround;
                }
                int index = col*3;
                bytes[index] = (byte) color.getRed();
                bytes[index+1] = (byte) color.getGreen();
                bytes[index+2] = (byte) color.getBlue();
            }
            if (buffer != null) {
                synchronized (lock) {
                    buffer.position(row * size * 3);
                    buffer.put(bytes);
                }
            }
            if (fbOs != null) {
                try {
                    writeLineToFrameBuffer(fbOs, row, bytes);
                } catch (IOException ex) {
                    Logger.getLogger(Rt.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        }
    }

    private static void writeLineToFrameBuffer( OutputStream fbOs, int line, byte[] pixels ) throws IOException {
        short type = 105;
        ByteBuffer bb = ByteBuffer.allocate(20 + pixels.length);
        bb.put((byte) 0x41);
        bb.put((byte) 0xFE);
        bb.putShort(type);
        bb.putInt(12 + pixels.length);
        bb.putInt(0);
        bb.putInt(line);
        bb.putInt(pixels.length/3);
        bb.put(pixels);
        fbOs.write(bb.array());
    }

    private static void writePixelToFrameBuffer( OutputStream fbOs, int xPixelNo, int yPixelNo, byte[] pixel) throws IOException {
        short type = 105;
        ByteBuffer bb = ByteBuffer.allocate(20 + 3);
        bb.put((byte) 0x41);
        bb.put((byte) 0xFE);
        bb.putShort(type);
        bb.putInt(12 + 1);
        bb.putInt(xPixelNo);
        bb.putInt(yPixelNo);
        bb.putInt(1);
        bb.put(pixel);
        fbOs.write(bb.array());
    }
}
