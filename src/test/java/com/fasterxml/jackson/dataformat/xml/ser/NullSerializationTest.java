package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.IOException;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class NullSerializationTest extends XmlTestBase
{
    static class WrapperBean<T>
    {
        public T value;

        public WrapperBean() { }
        public WrapperBean(T v) { value = v; }
    }

    // [dataformat-xml#360]
    public void testNil() throws IOException
    {
        final XmlMapper mapper = XmlMapper.builder()
                .configure(ToXmlGenerator.Feature.WRITE_NULLS_AS_XSI_NIL, true)
                .build();

        // First, map in a general wrapper
        assertEquals("<WrapperBean><value xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/></WrapperBean>",
                mapper.writeValueAsString(new WrapperBean<>(null)));

        // and then as root -- not sure what it should exactly look like but...
        String xml = mapper.writeValueAsString(null);
        assertEquals("<null xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>", xml);
    }
}
