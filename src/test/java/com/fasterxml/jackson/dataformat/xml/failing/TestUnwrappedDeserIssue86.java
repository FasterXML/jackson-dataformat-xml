package com.fasterxml.jackson.dataformat.xml.failing;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

public class TestUnwrappedDeserIssue86 extends XmlTestBase
{
    @JacksonXmlRootElement(localName = "test")
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
          return otherIssue86.id.equals(id) && otherIssue86.children.equals(children);
      }
    }

    /*
    /**********************************************************************
    /* Test methods
    /***********************************************************************
     */
    
    public void testDeserializeUnwrappedListWhenLocalNameForRootElementAndXmlPropertyMatch() throws Exception
    {
        final String source =
            "<test id=\"0\">" +
                "<test id=\"0.1\">" +
                    "<test id=\"0.1.1\"/>" +
                "</test>" +
                "<test id=\"0.2\"/>" +
                "<test id=\"0.3\">" +
                    "<test id=\"0.3.1\"/>" +
                "</test>" +
            "</test>";
    
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
        assertEquals(source, xml);
    
        final Issue86 after = mapper.readValue(xml, Issue86.class);
        assertEquals(before, after);
    }
}
