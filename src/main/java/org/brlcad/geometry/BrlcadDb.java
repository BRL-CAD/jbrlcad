package org.brlcad.geometry;

/**
 * This class holds a BRL-CAD database and provides interfaces to its contents
 *
 * @author Created by Omnicore CodeGuide
 */
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.brlcad.numerics.Tolerance;

/**
 * Object used to store and read data from a Brlcad *.g file.
 * @author jra
 */
public class BrlcadDb {

    /** Name of the BRL-CAD db file */
    private String dbFileName;
    /** The title of this BRL-CAD db */
    private String title;
    /** The RandomAccessFile object associated with this BRL-CAD db */
    private RandomAccessFile dbInput;
    /** Directory of BRL-CAD object names and their offset into the BRL-CAD db */
    private Map<String, DirectoryEntry> directory;
    /** Magic number (byte) at the start of the BRL-CAD db file */
    public static final byte DB5HDR_MAGIC1 = 'v';
    /** Magic number (byte) at the end of each object in the BRL-CAD database */
    public static final byte DB5HDR_MAGIC2 = '5';
    /** default tolerance */
    public static final double DEFAULT_TOL_DIST = 0.005;
    public static final double DEFAULT_TOL_PERP = 1.0e-6;
    public static Tolerance tolerance = new Tolerance(DEFAULT_TOL_DIST, DEFAULT_TOL_PERP);

    /** minimum number of faces to trigger use of pieces */
    public static int BOT_MINFACES = 32;
    /** number of triangles per Bot piece */
    public static int BOT_FACES_PER_PIECE = 4;
    public static final String COLOR_TABLE_KEY = "regionid_colortable";
    public static final String REGION_ID_KEY = "region_id";

    /**
     * cached value of top level objects (can be cached because db is read only)
     */
    private List<String> topLevelObjects = null;
    private static final Logger logger = Logger.getLogger(BrlcadDb.class.getName());

    /**
     * Constructor
     *
     * @param    dbFileName          Name of the BRL-CAD DB file
     *
     * @exception   FileNotFoundException
     * @exception   IOException
     * @exception   DbException
     *
     */
    public BrlcadDb(String dbFileName) throws FileNotFoundException, IOException, DbException {
        this.dbFileName = dbFileName;
        this.dbInput = new RandomAccessFile(this.dbFileName, "r");
        this.scan();
    }

    /**
     * Zero-Argument Constructor.
     */
    protected BrlcadDb() {
    }

    public void close() throws IOException {
        this.directory = new HashMap<String, DirectoryEntry>();
        this.dbFileName = "No DbFile Open";
        this.title = "No DbFile open";
        if (this.dbInput != null) {
            this.dbInput.close();
            this.dbInput = null;
        }
    }

    private void markReferences(Tree tree) {
        if (tree == null) {
            return;
        }   
        
        if (tree.getOp() == Operator.LEAF) {            
            DirectoryEntry de = directory.get(tree.getLeafName()); 
            if(de != null){
                de.incrementReferences();
            } else{ 
                logger.log(Level.SEVERE, "Non-Existent reference '" + tree.getLeafName() + "'. Skipping this object." );       
            }
        } else {
            markReferences(tree.getLeft());
            markReferences(tree.getRight());
        }
    }
    
