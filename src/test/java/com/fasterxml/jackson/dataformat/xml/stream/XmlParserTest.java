package com.fasterxml.jackson.dataformat.xml.stream;

import java.io.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.JsonParser.NumberType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;

public class XmlParserTest extends XmlTestBase
{
    protected final JsonFactory _jsonFactory = new JsonFactory();
    protected final XmlMapper _xmlMapper = newMapper();
    protected XmlFactory _xmlFactory = _xmlMapper.getFactory();

    /*
    /**********************************************************************
    /* Unit tests, simplest/manual
    /**********************************************************************
     */
    
    public void testSimplest() throws Exception
    {
        final String XML = "<root><leaf>abc</leaf></root>";
        // -> "{\"leaf\":\"abc\"}"

        try (JsonParser p = _xmlMapper.createParser(XML)) {
            assertToken(JsonToken.START_OBJECT, p.nextToken());
            assertToken(JsonToken.FIELD_NAME, p.nextToken());
            assertEquals("leaf", p.currentName());
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("abc", p.getText());
            assertToken(JsonToken.END_OBJECT, p.nextToken());
            assertNull(p.nextToken());
        }
    }

    public void testSimpleWithEmpty() throws Exception
    {
        // 21-Jun-2017, tatu: Depends on setting actually...

        final String XML = "<root><leaf /></root>";

        // -> "{"leaf":null}"
        try (JsonParser p = _xmlMapper.reader().with(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL)
                .createParser(XML)) {
            assertTrue(((FromXmlParser) p).isEnabled(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL));
            assertToken(JsonToken.START_OBJECT, p.nextToken());
            assertToken(JsonToken.FIELD_NAME, p.nextToken());
            assertEquals("leaf", p.currentName());
            assertToken(JsonToken.VALUE_NULL, p.nextToken());
            assertToken(JsonToken.END_OBJECT, p.nextToken());
            assertNull(p.nextToken());
        }

        // -> "{"leaf":""}"
        try (JsonParser p = _xmlMapper.reader().without(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL)
                .createParser(XML)) {
            assertFalse(((FromXmlParser) p).isEnabled(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL));
            assertToken(JsonToken.START_OBJECT, p.nextToken());
            assertToken(JsonToken.FIELD_NAME, p.nextToken());
            assertEquals("leaf", p.currentName());
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("", p.getText());
            assertToken(JsonToken.END_OBJECT, p.nextToken());
            assertNull(p.nextToken());
        }
    }

    /**
     * Test that verifies coercion of a "simple" cdata segment within root element
     * as matching scalar token, similar to how other elements work.
     */
    public void testRootScalar() throws Exception
    {
        // 02-Jul-2020, tatu: Does not work quite yet
        final String XML = "<data>value</data>";
        try (JsonParser p = _xmlMapper.createParser(XML)) {
            assertToken(JsonToken.START_OBJECT, p.nextToken());
            assertToken(JsonToken.FIELD_NAME, p.nextToken());
            assertEquals("", p.currentName());
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("value", p.getText());
            assertToken(JsonToken.END_OBJECT, p.nextToken());
            assertNull(p.nextToken());
            // should be ok to call again tho
            assertNull(p.nextToken());
        }
    }

    public void testRootMixed() throws Exception
    {
        // 02-Jul-2020, tatu: Does not work quite yet
        final String XML = "<data>value<child>abc</child></data>";
        try (JsonParser p = _xmlMapper.createParser(XML)) {
            assertToken(JsonToken.START_OBJECT, p.nextToken());

            assertToken(JsonToken.FIELD_NAME, p.nextToken());
            assertEquals("", p.currentName());
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("value", p.getText());

            assertToken(JsonToken.FIELD_NAME, p.nextToken());
            assertEquals("child", p.currentName());
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("abc", p.getText());

            assertToken(JsonToken.END_OBJECT, p.nextToken());
            assertNull(p.nextToken());
        }
    }

