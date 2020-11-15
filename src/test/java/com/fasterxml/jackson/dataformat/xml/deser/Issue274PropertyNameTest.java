package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.*;

import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

// [dataformat-xml#274]: Actually passes... can not reproduce failure
public class Issue274PropertyNameTest extends XmlTestBase
{
    private static final String XML =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
            "<dataroot xmlns:od=\"urn:schemas-microsoft-com:officedata\" generated=\"2017-06-07T10:11:20\">\n" + 
            "    <Event>\n" + 
            "        <EventId>34906566143035</EventId>\n" + 
            "    </Event>\n" + 
            "</dataroot>";
       
    static class Event {
        @JacksonXmlProperty(localName = "EventId")
        private String EventId;

        public String getEventId() {
            return EventId;
        }

        public void setEventId(String eventId) {
            this.EventId = eventId;
        }
    }

    @JsonRootName("dataroot")
    static class RootObject {
        @JacksonXmlProperty(localName = "Event")
        Event event;

        @JacksonXmlProperty(localName = "generated")
        String generated;
    }

    /*
    /********************************************************
    /* Test methods
    /********************************************************
     */
    
    // [dataformat-xml#274]
    public void testIssue274() throws Exception
    {
        final ObjectMapper xm = mapperBuilder()

        // serialization features
//        xm.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        xm.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            //this is for deserialization only and means we don't need to camelCase  xml property names
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
            .build();

        RootObject obj = xm.readValue(XML, RootObject.class);
        assertNotNull(obj);
    }
}
