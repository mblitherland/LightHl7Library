/*
 * Hl7Record.java
 *
 * Useful things for the LHL.
 *
 * Copyright (C) 2004-2012 M Litherland
 */
package org.nule.lighthl7lib.hl7;

import java.util.regex.*;

/**
 *
 * @author mike
 */
public class Hl7RecordUtil {

    public static final String sep0 = "\r";
    public static final Pattern hl7match = Pattern.compile("(MSH[^\\r]+\\r(?:[A-Z0-9]{3}[^\\r]*\\r)+)");
    public static final String defaultDelims = "^~\\&|";
    
    private static Pattern firstSeg = Pattern.compile(".*?(MSH\\W.*)");
    private static Pattern nextSegs = Pattern.compile("[A-Z0-9]{3}.*");
    
    /**
     * Accepts an HL7 record, sets the object separators based upon
     * those contained in the HL7 record.  If you data contains mixed
     * separators this needs to be called before every record.
     *
     * @param record
     * @throws IllegalArgumentException
     */
    public static String[] setSeparators(String record) throws IllegalArgumentException {
        if (!record.startsWith("MSH") && !record.startsWith("BHS") && ! record.startsWith("FHS")) {
            throw new IllegalArgumentException("Record not HL7");
        }
        String[] seps = new String[10];
        // These are wrapped in character classes, because some characters like | need it.
        seps[0] = "[" + record.substring(3, 4) + "]"; // Field separator
        seps[1] = "[" + record.substring(4, 5) + "]"; // Component separator
        seps[2] = "[" + record.substring(5, 6) + "]"; // Repetition separator
        seps[3] = "[" + record.substring(7, 8) + "]"; // Subcomponent separator
        seps[4] = record.substring(3, 4); // Field separator
        seps[5] = record.substring(4, 5); // Component separator
        seps[6] = record.substring(5, 6); // Repetition separator
        seps[7] = record.substring(7, 8); // Subcomponent separator
        // Capture the escape character as well
        seps[8] = "[" + record.substring(6, 7) + "]";
        seps[9] = record.substring(6, 7);
        for (int i = 0; i < 10; i++) {
            // some characters are special in Java regexes and a pain to escape.
            seps[i] = replaceSeps(seps[i]);
        }
        return seps;
    }

    private static String replaceSeps(String in) {
        String result = in;
        if (in.equals("[^]")) {
            result = "[\\^]";
        } else if (in.equals("[\\]")) {
            result = "[\\\\]";
        } else if (in.equals("\\")) {
            result = "\\\\";
        }
        return result;
    }

    /**
     * Some java versions don't like the hl7match pattern above applied to large
     * messages.  Here we try to avoid it.
     * @param fieldDelim
     * @return
     */
    public static String safeCleanString(String hl7) {
        String[] all = hl7.split("\r");
        StringBuilder sb = new StringBuilder();
        int c;
        for (c = 0; c < all.length; c++) {
            Matcher m = firstSeg.matcher(all[c]);
            if (m.matches()) {
                sb.append(m.group(1)).append("\r");
                break;
            }
        }
        if (sb.length() > 0) {
            for (int i = c + 1; i < all.length; i++) {
                try {
                    if (nextSegs.matcher(all[i]).matches()) {
                        sb.append(all[i]).append("\r");
                    } else {
                        // Don't keep capturing after a non-segment is found
                        break;
                    }
                } catch (Exception e) {
                    // that's OK too, except we're done with the message
                    break;
                }
            }
        }
        return sb.toString();
    }

    public static String[] setDefaultSeparators() {
        String record = "MSH|" + defaultDelims + "\r";
        return setSeparators(record);
    }
}
