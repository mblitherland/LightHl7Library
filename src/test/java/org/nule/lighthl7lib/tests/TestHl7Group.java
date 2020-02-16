/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nule.lighthl7lib.tests;

import java.util.List;
import org.nule.lighthl7lib.hl7.*;

import junit.framework.TestCase;

/**
 *
 * @author mike
 */
public class TestHl7Group extends TestCase {
    
    public TestHl7Group(String testName) {
        super(testName);
    }            

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    /**
     * Make sure the segments we pull out of the group are what we expect.
     */
    public void testGroupCountAndSizes() {
        Hl7Record hl7 = new Hl7Record(getTestMsg1());
        Hl7SegmentGroup[] groups = hl7.getGroupAll("OBR");
        // should have three obr groups
        assertEquals(3, groups.length);
        // confirm the number of segments in each group
        assertEquals(4, groups[0].size());
        assertEquals(7, groups[1].size());
        assertEquals(2, groups[2].size());
    }
    
    /**
     * Make sure subgroup counts are right.
     */
    public void testSubgroupCountAndSizes() {
        Hl7Record hl7 = new Hl7Record(getTestMsg1());
        Hl7SegmentGroup[] groups = hl7.getGroupAll("OBR");
        Hl7SegmentGroup[] obx = groups[0].getGroupAll("OBX");
        assertEquals(2, obx.length);
        assertEquals(1, obx[0].size());
        assertEquals(2, obx[1].size());
        obx = groups[1].getGroupAll("OBX");
        assertEquals(2, obx.length);
        assertEquals(2, obx[0].size());
        assertEquals(2, obx[1].size());
        obx = groups[2].getGroupAll("OBX");
        assertEquals(1, obx.length);
        assertEquals(1, obx[0].size());
    }
    
    /**
     * Check individual field contents for segments.
     */
    public void testGroupAndSubgroupSegmentContent() {
        Hl7Record hl7 = new Hl7Record(getTestMsg1());
        Hl7SegmentGroup obr = hl7.getGroup("OBR", 1);
        Hl7Segment seg = obr.get("OBR");
        assertEquals("OBR1", seg.field(2).toString());
        Hl7SegmentGroup obx = obr.getGroup("OBX", 1);
        seg = obx.get("OBX", 1);
        assertEquals("OBX1", seg.field(2).toString());
        obx = obr.getGroup("OBX", 2);
        seg = obx.get("OBX", 1);
        assertEquals("OBX2", seg.field(2).toString());
        seg = obx.get("NTE", 1);
        assertEquals("NTE1", seg.field(2).toString());
        obr = hl7.getGroup("OBR", 2);
        seg = obr.get("OBR");
        assertEquals("OBR2", seg.field(2).toString());
        seg = obr.get("NTE", 1);
        assertEquals("NTE2", seg.field(2).toString());
        seg = obr.get("NTE", 2);
        assertEquals("NTE3", seg.field(2).toString());
        obx = obr.getGroup("OBX", 1);
        seg = obx.get("OBX", 1);
        assertEquals("OBX3", seg.field(2).toString());
        seg = obx.get("NTE", 1);
        assertEquals("NTE4", seg.field(2).toString());
        obx = obr.getGroup("OBX", 2);
        seg = obx.get("OBX", 1);
        assertEquals("OBX4", seg.field(2).toString());
        seg = obx.get("NTE", 1);
        assertEquals("NTE5", seg.field(2).toString());
        obr = hl7.getGroup("OBR", 3);
        seg = obr.get("OBR");
        assertEquals("OBR3", seg.field(2).toString());
        seg = obr.get("OBX");
        assertEquals("OBX5", seg.field(2).toString());
    }
    
