/*
 * TestHuge
 * 
 * This is an attempt to work with an extremely large message in Light HL7
 * Library.  In windows some of the parsing REGEXs cause java stack overflow
 * exceptions on pretty much every version of java tested (but not on OS X,
 * and possibly other systems).  That's not a bug with the library, but I'm
 * forced to work around it.
 *
 * Copyright (C) 2004-2007 M Litherland
 */
package org.nule.lighthl7lib.tests;

import java.util.regex.*;

import junit.framework.TestCase;

import org.nule.lighthl7lib.hl7.*;

/**
 *
 * @author Administrator
 */
public class TestHuge extends TestCase {
    
    public void testClean() {
        try {
            String s = Hl7Record.cleanString(huge);
            if (s == null || s.equals("")) {
                TestCase.fail();
            }
        } catch (Exception e) {
            TestCase.fail();
        }
    }
    
    private String huge;

    protected void setUp() throws Exception {
        super.setUp();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 915; i++) {
            sb.append("MSH|^~\\&|IBEX|SVMMC|ED|SVMMC|20080428020202||ORU^R01|20080428020202|D|2.5|||||\r");
        }
        huge = sb.toString();
    }
}
