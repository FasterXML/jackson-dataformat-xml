package tools.jackson.dataformat.xml.tofix;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlText;
import tools.jackson.dataformat.xml.testutil.failure.JacksonTestFailureExpected;

import static org.junit.jupiter.api.Assertions.assertNotNull;

// [dataformat-xml#306]: Problem is that `@XmlText` has the nominal property name
// of empty String (""), and that is not properly bound. Worse, empty String has
// special meaning so that annotation CANNOT specify it, either.
public class XmlTextViaCreator306Test extends XmlTestUtil
{
    // [dataformat-xml#306]
    @JsonRootName("ROOT")
    static class Root {
        @JacksonXmlProperty(localName = "CHILD")
        final Child child;

        public Root(Child child) {
            this.child = child;
        }

        public Child getChild() {
            return child;
        }
    }

    static class Child {
        @JacksonXmlProperty(localName = "attr", isAttribute = true)
        String attr;

        @JacksonXmlText
        String el;

        @JsonCreator
        Child(@JsonProperty("attr") String attr, @JsonProperty("el") String el) {
            this.attr = attr;
            this.el = el;
        }
    }

    @JsonRootName("ROOT")
    static class RootWithoutConstructor {
        @JacksonXmlProperty(localName = "CHILD")
        final ChildWithoutConstructor child;

        @JsonCreator
        public RootWithoutConstructor(@JsonProperty("child") ChildWithoutConstructor child) {
            this.child = child;
        }

        public ChildWithoutConstructor getChild() {
            return child;
        }
    }

    static class ChildWithoutConstructor {
        @JacksonXmlProperty(isAttribute = true)
        public String attr;

        @JacksonXmlText
        public String el;
    }

    /*
    /********************************************************
    /* Test methods
    /********************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    // [dataformat-xml#306]
    @JacksonTestFailureExpected
    @Test
    public void testIssue306WithCtor() throws Exception
    {
        final String XML = "<ROOT><CHILD attr='attr_value'>text</CHILD></ROOT>";
        Root root = MAPPER.readValue(XML, Root.class);
        assertNotNull(root);
    }

    // [dataformat-xml#306]
    @JacksonTestFailureExpected
    @Test
    public void testIssue306NoCtor() throws Exception
    {
        final String XML = "<ROOT><CHILD attr='attr_value'>text</CHILD></ROOT>";
        RootWithoutConstructor rootNoCtor = MAPPER.readValue(XML, RootWithoutConstructor.class);
        assertNotNull(rootNoCtor);
    }
}
