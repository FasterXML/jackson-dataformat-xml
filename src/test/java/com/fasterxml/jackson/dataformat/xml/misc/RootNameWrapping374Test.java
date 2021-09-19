package com.fasterxml.jackson.dataformat.xml.misc;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.*;

// Test originally for [dataformat-xml#374] but later (2.13)
// for [dataformat-xml#485]
public class RootNameWrapping374Test extends XmlTestBase
{
    @JsonRootName("Root")
    static class Root {
        public int id = 1;
    }

    // By default neither adding nor expecting wrapping
    private final XmlMapper DEFAULT_MAPPER = newMapper();

    // 18-Sep-2021, tatu: Note! WRAP_ROOT_VALUE has not and does not work with XML
    //    at all, up to and including 2.13.
    //    But UNWRAP_ROOT_VALUE worked before and after 2.12
    //    (as per [dataformat-xml#485]).
    //
    //  There is hope that maybe WRAP_ROOT_VALUE should be supportable in future too.
    private final XmlMapper WRAPPING_MAPPER = mapperBuilder()
            .enable(SerializationFeature.WRAP_ROOT_VALUE)
            .enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
            .build();

    public void testWriteIgnoresWrapping() throws Exception
    {
        // Writing is without wrapping no matter what...
        String xmlDefault = DEFAULT_MAPPER.writeValueAsString(new Root());
        String xmlWrapEnabled = WRAPPING_MAPPER.writeValueAsString(new Root());

        assertEquals("<Root><id>1</id></Root>", xmlDefault);
        assertEquals(xmlDefault, xmlWrapEnabled);
    }

    public void testReadWithoutWrapping() throws Exception
    {
        String xml = DEFAULT_MAPPER.writeValueAsString(new Root());
        Root result = DEFAULT_MAPPER.readValue(xml, Root.class);
        assertNotNull(result);
    }

    public void testReadWithWrapping() throws Exception
    {
        String xml = DEFAULT_MAPPER.writeValueAsString(new Root());
        assertEquals("<Root><id>1</id></Root>", xml);

        String wrapped = "<ignoreMe>"+xml+"</ignoreMe>";
        Root result = WRAPPING_MAPPER.readValue(wrapped, Root.class);
        assertNotNull(result);
    }
}
