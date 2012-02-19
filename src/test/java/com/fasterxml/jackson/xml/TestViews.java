package com.fasterxml.jackson.xml;

import com.fasterxml.jackson.annotation.*;

import com.fasterxml.jackson.databind.*;

public class TestViews extends XmlTestBase
{
    /*
    /**********************************************************
    /* Helper types
    /**********************************************************
     */

    static class RestrictedView { };

    static class Foo
    {
        @JsonView(RestrictedView.class)
        @JsonProperty
        public String restrictedFooProperty;

        @JsonView(RestrictedView.class)
        @JsonProperty
        public Bar[] bars;
    }

    static class Bar
    {
        @JsonView(RestrictedView.class)
        @JsonProperty
        public int restrictedBarProperty;
    }    

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */
    
    public void testIssue7() throws Exception
    {
        Foo foo = new Foo();
        foo.restrictedFooProperty = "test";

        Bar bar1 = new Bar();
        bar1.restrictedBarProperty = 10;

        Bar bar2 = new Bar();
        bar2.restrictedBarProperty = 11;

        foo.bars = new Bar[] { bar1, bar2 };

        ObjectMapper xmlMapper = new XmlMapper();

        xmlMapper.configure(MapperFeature.AUTO_DETECT_FIELDS, false );
        xmlMapper.configure(MapperFeature.AUTO_DETECT_GETTERS, false );
        xmlMapper.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false );
        xmlMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false );

        String xml = xmlMapper.writerWithView(RestrictedView.class).writeValueAsString(foo);

        // views not used for deserialization
        Foo result = xmlMapper.readValue(xml, Foo.class);
        assertEquals("test", result.restrictedFooProperty);
        assertNotNull(result.bars);
        assertEquals(2, result.bars.length);
        assertEquals(10, result.bars[0].restrictedBarProperty);
        assertEquals(11, result.bars[1].restrictedBarProperty);
        
    }
}
