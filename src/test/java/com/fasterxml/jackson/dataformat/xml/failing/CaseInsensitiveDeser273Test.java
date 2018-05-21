package com.fasterxml.jackson.dataformat.xml.failing;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class CaseInsensitiveDeser273Test extends XmlTestBase
{
    // [dataformat-xml#273]
    static class Depots273
    {
        public String command;
        public String taskId;

        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Depot273> element;

        public void setElement(List<Depot273> l) {
//System.err.println("setElement: "+l);
            element = l;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Depot273
    {
        @JacksonXmlProperty(isAttribute = true)
        public String number;

        @JacksonXmlProperty(isAttribute = true)
        public String name;

        public void setNumber(String n) {
//System.err.println("SetNumber: '"+n+"'");
            number = n;
        }
        public void setName(String n) {
//System.err.println("setName: '"+n+"'");
            name = n;
        }
    }

    /*
    /********************************************************
    /* Test methods
    /********************************************************
     */

    private final ObjectMapper INSENSITIVE_MAPPER = newMapper()
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);

    // [dataformat-xml#273]
    public void testCaseInsensitiveComplex() throws Exception
    {
        final String DOC =
"<Depots273 Command='show depots' TaskId='1260'>\n"+
"  <Element Number='1' Name='accurev' Slice='1'\n"+
"exclusiveLocking='false' case='insensitive' locWidth='128'"+
"></Element>\n"+
"  <Element Number='2' Name='second accurev' Slice='2'\n"+
"exclusiveLocking='false' case='insensitive' locWidth='128'\n"+
"></Element>\n"+
"</Depots273>"
        ;

        Depots273 result = INSENSITIVE_MAPPER.readValue(DOC, Depots273.class);
        assertNotNull(result);
    }
}
