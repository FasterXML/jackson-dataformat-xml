package com.fasterxml.jackson.dataformat.xml.deser;

import java.util.Map;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class MapDeserializationTest extends XmlTestBase
{
    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    // [dataformat-xml#14]
    public void testMapWithAttr() throws Exception
    {
        final String xml = "<order><person lang='en'>John Smith</person></order>";
        Map<?,?> map = MAPPER.readValue(xml, Map.class);
     
     // Will result in equivalent of:
     // { "person" : {
     //     "lang" : "en",
     //     "" : "John Smith"
     //   }
     // }
     //
     // which may or may not be what we want. Without attribute
     // we would just have '{ "person" : "John Smith" }'
     
         assertNotNull(map);
    }

}