    /*
    /**********************************************************************
    /* Unit tests, slightly bigger, automated
    /**********************************************************************
     */
    
    public void testSimpleNested() throws Exception
    {
        assertEquals("{\"a\":{\"b\":{\"c\":\"xyz\"}}}",
                _readXmlWriteJson("<root><a><b><c>xyz</c></b></a></root>"));
    }

    /**
     * Unit test that verifies that we can write sample document from JSON
     * specification as XML, and read it back in "as JSON", with
     * expected transformation.
     */
    public void testRoundTripWithSample() throws Exception
    {
        // First: let's convert from sample JSON doc to default xml output
        JsonNode root = new ObjectMapper().readTree(SAMPLE_DOC_JSON_SPEC);
        String xml = _xmlMapper.writeValueAsString(root);
        
        // Here we would ideally use base class test method. Alas, it won't
        // work due to couple of problems;
        // (a) All values are reported as Strings (not ints, for example
        // (b) XML mangles arrays, so all we see are objects.
        // Former could be worked around; latter less so at this point.

        // So, for now, let's just do sort of minimal verification, manually
        JsonParser p = _xmlMapper.createParser(xml);
        
        assertToken(JsonToken.START_OBJECT, p.nextToken()); // main object

        assertToken(JsonToken.FIELD_NAME, p.nextToken()); // 'Image'
        verifyFieldName(p, "Image");
        assertToken(JsonToken.START_OBJECT, p.nextToken()); // 'image' object
        assertToken(JsonToken.FIELD_NAME, p.nextToken()); // 'Width'
        verifyFieldName(p, "Width");
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(String.valueOf(SAMPLE_SPEC_VALUE_WIDTH), p.getText());
        assertToken(JsonToken.FIELD_NAME, p.nextToken()); // 'Height'
        verifyFieldName(p, "Height");
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(String.valueOf(SAMPLE_SPEC_VALUE_HEIGHT), p.getText());
        assertToken(JsonToken.FIELD_NAME, p.nextToken()); // 'Title'
        verifyFieldName(p, "Title");
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(SAMPLE_SPEC_VALUE_TITLE, getAndVerifyText(p));
        assertToken(JsonToken.FIELD_NAME, p.nextToken()); // 'Thumbnail'
        verifyFieldName(p, "Thumbnail");
        assertToken(JsonToken.START_OBJECT, p.nextToken()); // 'thumbnail' object
        assertToken(JsonToken.FIELD_NAME, p.nextToken()); // 'Url'
        verifyFieldName(p, "Url");
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(SAMPLE_SPEC_VALUE_TN_URL, getAndVerifyText(p));
        assertToken(JsonToken.FIELD_NAME, p.nextToken()); // 'Height'
        verifyFieldName(p, "Height");
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(String.valueOf(SAMPLE_SPEC_VALUE_TN_HEIGHT), p.getText());
        assertToken(JsonToken.FIELD_NAME, p.nextToken()); // 'Width'
        verifyFieldName(p, "Width");
        // Width value is actually a String in the example
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(SAMPLE_SPEC_VALUE_TN_WIDTH, getAndVerifyText(p));

        assertToken(JsonToken.END_OBJECT, p.nextToken()); // 'thumbnail' object

        // Note: arrays are "eaten"; wrapping is done using BeanPropertyWriter, so:
        //assertToken(JsonToken.FIELD_NAME, p.nextToken()); // 'IDs'
        //verifyFieldName(p, "IDs");
        //assertToken(JsonToken.START_OBJECT, p.nextToken()); // 'ids' array

        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        verifyFieldName(p, "IDs");
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(String.valueOf(SAMPLE_SPEC_VALUE_TN_ID1), getAndVerifyText(p));
        assertToken(JsonToken.FIELD_NAME, p.nextToken()); 
        verifyFieldName(p, "IDs");
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(String.valueOf(SAMPLE_SPEC_VALUE_TN_ID2), getAndVerifyText(p));
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        verifyFieldName(p, "IDs");
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(String.valueOf(SAMPLE_SPEC_VALUE_TN_ID3), getAndVerifyText(p));
        assertToken(JsonToken.FIELD_NAME, p.nextToken()); 
        verifyFieldName(p, "IDs");
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(String.valueOf(SAMPLE_SPEC_VALUE_TN_ID4), getAndVerifyText(p));

        // no matching entry for array:
        //assertToken(JsonToken.END_OBJECT, p.nextToken()); // 'ids' array

        assertToken(JsonToken.END_OBJECT, p.nextToken()); // 'image' object

        assertToken(JsonToken.END_OBJECT, p.nextToken()); // main object
        
        p.close();
    }

