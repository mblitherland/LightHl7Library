/*
 * Hl7SegmentGroup.java
 *
 * Copyright (C) 2008-2012 M Litherland
 */

package org.nule.lighthl7lib.hl7;

import java.io.Serializable;
import java.util.*;
import org.nule.lighthl7lib.util.GroupBuilder;

/**
 *
 * @author mike
 */
public class Hl7SegmentGroup  implements Serializable {
    private static final long serialVersionUID = 3921753612704219884L;

    private Hl7Segment[] segs = null;
    private String[] seps = null;
    private Integer beginningIndex = null;
    private Integer endingIndex = null;
    private List groups = new ArrayList();

    /**
     * @deprecated
     *
     * @param segs
     * @param seps
     */
    public Hl7SegmentGroup(Hl7Segment[] segs, String[] seps) {
        this.segs = segs.clone();
        this.seps = seps.clone();
    }

    /**
     * @deprecated
     *
     * @param segs
     * @param seps
     */
    public Hl7SegmentGroup(List segs, String[] seps) {
        this.segs = new Hl7Segment[segs.size()];
        for (int i = 0; i < segs.size(); i++) {
            this.segs[i] = (Hl7Segment) segs.get(i);
        }
        this.seps = seps.clone();
    }

    public Hl7SegmentGroup(List segs, String[] seps, Integer beginningIndex,
            Integer endingIndex) {
        this.segs = new Hl7Segment[segs.size()];
        for (int i = 0; i < segs.size(); i++) {
            this.segs[i] = (Hl7Segment) segs.get(i);
        }
        this.seps = seps.clone();
        this.beginningIndex = beginningIndex;
        this.endingIndex = endingIndex;
    }

    /**
     * Returns the number of segments
     *
     * @return int
     */
    public int size() {
        return segs.length;
    }
    
    /**
     * Append a new segment to the end of the segment group.
     *
     * @param segHeader 
     * @throws IllegalArgumentException if the header isn't three characters.
     */
    public void append(String segHeader) {
        Hl7Segment[] newSegs = new Hl7Segment[segs.length + 1];
        if (segHeader.length() != 3) {
            throw new IllegalArgumentException("Header must be three characters.");
        }
        int i = 0;
        while (i < segs.length) {
            newSegs[i] = segs[i];
            i++;
        }
        newSegs[i] = new Hl7Segment(segHeader+"|", seps);
        segs = newSegs;
    }
    
    /**
     * Add a segment to the group at the specified position.  The position is
     * handled the same as it would be for java.util.List.add(int, Object).
     * Please note that for a segment group the position is relative to the
     * group, not to the entire segment.
     *
     * @param position An index position of where to insert the segment.
     * @param segHeader 
     * @throws IllegalArgumentException if the header isn't three characters.
     */
    public void add(int position, String segHeader) {
        Hl7Segment[] newSegs = new Hl7Segment[segs.length + 1];
        if (segHeader.length() != 3) {
            throw new IllegalArgumentException("Header must be three characters.");
        }
        int i = 0;
        boolean inserted = false;
        while (i < newSegs.length) {
            if (i == position) {
                inserted = true;
                newSegs[i] = new Hl7Segment(segHeader+"|", seps);
            } else {
                if (inserted) {
                    newSegs[i] = segs[i - 1];
                } else {
                    newSegs[i] = segs[i];
                }
            }
            i++;
        }
        segs = newSegs;
    }
    
    /**
     * Returns the first segment that matches the given segment ID.  If
     * no matching segment is found a null is returned.
     *
     * @param id
     * @return Hl7Segment.
     */
    public Hl7Segment get(String id) {
        return get(id, 1);
    }
    
    /**
     * Returns a specified iteration of the requested segment.  get("OBX", 1)
     * would return the first OBX found, get("NTE", 5) would return the 5th 
     * NTE.  With this method you should be aware that there's no logic to 
     * understand the relationships of segment "groups".  That is, that a NTE
     * is associated with, or a child of, any particular other segment.  See
     * the getGroup(String, int) method for an attempt at some nested
     * group handling.
     * 
     * @param id
     * @param count
     * @return
     */
    public Hl7Segment get(String id, int count) {
        int found = 0;
        for (int i = 0; i < segs.length; i++) {
            if (id.equals(((Hl7Segment) segs[i]).getId())) {
                found++;
                if (found == count) {
                    return (Hl7Segment) segs[i];
                }
            }
        }
        return null;
    }
    
    /**
     * Return a segment specified by it's number.  So the first segment
     * (MSH, I hope) is segment 1, evn is probably segment 2 and so on.
     * In other words, this is not a zero indexed array of segments, the
     * first one is indexed 1.  This is to attempt to match best the
     * concept of counting HL7 attempts to use.
     *
     * @param id
     * @return Hl7Segment
     */
    public Hl7Segment get(int id) {
        return (Hl7Segment) segs[id - 1];
    }
    
    /**
     * Returns all available Hl7Segments as an array of that kind.
     *
     * @return Hl7Segment[]
     */
    public Hl7Segment[] getAll() {
        return segs;
    }
    
    /**
     * Get a segment group based upon a provided segment ID.  This essentially 
     * returns an array of segments starting with the segment you identify by ID
     * and continues until either another segment with the same ID is found or
     * until the record ends.
     * 
     * @param segId The segment ID you wish to retrieve.
     * @param count The count of the segment group you want, the first group is "1"
     * @return the Hl7SegmentGroup
     */
    public Hl7SegmentGroup getGroup(String segId, int count) {
        List segList = new ArrayList();
        segList.addAll(Arrays.asList(segs));
        groups = new ArrayList();
        Hl7SegmentGroup group = GroupBuilder.singleGroup(segList, segId, count, seps);
        groups.add(group);
        return group;
    }
      
    /**
     * The same as getGroup(String, int) except the count is assumed to be 1.
     * 
     * @param segId The segment ID you wish to retrieve.
     * @return the Hl7SegmentGroup
     */  
    public Hl7SegmentGroup getGroup(String segId) {
        return getGroup(segId, 1);
    }
    
    /**
     * Return all groups as an array of HL7SegmentGroup according to the rules of
     * 
     * 
     * @param segId
     * @return
     */
    public Hl7SegmentGroup[] getGroupAll(String segId) {
        List segList = new ArrayList();
        segList.addAll(Arrays.asList(segs));
        groups = new ArrayList();
        Hl7SegmentGroup[] groupArray = GroupBuilder.allGroups(segList, segId, seps);
        groups.addAll(Arrays.asList(groupArray));
        return groupArray;
    }

    /**
     * This is really for calling from the parent entity rather than calling
     * directly.  It really doesn't do too much otherwise.
     *
     */
    public void rebuild() {
        if (groups.size() > 0) {
            segs = GroupBuilder.rebuildSegmentsFromGroups(segs, groups);
            groups = new ArrayList();
        }
    }

    /**
     * Return the beginning index of the group in terms of the parent object.
     *
     * @return
     */
    public Integer getBeginningIndex() {
        return beginningIndex;
    }

    /**
     * Return the ending index of the group in terms of the parent object.
     *
     * @return
     */
    public Integer getEndingIndex() {
        return endingIndex;
    }
    
}
