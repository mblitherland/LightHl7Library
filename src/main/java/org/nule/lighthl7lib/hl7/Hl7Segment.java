/*
 * Hl7Segment.java
 *
 * Created on March 24, 2006, 11:02 AM
 *
 * Copyright (C) 2004-2012 M Litherland
 */

package org.nule.lighthl7lib.hl7;

import java.io.Serializable;
import java.util.*;

/**
 *
 * @author litherm
 *
 * This class abstracts an HL7 segment.
 */
public class Hl7Segment implements Serializable {
    private static final long serialVersionUID = 6354801000294086100L;

    private static final String MSH = "MSH";
    private String segment;
    private String id;
    private String[] seps;
    private List fields;
    
    /**
     * Create a new segment object out of our string and separators. Figure
     * out our segment ID.
     *
     * @param mySegment
     * @param separators
     */
    public Hl7Segment(String mySegment, String[] separators) {
        segment = mySegment;
        id = segment.substring(0,3);
        seps = separators.clone();
        fields = null;
    }
    
    /**
     * Returns the list of separators we have found.  Only really
     * useful for testing.
     *
     * @return String[]
     */
    public String[] getSeparators() {
        return seps;
    }
    
    /**
     * Return the segment ID.
     *
     * @return char[]
     */
    public String getId() {
        return id;
    }
    
    /**
     * Return the segment itself as a string.
     *
     * @return the segment as a string.
     */
    @Override
    public String toString() {
        return segment;
    }
    
    /**
     * Private method to generate all the fields from within the segment
     * upon access.
     */
    private void makeFields() {
        fields = new ArrayList();
        String[] fieldList = segment.split(seps[0]);
        int i = 0;
        while (i < fieldList.length) {
            fields.add(new Hl7Field(fieldList[i], getSeparators()));
            i++;
        }
    }
    
    /**
     * Return all the fields as Hl7Field objects from the segment, starting with
     * field2 if MSH, field1 if anything else.
     *
     * @return Hl7Field objects as an array
     */
    public Hl7Field[] fieldAll() {
        if (fields == null) {
            makeFields();
        }
        int i = 0;
        int d = 1;
        Hl7Field[] retFields;
        if (MSH.equals(id)) {
            retFields = new Hl7Field[fields.size()];
            d = 0;
            retFields[i++] = new MshFieldOne(seps);
            retFields[i++] = (Hl7Field) fields.get(1);
        } else {
            retFields = new Hl7Field[fields.size() - 1];
        }
        while (i < fields.size() - d) {
            retFields[i] = (Hl7Field) fields.get(i + d);
            i++;
        }
        return retFields;
    }
    
    /**
     * Return the specified HL7 field adjusting as necessary for the MSH segment.
     *
     * @param fieldId
     * @return Hl7Field
     */
    public Hl7Field field(int fieldId) {
        int selectedField = fieldId;
        if (fields == null) {
            makeFields();
        }
        if (MSH.equals(id)) {
            if (selectedField == 1) {
                return new MshFieldOne(seps);
            }
            selectedField--;
        }
        if (selectedField >= fields.size()) {
            for (int i = fields.size(); i <= selectedField; i++) {
                fields.add(new Hl7Field("", seps, false));
            }
        }
        try {
            return (Hl7Field) fields.get(selectedField);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
    
    /**
     * Hand us a new string and regenerate the children.
     *
     * @param newRecord a segment as a string to replace this segment.
     */
    public void changeSegment(String newSegment) {
        segment = newSegment;
        fields = null;
    }
    
    /**
     * Rebuilds the segment from the fields in case they
     * have changed.
     *
     * @return Newly constructed string of record.
     */
    public String rebuild() {
        if (fields == null) {
            return segment;
        }
        StringBuilder newSegment = new StringBuilder();
        int lastExistingField = 0;
        for (int i = 0; i < fields.size(); i++) {
            if (((Hl7Field) fields.get(i)).isExisting()) {
                lastExistingField = i;
            }
        }
    	for (int i = 0; i <= lastExistingField; i++) {
            if (i > 0) {
                newSegment.append(seps[4]);
            }
            newSegment.append(((Hl7Field) fields.get(i)).rebuild());
    	}
        changeSegment(newSegment.toString());
        return newSegment.toString();
    }
}
