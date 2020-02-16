/*
 * TestHl7Util.java
 *
 * Created on March 27, 2006, 1:58 PM
 *
 * Copyright (C) 2004-2007 M Litherland
 */
package org.nule.lighthl7lib.tests;

import java.util.*;

import junit.framework.TestCase;

import org.nule.lighthl7lib.hl7.*;
import org.nule.lighthl7lib.util.*;

/**
 *
 * @author litherm
 */
public class TestHl7Util extends TestCase {

    private static final String testRec1 = "MSH|^~\\&|NULEORG|HL7BROWSER_TNG|||" +
            "20040825120000||ACK||P|2.3||||NE|\rZZZ|this^is^not^valid|hl7|" +
            "but~I^don't~care|even^if&I&did&I^wouldn't^tell|you\r";
    private static final String testRec2 = "MSH|^~\\&|\rSCH|\rNTE|\rNTE|\r" +
            "PID|\rAIL|\rAIP|\rNTE|\rNTE|\rNTE|\r";
    private static final String testRec3 = "MSH|^~\\&|\rSCH|\rNTE|\rNTE|\r" +
            "PID|\rAIL\rAIP|\rNTE|\rNTE|\rNTE|\r";
    private static final String testRec4 = "MSH|^~\\&|\rSCH|\rNTE|\rNTE|\r" +
            "PID|\rAIL\rAIP|\rNTE|\rNTE|\rNTE\r";
    private Hl7Record h7u;

    public void setUp() {
        h7u = new Hl7Record(testRec1);
        //System.out.println(testRec1);
    }

    public void testSeparators() {
        String[] expected = {"[|]", "[\\^]", "[~]", "[&]", "|", "^", "~", "&", "[\\\\]", "\\\\"};
        String[] got = h7u.getSeparators();
        int i = 0;
        while (i < expected.length) {
            assertEquals(expected[i], got[i]);
            i++;
        }
    }

    public void testExtract() {
        String dirty = "2004-08-26 10:51:00 Randomstuff, here's a message: <" + testRec1 + ">\r\n";
        String clean = Hl7Record.cleanString(dirty);
        assertEquals(testRec1, clean);
        dirty = testRec1 + "\n";
        clean = Hl7Record.cleanString(dirty);
        assertEquals(testRec1, clean);
        dirty = "ABC annoying test case\r" + testRec1;
        clean = Hl7Record.cleanString(dirty);
        assertEquals(testRec1, clean);
    }

    public void testSegments() {
        String[] expectSegments = testRec1.split("\r");

        // test using getAll
        Hl7Segment[] gotSegments = h7u.getAll();
        assertEquals(expectSegments[0], gotSegments[0].toString());
        assertEquals(expectSegments[1], gotSegments[1].toString());

        // test using get(int)
        assertEquals(expectSegments[0], h7u.get(1).toString());
        assertEquals(expectSegments[1], h7u.get(2).toString());

        // test using get(string)
        assertEquals(expectSegments[0], h7u.get("MSH").toString());
        assertEquals(expectSegments[1], h7u.get("ZZZ").toString());
    }

    public void testSegmentsExt() {
        String localTestRec3 = testRec1 + "MSH|test|test|\rZZZ|Test again|\r";
        String[] expectSegments = localTestRec3.split("\r");

        // test using get(string, count)
        Hl7Record h7u2 = new Hl7Record(localTestRec3);
        assertEquals(expectSegments[0], h7u2.get("MSH", 1).toString());
        assertEquals(expectSegments[1], h7u2.get("ZZZ", 1).toString());
        assertEquals(expectSegments[2], h7u2.get("MSH", 2).toString());
        assertEquals(expectSegments[3], h7u2.get("ZZZ", 2).toString());

        // Make sure the segment count is right.
        assertEquals(4, h7u2.size());
    }

    public void testFields() {
        String segment1 = testRec1.split("\r")[0];
        String segment2 = testRec1.split("\r")[1];
        // Fourth field of MSH
        String expectField1 = segment1.split("[|]")[3];
        // Second field of ZZZ
        String expectField2 = segment2.split("[|]")[2];

        assertEquals(expectField1, h7u.get("MSH").field(4).toString());
        assertEquals(expectField2, h7u.get("ZZZ").field(2).toString());
    }

    public void testFieldAll() {
        Hl7Record testHl7 = new Hl7Record(testRec1);
        Hl7Field[] fields = testHl7.get("MSH").fieldAll();
        String[] expectFields1 = {
            "|",
            "^~\\&",
            "NULEORG",
            "HL7BROWSER_TNG",
            "",
            "",
            "20040825120000",
            "",
            "ACK",
            "",
            "P",
            "2.3",
            "",
            "",
            "",
            "NE"
        };
        for (int i = 0; i < fields.length; i++) {
            assertEquals(expectFields1[i], fields[i].toString());
        }
        fields = testHl7.get("ZZZ").fieldAll();
        String[] expectFields2 = {
            "this^is^not^valid",
            "hl7",
            "but~I^don't~care",
            "even^if&I&did&I^wouldn't^tell",
            "you"
        };
        for (int i = 0; i < fields.length; i++) {
            assertEquals(expectFields2[i], fields[i].toString());
        }
    }

