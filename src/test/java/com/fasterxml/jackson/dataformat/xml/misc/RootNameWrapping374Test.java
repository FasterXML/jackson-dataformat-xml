package com.fasterxml.jackson.dataformat.xml.misc;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.*;

public class RootNameWrapping374Test extends XmlTestBase
{
    @JsonRootName("Root")
    static class Root {
        public int id = 1;
    }

    private final XmlMapper MAPPER = mapperBuilder()
            .enable(SerializationFeature.WRAP_ROOT_VALUE)
            .enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
            .build();

    public void testUnwrappedRoundTrip() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new Root());
        assertEquals("<Root><id>1</id></Root>", xml);
//System.err.println("XML: "+xml);
        Root result = MAPPER.readValue(xml, Root.class);
        assertNotNull(result);
    }
}
