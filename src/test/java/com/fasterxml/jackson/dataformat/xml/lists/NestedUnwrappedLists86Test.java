package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class NestedUnwrappedLists86Test extends XmlTestBase
{
    @JsonRootName("test")
    public static class Issue86 {

      @JacksonXmlProperty(localName = "id", isAttribute = true)
      public String id;

      @JacksonXmlElementWrapper(useWrapping = false)
      @JacksonXmlProperty(localName = "test")
      public List<Issue86> children;

      public Issue86() {}

      public Issue86(final String id, final List<Issue86> children) {
        this.id = id;
        this.children = children;
      }

      @Override
      public boolean equals(final Object other) {
          if (other == this) return true;
          if (other == null) return false;

          if (!(other instanceof Issue86)) {
              return false;
          }

          final Issue86 otherIssue86 = (Issue86) other;
          return Objects.equals(id, otherIssue86.id)
                 && Objects.deepEquals(children, otherIssue86.children);
      }

      @Override
      public String toString() {
          StringBuilder sb = new StringBuilder();
          sb.append("{id='").append(id)
              .append("', children=").append(children)
              .append('}');
          return sb.toString();
      }
    }

    /*
    /**********************************************************************
    /* Test methods
    /***********************************************************************
     */

    public void testDeserializeUnwrappedListWhenLocalNameForRootElementAndXmlPropertyMatch() throws Exception
    {
        final String sourceIndented =
            "<test id=\"0\">\n" +
                "<test id=\"0.1\">\n" +
                    "<test id=\"0.1.1\"/>\n" +
                "</test>\n" +
                "<test id=\"0.2\"/>\n" +
                "<test id=\"0.3\">\n" +
                    "<test id=\"0.3.1\"/>\n" +
                "</test>\n" +
            "</test>";
        final String sourceCompact = sourceIndented.replaceAll("\n", "");
        final Issue86 before = new Issue86("0",
            Arrays.asList(new Issue86("0.1",
                    Arrays.asList(new Issue86("0.1.1", null))),
                new Issue86("0.2", null),
                new Issue86("0.3",
                    Arrays.asList(
                        new Issue86("0.3.1", null)))));
    
        final XmlMapper mapper = new XmlMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
    
        final String xml = mapper.writeValueAsString(before);
        assertEquals(sourceCompact, xml);
    
        final Issue86 after = mapper.readValue(sourceIndented, Issue86.class);
        assertEquals(before, after);
    }
}