    public void testMshOneAndTwo() {
        String msh1 = "|";
        String msh2 = "^~\\&";
        Hl7Record h7u2 = new Hl7Record(testRec1);

        assertEquals(msh1, h7u2.get("MSH").field(1).toString());
        assertEquals(msh2, h7u2.get("MSH").field(2).toString());
    }

    public void testComps() {
        String segment1 = testRec1.split("\r")[1];
        // First field of ZZZ
        String field1 = segment1.split("[|]")[1];
        // Third component of field1
        String expectComp1 = field1.split("[\\^]")[2];

        assertEquals(expectComp1, h7u.get(2).field(1).getComp(3).toString());
    }

    public void testReps() {
        String segment1 = testRec1.split("\r")[1];
        // Third field of ZZZ
        String field1 = segment1.split("[|]")[3];
        // Third repetition of field1
        String expectComp1 = field1.split("[~]")[2];

        assertEquals(expectComp1, h7u.get(2).field(3).getRep(3).toString());
    }

    public void testSubcomps() {
        String segment1 = testRec1.split("\r")[1];
        // Fourth field of ZZZ
        String field1 = segment1.split("[|]")[4];
        // Second comp of field1
        String comp2 = field1.split("[\\^]")[1];
        // Second subcom of rep2
        String expectSub1 = comp2.split("[&]")[2];

        assertEquals(expectSub1, h7u.get(2).field(4).getComp(2).getSubcomp(3).toString());
    }

    /**
     * Make sure that our various get methods don't throw exceptions if we give
     * them out of bounds values - they should return an Hl7Field object
     * containing an empty string.
     *
     */
    public void testOutOfBounds() {
        assertEquals(null, h7u.get("ABC"));
        assertEquals(null, h7u.get("ZZZ", 2));
        assertEquals("NULEORG", h7u.get("MSH").field(3).getComp(1).toString());
        assertEquals("", h7u.get("MSH").field(3).getComp(2).toString());
        assertEquals("care", h7u.get("ZZZ").field(3).getRep(3).toString());
        assertEquals("", h7u.get("ZZZ").field(3).getRep(4).toString());
        assertEquals("did", h7u.get("ZZZ").field(4).getComp(2).getSubcomp(3).toString());
        assertEquals("", h7u.get("ZZZ").field(4).getComp(2).getSubcomp(5).toString());
    }

    /**
     * Test new functionality to allow for the alteration of messages.
     *
     */
    public void testChangeRecord() {
        String localTestRec2 = "MSH|^~\\&|NULEORG|HL7BROWSER_TNG|||20040825120000||" +
                "ACK||P|2.3||||NE|\rZZZ|this^is^^valid|hl7|but~I^don't~care|" +
                "even^if&I&did&I^wouldn't^tell|you\r";
        String testSeg1 = "ZZZ|this^is^not^valid|hl7|but~I^don't~care|" +
                "even^if&I&did&I^wouldn't^tell|you";
        String testSeg2 = "ZZZ|this^is^^valid|hl7|but~I^don't~care|" +
                "even^if&I&did&I^wouldn't^tell|you";
        Hl7Record h7u1 = new Hl7Record(testRec1);
        assertEquals(testSeg1, h7u1.get("ZZZ").toString());
        h7u1.changeRecord(localTestRec2);
        assertEquals(testSeg2, h7u1.get("ZZZ").toString());
        h7u1.get("ZZZ").changeSegment(testSeg1);
        h7u1.rebuild();
        assertEquals(testSeg1, h7u1.get("ZZZ").toString());
        h7u1.get("ZZZ").changeSegment(testSeg2);
        h7u1.rebuild();
        assertEquals(testSeg2, h7u1.get("ZZZ").toString());
        h7u1.get("ZZZ").field(1).changeField("this^is^not^valid");
        h7u1.rebuild();
        assertEquals(testSeg1, h7u1.get("ZZZ").toString());
        assertEquals("not", h7u1.get("ZZZ").field(1).getComp(3).toString());
        h7u1.get("ZZZ").field(1).getRep(1).getComp(3).changeField("");
        h7u1.rebuild();
        assertEquals("", h7u1.get("ZZZ").field(1).getRep(1).getComp(3).toString());
        h7u1.get("ZZZ").field(1).getRep(1).getComp(3).changeField("not");
        h7u1.rebuild();
        assertEquals("not", h7u1.get("ZZZ").field(1).getRep(1).getComp(3).toString());
    }

    /**
     * Test segment list functionality.
     *
     */
    public void testSegmentLists() {
        List l = new ArrayList();
        l.add("MSH");
        l.add("SCH");
        l.add("NTE");
        l.add("NTE");
        l.add("PID");
        l.add("AIL");
        l.add("AIP");
        l.add("NTE");
        l.add("NTE");
        l.add("NTE");
        Hl7Record hl7 = new Hl7Record(testRec2);
        assertEquals(l, hl7.listSegments());
        hl7 = new Hl7Record(testRec3);
        assertEquals(l, hl7.listSegments());
        hl7 = new Hl7Record(testRec4);
        assertEquals(l, hl7.listSegments());
    }

