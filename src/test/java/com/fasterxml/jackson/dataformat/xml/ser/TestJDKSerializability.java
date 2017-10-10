package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.*;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Unit test related to core (https://github.com/FasterXML/jackson-core/issues/31)
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

    public void testMapper() throws IOException
    {
        XmlMapper mapper = xmlMapper(true);
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
    private <T> T jdkDeserialize(byte[] raw) throws IOException
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
}