    /**
     * Test to ensure functionality used to force an element to be reported
     * as "JSON" Array, instead of default Object.
     */
    public void testForceElementAsArray() throws Exception
    {
        final String XML = "<array><elem>value</elem><elem><property>123</property></elem><elem>1</elem></array>";

        FromXmlParser xp = (FromXmlParser) _xmlFactory.createParser(new StringReader(XML));

        // First: verify handling without forcing array handling:
        assertToken(JsonToken.START_OBJECT, xp.nextToken()); // <array>
        assertToken(JsonToken.FIELD_NAME, xp.nextToken()); // <elem>
        assertEquals("elem", xp.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, xp.nextToken());
        assertEquals("value", xp.getText());

        assertToken(JsonToken.FIELD_NAME, xp.nextToken()); // <elem>
        assertEquals("elem", xp.getCurrentName());
        assertToken(JsonToken.START_OBJECT, xp.nextToken()); // <property>
        assertToken(JsonToken.FIELD_NAME, xp.nextToken());
        assertEquals("property", xp.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, xp.nextToken());
        assertEquals("123", xp.getText());
        assertToken(JsonToken.END_OBJECT, xp.nextToken()); // <object>

        assertToken(JsonToken.FIELD_NAME, xp.nextToken()); // <elem>
        assertEquals("elem", xp.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, xp.nextToken());
        assertEquals("1", xp.getText());

        assertToken(JsonToken.END_OBJECT, xp.nextToken()); // </array>
        xp.close();

        // And then with array handling:
        xp = (FromXmlParser) _xmlMapper.createParser(XML);
        assertTrue(xp.getParsingContext().inRoot());

        assertToken(JsonToken.START_OBJECT, xp.nextToken()); // <array>
        assertTrue(xp.getParsingContext().inObject()); // true until we do following:

        // must request 'as-array' handling, which will "convert" current token:
        assertTrue("Should 'convert' START_OBJECT to START_ARRAY", xp.isExpectedStartArrayToken());
        assertToken(JsonToken.START_ARRAY, xp.getCurrentToken()); // <elem>
        assertTrue(xp.getParsingContext().inArray());

        assertToken(JsonToken.VALUE_STRING, xp.nextToken());
        assertTrue(xp.getParsingContext().inArray());
        assertEquals("value", xp.getText());

        assertToken(JsonToken.START_OBJECT, xp.nextToken()); // <property>
        assertTrue(xp.getParsingContext().inObject());
        assertToken(JsonToken.FIELD_NAME, xp.nextToken());
        assertEquals("property", xp.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, xp.nextToken());
        assertEquals("123", xp.getText());

        StringWriter w = new StringWriter();
        assertEquals(3, xp.getText(w));
        assertEquals("123", w.toString());
        
        assertTrue(xp.getParsingContext().inObject());
        assertToken(JsonToken.END_OBJECT, xp.nextToken()); // </property>
        assertTrue(xp.getParsingContext().inArray());

        assertToken(JsonToken.VALUE_STRING, xp.nextToken());
        assertTrue(xp.getParsingContext().inArray());
        assertEquals("1", xp.getText());

        assertToken(JsonToken.END_ARRAY, xp.nextToken()); // </array>
        assertTrue(xp.getParsingContext().inRoot());
        xp.close();
    }