    /**
     * Test record for field, rep and component append
     */
    public void testAppendField() {
        Hl7Record hl7 = new Hl7Record(testRec2);
        hl7.get("SCH").field(10).changeField("blah");
        String expected = "MSH|^~\\&|\rSCH||||||||||blah\rNTE|\rNTE|\r" +
                "PID|\rAIL|\rAIP|\rNTE|\rNTE|\rNTE|\r";
        hl7.rebuild();
        assertEquals(expected, hl7.toString());
        expected = "MSH|^~\\&|\rSCH||||||||||blah|blah\rNTE|\rNTE|\r" +
                "PID|\rAIL|\rAIP|\rNTE|\rNTE|\rNTE|\r";
        hl7.get("SCH").field(11).changeField("blah");
        hl7.rebuild();
        assertEquals(expected, hl7.toString());
        expected = "MSH|^~\\&|\rSCH||||||||blah||blah|\rNTE|\rNTE|\r" +
                "PID|\rAIL|\rAIP|\rNTE|\rNTE|\rNTE|\r";
        hl7.get("SCH").field(11).changeField("");
        hl7.get("SCH").field(8).changeField("blah");
        hl7.rebuild();
        assertEquals(expected, hl7.toString());
        expected = "MSH|^~\\&|\rSCH||||||||blah^^^huh?||blah\rNTE|\rNTE|\r" +
                "PID|\rAIL|\rAIP|\rNTE|\rNTE|\rNTE|\r";
        hl7.get("SCH").field(8).getComp(4).changeField("huh?");
        hl7.rebuild();
        assertEquals(expected, hl7.toString());
        expected = "MSH|^~\\&|\rSCH||||||||blah^^^huh?~^^^blargh||blah\rNTE|\rNTE|\r" +
                "PID|\rAIL|\rAIP|\rNTE|\rNTE|\rNTE|\r";
        hl7.get("SCH").field(8).getRep(2).getComp(4).changeField("blargh");
        hl7.rebuild();
        assertEquals(expected, hl7.toString());
        expected = "MSH|^~\\&|\rSCH||||||||blah^^^what?~^^^blargh||blah\rNTE|\rNTE|\r" +
                "PID|\rAIL|\rAIP|\rNTE|\rNTE|\rNTE|\r";
        hl7.get("SCH").field(8).getRep(1).getComp(4).changeField("what?");
        hl7.rebuild();
        assertEquals(expected, hl7.toString());
        int count = hl7.get("SCH").field(8).getRepCount();
        assertEquals(count, 2);
    }

    /**
     * Test for segment append and insert.
     */
    public void testAppendSeg() {
        Hl7Record hl7 = new Hl7Record(testRec2);
        // Got a report of two rebuilds in a row throwing NPEs
        hl7.rebuild();
        hl7.rebuild();
        // Also applied after an append
        hl7.append("PV1");
        hl7.rebuild();
        // Now on to our regularly scheduled test
        hl7 = new Hl7Record(testRec2);
        hl7.append("XYZ");
        List l = new ArrayList();
        l.add("MSH");
        l.add("SCH");
        l.add("NTE");
        l.add("NTE");
        l.add("PID");
        l.add("AIL");
        l.add("AIP");
        l.add("NTE");
        l.add("NTE");
        l.add("NTE");
        l.add("XYZ"); // new append segment
        assertEquals(l, hl7.listSegments());
        hl7.add(5, "ABC");
        l = new ArrayList();
        l.add("MSH");
        l.add("SCH");
        l.add("NTE");
        l.add("NTE");
        l.add("PID");
        l.add("ABC"); // new add segment
        l.add("AIL");
        l.add("AIP");
        l.add("NTE");
        l.add("NTE");
        l.add("NTE");
        l.add("XYZ");
        assertEquals(l, hl7.listSegments());
    }

    /**
     * Test for record creation
     */
    public void testNewRecord() {
        String[] segs = {"MSH", "EVN", "PID", "ORC", "OBR", "NTE", "NTE", "OBR", "NTE"};
        Hl7Record hl7 = new Hl7Record(segs);
        String expected = "MSH|^~\\&|\rEVN|\rPID|\rORC|\rOBR|\rNTE|\rNTE|\rOBR|\rNTE|\r";
        assertEquals(expected, hl7.toString());
        List l = new ArrayList();
        l.add("MSH");
        l.add("EVN");
        l.add("PID");
        l.add("ORC");
        l.add("OBR");
        l.add("NTE");
        l.add("NTE");
        l.add("OBR");
        l.add("NTE");
        assertEquals(l, hl7.listSegments());
        hl7 = new Hl7Record(segs, ":^~\\(");
        expected = "MSH:^~\\(\rEVN:\rPID:\rORC:\rOBR:\rNTE:\rNTE:\rOBR:\rNTE:\r";
        assertEquals(expected, hl7.toString());
        hl7.get("PID").field(5).getComp(1).changeField("PATIENT");
        hl7.get("PID").field(5).getComp(2).changeField("TEST");
        hl7.get("PID").field(5).getComp(3).changeField("A");
        hl7.rebuild();
        expected = "MSH:^~\\(\rEVN:\rPID:::::PATIENT^TEST^A\rORC:\rOBR:\rNTE:\rNTE:\rOBR:\rNTE:\r";
        assertEquals(expected, hl7.toString());
    }

