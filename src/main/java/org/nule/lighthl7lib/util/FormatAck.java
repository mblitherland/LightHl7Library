/*
 * FormatAck.java
 *
 * Created on March 24, 2006, 1:46 PM
 *
 * Copyright (C) 2005-2012 M Litherland
 */

package org.nule.lighthl7lib.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.nule.lighthl7lib.hl7.*;

/**
 *
 * @author litherm
 *
 * Format an ack or a nack based on a supplied message.
 */
public class FormatAck {
    private String ackString = "MSH|^~\\&| | | | | ||ACK|12345678| | |\rMSA|AA| |MSG OK|\r";
    private Hl7Record ack;
    private Hl7Record in;
    
    public FormatAck(String msg) {
        ack = new Hl7Record(ackString);
        //String clean = Hl7Record.cleanString(msg);
        in = new Hl7Record(msg);
        ack.get("MSH").field(3).changeField(in.get("MSH").field(5).toString());
        ack.rebuild();
        ack.get("MSH").field(4).changeField(in.get("MSH").field(6).toString());
        ack.rebuild();
        ack.get("MSH").field(5).changeField(in.get("MSH").field(3).toString());
        ack.rebuild();
        ack.get("MSH").field(6).changeField(in.get("MSH").field(4).toString());
        ack.rebuild();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = sdf.format(new Date());
        ack.get("MSH").field(7).changeField(date);
        ack.rebuild();
        ack.get("MSH").field(11).changeField(in.get("MSH").field(11).toString());
        ack.get("MSH").field(12).changeField(in.get("MSH").field(12).toString());
        ack.get("MSA").field(2).changeField(in.get("MSH").field(10).toString());
        ack.rebuild();
    }
    
    public String getAck() {
        return ack.toString();
    }
    
    public String getNack() {
        ack.get("MSA").field(1).changeField("AR");
        ack.get("MSA").field(3).changeField("Application Rejection");
        ack.rebuild();
        return ack.toString();
    }
    
    public String getException() {
        ack.get("MSA").field(1).changeField("AE");
        ack.get("MSA").field(3).changeField("Application Exception");
        ack.rebuild();
        return ack.toString();
    }
}
