package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class XsiNilBasicTest extends XmlTestBase
{
    private final static String XSI_NS_DECL = "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'";

    protected static class DoubleWrapper {
        public Double d;

        public DoubleWrapper() { }
        public DoubleWrapper(Double value) {
            d = value;
        }
    }

    protected static class DoubleWrapper2 {
        public Double a = 100.0; // init to ensure it gets overwritten
        public Double b = 200.0;

        public DoubleWrapper2() { }
    }

    private final XmlMapper MAPPER = newMapper();

    public void testWithDoubleAsNull() throws Exception
    {
        DoubleWrapper bean = MAPPER.readValue(
"<DoubleWrapper "+XSI_NS_DECL+"><d xsi:nil='true' /></DoubleWrapper>",
                DoubleWrapper.class);
        assertNotNull(bean);
        assertNull(bean.d);

        bean = MAPPER.readValue(
"<DoubleWrapper "+XSI_NS_DECL+"><d xsi:nil='true'>  </d></DoubleWrapper>",
                DoubleWrapper.class);
        assertNotNull(bean);
        assertNull(bean.d);

        // actually we should perhaps also verify there is no content but... for now, let's leave it.
    }

    public void testWithDoubleAsNonNull() throws Exception
    {
        DoubleWrapper bean = MAPPER.readValue(
"<DoubleWrapper "+XSI_NS_DECL+"><d xsi:nil='false'>0.25</d></DoubleWrapper>",
                DoubleWrapper.class);
        assertNotNull(bean);
        assertEquals(Double.valueOf(0.25), bean.d);
    }

    public void testWithDoubleAsMixed() throws Exception
    {
        DoubleWrapper2 bean = MAPPER.readValue(
"<DoubleWrapper "+XSI_NS_DECL+">\n"
+"<a xsi:nil='true'></a>\n"
+"<b xsi:nil='false'>0.25</b>\n"
+"</DoubleWrapper>",
            DoubleWrapper2.class);
        assertNotNull(bean);
        assertNull(bean.a);
        assertEquals(Double.valueOf(0.25), bean.b);

        bean = MAPPER.readValue(
"<DoubleWrapper "+XSI_NS_DECL+">\n"
+"<a xsi:nil='false'>0.25</a>\n"
+"<b xsi:nil='true'></b>\n"
+"</DoubleWrapper>",
            DoubleWrapper2.class);
        assertNotNull(bean);
        assertEquals(Double.valueOf(0.25), bean.a);
        assertNull(bean.b);

        // and last one just for ... funsies
        DoubleWrapper2 defaultValue = new DoubleWrapper2();
        bean = MAPPER.readValue(
"<DoubleWrapper "+XSI_NS_DECL+">\n"
+"</DoubleWrapper>",
            DoubleWrapper2.class);
        assertNotNull(bean.a);
        assertNotNull(bean.b);
        assertEquals(defaultValue.a, bean.a);
        assertEquals(defaultValue.b, bean.b);
    }

    public void testRootPojoAsNull() throws Exception
    {
        Point bean = MAPPER.readValue(
"<Point "+XSI_NS_DECL+" xsi:nil='true' />",
                Point.class);
        assertNull(bean);
    }

    public void testRootPojoAsNonNull() throws Exception
    {
        Point bean = MAPPER.readValue(
"<Point "+XSI_NS_DECL+" xsi:nil='false'></Point>",
                Point.class);
        assertNotNull(bean);
    }

    // [dataformat-xml#467]: Ok to have contents within "xsi:nil" element
    public void testXsiNilWithNonEmptyElement() throws Exception
    {
        JsonNode node = MAPPER.readTree(
"<e>"
+"<h "+XSI_NS_DECL+" xsi:nil='true'><child>stuff</child></h>"
+"</e>"
                );
        assertNotNull(node);
        assertEquals(a2q("{'h':null}"), node.toString());
    }

    // [dataformat-xml#468]: Allow disabling xsi:nil special handling
    public void testDisableXsiNilLeafProcessing() throws Exception
    {
        final ObjectReader r = MAPPER.readerFor(JsonNode.class);
        final String DOC = "<Point "+XSI_NS_DECL+"><x xsi:nil='true'></x></Point>";
 
        // with default processing:
        assertEquals(a2q("{'x':null}"), r.readValue(DOC).toString());

        assertEquals(a2q("{'x':{'nil':'true'}}"),
                r.without(FromXmlParser.Feature.PROCESS_XSI_NIL)
                    .readValue(DOC).toString());
    }

    // [dataformat-xml#468]: Allow disabling xsi:nil special handling

    public void testDisableXsiNilRootProcessing() throws Exception
    {
        final ObjectReader r = MAPPER.readerFor(JsonNode.class);
        final String DOC = "<Point "+XSI_NS_DECL+" xsi:nil='true'></Point>";

        // with default processing:
        assertEquals("null", r.readValue(DOC).toString());

        // 07-Jul-2021, tatu: Alas! 2.x sets format feature flags too late to
        //   affect root element (3.0 works correctly). So cannot test
/*
        ObjectReader noXsiNilReader = r.without(FromXmlParser.Feature.PROCESS_XSI_NIL);
        assertEquals(a2q("{'nil':'true'}"),
                noXsiNilReader.readValue(DOC).toString());
                */
    }
}
