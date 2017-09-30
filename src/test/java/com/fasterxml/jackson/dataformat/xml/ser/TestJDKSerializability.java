package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.*;

import javax.xml.namespace.QName;

import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

/**
 * Unit test related to core [Issue#31] (https://github.com/FasterXML/jackson-core/issues/31)
 * as it relates to XmlFactory.
 */
public class TestJDKSerializability extends XmlTestBase
{
    @JacksonXmlRootElement(localName="MyPojo")
    static class MyPojo {
        public int x;
        int y;
        
        public MyPojo() { }
        public MyPojo(int x0, int y0) {
            x = x0;
            y = y0;
        }
        
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
    }
    
    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */    

    public void testXmlFactory() throws Exception
    {
        XmlFactory f = new XmlFactory();
        String origXml = "<root><a>text</a></root>";
        assertEquals(origXml, _writeXml(f, false));

        // Ok: freeze dry factory, thaw, and try to use again:
        byte[] frozen = jdkSerialize(f);
        XmlFactory f2 = jdkDeserialize(frozen);
        assertNotNull(f2);
        assertEquals(origXml, _writeXml(f2, false));

        // Let's also try byte-based variant, for fun...
        assertEquals(origXml, _writeXml(f2, true));
    }

    public void testMapper() throws IOException
    {
        XmlMapper mapper = new XmlMapper();
        final String EXP = "<MyPojo><x>2</x><y>3</y></MyPojo>";
        final MyPojo p = new MyPojo(2, 3);
        assertEquals(EXP, mapper.writeValueAsString(p));

        byte[] bytes = jdkSerialize(mapper);
        XmlMapper mapper2 = jdkDeserialize(bytes);
        assertEquals(EXP, mapper2.writeValueAsString(p));
        MyPojo p2 = mapper2.readValue(EXP, MyPojo.class);
        assertEquals(p.x, p2.x);
        assertEquals(p.y, p2.y);
    }
    
    /*
    /**********************************************************
    /* Helper methods
    /**********************************************************
     */
    
    protected byte[] jdkSerialize(Object o) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(1000);
        ObjectOutputStream obOut = new ObjectOutputStream(bytes);
        obOut.writeObject(o);
        obOut.close();
        return bytes.toByteArray();
    }

    @SuppressWarnings("unchecked")
    protected <T> T jdkDeserialize(byte[] raw) throws IOException
    {
        ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(raw));
        try {
            return (T) objIn.readObject();
        } catch (ClassNotFoundException e) {
            fail("Missing class: "+e.getMessage());
            return null;
        } finally {
            objIn.close();
        }
    }
    
    @SuppressWarnings("resource")
    protected String _writeXml(XmlFactory f, boolean useBytes) throws IOException
    {
        if (useBytes) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ToXmlGenerator jg = (ToXmlGenerator) f.createGenerator(bytes);
            _write(jg);
            return bytes.toString("UTF-8");
        }
        StringWriter sw = new StringWriter();
        ToXmlGenerator xg = (ToXmlGenerator) f.createGenerator(sw);
        _write(xg);
        return sw.toString();
    }
        
    protected void _write(ToXmlGenerator xg) throws IOException
    {
        xg.setNextName(new QName("root"));
        xg.writeStartObject();
        xg.writeFieldName("a");
        xg.writeString("text");
        xg.writeEndObject();
        xg.close();
    }
}
