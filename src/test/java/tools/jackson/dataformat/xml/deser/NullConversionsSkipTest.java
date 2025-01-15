package tools.jackson.dataformat.xml.deser;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import tools.jackson.databind.*;

import tools.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

// for [databind#1402]; configurable null handling, specifically with SKIP
public class NullConversionsSkipTest extends XmlTestUtil
{
    static class NullSkipField {
        public String nullsOk = "a";

        @JsonSetter(nulls=Nulls.SKIP)
        public String noNulls = "b";
    }

    static class NullSkipMethod {
        String _nullsOk = "a";
        String _noNulls = "b";

        public void setNullsOk(String v) {
            _nullsOk = v;
        }

        @JsonSetter(nulls=Nulls.SKIP)
        public void setNoNulls(String v) {
            _noNulls = v;
        }
    }
    
    static class StringValue {
        String value = "default";

        public void setValue(String v) {
            value = v;
        }
    }

    /*
    /**********************************************************
    /* Test methods, straight annotation
    /**********************************************************
     */

    private final XmlMapper NULL_EXPOSING_MAPPER = mapperBuilder()
            .enable(XmlReadFeature.EMPTY_ELEMENT_AS_NULL)
            .build();

    @Test
    public void testSkipNullField1() throws Exception
    {
        // first, ok if assigning non-null to not-nullable, null for nullable
        NullSkipField result = NULL_EXPOSING_MAPPER.readValue(
//"<NullSkipField><noNulls>foo</noNulls><nullsOk></nullsOk></NullSkipField>",
"<NullSkipField><noNulls>foo</noNulls><nullsOk /></NullSkipField>",
                NullSkipField.class);
        assertEquals("foo", result.noNulls);
        assertNull(result.nullsOk);
    }

    @Test
    public void testSkipNullField2() throws Exception
    {
        // and then see that nulls are not ok for non-nullable
        NullSkipField result = NULL_EXPOSING_MAPPER.readValue("<NullSkipField><noNulls /></NullSkipField>",
                NullSkipField.class);
        assertEquals("b", result.noNulls);
        assertEquals("a", result.nullsOk);
    }

    @Test
    public void testSkipNullMethod1() throws Exception
    {
        NullSkipMethod result = NULL_EXPOSING_MAPPER.readValue(
//"<NullSkipMethod><noNulls>foo<noNulls><nullsOk></nullsOk></NullSkipMethod>",
"<NullSkipMethod><noNulls>foo</noNulls><nullsOk /></NullSkipMethod>",
                NullSkipMethod.class);
        assertEquals("foo", result._noNulls);
        assertNull(result._nullsOk);
    }

    @Test
    public void testSkipNullMethod2() throws Exception
    {
        NullSkipMethod result = NULL_EXPOSING_MAPPER.readValue("<NullSkipMethod><noNulls /></NullSkipMethod>",
                NullSkipMethod.class);
        assertEquals("b", result._noNulls);
        assertEquals("a", result._nullsOk);
    }

    /*
    /**********************************************************
    /* Test methods, defaulting
    /**********************************************************
     */
    
    @Test
    public void testSkipNullWithDefaults() throws Exception
    {
//        String doc = "<StringValue><value></value></StringValue>";
        String doc = "<StringValue><value /></StringValue>";
        StringValue result = NULL_EXPOSING_MAPPER.readValue(doc, StringValue.class);
        assertNull(result.value);

        ObjectMapper mapper = mapperBuilder()
                .enable(XmlReadFeature.EMPTY_ELEMENT_AS_NULL)
                .withConfigOverride(String.class,
                        o -> o.setNullHandling(JsonSetter.Value.forValueNulls(Nulls.SKIP)))
                .build();
        result = mapper.readValue(doc, StringValue.class);
        assertEquals("default", result.value);
    }
}
