/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brlcad.geometry;

/**
 *
 * @author jra
 */
public class DirectoryEntry {
    // offset into the file where this object resides
    private long offset;

    // major type of this object
    private byte majorType;

    // minor type of this object
    private byte minorType;

    // used by findTopLevelObjects()
    private int referenceCount;

    public DirectoryEntry(long offset, byte major, byte minor) {
        this.offset = offset;
        this.majorType = major;
        this.minorType = minor;
        this.referenceCount = 0;
    }

    /**
     * @return the offset
     */
    public long getOffset() {
        return offset;
    }

    /**
     * @param offset the offset to set
     */
    public void setOffset(long offset) {
        this.offset = offset;
    }

    /**
     * @return the majorType
     */
    public byte getMajorType() {
        return majorType;
    }

    /**
     * @param majorType the majorType to set
     */
    public void setMajorType(byte majorType) {
        this.majorType = majorType;
    }

    /**
     * @return the minorType
     */
    public byte getMinorType() {
        return minorType;
    }

    /**
     * @param minorType the minorType to set
     */
    public void setMinorType(byte minorType) {
        this.minorType = minorType;
    }

    /**
     * @return the referenceCount
     */
    public int getReferenceCount() {
        return referenceCount;
    }

    /**
     * @param referenceCount the referenceCount to set
     */
    public void setReferenceCount(int referenceCount) {
        this.referenceCount = referenceCount;
    }

    public void incrementReferences() {
        this.referenceCount++;
    }
}
