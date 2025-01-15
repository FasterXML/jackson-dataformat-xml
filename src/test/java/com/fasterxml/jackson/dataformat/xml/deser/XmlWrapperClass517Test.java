package com.fasterxml.jackson.dataformat.xml.deser;

import java.util.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;

// [databind#517] XML wrapper doesn't work with java records
// Equivalent to on in jdk17/.../records/XmlWrapperRecord517Test.java
public class XmlWrapperClass517Test
    extends XmlTestUtil
{
    public static final class Request {
        @JacksonXmlElementWrapper(localName = "messages")
        @JacksonXmlProperty(localName = "message")
        private final List<Message> messages;

        Request() { this.messages = null; }
        public Request(List<Message> messages) { this.messages = messages; }

        public List<Message> getMessages() { return messages; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Request)) return false;
            return Objects.equals(messages, ((Request)o).messages);
        }

        @Override
        public int hashCode() { return Objects.hash(messages); }

        @Override
        public String toString() { return "Request{messages=" + messages + '}'; }
    }

    public static final class Message {

        private final String text;

        Message() { this.text = null; }
        public Message(String text) { this.text = text; }

        public String getText() { return text; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Message)) return false;
            Message message = (Message) o;
            return Objects.equals(text, message.text);
        }

        @Override
        public int hashCode() { return Objects.hash(text); }

        @Override
        public String toString() { return "Message{text='" + text + "\'}"; }
    }

    private final ObjectMapper mapper = newMapper();

    private final String expectedXML =
                "<Request>" +
                    "<messages>" +
                        "<message>" +
                            "<text>given text</text>" +
                        "</message>" +
                    "</messages>" +
                "</Request>";

    @Test
    public void testShouldSerialize() throws Exception {
        Request givenRequest = _createRequest("given text");

        String  actualXml = mapper.writeValueAsString(givenRequest);

        assertEquals(expectedXML, actualXml);
    }

    @Test
    public void testShouldDeserialize() throws Exception {
        Request expected = _createRequest("given text");

        Request actualRequest = mapper.readValue(expectedXML, Request.class);

        assertEquals(expected, actualRequest);
    }

    private Request _createRequest(String givenText) {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message(givenText));
        return new Request(messages);
    }

}