    private void findTopLevelObjects() throws DbException {
        for (Entry<String,DirectoryEntry> entry : directory.entrySet()) {
            DirectoryEntry de = entry.getValue();
            if (de.getMajorType() == Combination.majorType && de.getMinorType() == Combination.minorType) {
                try {
                    DbObject dbObj = this.getInternal(entry.getKey());
                    if ( !(dbObj instanceof Combination)) {
                        logger.severe(entry.getKey() + " expected to be a Combination, but was a " +
                                dbObj.getClass().getSimpleName());
                        throw new DbException( entry.getKey() + " expected to be a Combination, but was a " +
                                dbObj.getClass().getSimpleName());
                    }
                    Combination comb = (Combination) dbObj;
                    Tree tree = comb.getTree();
                    markReferences(tree);
                } catch (IOException ex) {
                    Logger.getLogger(BrlcadDb.class.getName()).log(Level.SEVERE, null, ex);
                
                } catch (DbNameNotFoundException ex) {
                    Logger.getLogger(BrlcadDb.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        topLevelObjects = new ArrayList<String>();
        for (Entry<String,DirectoryEntry> entry : directory.entrySet()) {
            DirectoryEntry de = entry.getValue();
            if (de.getMajorType() == DbAttributeOnly.majorType && de.getMinorType() == DbAttributeOnly.minorType) {
                continue;
            }
            if (de.getReferenceCount() == 0) {
                topLevelObjects.add(entry.getKey());
            }
        }
        Collections.sort(topLevelObjects);
    }

    public List<String> getTopLevelObjects() throws DbException {
        if (topLevelObjects == null) {
            findTopLevelObjects();
        }
        return Collections.unmodifiableList(topLevelObjects);
    }

    /**
     * Sets Tolerance
     *
     * @param    tolerance           a  Tolerance
     */
    public static void setTolerance(Tolerance tolerance) {
        BrlcadDb.tolerance = tolerance;
    }

    /**
     * Returns Tolerance
     *
     * @return    a  Tolerance
     */
    public static Tolerance getTolerance() {
        return tolerance;
    }

    /**
     * Get the RandomAccesFile object asociated with this BRL-CAD database
     *
     * @return   a RandomAccessFile
     *
     */
    public RandomAccessFile getRAM() {
        return this.dbInput;
    }

    /**
     * Scan the BRL-CAD database, constructing the directory and extracting the title
     *
     * @exception   IOException
     * @exception   DbException
     *
     */
    protected void scan() throws IOException, DbException {
        // this will hold the 8 byte header at the start of the file
        byte fileHeader[] = new byte[8];

        // create a new directory
        this.directory = new HashMap<String, DirectoryEntry>();
        try {
            // make sure we start at the start
            this.dbInput.seek(0L);

            // read the file header
            this.dbInput.readFully(fileHeader);

            // verify the file header
            if (!this.fileHeaderIsValid(fileHeader)) {
                logger.severe("Invalid file header");
                throw new IOException("Invalid file header");
            }

            // scan the rest of the file by importing DbExternal objects
            Long offset;
            while (true) {
                offset = this.dbInput.getFilePointer();
                DbExternalObject dbExt = new DbExternalObject(this, offset);

                // if this object has a name, add it to the directory
                String name = dbExt.getName();
                if (name != null) {
                    if (this.directory.get(name) != null) {
                        logger.severe("Duplicate name (" + name + ") ignored");
                    } else {
//						System.out.println( name + ": " + offset
//							+ "; major " + dbExt.getMajorType()
//							+ "; minor " + dbExt.getMinorType());
                        this.directory.put(name, new DirectoryEntry(offset, dbExt.getMajorType(), dbExt.getMinorType()));
                    }
                }
            }
        } catch (EOFException e) {
            // we reached the end of the file

            // get the title from this database
            try {
                DbObject global = this.getInternal("_GLOBAL");
                this.title = global.getAttribute("title");
            } catch (Exception e1) {
                logger.severe("Db has no GLOBAL object ");
                throw new DbException("Db has no GLOBAL object ", e1);
            }
            return;
        }

    }

    /**
     * Get the DbObject that has the specified name
     *
     * @param    name                The name of the desired object
     *
     * @return   a DbObject
     *
     * @exception   IOException
     * @exception   DbException
     * @exception   DbNameNotFoundException
     *
     */
    public DbObject getInternal(String name) throws IOException, DbException, DbNameNotFoundException {
        // Lookup this name in the directory
        DirectoryEntry de = this.directory.get(name);

        if (de == null) {
            // no such object
            logger.severe("Error: " + name + " not found");
            throw new DbNameNotFoundException("Error: " + name + " not found");
        }
        long offset = de.getOffset();

        // Read the object in external form
        DbExternalObject dbExt = new DbExternalObject(this, offset);

        // Import this object into its internal form (this will usually be a subclass of DbObject)
        DbObject dbObj = this.importObj(dbExt);
        return dbObj;
    }

    /**
     * Import a DbExternal object into its internal form
     *
     * @param    dbExt               a  DbExternal object
     *
     * @return   a  dbObject or, more likely, a subclass of DbObject
     */
    private DbObject importObj(DbExternalObject dbExt) throws DbException {
        switch (dbExt.getMajorType()) {
            case 0:
                logger.severe("Illegal major type number (0) for dbExt Object with name " + dbExt.getName());
                throw new DbException("Illegal major type number (0)");
            case 1:
                switch (dbExt.getMinorType()) {
                    case Ellipsoid.minorType:
                        return new Ellipsoid(dbExt);
                    case Arb8.minorType:
                        return new Arb8(dbExt);
                    case Bot.minorType:
                        return new Bot(dbExt);
                    case Combination.minorType:
                        return new Combination(dbExt);
                    case Tgc.minorType:
                        return new Tgc(dbExt);
                    case Torus.minorType:
                        return new Torus(dbExt);
                    case Ars.minorType:
                        return new Ars(dbExt);
                    default:
                        logger.severe("Unrecognized minor type (" +
                                dbExt.getMinorType() + "), for object: " + dbExt.getName());
                        throw new DbException("Unrecognized minor type (" +
                                dbExt.getMinorType() + "), for object: " + dbExt.getName());
                }
            case 2:
                return new DbAttributeOnly(dbExt);

            default:
                logger.severe("Unrecognized major type (" +
                        dbExt.getMajorType() + ")");
                throw new DbException("Unrecognized major type (" +
                        dbExt.getMajorType() + ")");
        }
    }

    /**
     * Get the description of an object with the specified name
     *
     * @param    name                a  String
     *
     * @return   a description of the specified object
     *
     * @exception   DbException
     * @exception   IOException
     * @exception   DbNameNotFoundException
     *
     */
    public String describe(String name) throws DbException, IOException, DbNameNotFoundException {
        DbObject obj = this.getInternal(name);
        return obj.toString();
    }

    /**
     * Read an unsigned integral number from the database using the specified number of bytes
     *
     * @param    numBytes            the number of bytes to read (must be 1, 2, 4, or 8)
     *
     * @return   a long
     *
     * @exception   IOException
     *
     */
    public long getUnsignedLong(int numBytes) throws IOException {
        long longNum;

        switch (numBytes) {
            case 1:
                return this.dbInput.readUnsignedByte();
            case 2:
                return this.dbInput.readUnsignedShort();
            case 4:
                byte[] bytes = new byte[4];
                this.dbInput.readFully(bytes);
                longNum = (bytes[0] & 0xff);
                for (int i = 1; i < 4; i++) {
                    longNum <<= 8;
                    longNum |= (bytes[i] & 0xff);
                }
                return longNum;
            case 8:
                longNum = this.dbInput.readLong();
                if (longNum < 0) {
                    logger.severe("Database contains an unsigned long that we cannot read in Java!!");
                    throw new IOException("Database contains an unsigned long that we cannot read in Java!!");
                }
                return longNum;
            default:
                logger.severe("BrlcadDb.getLong(): Illegal length (" + numBytes + ")");
                throw new IOException("BrlcadDb.getLong(): Illegal length (" + numBytes + ")");
        }
    }

    /**
     * Read a signed integral number from the database using the specified number of bytes
     *
     * @param    numBytes            The number of bytes to read
     *
     * @return   a long
     *
     * @exception   IOException
     *
     */
    public long getLong(int numBytes) throws IOException {
        switch (numBytes) {
            case 1:
                return this.dbInput.readByte();
            case 2:
                return this.dbInput.readShort();
            case 4:
                return this.dbInput.readInt();
            case 8:
                return this.dbInput.readLong();
            default:
                logger.severe("BrlcadDb.getLong(): Illegal length (" + numBytes + ")");
                throw new IOException("BrlcadDb.getLong(): Illegal length (" + numBytes + ")");
        }
    }

    /**
     * Extract a long value using bytes from the provided input array
     *
     * @param bytes	The array containing the bytes
     * @param pointer	The location in the above array to start converting
     * @param length	The number of bytes to convert
     * @return	A long
     */
    public static long getLong(byte[] bytes, int pointer, int length) {
        long longBytes = (bytes[pointer] & 0xff);
        for (int i = 1; i < length; i++) {
            longBytes <<= 8;
            longBytes |= ((long) bytes[pointer + i] & 0xff);
        }

        return longBytes;
    }

    /**
     * Extract a double value using 8 bytes from the provided input array starting with the
     * array element at index "pointer"
     *
     * @param    bytes               a  byte[]
     * @param    pointer             the starting index in the above array
     *
     * @return   a double
     *
     */
    public static double getDouble(byte[] bytes, int pointer) {
        long longBytes = (bytes[pointer] & 0xff);
        for (int i = 1; i < 8; i++) {
            longBytes <<= 8;
            longBytes |= ((long) bytes[pointer + i] & 0xff);
        }
        return Double.longBitsToDouble(longBytes);
    }

    /**
     * Extract a BitSet from an input array of bytes. The end of the BitSet is
     * the first 0 value byte after the staring point.
     * @param bytes	The array of bytes
     * @param pointer	The starting point of the BitSet in the above array
     * @return	A BitSet
     */
    public static BitSet getBitSet(byte[] bytes, int pointer) {
        BitSet bitset;

        // find start of bit vector
        while (Character.isWhitespace(bytes[pointer])) {
            pointer++;
        }
        // count number of hex digits in bit vector
        int index = pointer;
        while (bytes[index] != 0) {
            index++;
        }
        int length = index - pointer;
        if (length < 2 || (length % 2) != 0) {
            return null;
        }
        bitset = new BitSet(length * 4);

        // set the bits
        int bitNum = length * 4 - 1;
        while (bytes[pointer] != 0) {
            byte mask = (byte) 0x8;
            byte b = Byte.decode("0x" + (char) bytes[pointer]);
            for (int i = 0; i < 4; i++) {
                if ((b & mask) != 0) {
                    bitset.set(bitNum, true);
                }
                mask = (byte) (mask >> 1);
                bitNum--;
            }
            pointer++;
        }
        return bitset;
    }

    /**
     * Method fileHeaderIsValid
     *
     * @param    fileHeader          a  byte[]
     *
     * @return   a  boolean
     */
    private boolean fileHeaderIsValid(byte[] h) {
        if (h[0] != DB5HDR_MAGIC1) {
            return false;
        }
        if (h[7] != DB5HDR_MAGIC2) {
            return false;
        }
        return true;
    }

    /**
     * Method getObjectNames
     * 
     * @return  Set of object names in the file in alphabetical order
     */
    public Set<String> getObjectNames() {
        return new TreeSet<String>(directory.keySet());
    }

    /**
     * Method getDbExternal
     * 
     * @param name String which contains the name of the object to return
     * 
     * @return  DbExternal object for given name; null if it doesn't exist
     */
    public DbExternalObject getDbExternal(String name) {
        DirectoryEntry de = this.directory.get(name);

        if (de == null) {
            return null;
        }
        long offset = de.getOffset();
        try {
            return new DbExternalObject(this, offset);
        } catch (IOException ioe) {
            return null;
        }
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * 
     * @return the Brlcad geometry file name
     */
    public String getDbFileName() {
        return this.dbFileName;
    }
}
