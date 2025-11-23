package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for [dataformat-xml#787]: List deserialization should throw an exception
 * when encountering mismatched element types (e.g., &lt;bar&gt; elements when
 * expecting &lt;foo&gt; in a list)
 */
public class ListDeser787Test extends XmlTestUtil
{
    static class Root787 {
        @JacksonXmlProperty
        List<Foo787> foos = new ArrayList<>();
    }

    static class Foo787 {
        @JacksonXmlText
        String text;

        @JacksonXmlProperty(isAttribute = true)
        Integer sequenceNr;

        public Foo787() { }

        public Foo787(String text, Integer sequenceNr) {
            this.text = text;
            this.sequenceNr = sequenceNr;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Foo787 foo = (Foo787) o;
            return Objects.equals(text, foo.text) && Objects.equals(sequenceNr, foo.sequenceNr);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, sequenceNr);
        }

        @Override
        public String toString() {
            return "Foo787{" +
                    "text='" + text + '\'' +
                    ", sequenceNr=" + sequenceNr +
                    '}';
        }
    }

    private final ObjectMapper MAPPER = newMapper();

    // [dataformat-xml#787]: Should throw exception on non-matching elements in list
    @Test
    public void testDeser787MixedElements() throws Exception
    {
        String xml =
                "<root>\n" +
                "    <foos>\n" +
                "        <foo sequenceNr=\"1\">somefoo</foo>\n" +
                "        <foo sequenceNr=\"2\">otherfoo</foo>\n" +
                "        <bar>something very different</bar>\n" +
                "    </foos>\n" +
                "</root>";

        // Should throw exception when encountering <bar> element in a list expecting <foo> elements
        JsonMappingException exception = assertThrows(JsonMappingException.class, () -> {
            MAPPER.readValue(xml, Root787.class);
        });

        // Verify the error message mentions the mismatched element names
        String message = exception.getMessage();
        assertTrue(message.contains("Unexpected element name 'bar'"),
                "Error message should mention 'bar': " + message);
        assertTrue(message.contains("expected 'foo'"),
                "Error message should mention expected 'foo': " + message);
    }
}
