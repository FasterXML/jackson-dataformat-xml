package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test for [dataformat-xml#787]: List deserialization should ignore
 * non-matching element types (e.g., &lt;bar&gt; elements when deserializing
 * into List&lt;Foo&gt;)
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

    // [dataformat-xml#787]: Should skip non-matching elements in list
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

        Root787 root = MAPPER.readValue(xml, Root787.class);

        // Should only have 2 Foo elements, <bar> should be ignored
        assertNotNull(root.foos);
        assertEquals(2, root.foos.size());

        Foo787 foo1 = root.foos.get(0);
        assertEquals("somefoo", foo1.text);
        assertEquals(Integer.valueOf(1), foo1.sequenceNr);

        Foo787 foo2 = root.foos.get(1);
        assertEquals("otherfoo", foo2.text);
        assertEquals(Integer.valueOf(2), foo2.sequenceNr);
    }
}
