package com.fasterxml.jackson.dataformat.xml.failing;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.*;

import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

public class UnwrappedAndList299DeserTest extends XmlTestBase
{
    static class Request {
         @JsonUnwrapped
         public Composite composite = new Composite();
    }

    static class Composite {
        public String messageId;
        public Integer number;

        @JacksonXmlElementWrapper(useWrapping=false)
        public List<Header> headers = new ArrayList<>();
    }

    static class Header {
        public String headerId;
    }

    private final ObjectMapper MAPPER = newMapper();

    public void testXmlMarshallingAndUnmarshalling() throws Exception {
        final Request request = new Request();
        request.composite.messageId = "ABC";
        request.composite.number = 123;
        
        final Header header1 = new Header();
        header1.headerId = "headerID1";
        final Header header2 = new Header();
        header2.headerId = "headerID2";
        
        request.composite.headers.add(header1);
        request.composite.headers.add(header2);
        
        String xmlString = MAPPER.writerWithDefaultPrettyPrinter()
                .writeValueAsString(request);

//System.err.println("XML:\n"+xmlString);
        
        final Request anotherRequest = MAPPER.readValue(xmlString, Request.class);
        
        assertEquals(request.composite.messageId, anotherRequest.composite.messageId);
        assertEquals(request.composite.number, anotherRequest.composite.number);
        
        assertEquals("ABC", anotherRequest.composite.messageId);
        assertEquals(Integer.valueOf(123), anotherRequest.composite.number);
        assertEquals(2, anotherRequest.composite.headers.size());
        assertEquals("headerID1", anotherRequest.composite.headers.get(0).headerId);
        assertEquals("headerID2", anotherRequest.composite.headers.get(1).headerId);
    }
}
