package com.fasterxml.jackson.dataformat.xml.failing;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// 13-Nov-2020, tatu: Not quite sure how to configure test to pass;
//   seems like it should work but does not. Leaving for future generations
//   to figure out...
public class ElementWrapperViaCreator149Test extends XmlTestUtil
{
    @JsonRootName("body")
    static class Body149 {
        final String type;

        @JacksonXmlProperty(localName="label")
        final List<String> labels;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public Body149(@JacksonXmlProperty(localName="type")
                    String type,
                    @JacksonXmlElementWrapper(localName="labels")
                    @JacksonXmlProperty(localName="label")
                    List<String> labels)
        {
            this.type = type;
            this.labels = labels;
        }
    }    

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    private final ObjectMapper MAPPER = newMapper();

    // [dataformat-xml#149]
    @Test
    public void testElementWrapper149() throws Exception
    {
        final String XML = "<body>\n" +
                "  <type>TYPE</type>\n" +
                "  <labels><label>foo</label><label>bar</label></labels>\n" +
                "</body>";
        Body149 result = MAPPER.readValue(XML, Body149.class);
        assertEquals("TYPE", result.type);
        assertNotNull(result.labels);
        assertEquals(Arrays.asList("foo", "bar"), result.labels);

//System.err.println("XML: "+MAPPER.writeValueAsString(result));        
    }
}
