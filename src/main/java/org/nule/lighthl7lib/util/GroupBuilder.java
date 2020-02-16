/*
 * Copyright (C) 2010-2012 M Litherland
 */

package org.nule.lighthl7lib.util;

import java.util.*;
import org.nule.lighthl7lib.hl7.*;

/**
 *
 * @author mike
 */
public class GroupBuilder {
    
    /**
     * Create a segment group out of the segments provided in a list.
     * 
     * @param segs A list of segments.
     * @param segId The segment ID to build the groups from.
     * @param count Which iteration of the group to return.
     * @param seps The record separators.
     * @return An Hl7SegmentGroup object
     */
    public static Hl7SegmentGroup singleGroup(List segs, String segId, int count, 
            String[] seps) {
        int current = 0;
        Integer beginningIndex = null;
        Integer endingIndex = null;
        boolean building = false;
        List groupBuilder = new ArrayList();
        for (int i = 0; i < segs.size(); i++) {
            Hl7Segment seg = (Hl7Segment) segs.get(i);
            if (seg.getId().equals(segId)) {
                current++;
                if (current > count) {
                    break;
                } else if (count == current) {
                    building = true;
                    beginningIndex = Integer.valueOf(i);
                } else {
                    building = false;
                }
            }
            if (building) {
                groupBuilder.add(seg);
                endingIndex = Integer.valueOf(i);
            }
        }
        return new Hl7SegmentGroup(groupBuilder, seps, beginningIndex, endingIndex);
    }
    
    /**
     * Create an array of segment groups out of the segments provided in a list.
     * 
     * @param segs A list of segments.
     * @param segId The segment ID to build the groups from.
     * @param seps The record separators.
     * @return An array of Hl7SegmentGroup objects
     */
    public static Hl7SegmentGroup[] allGroups(List segs, String segId, 
            String[] seps) {
        Integer beginningIndex = null;
        Integer endingIndex = null;
        List segGroups = new ArrayList();
        boolean building = false;
        List groupBuilder = null;
        for (int i = 0; i < segs.size(); i++) {
            Hl7Segment seg = (Hl7Segment) segs.get(i);
            if (seg.getId().equals(segId)) {
                building = true;
                if (groupBuilder != null) {
                    segGroups.add(new Hl7SegmentGroup(groupBuilder, seps, beginningIndex, endingIndex));
                }
                beginningIndex = Integer.valueOf(i);
                groupBuilder = new ArrayList();
            }
            if (building) {
                groupBuilder.add(seg);
                endingIndex = Integer.valueOf(i);
            }
        }
        if (groupBuilder != null && building) {
            segGroups.add(new Hl7SegmentGroup(groupBuilder, seps, beginningIndex, endingIndex));
        }
        if (groupBuilder == null) {
            return null;
        }
        Hl7SegmentGroup[] groups = new Hl7SegmentGroup[segGroups.size()];
        for (int i = 0; i < segGroups.size(); i++) {
            groups[i] = (Hl7SegmentGroup) segGroups.get(i);
        }
        return groups;
    }

    /**
     * This method can be passed the initial list of segments and a list of groups
     * were created from these segments and in theory it will return an updated list
     * of segments with the modified groups inserted properly.
     *
     * There is much that can mess with this delicate arrangement.  For example
     * rebuilding but not refetching the Hl7SegmentGroups before modifying them
     * can badly mess up the indexing of the stored groups.
     *
     * @param segs
     * @param groups
     * @return
     */
    public static Hl7Segment[] rebuildSegmentsFromGroups(Hl7Segment[] segs, List groups) {
        int start = -1;
        int end = -1;
        for (int i = 0; i < groups.size(); i++) {
            Hl7SegmentGroup group = (Hl7SegmentGroup) groups.get(i);
            if (group.getBeginningIndex() != null) {
                int comp = group.getBeginningIndex().intValue();
                if (start == -1 || comp < start) {
                    start = comp;
                }
            }
            if (group.getEndingIndex() != null) {
                int comp = group.getEndingIndex().intValue();
                if (end == -1 || comp > end) {
                    end = comp;
                }
            }
        }
        if (start > 0 && end >= start) {
            List newSegs = new ArrayList();
            for (int i = 0; i < start; i++) {
                newSegs.add(segs[i]);
            }
            for (int i = 0; i < groups.size(); i++) {
                Hl7SegmentGroup nestedGroup = (Hl7SegmentGroup) groups.get(i);
                nestedGroup.rebuild();
                Hl7Segment[] nestedSegments = nestedGroup.getAll();
                newSegs.addAll(Arrays.asList(nestedSegments));
            }
            for (int i = end + 1; i < segs.length; i++) {
                newSegs.add(segs[i]);
            }
            Hl7Segment[] result = new Hl7Segment[newSegs.size()];
            for (int i = 0; i < newSegs.size(); i++) {
                result[i] = (Hl7Segment) newSegs.get(i);
            }
            return result;
        }
        return segs;
        // Note that segs themselves are going to be rebuilt by Hl7Record.rebuild()
    }
}
