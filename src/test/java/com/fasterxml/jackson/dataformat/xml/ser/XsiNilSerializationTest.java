package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.IOException;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

// [dataformat-xml#360]
public class XsiNilSerializationTest extends XmlTestBase
{
    static class WrapperBean<T>
    {
        public T value;

        public WrapperBean() { }
        public WrapperBean(T v) { value = v; }
    }

    private final XmlMapper MAPPER = XmlMapper.builder()
            .configure(ToXmlGenerator.Feature.WRITE_NULLS_AS_XSI_NIL, true)
            .build();
    
    // [dataformat-xml#360]
    public void testNilPropertyNoIndent() throws IOException
    {
        assertEquals("<WrapperBean><value xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/></WrapperBean>",
                MAPPER.writeValueAsString(new WrapperBean<>(null)));
    }

    // [dataformat-xml#360]
    public void testNilPropertyRoot() throws IOException
    {
        // Not sure root element name defined but... "<null>" is what it is :)
        assertEquals("<null xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>",
                MAPPER.writeValueAsString(null));
    }

    // [dataformat-xml#432]
    public void testNilPropertyWithIndent() throws IOException
    {
        final String xml = MAPPER.writerWithDefaultPrettyPrinter()
                .writeValueAsString(new WrapperBean<>(null))
                .trim();
        assertEquals(
"<WrapperBean>\n"
+"  <value xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>\n"
+"</WrapperBean>", xml);
    }
}
