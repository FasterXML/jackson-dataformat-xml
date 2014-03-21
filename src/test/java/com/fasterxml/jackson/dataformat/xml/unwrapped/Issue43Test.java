package com.fasterxml.jackson.dataformat.xml.unwrapped;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

public class Issue43Test extends XmlTestBase
{
    static class Name {
        @JacksonXmlProperty(isAttribute=true)
        public String language;

        @JacksonXmlText
        public String text;

//        public String data;

        public Name() { }
    }

    static class RoomName {
        @JacksonXmlElementWrapper(localName = "names", useWrapping=true)
        @JsonProperty("name")
        public List<Name> names;
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */
    
    public void testIssue43() throws Exception
    {
        String xmlData = "<roomName><names>"
                +"<name language=\"en\">SPECIAL</name>"
                +"</names></roomName>";

        XmlMapper xmlMapper = new XmlMapper();
        RoomName roomName = xmlMapper.readValue(xmlData, RoomName.class);
        assertEquals(1, roomName.names.size());
        assertEquals("SPECIAL", roomName.names.get(0).text);
    }
}
