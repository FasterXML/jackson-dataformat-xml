package com.fasterxml.jackson.dataformat.xml.ser;

import java.nio.*;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

/**
 * See <a href="https://github.com/FasterXML/jackson-dataformat-xml/issues/270">issue #270</a>
 * for details
 */
public class TestBinaryStreamToXMLSerialization extends XmlTestBase
{
    private final XmlMapper MAPPER = newMapper();

    public void testWith0Bytes() throws Exception 
    {
        String xml = MAPPER.writeValueAsString(createPojo());
        assertEquals("<TestPojo><field/></TestPojo>", xml);
    }

    public void testWith1Byte() throws Exception 
    {
        String xml = MAPPER.writeValueAsString(createPojo( 'A' ));
        assertEquals("<TestPojo><field>QQ==</field></TestPojo>", xml);
    }

    public void testWith2Bytes() throws Exception 
    {
        String xml = MAPPER.writeValueAsString(createPojo( 'A', 'B' ));
        assertEquals("<TestPojo><field>QUI=</field></TestPojo>", xml);
    }

    public void testWith3Bytes() throws Exception 
    {
        String xml = MAPPER.writeValueAsString(createPojo( 'A', 'B', 'C' ));
        assertEquals("<TestPojo><field>QUJD</field></TestPojo>", xml);
    }

    public void testWith4Bytes() throws Exception 
    {
        String xml = MAPPER.writeValueAsString(createPojo( 'A', 'B', 'C', 'D' ));
        assertEquals("<TestPojo><field>QUJDRA==</field></TestPojo>", xml);
    }

    private TestPojo createPojo(char... content) {
        TestPojo obj = new TestPojo();
        // DirectByteBuffer does not have an underlying array
        // so the ByteArraySerializer has to fallback to stream writing
        obj.field = ByteBuffer.allocateDirect(content.length);
        for(char b : content) {
            obj.field.put((byte) b);
        }
        obj.field.position(0);
        return obj;
    }

    public static class TestPojo {
        public ByteBuffer field;
    }
} 
