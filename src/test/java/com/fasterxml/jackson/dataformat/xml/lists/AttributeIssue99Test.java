package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.*;

import com.fasterxml.jackson.databind.*;

import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

public class AttributeIssue99Test extends XmlTestBase
{
    static class Root {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "value")
        public List<Value> values;
    }

    public static class Value {
        @JacksonXmlProperty(isAttribute = true)
        public int id;
    }

    public void testListWithAttributes() throws Exception
    {
        String source = "<Root>"
                + "     <value id=\"1\"/>"
                + "     <fail/>"
                + "</Root>";
        ObjectMapper mapper = new XmlMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        Root root = mapper.readValue(source, Root.class);
        mapper.writeValue(System.out, root);
        System.out.println();
    }

}
