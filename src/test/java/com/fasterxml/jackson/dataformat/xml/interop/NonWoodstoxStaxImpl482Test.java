package com.fasterxml.jackson.dataformat.xml.interop;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.sun.xml.stream.ZephyrParserFactory;
import com.sun.xml.stream.ZephyrWriterFactory;

// to verify issue behind [dataformat-xml#482]
public class NonWoodstoxStaxImpl482Test extends XmlTestBase
{
    static class Root {
        public int value = 3;
    }

    private final XmlMapper SJSXP_MAPPER = XmlMapper.builder(
            XmlFactory.builder()
                .xmlInputFactory(new ZephyrParserFactory())
                .xmlOutputFactory(new ZephyrWriterFactory())
            .build())
            .build();
 
    // [dataformat-xml#482]
    public void testSjsxpFromByteArray() throws Exception
    {
        byte[] xml0 = SJSXP_MAPPER.writeValueAsBytes(new Root());
        // and just for fun, ensure offset handling works:
        byte[] xml = new byte[xml0.length + 10];
        System.arraycopy(xml0, 0, xml, 5, xml0.length);
        Root result = SJSXP_MAPPER.readValue(xml, 5, xml0.length, Root.class);
        assertNotNull(result);
    }

    // [dataformat-xml#482]
    public void testSjsxpFromCharArray() throws Exception
    {
        char[] xml0 = SJSXP_MAPPER.writeValueAsString(new Root()).toCharArray();
        // add offset
        char[] xml = new char[xml0.length + 10];
        System.arraycopy(xml0, 0, xml, 5, xml0.length);
        ObjectReader r = SJSXP_MAPPER.readerFor(Root.class);
        JsonParser p = r.createParser(xml, 5, xml0.length);
        Root result =  r.readValue(p);
        p.close();
        assertNotNull(result);
    }
}
