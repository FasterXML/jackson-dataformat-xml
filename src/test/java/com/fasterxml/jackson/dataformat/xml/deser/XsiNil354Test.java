package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class XsiNil354Test extends XmlTestBase
{
    protected static class DoubleWrapper {
        public Double d;

        public DoubleWrapper() { }
        public DoubleWrapper(Double value) {
            d = value;
        }
    }

    protected static class Parent {
        public Level1 level1;
    }

    protected static class Level1 {
        public Level2 level2;
        public String field; // this should not be needed, but an unknown element is thrown without it
    }

    protected static class Level2 {
        public String ignored;
        public String field;
    }

    private final XmlMapper MAPPER = newMapper();

    public void testWithDoubleAsNull() throws Exception
    {
        DoubleWrapper bean = MAPPER.readValue(
"<DoubleWrapper xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><d xsi:nil='true' /></DoubleWrapper>",
                DoubleWrapper.class);
        assertNotNull(bean);
        assertNull(bean.d);

        bean = MAPPER.readValue(
"<DoubleWrapper xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><d xsi:nil='true'>  </d></DoubleWrapper>",
                DoubleWrapper.class);
        assertNotNull(bean);
        assertNull(bean.d);

        // actually we should perhaps also verify there is no content but... for now, let's leave it.
    }

    public void testWithDoubleAsNonNull() throws Exception
    {
        DoubleWrapper bean = MAPPER.readValue(
                "<DoubleWrapper xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><d xsi:nil='false'>0.25</d></DoubleWrapper>",
                DoubleWrapper.class);
        assertNotNull(bean);
        assertEquals(Double.valueOf(0.25), bean.d);
    }

    public void testRootPojoAsNull() throws Exception
    {
        Point bean = MAPPER.readValue(
                "<Point xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:nil='true' />",
                Point.class);
        assertNull(bean);
    }

    public void testRootPojoAsNonNull() throws Exception
    {
        Point bean = MAPPER.readValue(
                "<Point xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:nil='false'></Point>",
                Point.class);
        assertNotNull(bean);
    }

    public void testDoesNotAffectHierarchy() throws Exception
    {
        String xml = "<Parent xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<level1>"
                + "<level2>"
                + "<ignored xsi:nil=\"true\"/>"
                + "<field>test-value</field>"
                + "</level2>"
                + "</level1>"
                + "</Parent>";
        Parent bean = MAPPER.readValue(xml, Parent.class);

        assertNotNull(bean);

        // this should not be set, but having an xsi:nil field before it causes it to set the next field on the wrong class
        assertEquals("test-value", bean.level1.field);

        // fails because field is set on level1 instead of on level2
        assertEquals("test-value", bean.level1.level2.field);
    }
}