    /**
     * Tests for the field match object. Note that the getFieldObj method returns
     * an Hl7Field object and getField returns the string from the toString
     * method of that object.  Therefore the both don't need to be tested except
     * where the getFieldObj(hl7).changeField(); hl7.rebuild() is applied.
     */
    public void testFieldMatch() {
        Hl7Record hl7 = new Hl7Record(testRec1);
        FieldMatch fm = FieldMatch.verifyFields("ZZZ:3");
        String expected = "but~I^don't~care";
        assertEquals(expected, fm.getField(hl7));
        fm = FieldMatch.verifyFields("ZZZ:6");
        fm.getFieldObj(hl7).changeField("not");
        hl7.rebuild();
        expected = "MSH|^~\\&|NULEORG|HL7BROWSER_TNG|||" +
                "20040825120000||ACK||P|2.3||||NE|\rZZZ|this^is^not^valid|hl7|" +
                "but~I^don't~care|even^if&I&did&I^wouldn't^tell|you|not\r";
        assertEquals(expected, hl7.toString());
        expected = "did";
        assertEquals(expected, hl7.getField("ZZZ:4-0-2-3"));
        hl7.setField("ZZZ:6", "");
        hl7.setField("ZZZ:3-2-2", "do");
        hl7.setField("ZZZ:4-0-3", "would");
        hl7.rebuild();
        expected = "MSH|^~\\&|NULEORG|HL7BROWSER_TNG|||" +
                "20040825120000||ACK||P|2.3||||NE|\rZZZ|this^is^not^valid|hl7|" +
                "but~I^do~care|even^if&I&did&I^would^tell|you|\r";
        assertEquals(expected, hl7.toString());
    }

    /**
     * These are tests that I'm just using to help determine the desirable
     * behavoir of the getComp, getEtc., methods that can alter the message
     * structure.
     */
    public void testReadOnlyComp() {
        String originalMessage = "MSH|^~\\&|||||||\rPID|P1||P3~P4|P5^P6\r";
        Hl7Record msg = new Hl7Record(originalMessage);
        assertEquals("Check segment has not changed", originalMessage, msg.toString());

        // Warm up to ensure we are referencing the correct fields
        assertEquals("Check field PID-1 is P1", "P1", msg.get("PID").field(1).toString());
        assertEquals("Check field PID-2 is Empty", "", msg.get("PID").field(2).toString());
        assertEquals("Check field PID-3 is P3~P4", "P3~P4", msg.get("PID").field(3).toString());

        // Now check with and without component being there
        assertEquals("Check one component P1", "P1", msg.get("PID").field(1).getComp(1).toString());
        msg.rebuild();
        assertEquals("Check segment has not changed", originalMessage, msg.toString());

        String newMessage = "MSH|^~\\&|||||||\rPID|P1^hi||P3~P4|P5^P6\r";
        msg.get("PID").field(1).getComp(2).changeField("hi");
        msg.rebuild();
        assertEquals(newMessage, msg.toString());

        // Testing the logic that prevents instantiated but never populated fields
        // from being included in rebuilt messages
        msg = new Hl7Record(originalMessage);
        assertEquals("Check second (non-existing) component P1", "", msg.get("PID").field(1).getComp(2).toString());
        msg.rebuild();
        assertEquals("Check segment has not changed", originalMessage, msg.toString());

        // More complex versions of before;
        assertEquals("Check second (non-existing) component P1", "", msg.get("PID").field(1).getComp(2).getSubcomp(3).toString());
        msg.rebuild();
        assertEquals("Check segment has not changed", originalMessage, msg.toString());

        // Finally, populate a deeply nested instantiated subfield and make sure that it
        // actually updates right on the rebuild.
        newMessage = "MSH|^~\\&|||||||\rPID|P1|^^&&&hey|P3~P4|P5^P6\r";
        assertEquals("Check second (non-existing) component P1", "", msg.get("PID").field(2).getComp(3).getSubcomp(4).toString());
        msg.get("PID").field(2).getComp(3).getSubcomp(4).changeField("hey");
        msg.rebuild();
        assertEquals("Check segment has not changed", newMessage, msg.toString());
    }

    /**
     * These are tests submitted by Peter to test the .size calls
     */
    public void testSizeMethods() {
        String originalMessage = "MSH|^~\\&|||||||\rPID|P1||P3~P4|P5^P6\r";
        Hl7Record msg = new Hl7Record(originalMessage);
        assertEquals("Check count before starting", 2, msg.size());

        originalMessage = "MSH|^~\\&|||||||\rPID|P1||P3~P4|P5^P6\r";
        msg = new Hl7Record(originalMessage);
        assertEquals("Check 1st field", "P1", msg.get("PID").field(1).toString());
        assertEquals("Check count after field lookup", 2, msg.size());

        originalMessage = "MSH|^~\\&|||||||\rPID|P1||P3~P4|P5^P6\r";
        msg = new Hl7Record(originalMessage);
        msg.get("PID").field(1).changeField("hi");
        msg.rebuild();
        assertEquals("Check count after rebuild on changed field", 2, msg.size());

        originalMessage = "MSH|^~\\&|||||||\rPID|P1||P3~P4|P5^P6\r";
        msg = new Hl7Record(originalMessage);
        msg.get("PID").field(2).changeField("hi"); //There is nothing in field two
        assertEquals("Check count before rebuild but after changed non-existing field", 2, msg.size());
        msg.rebuild();
        assertEquals("Check count after rebuild and after changed non-existing field", 2, msg.size());

        originalMessage = "MSH|^~\\&|||||||\rPID|P1||P3~P4|P5^P6\r";
        msg = new Hl7Record(originalMessage);
        msg.get("PID").field(1).getComp(2).changeField("hi"); //There is nothing in field two
        assertEquals("Check count before rebuild but after changed non-existing field component", 2, msg.size());
        msg.rebuild();
        assertEquals("Check count before rebuild and after changed non-existing field component", 2, msg.size());

        originalMessage = "MSH|^~\\&|||||||\rPID|P1||P3~P4|P5^P6\r";
        msg = new Hl7Record(originalMessage);
        msg.get("PID").field(1).getComp(1).changeField("hi"); //There is nothing in field two
        assertEquals("Check count before rebuild but after changed existing field component", 2, msg.size());
        msg.rebuild();
        assertEquals("Check count before rebuild and after changed existing field component", 2, msg.size());
    }

