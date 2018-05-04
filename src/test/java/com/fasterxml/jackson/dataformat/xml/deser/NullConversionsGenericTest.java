package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

// Copied from `com.fasterxml.jackson.databind.deser.filter.` in `jackson-databind`
public class NullConversionsGenericTest extends XmlTestBase
{
    static class PointWrapper {
        @JsonSetter(nulls=Nulls.AS_EMPTY)
        public Point p;
    }

    static class GeneralEmpty<T> {
        // 09-Feb-2017, tatu: Should only need annotation either for field OR setter, not both:
//        @JsonSetter(nulls=Nulls.AS_EMPTY)
        T value;

        protected GeneralEmpty() { }
        public GeneralEmpty(T v) { value = v; }

        @JsonSetter(nulls=Nulls.AS_EMPTY)
        public void setValue(T v) {
            value = v;
        }

        public T getValue() { return value; }
    }

    static class NoCtorWrapper {
        @JsonSetter(nulls=Nulls.AS_EMPTY)
        public NoCtorPOJO value;
    }

    static class NoCtorPOJO {
        public NoCtorPOJO(boolean b) { }
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    private final static String EMPTY_XML = "<GeneralEmpty><value /></GeneralEmpty>";

    public void testNullsToEmptyPojo() throws Exception
    {
        PointWrapper pw = MAPPER.readValue("<PointWrapper><p /></PointWrapper>",
                PointWrapper.class);
        assertNotNull(pw);
        assertNotNull(pw.p);
        assertEquals(0, pw.p.x);
        assertEquals(0, pw.p.y);
    }

    public void testNullsToGenericPojo() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new GeneralEmpty<Point>(new Point(1, 2)));
        GeneralEmpty<Point> result = MAPPER.readValue(EMPTY_XML,
                new TypeReference<GeneralEmpty<Point>>() { });
        assertNotNull(result.value);
        Point p = result.value;
        assertEquals(0, p.x);
        assertEquals(0, p.y);

        // and then also failing case with no suitable creator:
        try {
            /* NoCtorWrapper nogo =*/ MAPPER.readValue(EMPTY_XML,
                    NoCtorWrapper.class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Cannot create empty instance");
        }
    }

    // 04-May-2018, tatu: In theory could be supportable, but wrapping (or not)
    //   of Collections, other requirements, make it... not that easy.
/*
    public void testNullsToEmptyCollection() throws Exception
    {
        GeneralEmpty<List<String>> result = MAPPER.readValue(EMPTY_XML,
                new TypeReference<GeneralEmpty<List<String>>>() { });
        assertNotNull(result.value);
        assertEquals(0, result.value.size());

        // but also non-String type, since impls vary
        GeneralEmpty<List<Integer>> result2 = MAPPER.readValue(EMPTY_XML,
                new TypeReference<GeneralEmpty<List<Integer>>>() { });
        assertNotNull(result2.value);
        assertEquals(0, result2.value.size());
    }
    */

    // 04-May-2018, tatu: Maps and XML do not mix well, alas:
    /*
    public void testNullsToEmptyMap() throws Exception
    {
        GeneralEmpty<Map<String,String>> result = MAPPER.readValue(EMPTY_XML,
                new TypeReference<GeneralEmpty<Map<String,String>>>() { });
        assertNotNull(result.value);
        assertEquals(0, result.value.size());
    }
    */

    public void testNullsToEmptyArrays() throws Exception
    {
        final String doc = EMPTY_XML;

        GeneralEmpty<Object[]> result = MAPPER.readValue(doc,
                new TypeReference<GeneralEmpty<Object[]>>() { });
        assertNotNull(result.value);
        assertEquals(0, result.value.length);

        GeneralEmpty<String[]> result2 = MAPPER.readValue(doc,
                new TypeReference<GeneralEmpty<String[]>>() { });
        assertNotNull(result2.value);
        assertEquals(0, result2.value.length);

        GeneralEmpty<int[]> result3 = MAPPER.readValue(doc,
                new TypeReference<GeneralEmpty<int[]>>() { });
        assertNotNull(result3.value);
        assertEquals(0, result3.value.length);

        GeneralEmpty<double[]> result4 = MAPPER.readValue(doc,
                new TypeReference<GeneralEmpty<double[]>>() { });
        assertNotNull(result4.value);
        assertEquals(0, result4.value.length);

        GeneralEmpty<boolean[]> result5 = MAPPER.readValue(doc,
                new TypeReference<GeneralEmpty<boolean[]>>() { });
        assertNotNull(result5.value);
        assertEquals(0, result5.value.length);
    }
}
