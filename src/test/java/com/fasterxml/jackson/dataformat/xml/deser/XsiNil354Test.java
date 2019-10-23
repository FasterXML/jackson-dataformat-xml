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

    private final XmlMapper MAPPER = newMapper();

    public void testWithDoubleAsNull() throws Exception
    {
        DoubleWrapper bean = MAPPER.readValue(
"<DoubleWrapper xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'><d xsi:nil='true' /></DoubleWrapper>",
                DoubleWrapper.class);
        assertNotNull(bean);
        assertNull(bean.d);

        bean = MAPPER.readValue(
"<DoubleWrapper xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'><d xsi:nil='true'>  </d></DoubleWrapper>",
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
}