    /**
     * Peter's set of rebuild tests
     */
    public void testLotsOfRebuilds() {

        //Mike's test
        String originalMessage = "MSH|^~\\&|||||||\rPID|P1||P3~P4|P5^P6\r";
        Hl7Record msg = new Hl7Record(originalMessage);
        String newMessage = "MSH|^~\\&|||||||\rPID|P1^hi||P3~P4|P5^P6\r";
        msg.get("PID").field(1).getComp(2).changeField("hi");
        msg.rebuild();
        assertEquals(newMessage, msg.toString());


        //Mike's test changed to getSubComp
        originalMessage = "MSH|^~\\&|||||||\rPID|P1||P3~P4|P5^P6\r";
        msg = new Hl7Record(originalMessage);
        newMessage = "MSH|^~\\&|||||||\rPID|P1&hi||P3~P4|P5^P6\r";
        msg.get("PID").field(1).getComp(1).getSubcomp(2).changeField("hi");
        msg.rebuild();
        assertEquals(newMessage, msg.toString());


        //Mike's test changed to getrep
        originalMessage = "MSH|^~\\&|||||||\rPID|P1||P3~P4|P5^P6\r";
        msg = new Hl7Record(originalMessage);
        newMessage = "MSH|^~\\&|||||||\rPID|P1~hi||P3~P4|P5^P6\r";
        msg.get("PID").field(1).getRep(2).changeField("hi");
        msg.rebuild();
        assertEquals(newMessage, msg.toString());


        //Test getComp before and after rebuild
        originalMessage = "MSH|^~\\&|||||||\rPID|P1||P3~P4|P5^P6\r";
        msg = new Hl7Record(originalMessage);
        Hl7Record frozenmsg = new Hl7Record(originalMessage);
        assertEquals("Normal getComp on present component", "P1", msg.get("PID").field(1).getComp(1).toString());
        assertEquals("getComp on missing component", "", msg.get("PID").field(1).getComp(2).toString());
        assertEquals("There should be no extra ^", "P1", msg.get("PID").field(1).toString());
        assertEquals("Count should be 1", 1, msg.get("PID").field(1).getCompCount());
        assertEquals("Another getComp before rebuild", "P1", msg.get("PID").field(1).getComp(1).toString());
        assertEquals("Another getComp before rebuild", "", msg.get("PID").field(1).getComp(2).toString());
        assertEquals(frozenmsg.toString(), msg.toString());
        msg.rebuild();
        assertEquals("There should be no extra ^ after rebuild", "P1", msg.get("PID").field(1).toString());
        assertEquals("Count should be 1", 1, msg.get("PID").field(1).getCompCount());
        assertEquals(frozenmsg.toString(), msg.toString());


        //Test getSubcomp before and after rebuild
        originalMessage = "MSH|^~\\&|||||||\rPID|P1||P3~P4|P5^P6\r";
        msg = new Hl7Record(originalMessage);
        frozenmsg = new Hl7Record(originalMessage);
        assertEquals("Normal getComp on present component", "P1", msg.get("PID").field(1).getComp(1).getSubcomp(1).toString());
        assertEquals("getSubcomp on missing component", "", msg.get("PID").field(1).getComp(1).getSubcomp(2).toString());
        assertEquals("There should be no extra &", "P1", msg.get("PID").field(1).toString());
        assertEquals("Count should be 1", 1, msg.get("PID").field(1).getCompCount());
        assertEquals("Subcomponent Count should be 1", 1, msg.get("PID").field(1).getSubcomp(1).getCompCount());
        assertEquals("Another getSubcomp before rebuild", "P1", msg.get("PID").field(1).getComp(1).getSubcomp(1).toString());
        assertEquals("Another getSubcomp before rebuild", "", msg.get("PID").field(1).getComp(1).getSubcomp(2).toString());
        assertEquals(frozenmsg.toString(), msg.toString());
        msg.rebuild();
        assertEquals("There should be no extra ^ or & after rebuild", "P1", msg.get("PID").field(1).toString());
        assertEquals("Count should still be 1 after rebuild", 1, msg.get("PID").field(1).getCompCount());
        assertEquals("Subcomponent Count should still be 1 after rebuild", 1, msg.get("PID").field(1).getSubcomp(1).getCompCount());
        assertEquals("Message should be unchanged after rebuild", frozenmsg.toString(), msg.toString());


        //Test getRep before and after rebuild
        originalMessage = "MSH|^~\\&|||||||\rPID|P1||P3~P4|P5^P6\r";
        msg = new Hl7Record(originalMessage);
        frozenmsg = new Hl7Record(originalMessage);
        assertEquals("Normal getRep on present repetition", "P1", msg.get("PID").field(1).getRep(1).toString());
        assertEquals("Normal getRepCount should be 1", 1, msg.get("PID").field(1).getRepCount());
        assertEquals("getRep on missing repetition", "", msg.get("PID").field(1).getRep(2).toString());
        assertEquals("There should be no extra ~", "P1", msg.get("PID").field(1).toString());
        assertEquals("getCompCount should be 1", 1, msg.get("PID").field(1).getCompCount());
        assertEquals("getRepCount should be 1", 1, msg.get("PID").field(1).getRepCount());
        assertEquals("getSubcompCount should be 1", 1, msg.get("PID").field(1).getComp(1).getSubcompCount());

        assertEquals("Subcomponent Count should be 1", 1, msg.get("PID").field(1).getSubcomp(1).getCompCount());
        assertEquals("Another getRep before rebuild", "P1", msg.get("PID").field(1).getRep(1).toString());
        assertEquals("Another getRep before rebuild", "", msg.get("PID").field(1).getRep(2).toString());
        assertEquals(frozenmsg.toString(), msg.toString());
        msg.rebuild();
        assertEquals("There should be no extra ^ or & or ~ after rebuild", "P1", msg.get("PID").field(1).toString());
        assertEquals("Count should still be 1 after rebuild", 1, msg.get("PID").field(1).getRepCount());
        assertEquals("Subcomponent Count should still be 1 after rebuild", 1, msg.get("PID").field(1).getSubcomp(1).getCompCount());
        assertEquals("Message should be unchanged after rebuild", frozenmsg.toString(), msg.toString());
    }