    public void testXmlAttributes() throws Exception
    {
        final String XML = "<data max=\"7\" offset=\"9\"/>";

        FromXmlParser xp = (FromXmlParser) _xmlMapper.createParser(XML);

        // First: verify handling without forcing array handling:
        assertToken(JsonToken.START_OBJECT, xp.nextToken()); // <data>
        assertToken(JsonToken.FIELD_NAME, xp.nextToken()); // <max>
        assertEquals("max", xp.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, xp.nextToken());
        assertEquals("7", xp.getText());

        assertToken(JsonToken.FIELD_NAME, xp.nextToken()); // <offset>
        assertEquals("offset", xp.getCurrentName());

        StringWriter w = new StringWriter();
        assertEquals(6, xp.getText(w));
        assertEquals("offset", w.toString());
        
        assertToken(JsonToken.VALUE_STRING, xp.nextToken());
        assertEquals("9", xp.getText());

        w = new StringWriter();
        assertEquals(1, xp.getText(w));
        assertEquals("9", w.toString());
        
        assertToken(JsonToken.END_OBJECT, xp.nextToken()); // </data>
        xp.close();
    }

    public void testMixedContent() throws Exception
    {
        String exp = a2q("{'':'first','a':'123','':'second','b':'456','':'last'}");
        String result = _readXmlWriteJson("<root>first<a>123</a>second<b>456</b>last</root>");

//System.err.println("result = \n"+result);
        assertEquals(exp, result);
    }

    public void testInferredNumbers() throws Exception
    {
        final String XML = "<data value1='abc' value2='42'>123456789012</data>";

        FromXmlParser xp = (FromXmlParser) _xmlMapper.createParser(XML);

        // First: verify handling without forcing array handling:
        assertToken(JsonToken.START_OBJECT, xp.nextToken()); // <data>
        assertToken(JsonToken.FIELD_NAME, xp.nextToken()); // @value1
        assertEquals("value1", xp.currentName());
        assertToken(JsonToken.VALUE_STRING, xp.nextToken());
        assertFalse(xp.isExpectedNumberIntToken());
        assertEquals("abc", xp.getText());

        assertToken(JsonToken.FIELD_NAME, xp.nextToken()); // @value2
        assertEquals("value2", xp.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, xp.nextToken());
        assertTrue(xp.isExpectedNumberIntToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, xp.currentToken());
        assertEquals(NumberType.INT, xp.getNumberType());
        assertEquals(42, xp.getIntValue());

        assertToken(JsonToken.FIELD_NAME, xp.nextToken()); // implicit for text
        assertEquals("", xp.getCurrentName());

        assertToken(JsonToken.VALUE_STRING, xp.nextToken());
        assertTrue(xp.isExpectedNumberIntToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, xp.currentToken());
        assertEquals(NumberType.LONG, xp.getNumberType());
        assertEquals(123456789012L, xp.getLongValue());
        
        assertToken(JsonToken.END_OBJECT, xp.nextToken()); // </data>
        xp.close();
    }

    /*
    /**********************************************************************
    /* Helper methods
    /**********************************************************************
     */

    private String _readXmlWriteJson(String xml) throws IOException
    {
        return _readXmlWriteJson(_xmlFactory, xml);
    }

    private String _readXmlWriteJson(XmlFactory xmlFactory, String xml) throws IOException
    {
        StringWriter w = new StringWriter();
        try (JsonParser p = xmlFactory.createParser(xml)) {
            try (JsonGenerator jg = _jsonFactory.createGenerator(w)) {
                while (p.nextToken() != null) {
                    jg.copyCurrentEvent(p);
                }
            }
        }
        return w.toString();
    }
}
