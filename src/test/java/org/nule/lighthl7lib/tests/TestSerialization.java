package org.nule.lighthl7lib.tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import junit.framework.TestCase;
import org.nule.lighthl7lib.hl7.Hl7Field;
import org.nule.lighthl7lib.hl7.Hl7Record;
import org.nule.lighthl7lib.hl7.Hl7Segment;
import org.nule.lighthl7lib.hl7.Hl7SegmentGroup;

/**
 *
 * @author John Paulett (john -at- 7oars.com)
 */
public class TestSerialization extends TestCase {

    private Hl7Record record;

    protected void setUp() throws Exception {
        record = new Hl7Record(getTestMsg1());
    }

    public void testSerializeRecord() throws Exception {
        byte[] s = serialize(record);
        Hl7Record newRecord = (Hl7Record) deserialize(s);

        Hl7Segment segment = newRecord.getSegment("OBR");
        // should have one obr group
        assertEquals("OBR", segment.getId());
    }

    public void testSerializeSegment() throws Exception {
        byte[] s = serialize(record.getSegment("PID"));
        Hl7Segment newSegment = (Hl7Segment) deserialize(s);

        assertEquals("PID", newSegment.getId());
    }

    public void testSerializeSegmentGroup() throws Exception {
        byte[] s = serialize(record.getGroup("OBR"));
        Hl7SegmentGroup newSegmentGroup = (Hl7SegmentGroup) deserialize(s);

        assertEquals("OBX", newSegmentGroup.get("OBX").getId());
    }

    public void testSerializeField() throws Exception {
        byte[] s = serialize(record.getSegment("PID").field(3));
        Hl7Field newField = (Hl7Field) deserialize(s);

        assertEquals("12345678", newField.toString());
    }

    /**
     * Simple method for Serializing an object into a byte array.
     * @param object
     * @return
     * @throws java.io.IOException
     */
    private byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream objstream = new ObjectOutputStream(stream);
        objstream.writeObject(object);

        byte[] retVal = stream.toByteArray();

        objstream.close();
        stream.close();

        return retVal;
    }

    /**
     * Turn a byte array back into an Object.
     * @param byteStream
     * @return
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    private Object deserialize(byte[] byteStream) throws IOException, ClassNotFoundException {
        ObjectInputStream objstream = new ObjectInputStream(new ByteArrayInputStream(byteStream));

        Object object = objstream.readObject();

        objstream.close();

        return object;
    }

    private String getTestMsg1() {
        return "MSH|^~\\&|NULEORG|LHL|||20081022120000||ACK||P|2.3||||NE|\r" +
                "PID|||12345678||SMITH^PAT|\r" +
                "OBR|NOTAVALIDRESULT|OBR1|\r" +
                "OBX|NOTAVALIDTEST|OBX1|\r";
    }
}