    /**
     * Test of count_segment method, of class ihealth.hl7_extensions.
     */
    public void testReadOnlyComp_getFieldObj() {
        String originalMessage = "MSH|^~\\&|||||||\rPID|P1||P3~P4|P5^P61&P62\r";
        Hl7Record msg = new Hl7Record(originalMessage);
        assertEquals("Check segment has not changed", originalMessage, msg.toString());
        assertEquals("Check referencing correct fields using get", "P1", msg.getField("PID:1").toString());
        assertEquals("Check referencing correct fields using get", "P5^P61&P62", msg.getField("PID:4").toString());
        assertEquals("Check referencing correct fields using get", "P61&P62", msg.getField("PID:4-0-2").toString());
        assertEquals("Check referencing correct fields using get", "P61", msg.getField("PID:4-0-2-1").toString());
        assertEquals("Check referencing correct fields using get", "P62", msg.getField("PID:4-0-2-2").toString());
        // Warm up to ensure we are referencing the correct fields
        assertEquals("Check field PID-1 is P1", "P1", msg.getField("PID:1").toString());
        assertEquals("Check field PID-2 is Empty", "", msg.getField("PID:2").toString());
        assertEquals("Check field PID-3 is P3~P4", "P3~P4", msg.getField("PID:3").toString());

        // Now check with and without component being there
        assertEquals("Check one component P1", "P1", msg.getField("PID:1-0-1").toString());
        msg.rebuild();
        assertEquals("Check segment has not changed", originalMessage, msg.toString());

        String newMessage = "MSH|^~\\&|||||||\rPID|P1^hi||P3~P4|P5^P61&P62\r";
        msg.getFieldObj("PID:1-0-2").changeField("hi");
        msg.rebuild();
        assertEquals(newMessage, msg.toString());

        // Testing the logic that prevents instantiated but never populated fields
        // from being included in rebuilt messages
        msg = new Hl7Record(originalMessage);
        assertEquals("Check second (non-existing) component P1", "", msg.getField("PID:1-0-2").toString());
        msg.rebuild();
        assertEquals("Check segment has not changed", originalMessage, msg.toString());

        // More complex versions of before;
        assertEquals("Check second (non-existing) component P1", "", msg.getFieldObj("PID:1-0-2-3").toString());
        msg.rebuild();
        assertEquals("Check segment has not changed", originalMessage, msg.toString());

        // Finally, populate a deeply nested instantiated subfield and make sure that it
        // actually updates right on the rebuild.
        newMessage = "MSH|^~\\&|||||||\rPID|P1|^^&&&hey|P3~P4|P5^P61&P62\r";
        assertEquals("Check second (non-existing) component P1", "", msg.getFieldObj("PID:2-0-3-4").toString());
        msg.getFieldObj("PID:2-0-3-4").changeField("hey");
        msg.rebuild();
        assertEquals("Check segment has not changed", newMessage, msg.toString());
    }

