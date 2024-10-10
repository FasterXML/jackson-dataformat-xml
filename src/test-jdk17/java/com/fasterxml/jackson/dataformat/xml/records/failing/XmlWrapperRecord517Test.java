package com.fasterxml.jackson.dataformat.xml.records.failing;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import org.junit.Test;

// [databind#517] XML wrapper doesn't work with java records
// Equivalent to on in jdk17/.../deser/XmlWrapperRecord517Test.java
public class XmlWrapperRecord517Test
        extends XmlTestBase
{

    public record Request(
            @JacksonXmlElementWrapper(localName = "messages")
            @JacksonXmlProperty(localName = "message")
            List<Message> messages
    ) {
        public Request {}

        private Request() {this(null);}
    }

    public record Message(String text) {
        public Message {
        }

        private Message() {
            this(null);
        }
    }

    private String expectedXML =
                "<Request>" +
                    "<messages>" +
                        "<message>" +
                            "<text>Hello, World!</text>" +
                        "</message>" +
                    "</messages>" +
                "</Request>";

    @Test
    public void testWrapper() throws Exception {
        XmlWrapperRecord517Test.Request request = new Request(List.of(new Message("Hello, World!")));

        // test serialization
        String xml = newMapper().writeValueAsString(request);
        assertEquals(expectedXML, xml);

        // test deserialization
        Request result = newMapper().readValue(xml, Request.class);

        assertEquals(request.messages().size(), result.messages().size());
        assertEquals(request.messages().get(0).text(), result.messages().get(0).text());
    }
}
