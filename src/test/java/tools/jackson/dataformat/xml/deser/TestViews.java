package tools.jackson.dataformat.xml.deser;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import tools.jackson.databind.*;
import tools.jackson.databind.ser.FilterProvider;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;
import tools.jackson.dataformat.xml.*;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Tests for ('JSON') Views, other filtering.
 */
public class TestViews extends XmlTestUtil
{
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class NonNullBean
    {
        public String nullName = null;
        public String name = "Bob";
    }

    @JsonFilter("filter44")
    public class Issue44Bean {
        @JacksonXmlProperty(isAttribute=true)
        protected String first = "abc";

        public int second = 13;
    }

    /*
    /**********************************************************
    /* Set up
    /**********************************************************
     */

    protected XmlMapper _xmlMapper;

    // let's actually reuse XmlMapper to make things bit faster
    @BeforeEach
    public void setUp() throws Exception {
        _xmlMapper = new XmlMapper();
    }
    
    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */
    
    @Test
    public void testIssue7() throws Exception
    {
        Foo foo = new Foo();
        foo.restrictedFooProperty = "test";

        Bar bar1 = new Bar();
        bar1.restrictedBarProperty = 10;

        Bar bar2 = new Bar();
        bar2.restrictedBarProperty = 11;

        foo.bars = new Bar[] { bar1, bar2 };

        XmlMapper xmlMapper = XmlMapper.builder()
                .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
                .changeDefaultVisibility(vc -> 
                    vc.withVisibility(PropertyAccessor.FIELD, Visibility.NONE)
                        .withVisibility(PropertyAccessor.GETTER, Visibility.NONE)
                        .withVisibility(PropertyAccessor.IS_GETTER, Visibility.NONE))
                .build();

        String xml = xmlMapper.writerWithView(RestrictedView.class).writeValueAsString(foo);
        
        // views not used for deserialization
        Foo result = xmlMapper.readValue(xml, Foo.class);
        assertEquals("test", result.restrictedFooProperty);
        assertNotNull(result.bars);
        assertEquals(2, result.bars.length);
        assertEquals(10, result.bars[0].restrictedBarProperty);
        assertEquals(11, result.bars[1].restrictedBarProperty);
        
    }

    @Test
    public void testNullSuppression() throws Exception
    {
        String xml = _xmlMapper.writeValueAsString(new NonNullBean());
        assertEquals("<NonNullBean><name>Bob</name></NonNullBean>", xml);
    }

    @Test
    public void testIssue44() throws IOException
    {
        String exp = "<Issue44Bean first=\"abc\"><second>13</second></Issue44Bean>";
        Issue44Bean bean = new Issue44Bean();

        FilterProvider prov = new SimpleFilterProvider().addFilter("filter44",
                SimpleBeanPropertyFilter.serializeAllExcept("filterMe"));
        ObjectWriter writer = _xmlMapper.writer(prov);

        // as well as with proper filter
        assertEquals(exp, writer.writeValueAsString(bean));
    }
}