    public void testReadOnlyComp_getField_end_of_segment() {
        String originalMessage = "MSH|^~\\&|||||||\rPID|P1||P3~P4|P5^P61&P62\r";
        Hl7Record msg = new Hl7Record(originalMessage);
        assertEquals("Check segment has not changed", originalMessage, msg.toString());
        assertEquals("Check referencing correct fields using get", "P1", msg.getField("PID:1").toString());
        assertEquals("Check referencing correct fields using get", "P5^P61&P62", msg.getField("PID:4").toString());
        assertEquals("Check referencing correct fields using get", "P61&P62", msg.getField("PID:4-0-2").toString());
        assertEquals("Check referencing correct fields using get", "P61", msg.getField("PID:4-0-2-1").toString());
        assertEquals("Check referencing correct fields using get", "P62", msg.getField("PID:4-0-2-2").toString());
        // Warm up to ensure we are referencing the correct fields
        assertEquals("Check field PID-1 is P1", "P1", msg.getField("PID:1").toString());
        assertEquals("Check field PID-2 is Empty", "", msg.getField("PID:2").toString());
        assertEquals("Check field PID-3 is P3~P4", "P3~P4", msg.getField("PID:3").toString());

        // Now check without component being there
        assertEquals("Check non-exiting component", "", msg.get("PID").field(1).getComp(2).toString());
        msg.rebuild();
        assertEquals("Check segment has not changed", originalMessage, msg.toString());

        // Now check beyond end of segment using getField
        assertEquals("Check one component P1", "", msg.getField("PID:9").toString());
        msg.rebuild();
        assertEquals("Check segment has not changed", originalMessage, msg.toString());

        // Now check beyond end of segment using get
        assertEquals("Check one component P1", "", msg.get("PID").field(9).toString());
        msg.rebuild();
        assertEquals("Check segment has not changed", originalMessage, msg.toString());

        // Now check without repition being there
        assertEquals("Warm-up on existing field ", "P3~P4", msg.get("PID").field(3).toString());
        assertEquals("Warm-up on existing field repitition", "P4", msg.get("PID").field(3).getRep(2).toString());
        assertEquals("Now get a non-existing repition using get", "", msg.get("PID").field(3).getRep(3).toString());
        msg.rebuild();
        assertEquals("Check segment has not changed", originalMessage, msg.toString());

        // Now check without repition being there using getField
        assertEquals("Warm-up on existing field ", "P3~P4", msg.getField("PID:3").toString());
        assertEquals("Warm-up on existing field repitition", "P4", msg.getField("PID:3-2").toString());
        assertEquals("Now get a non-existing repition using getField", "", msg.getField("PID:3-0-3").toString());
        msg.rebuild();
        assertEquals("Check segment has not changed", originalMessage, msg.toString());

        // Now check without repition being there using getFieldObj
        assertEquals("Warm-up on existing field ", "P3~P4", msg.getFieldObj("PID:3").toString());
        assertEquals("Warm-up on existing field repitition", "P4", msg.getFieldObj("PID:3-2").toString());
        assertEquals("Now get a non-existing repition using getField", "", msg.getFieldObj("PID:3-0-3").toString());
        msg.rebuild();
        assertEquals("Check segment has not changed", originalMessage, msg.toString());

    }

    public void testRepeatingSegmentSnip() {
        String repRec = "MSH|^~\\&|ABC|DEF|||20040825120000||ADT^A18||P|2.3||||NE|\r" +
                "EVN|\rPID||||SMITH^BOB^A|||\rRCX||test1||\rRCX||test2||\rRCX||test3||\r" +
                "RCX||test4||\rAIP|\rNTE|\rNTE|\rNTE|\r";
        Hl7Record repHl7 = new Hl7Record(repRec);
        FieldMatch fm = FieldMatch.verifyFields("RCX1:2");
        assertEquals("test1", fm.getField(repHl7));
        fm = FieldMatch.verifyFields("RCX2:2");
        assertEquals("test2", fm.getField(repHl7));
        fm = FieldMatch.verifyFields("RCX3:2");
        assertEquals("test3", fm.getField(repHl7));
        fm = FieldMatch.verifyFields("RCX4:2");
        assertEquals("test4", fm.getField(repHl7));
    }

    public void testEscapedFields() {
        String escRec = "MSH|^~\\&|ABC|DEF|\r" +
                "OBX|||||BASIC FIELD|\r" +
                "OBX|||||BASIC\\F\\FIELD|\r" +
                "OBX|||||BASIC\\S\\FIELD|\r" +
                "OBX|||||BASIC\\T\\FIELD|\r" +
                "OBX|||||BASIC\\R\\FIELD|\r" +
                "OBX|||||BASIC\\E\\FIELD|\r" +
                "OBX|||||A \\T\\ B \\T\\ C \\T\\ D|\r";
        Hl7Record escHl7 = new Hl7Record(escRec);
        // Field escape
        assertEquals("BASIC\\F\\FIELD", escHl7.get("OBX", 2).field(5).toString());
        assertEquals("BASIC|FIELD", escHl7.get("OBX", 2).field(5).toStringEsc());
        // Comp escape
        assertEquals("BASIC\\S\\FIELD", escHl7.get("OBX", 3).field(5).toString());
        assertEquals("BASIC^FIELD", escHl7.get("OBX", 3).field(5).toStringEsc());
        // Subcomp escape
        assertEquals("BASIC\\T\\FIELD", escHl7.get("OBX", 4).field(5).toString());
        assertEquals("BASIC&FIELD", escHl7.get("OBX", 4).field(5).toStringEsc());
        // Repeat escape
        assertEquals("BASIC\\R\\FIELD", escHl7.get("OBX", 5).field(5).toString());
        assertEquals("BASIC~FIELD", escHl7.get("OBX", 5).field(5).toStringEsc());
        // Escape escape
        assertEquals("BASIC\\E\\FIELD", escHl7.get("OBX", 6).field(5).toString());
        assertEquals("BASIC\\FIELD", escHl7.get("OBX", 6).field(5).toStringEsc());
        // Multiples escape
        assertEquals("A \\T\\ B \\T\\ C \\T\\ D", escHl7.get("OBX", 7).field(5).toString());
        assertEquals("A & B & C & D", escHl7.get("OBX", 7).field(5).toStringEsc());
    }

