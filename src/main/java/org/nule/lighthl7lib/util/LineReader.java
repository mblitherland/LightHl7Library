/*
 * LineReader.java
 *
 * Created on March 24, 2006, 3:03 PM
 *
 * Copyright (C) 2004-2006 M Litherland
 */

package org.nule.lighthl7lib.util;

import java.io.*;

/**
 *
 * @author litherm
 *
 * The LineReader class works like a hacked up BufferedReader except that
 * the record separator is always just a newline.  
 */
public class LineReader {
    
    private static final String NEWLINE = "\n";
    protected Reader r;
    protected StringBuffer sb;
    
    /**
     * Create a new LineReader object from whatever Reader was
     * passed to us.
     *
     * @param newR a Reader.
     */
    public LineReader(Reader newR) {
        r = newR;
        sb = new StringBuffer();
    }
    
    /**
     * Read from our reader returning only when we find a
     * newline in our data or we run out of data from our
     * reader.
     *
     * @return A string containing our data, or if we're out of data a null.
     * @throws IOException if our reader throws one to us.
     */
    public String readLine() throws IOException {
        char[] c = new char[1024];
        while (sb.indexOf(NEWLINE) == -1) {
            int size = r.read(c);
            if (size == -1) {
                if (sb.length() == 0) {
                    return null;
                } else {
                    String s = sb.toString();
                    sb.delete(0, sb.length());
                    return s;
                }
            }
            String read = new String(c);
            sb.append(read.substring(0, size));
        }
        int pos = sb.indexOf(NEWLINE);
        String retVal = sb.substring(0, pos + 1);
        sb.delete(0, pos + 1);
        return retVal;
    }
}
