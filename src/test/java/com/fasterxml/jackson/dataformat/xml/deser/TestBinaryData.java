package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class TestBinaryData extends XmlTestBase
{
    public static class Data {
        public byte[] bytes;
    }

    public static class TwoData {
        public Data data1;
        public Data data2;
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

//    private final XmlMapper MAPPER = new XmlMapper();
    
    // for [https://github.com/FasterXML/jackson-dataformat-xml/issues/29]
    public void testTwoBinaryProps() throws Exception
    {
        /* Hmmh. Looks like XmlMapper has some issues with convertValue:
         * should investigate at some point. But not now...
         */
        final ObjectMapper jsonMapper = new ObjectMapper();
        String BIN1 = jsonMapper.convertValue("Hello".getBytes("UTF-8"), String.class);
        String BIN2 = jsonMapper.convertValue("world!!".getBytes("UTF-8"), String.class);
        String xml = 
            "<TwoData>" +
                    "<data1><bytes>" + BIN1 + "</bytes></data1>" +
                    "<data2><bytes>" + BIN2 + "</bytes></data2>" +
            "</TwoData>";

        TwoData two = new XmlMapper().readValue(xml, TwoData.class);
        assertEquals("Hello", new String(two.data1.bytes, "UTF-8"));
        assertEquals("world!!", new String(two.data2.bytes, "UTF-8"));
    }
}