    public void testEscapeFields() {
        String escRec = "MSH|^~\\&|ABC|DEF|\r" +
                "OBX|||||BASIC FIELD|\r";
        Hl7Record escHl7 = new Hl7Record(escRec);
        // Field escape
        escHl7.get("OBX").field(5).changeFieldEsc("BASIC|FIELD");
        assertEquals("BASIC\\F\\FIELD", escHl7.get("OBX").field(5).toString());
        // Comp escape
        escHl7.get("OBX").field(5).changeFieldEsc("BASIC^FIELD");
        assertEquals("BASIC\\S\\FIELD", escHl7.get("OBX").field(5).toString());
        // Subcomp escape
        escHl7.get("OBX").field(5).changeFieldEsc("BASIC&FIELD");
        assertEquals("BASIC\\T\\FIELD", escHl7.get("OBX").field(5).toString());
        // Repeat escape
        escHl7.get("OBX").field(5).changeFieldEsc("BASIC~FIELD");
        assertEquals("BASIC\\R\\FIELD", escHl7.get("OBX").field(5).toString());
        // Escape escape
        escHl7.get("OBX").field(5).changeFieldEsc("BASIC\\FIELD");
        assertEquals("BASIC\\E\\FIELD", escHl7.get("OBX").field(5).toString());
    }

    public void testDoubleCheckMshCounts() {
        String mshEx = "MSH|^~\\&|ABC||DEV||20090113083316||ORU^R01|12345678901|P|2.1||||\r" +
                "PID|||1234567^^^1||AAAABCDEFRGHII,B||19901111|F||||||||||12345678||\r";
        Hl7Record hl7 = new Hl7Record(mshEx);
        assertEquals("ABC", hl7.get("MSH").field(3).toString());
        assertEquals("DEV", hl7.get("MSH").field(5).toString());
        assertEquals("ORU^R01", hl7.get("MSH").field(9).toString());
        assertEquals("1234567^^^1", hl7.get("PID").field(3).toString());

        Hl7Field[] fields = hl7.get("MSH").fieldAll();
        assertEquals("ABC", fields[2].toString());
        assertEquals("DEV", fields[4].toString());
    }

    public void testMshFieldAll() {
        String msg = "MSH|^~\\&|ADT1|MCM|LABADT|MCM|198808181126|SECURITY|ADT^A01|MSG00001|P|2.3.1|\r" +
                "EVN|A01|198808181123||\r" +
                "PID|1||PATID1234^5^M11^ADT1^MR^MCM~123456789^^^USSSA^SS||JONES^WILLIAM^A^III||19610615|MÂ||C|1200 N ELM STREET^^GREENSBORO^NC^27401 1020|GL|(919)379 1212|(919)271 3434||S||PATID12345001^2^M10^ADT1^AN^A|123456789|987654^NC|\r" +
                "NK1|1|JONES^BARBARA^K|WI^WIFE||||NK^NEXT OF KIN\r" +
                "PV1|1|I|2000^2012^01||||004777^LEBAUER^SIDNEY^J.|||SUR||||ADM|A0|";

        Hl7Record hr = new Hl7Record(msg);
        Hl7Segment msh = hr.get(1);
        Hl7Field[] field_tab = msh.fieldAll();

        assertEquals(field_tab[0].toString(), hr.get("MSH").field(1).toString());
        assertEquals(field_tab[1].toString(), hr.get("MSH").field(2).toString());
        assertEquals(field_tab[2].toString(), hr.get("MSH").field(3).toString());
        assertEquals(field_tab[3].toString(), hr.get("MSH").field(4).toString());

        msh = hr.get("MSH");
        field_tab = msh.fieldAll();

        assertEquals(field_tab[0].toString(), hr.get("MSH").field(1).toString());
        assertEquals(field_tab[1].toString(), hr.get("MSH").field(2).toString());
        assertEquals(field_tab[2].toString(), hr.get("MSH").field(3).toString());
        assertEquals(field_tab[3].toString(), hr.get("MSH").field(4).toString());

        Hl7Segment pid = hr.get(3);
        field_tab = pid.fieldAll();

        assertEquals(field_tab[0].toString(), hr.get("PID").field(1).toString());
        assertEquals(field_tab[1].toString(), hr.get("PID").field(2).toString());
        assertEquals(field_tab[2].toString(), hr.get("PID").field(3).toString());
        assertEquals(field_tab[3].toString(), hr.get("PID").field(4).toString());

    }
}
