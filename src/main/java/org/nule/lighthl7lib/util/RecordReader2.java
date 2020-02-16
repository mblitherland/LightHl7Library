/*
 * RecordReader2.java
 *
 * Created on March 24, 2006, 3:05 PM
 *
 * Copyright (C) 2004-2012 M Litherland
 */

package org.nule.lighthl7lib.util;

import java.io.*;

/**
 *
 * @author litherm
 *
 * Implement all the same features as LineReader, but read
 * in a record oriented fashion.
 */
public class RecordReader2 extends LineReader {
    
    private String sor;
    
    /**
     * Create a record reader object from a reader.
     *
     * @param r - a reader object.
     */
    public RecordReader2(Reader r) {
        super(r);
        sor = "";
    }
    
    /**
     * Define the start of record
     *
     * @param mySor - The start of record string we wish to define
     */
    public void setStartOfRecord(String mySor) {
        sor = mySor;
    }
    
    /**
     * Checks to see if a complete record already exists in
     * the string buffer
     * @return
     */
    public String hasCompleteRecord() {
        int pos = sb.indexOf(sor);
        if (pos != -1) {
            int pos2 = sb.indexOf(sor, pos+1);
            if (pos2 != -1) {
                String retVal = sb.substring(pos, pos2);
                sb.delete(0, pos2);
                return retVal;
            }
        }
        return null;
    }
    
    /**
     * Read in a record at a time with some tricky buffering.  Because
     * this is HL7-centric, we strip newlines out of the record.
     */
    @Override
    public String readLine() throws IOException {
        String retVal;
        // Case where we've read in multiple records and a full record
        // is still sitting within our stringbuffer (which is inherited
        // from LineReader)
        String complete = hasCompleteRecord();
        if (complete != null) {
            return complete.replaceAll("\n", "");
        }
        while (true) {
            char[] c = new char[1024];
            if (r.read(c) == -1) {
                if (c[0] != 0) {
                    sb.append(new String(c));
                }
                if (sb.length() == 0) {
                    return null;
                } else {
                    String s = sb.toString();
                    sb.delete(0, sb.length());
                    return s.replaceAll("\n", "");
                }
            }
            sb.append(c);
            complete = hasCompleteRecord();
            if (complete != null) {
                return complete.replaceAll("\n", "");
            }
        }
    }
}
