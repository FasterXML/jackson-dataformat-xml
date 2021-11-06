package com.fasterxml.jackson.dataformat.xml.misc;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

// for #12
public class UnwrappingWithXMLTest extends XmlTestBase
{
    @JsonPropertyOrder({"x", "y"})
    final static class Location {
        public int x;
        public int y;

        public Location() { }
        public Location(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    // IMPORTANT: ordering DOES matter here
    @JsonPropertyOrder({ "name", "location" })
    static class Unwrapping {
        public String name;
        @JsonUnwrapped(prefix="loc.")
        public Location location;

        public Unwrapping() { }
        public Unwrapping(String str, int x, int y) {
            name = str;
            location = new Location(x, y);
        }
    }

    @JsonPropertyOrder({ "name", "location" })
    static class UnwrappingWithAttributes{
        @JacksonXmlProperty(isAttribute=true)
        public String name;
        @JacksonXmlProperty(isAttribute=true)
        @JsonUnwrapped(prefix="loc.")
        public Location location;

        public UnwrappingWithAttributes() { }
        public UnwrappingWithAttributes(String str, int x, int y) {
            name = str;
            location = new Location(x, y);
        }
    }

    static class UnwrappingSubWithAttributes{
        @JacksonXmlProperty(isAttribute=true)
        public String name;
        @JsonUnwrapped(prefix="loc.")
        public LocationWithAttributes location;

        public UnwrappingSubWithAttributes() { }
        public UnwrappingSubWithAttributes(String str, int x, int y) {
            name = str;
            location = new LocationWithAttributes(x, y);
        }
    }

    @JsonPropertyOrder({"x", "y"})
    final static class LocationWithAttributes {
        @JacksonXmlProperty(isAttribute=true)
        public int x;
        public int y;

        public LocationWithAttributes() { }
        public LocationWithAttributes(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    /*
    /**********************************************************
    /* Tests
    /**********************************************************
     */

    /**
     * Simple test to verify that explicit schema mapping works fine
     * with unwrapped entities
     */
    public void testSimpleUnwrappingRoundtrip()
        throws Exception
    {
        final String XML = "<Unwrapping><name>Joe</name><loc.x>15</loc.x><loc.y>27</loc.y></Unwrapping>";
        ObjectMapper mapper = xmlMapper(false);
        Unwrapping wrapper = mapper.readerFor(Unwrapping.class).readValue(XML);
        assertNotNull(wrapper);
        assertNotNull(wrapper.location);
        assertEquals(15, wrapper.location.x);
        assertEquals(27, wrapper.location.y);

        // should also write out the same way
        assertEquals(XML, mapper.writerFor(Unwrapping.class).writeValueAsString(wrapper));
    }

    public void testUnwrappingWithAttribute()
        throws Exception
    {
        final String XML = "<UnwrappingWithAttributes name=\"Joe\" loc.x=\"15\" loc.y=\"27\"/>";
        ObjectMapper mapper = xmlMapper(false);
        UnwrappingWithAttributes wrapper = mapper.readerFor(UnwrappingWithAttributes.class).readValue(XML);
        assertNotNull(wrapper);
        assertNotNull(wrapper.location);
        assertEquals(15, wrapper.location.x);
        assertEquals(27, wrapper.location.y);

        // should also write out the same way
        assertEquals(XML, mapper.writerFor(UnwrappingWithAttributes.class).writeValueAsString(wrapper));
    }

    public void testUnwrappingSubWithAttribute()
        throws Exception
    {
        final String XML = "<UnwrappingSubWithAttributes name=\"Joe\" loc.x=\"15\"><loc.y>27</loc.y></UnwrappingSubWithAttributes>";
        ObjectMapper mapper = xmlMapper(false);
        UnwrappingSubWithAttributes wrapper = mapper.readerFor(UnwrappingSubWithAttributes.class).readValue(XML);
        assertNotNull(wrapper);
        assertNotNull(wrapper.location);
        assertEquals(15, wrapper.location.x);
        assertEquals(27, wrapper.location.y);

        // should also write out the same way
        assertEquals(XML, mapper.writerFor(UnwrappingSubWithAttributes.class).writeValueAsString(wrapper));
    }
}
