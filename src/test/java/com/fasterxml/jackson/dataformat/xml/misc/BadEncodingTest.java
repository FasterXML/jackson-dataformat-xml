package com.fasterxml.jackson.dataformat.xml.misc;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

// [dataformat-xml#428]: problem with an encoding supported via JDK
public class BadEncodingTest extends XmlTestBase
{
//    private static final String xml = "<?xml version='1.0' encoding='WINDOWS-1252'?><x/>";
    private static final String xml = "<?xml version='1.0' encoding='WINDOWS-1252'?><x/>";

    private final ObjectMapper XML_MAPPER = newMapper();

    public void testEncoding() throws Exception {
        final byte[] b = xml.getBytes("UTF-8");
        assertNotNull(XML_MAPPER.readValue(b, Map.class));

//        try (InputStream in = new ByteArrayInputStream(b)) {
//            XML_MAPPER.readValue(in, Map.class);
//        }
    }
}