    /**
     * Another content check but more focused on calling the fields
     * in an OO manner.  This has more to do with dealing with the
     * structures when they're being modified than merely read, but
     * isn't bad for an extra sanity check.
     */
    public void testOoGroupAndSubgroups() {
        Hl7Record hl7 = new Hl7Record(getTestMsg1());
        assertEquals("OBR1", hl7.getGroup("OBR", 1).get("OBR").field(2).toString());
        assertEquals("OBX1", hl7.getGroup("OBR", 1).get("OBX", 1).field(2).toString());
        assertEquals("OBX2", hl7.getGroup("OBR", 1).get("OBX", 2).field(2).toString());
        assertEquals("NTE1", hl7.getGroup("OBR", 1).getGroup("OBX", 2).get("NTE").field(2).toString());
        
    }

    public void testGroupIndexes() {
        Hl7Record hl7 = new Hl7Record(getTestMsg1());
        Hl7SegmentGroup group = hl7.getGroup("OBR", 1);
        assertEquals(new Integer(2), group.getBeginningIndex());
        assertEquals(new Integer(5), group.getEndingIndex());
        group = hl7.getGroup("OBR", 2);
        assertEquals(new Integer(6), group.getBeginningIndex());
        assertEquals(new Integer(12), group.getEndingIndex());
    }

    /**
     * Make sure rebuild works when there are no changes.
     */
    public void testGroupRebuild() {
        Hl7Record hl7 = new Hl7Record(getTestMsg1());
        hl7.getGroup("OBR", 1).get("OBX").field(1).changeField("Something");
        hl7.rebuild();
        assertEquals("Something", hl7.getGroup("OBR", 1).get("OBX").field(1).toString());
        assertEquals("Something", hl7.get("OBX").field(1).toString());
    }

    /**
     * Add segments in various places and make sure the rebuild works.
     */
    public void testGroupAddSegment() {
        Hl7Record hl7 = new Hl7Record(getTestMsg1());
        assertEquals(15, hl7.size());
        Hl7SegmentGroup[] obrGroups = hl7.getGroupAll("OBR");
        obrGroups[0].add(0, "ABC");
        obrGroups[0].get("ABC").field(2).changeField("2");
        hl7.rebuild();
        assertEquals(16, hl7.size());
        obrGroups = hl7.getGroupAll("OBR");
        obrGroups[1].append("DEF");
        obrGroups[1].get("DEF").changeSegment("DEF|1|2|3|4|5");
        hl7.rebuild();
        assertEquals(17, hl7.size());
        obrGroups = hl7.getGroupAll("OBR");
        obrGroups[0].getGroup("OBX", 2).add(1, "XYZ");
        hl7.rebuild();
        assertEquals(18, hl7.size());
        String[] segIds = { "MSH", "PID", "ABC", "OBR", "OBX", "OBX",
            "XYZ", "NTE", "OBR", "NTE", "NTE", "OBX", "NTE", "OBX", "NTE",
            "DEF", "OBR", "OBX"
        };
        List segments = hl7.listSegments();
        for (int i = 0; i < segIds.length; i++) {
            assertEquals(segIds[i], segments.get(i));
        }
    }

    /**
     * Append a segment and rebuild.
     */
    public void testGroupAppendSegment() {


    }
    
    private String getTestMsg1() {
        return "MSH|^~\\&|NULEORG|LHL|||20081022120000||ACK||P|2.3||||NE|\r" +
                "PID|||12345678||SMITH^PAT|\r" +
                "OBR|NOTAVALIDRESULT|OBR1|\r" +
                "OBX|NOTAVALIDTEST|OBX1|\r" +
                "OBX|NOTAVALIDTEST|OBX2|\r" +
                "NTE|NOTAVALIDNOTE|NTE1|\r" +
                "OBR|NOTAVALIDRESULT|OBR2|\r" +
                "NTE|NOTAVALIDNOTE|NTE2|\r" +
                "NTE|NOTAVALIDNOTE|NTE3|\r" +
                "OBX|NOTAVALIDTEST|OBX3|\r" +
                "NTE|NOTAVALIDNOTE|NTE4|\r" +
                "OBX|NOTAVALIDTEST|OBX4|\r" +
                "NTE|NOTAVALIDNOTE|NTE5|\r" +
                "OBR|NOTAVALIDRESULT|OBR3|\r" +
                "OBX|NOTAVALIDTEST|OBX5|\r";
    }

}
