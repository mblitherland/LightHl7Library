/*
 * Hl7Record.java
 *
 * Copyright (C) 2004-2012 M Litherland
 */

package org.nule.lighthl7lib.hl7;

import java.io.Serializable;
import java.util.*;
import java.util.regex.*;
import org.nule.lighthl7lib.util.*;

/**
 *
 * @author litherm
 *
 * This is a utility class that can be used to manipulate or
 * extract HL7 messages.  No attempt has been made to make this
 * object thread safe.
 */
public class Hl7Record implements Serializable {
    private static final long serialVersionUID = -4985318546516958032L;

    
    private String record;
    private String[] seps;
    private List segs = null;
    private List groups = new ArrayList();
    
    /**
     * Create a new HL7 record object from a string.
     *
     * @param record
     */
    public Hl7Record(String newRecord) {
        record = newRecord;
        seps = Hl7RecordUtil.setSeparators(record);
    }
    
    /**
     * Create a new HL7 record object from an array of strings listing segment
     * headers.  We don't assume to prefix the record with the MSH segment, so
     * you should really consider including one, if you want your recort to be
     * parsable by anything.
     *
     * Note: to create a very minimal message just pass it {"MSH"}.
     */
    public Hl7Record(String[] segHeaders) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segHeaders.length; i++) {
            sb.append(segHeaders[i]).append("|");
            if (i == 0) {
                sb.append(Hl7RecordUtil.defaultDelims);
            }
            sb.append("\r");
        }
        record = sb.toString();
        seps = Hl7RecordUtil.setSeparators(record);
    }
    
    /**
     * Similar to the Hl7Record(String[]) constructor, except you specify the
     * delimiters to use.  Please understand Java escape characters before you
     * use this (or at least test it well...)
     *
     * Note: to create a very minimal message just pass it {"MSH"} and the
     * delims of your choice.
     */
    public Hl7Record(String[] segHeaders, String delims) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segHeaders.length; i++) {
            sb.append(segHeaders[i]).append(delims.substring(0, 1));
            if (i == 0) {
                sb.append(delims.substring(1));
            }
            sb.append("\r");
        }
        record = sb.toString();
        seps = Hl7RecordUtil.setSeparators(record);
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
     * Returns a list of segment headers based upon the current makeup of the message
     *
     * @return List
     */
    public List listSegments() {
        List l = new ArrayList();
        makeSegments();
        for (int i = 0; i < segs.size(); i++) {
            l.add(((Hl7Segment) segs.get(i)).getId());
        }
        return l;
    }
    
    /**
     * Parse the segments out of the record we were created with.
     * Each segments becomes a new Hl7Segment object.
     */
    private void makeSegments() throws IllegalArgumentException {
        if (segs != null) {
            return;
        }
        segs = new ArrayList();
        String[] segments = record.split(Hl7RecordUtil.sep0);
        for (int i = 0; i < segments.length; i++) {
            if (segments[i].length() > 2) {
                segs.add(new Hl7Segment(segments[i], getSeparators()));
            } else {
                throw new IllegalArgumentException("Too short segment received.");
            }
        }
    }
    
    /**
     * Returns the number of segments
     *
     * @return int
     */
    public int size() {
        makeSegments();
        return segs.size();
    }
    
    /**
     * Append a new segment to the end of the HL7 record.
     *
     * @param segHeader 
     * @throws IllegalArgumentException if the header isn't three characters.
     */
    public void append(String segHeader) {
        makeSegments();
        if (segHeader.length() != 3) {
            throw new IllegalArgumentException("Header must be three characters.");
        }
        segs.add(new Hl7Segment(segHeader+"|", seps));
        rebuild();
    }
    
    /**
     * Add a segment to the record at the specified position.  The position is
     * handled the same as it would be for java.util.List.add(int, Object)
     *
     * @param position An index position of where to insert the segment.
     * @param segHeader 
     * @throws IllegalArgumentException if the header isn't three characters.
     */
    public void add(int position, String segHeader) {
        makeSegments();
        if (segHeader.length() != 3) {
            throw new IllegalArgumentException("Header must be three characters.");
        }
        segs.add(position, new Hl7Segment(segHeader+"|", seps));
        rebuild();
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
        makeSegments();
        int found = 0;
        for (int i = 0; i < segs.size(); i++) {
            if (id.equals(((Hl7Segment) segs.get(i)).getId())) {
                found++;
                if (found == count) {
                    return (Hl7Segment) segs.get(i);
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
        makeSegments();
        return (Hl7Segment) segs.get(id - 1);
    }
    
    /**
     * Returns all available Hl7Segments as an array of that kind.
     *
     * @return Hl7Segment[]
     */
    public Hl7Segment[] getAll() {
        makeSegments();
        Hl7Segment[] segments = new Hl7Segment[segs.size()];
        for (int i = 0; i < segs.size(); i++) {
            segments[i] = (Hl7Segment) segs.get(i);
        }
        return segments;
    }
    
    /**
     * Get a segment group based upon a provided segment ID.  This essentially 
     * returns an array of segments starting with the segment you identify by ID
     * and continues until either another segment with the same ID is found or
     * until the record ends.
     *
     * Warning: calling any method that creates segment groups can cause unexpected
     * behavoirs when rebuilding.  Only the most recently requested group or groups
     * are stored for the rebuild to call.  Adding segments using the normal add
     * method (which also causes an immediate rebuild) could potentially throw the
     * stored indexes off, and further add to the confusion.
     * 
     * @param segId The segment ID you wish to retrieve.
     * @param count The count of the segment group you want, the first group is "1"
     * @return the Hl7SegmentGroup
     */
    public Hl7SegmentGroup getGroup(String segId, int count) {
        makeSegments();
        groups = new ArrayList();
        Hl7SegmentGroup group = GroupBuilder.singleGroup(segs, segId, count, seps);
        groups.add(group);
        return group;
    }
      
    /**
     * The same as getGroup(String, int) except the count is assumed to be 1.
     *
     * Warning: calling any method that creates segment groups can cause unexpected
     * behavoirs when rebuilding.  Only the most recently requested group or groups
     * are stored for the rebuild to call.  Adding segments using the normal add
     * method (which also causes an immediate rebuild) could potentially throw the
     * stored indexes off, and further add to the confusion.
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
     * Warning: calling any method that creates segment groups can cause unexpected
     * behavoirs when rebuilding.  Only the most recently requested group or groups
     * are stored for the rebuild to call.  Adding segments using the normal add
     * method (which also causes an immediate rebuild) could potentially throw the
     * stored indexes off, and further add to the confusion.
     * 
     * @param segId
     * @return
     */
    public Hl7SegmentGroup[] getGroupAll(String segId) {
        makeSegments();
        groups = new ArrayList();
        Hl7SegmentGroup[] groupArray = GroupBuilder.allGroups(segs, segId, seps);
        groups.addAll(Arrays.asList(groupArray));
        return groupArray;
    }
    
    /**
     * Accept a string with an HL7 record somewhere in it and return
     * only the HL7 data out of it.
     * 
     * A null string or non-matching string will return null.
     *
     * @param dirty
     * @return String
     */
    public static String cleanString(String dirty) {
        if (dirty == null) {
            return null;
        }
        String result =  Hl7RecordUtil.safeCleanString(dirty);
        if (result.equals("")) {
            return null;
        }
        return result;
    }
    
    /**
     * Hand us a new string and regenerate the children.
     *
     * @param newRecord
     */
    public void changeRecord(String newRecord) {
        record = newRecord;
        seps = Hl7RecordUtil.setSeparators(record);
        segs = null;
    }
    
    /**
     * Rebuilds the record from the segments in case they
     * have changed.
     *
     * @return Newly constructed string of record.
     */
    public String rebuild() {
        // if segs is null, nothing to rebuild
        if (segs == null) {
            return toString();
        }
        StringBuilder newRecord = new StringBuilder();
        if (groups.size() > 0) {
            Hl7Segment[] segArray = getAll();
            segArray = GroupBuilder.rebuildSegmentsFromGroups(segArray, groups);
            groups = new ArrayList();
            for (int i = 0; i < segArray.length; i++) {
                newRecord.append(segArray[i].rebuild()).append(Hl7RecordUtil.sep0);
            }
        } else {
            for (int i = 0; i < segs.size(); i++) {
                newRecord.append(((Hl7Segment) segs.get(i)).rebuild()).append(Hl7RecordUtil.sep0);
            }
        }
        changeRecord(newRecord.toString());
        return newRecord.toString();
    }
    
    /**
     * Return the defined field as a string.
     * 
     * @param fieldDef a valid field definition.
     */
    public String getField(String fieldDef) {
        try {
            return getFieldObj(fieldDef).toString();
        } catch (NullPointerException e) {
            return null;
        }
    }
    
    /**
     * Return the defined field as an Hl7Field object.
     * 
     * @param fieldDef a valid field definition.
     */
    public Hl7Field getFieldObj(String fieldDef) {
        FieldMatch fm = FieldMatch.verifyFields(fieldDef);
        if (fm == null) {
            return null;
        }
        return fm.getFieldObj(this);
    }
    
    /**
     * Set the desired field with the specified string.  Will throw an exception
     * (NPE I believe) if the field definition is invalid.
     * 
     * @param fieldDef a valid field definition.
     * @param newValue a new string value for that field.
     */
    public void setField(String fieldDef, String newValue) {
        getFieldObj(fieldDef).changeField(newValue);
    }
    
    /**
     * Return the HL7 record as a string
     * 
     * @return the record string.
     */
    @Override
    public String toString() {
        return record;
    }
    
    /**
     * This returns all the segment IDs concatinated together.  This is used, as
     * far as I know, only interally by the interpret method that was added by a
     * library user for their own purposes.
     * 
     * @return a concatinated string of all message IDs.
     */
    public String getAllSegmentIds() {
        StringBuilder buffer = new StringBuilder();
        String[] segments = record.split(Hl7RecordUtil.sep0);
        for (int i = 0; i < segments.length; i++) {
            buffer.append((segments[i].length()>=3) ?  segments[i].substring(0,3) : "");
        }
        return buffer.toString();
    }
    
    /**
     * Use the allSegmentIds method to compare the message structure to desired
     * message segment patterns.
     * 
     * @param regex a pattern to compare segment IDs to.
     * @return the validation status.
     */
    public boolean validateMessageStructure(String regex){
        Pattern valid = Pattern.compile(regex);
        return valid.matcher(getAllSegmentIds()).matches();
    }
    
    /**
     * Added by a library user.
     * 
     * @param regex
     * @return
     */
    public Hl7Segment getSegment(String regex) {
        return getSegment(regex,1);
    }
    
    /**
     * Added by a library user.
     * 
     * @param regex
     * @return
     */
    public Hl7Segment getSegment(String regex,int regExMatchCount) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(getAllSegmentIds());
        //System.out.println( matcher.find());
        int count = 0;
        boolean found =  true;
        while(found && count < regExMatchCount){
            found = matcher.find();
            if (found){
                count++;
                //System.out.println("Start:"+matcher.start());
                //System.out.println("End:"+matcher.end());
                if (count==regExMatchCount && matcher.end()%3 == 0){
                    return this.get(matcher.end()/3);
                }
            }
        }
        return null;
    }
    
    /**
     * Added by a library user.
     * 
     * @param regex
     * @return
     */
    public String interpret(String expression){
        return interpret(null,1,expression);
    }
    
    /**
     * Added by a library user.
     * 
     * @param regex
     * @return
     */
    public String interpret(String segmentRegEx, int regExMatchCount, String expression){
        //EXPRESSION            ::= SEGMENT '^' FIELD ['^' REFINEMENT EXPRESSION*]
        //SEGMENT               ::= 'SEGMENT' '(' LITERAL ',' OCCURENCE_INDEX ')'
        //FIELD                 ::= 'FIELD' '(' FIELD_INDEX ',' REPEAT_INDEX  ')'
        //REFINEMENT EXPRESSION ::= REFINEMENT ^ REFINEMENT
        //REFINEMENT            ::= FIELD | COMPONENT | SUBCOMPONENT]
        //COMPONENT             ::= 'COMPONENT' '(' COMPONENT_INDEX ')'
        //SUBCOMPONENT          ::= 'SUBCOMPONENT' '(' SUBCOMPONENT_INDEX ')'
        //LITERAL               ::= CHARACTERS
        //FIELD_INDEX           ::= NUMBER
        //OCCURENCE_INDEX       ::= NUMBER
        //COMPONENT_INDEX       ::= NUMBER
        //SUBCOMPONENT_INDEX    ::= NUMBER
        //REPEAT_INDEX          ::= NUMBER
        //
        //EXAMPLE
        /*
         * SEGMENT(PID,1)^FIELD(5,2)^COMPONENT(2)
         *
         */
        String separator = "^";
        String exp = expression.replaceAll(" ", "");
        StringTokenizer tokenizer = new StringTokenizer(exp,separator);
        Hl7Segment hl7Segment = null;
        Hl7Field hl7Field = null;
        
        if (segmentRegEx!=null){
            hl7Segment = this.getSegment(segmentRegEx,regExMatchCount);
            
            if (hl7Segment==null) {
                return "";
            }
        }
        
        int counter = 0;
        while (tokenizer.hasMoreTokens()){
            counter++;
            
            String token = tokenizer.nextToken();
            
            if (token.startsWith("SEGMENT")){
                int startpos = token.indexOf('(');
                int endpos = token.indexOf(')');
                int commapos = token.indexOf(',');
                
                String segmentID = token.substring(startpos+1,commapos);
                int segmentOccurence = Integer.parseInt(token.substring(commapos+1,endpos));
                
                hl7Segment = this.get(segmentID,segmentOccurence);
            } else if (token.startsWith("FIELD")){
                int startpos = token.indexOf('(');
                int endpos = token.indexOf(')');
                int commapos = token.indexOf(',');
                
                int fieldIndex = Integer.parseInt(token.substring(startpos+1,commapos));
                int repeatIndex = Integer.parseInt(token.substring(commapos+1,endpos));
                
                if (hl7Field==null){
                    hl7Field = hl7Segment.field(fieldIndex);
                    if (hl7Field !=null && repeatIndex>1){
                        hl7Field = hl7Field.getRep(repeatIndex);
                    }
                }
            } else if (token.startsWith("COMPONENT")){
                int startpos = token.indexOf('(');
                int endpos = token.indexOf(')');
                
                int componentIndex = Integer.parseInt(token.substring(startpos+1,endpos));
                
                if (hl7Field !=null){
                    hl7Field = hl7Field.getComp(componentIndex);
                }
            } else if (token.startsWith("SUBCOMPONENT")){
                int startpos = token.indexOf('(');
                int endpos = token.indexOf(')');
                
                int subcomponentIndex = Integer.parseInt(token.substring(startpos+1,endpos));
                
                if (hl7Field !=null){
                    hl7Field = hl7Field.getSubcomp(subcomponentIndex);
                }
            }
            
            if (hl7Segment==null) {
                break;
            }
            
            if (counter > 1 && hl7Field==null) {
                break;
            }
        }
        
        return (hl7Field!=null ? hl7Field.toString() : "");
        
    }
}
