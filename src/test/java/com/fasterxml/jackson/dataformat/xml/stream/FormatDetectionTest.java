package com.fasterxml.jackson.dataformat.xml.stream;

import java.io.ByteArrayInputStream;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.format.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

public class FormatDetectionTest extends XmlTestBase
{
    static class POJO {
        public int x, y;
        
        public POJO() { }
        public POJO(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class ListPOJO {
        @JacksonXmlElementWrapper(localName="list")
        public List<POJO> v = new ArrayList<POJO>();
    }
    
    /*
    /**********************************************************
    /* Test methods, success
    /**********************************************************
     */

    private final XmlFactory XML_F = new XmlFactory();
    
    public void testSimpleValidXmlDecl() throws Exception
    {
        DataFormatDetector detector = new DataFormatDetector(XML_F);
        String XML = "<?xml version='1.0'?><root/>";
        DataFormatMatcher matcher = detector.findFormat(new ByteArrayInputStream(XML.getBytes("UTF-8")));
        assertTrue(matcher.hasMatch());
        assertEquals("XML", matcher.getMatchedFormatName());
        assertSame(XML_F, matcher.getMatch());
        assertEquals(MatchStrength.FULL_MATCH, matcher.getMatchStrength());
        // ensure we could build a parser...
        try (JsonParser p = matcher.createParserWithMatch()) {
            assertToken(JsonToken.START_OBJECT, p.nextToken());
            assertToken(JsonToken.END_OBJECT, p.nextToken());
            assertNull(p.nextToken());
        }
    }

    public void testSimpleValidRoot() throws Exception
    {
        DataFormatDetector detector = new DataFormatDetector(XML_F);
        String XML = "<root/>";
        DataFormatMatcher matcher = detector.findFormat(new ByteArrayInputStream(XML.getBytes("UTF-8")));
        assertTrue(matcher.hasMatch());
        assertEquals("XML", matcher.getMatchedFormatName());
        assertSame(XML_F, matcher.getMatch());
        assertEquals(MatchStrength.SOLID_MATCH, matcher.getMatchStrength());
        // ensure we could build a parser...
        try (JsonParser p = matcher.createParserWithMatch()) {
//            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertToken(JsonToken.START_OBJECT, p.nextToken());
            assertToken(JsonToken.END_OBJECT, p.nextToken());
            assertNull(p.nextToken());
        }
    }

    public void testSimpleValidDoctype() throws Exception
    {
        DataFormatDetector detector = new DataFormatDetector(XML_F);
        String XML = "<!DOCTYPE root [ ]>   <root />";
        DataFormatMatcher matcher = detector.findFormat(new ByteArrayInputStream(XML.getBytes("UTF-8")));
        assertTrue(matcher.hasMatch());
        assertEquals("XML", matcher.getMatchedFormatName());
        assertSame(XML_F, matcher.getMatch());
        assertEquals(MatchStrength.SOLID_MATCH, matcher.getMatchStrength());
        // ensure we could build a parser...
        try (JsonParser p = matcher.createParserWithMatch()) {
            assertToken(JsonToken.START_OBJECT, p.nextToken());
            assertToken(JsonToken.END_OBJECT, p.nextToken());
            assertNull(p.nextToken());
        }
    }

    public void testSimpleValidComment() throws Exception
    {
        DataFormatDetector detector = new DataFormatDetector(XML_F);
        String XML = "  <!-- comment -->  <root><child /></root>";
        DataFormatMatcher matcher = detector.findFormat(new ByteArrayInputStream(XML.getBytes("UTF-8")));
        assertTrue(matcher.hasMatch());
        assertEquals("XML", matcher.getMatchedFormatName());
        assertSame(XML_F, matcher.getMatch());
        assertEquals(MatchStrength.SOLID_MATCH, matcher.getMatchStrength());
        // ensure we could build a parser...
        try (JsonParser p = matcher.createParserWithMatch()) {
            assertToken(JsonToken.START_OBJECT, p.nextToken());
        }
    }

    public void testSimpleValidPI() throws Exception
    {
        DataFormatDetector detector = new DataFormatDetector(XML_F);
        String XML = "<?target foo?><root attr='1' />";
        DataFormatMatcher matcher = detector.findFormat(new ByteArrayInputStream(XML.getBytes("UTF-8")));
        assertTrue(matcher.hasMatch());
        assertEquals("XML", matcher.getMatchedFormatName());
        assertSame(XML_F, matcher.getMatch());
        assertEquals(MatchStrength.SOLID_MATCH, matcher.getMatchStrength());
        // ensure we could build a parser...
        try (JsonParser p = matcher.createParserWithMatch()) {
            assertToken(JsonToken.START_OBJECT, p.nextToken());
        }
    }

    public void testSimpleViaObjectReader() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        XmlMapper xmlMapper = new XmlMapper();

        ObjectReader detecting = mapper.readerFor(POJO.class);
        detecting = detecting
                .withFormatDetection(detecting, xmlMapper.readerFor(POJO.class));
        POJO pojo = detecting.readValue(utf8Bytes("<POJO><y>3</y><x>1</x></POJO>"));
        assertNotNull(pojo);
        assertEquals(1, pojo.x);
        assertEquals(3, pojo.y);
    }

    public void testListViaObjectReader() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        XmlMapper xmlMapper = new XmlMapper();
        ListPOJO list = new ListPOJO();
        list.v.add(new POJO(1, 2));
        list.v.add(new POJO(3, 4));
        String xml = xmlMapper.writeValueAsString(list);

        ObjectReader detecting = mapper.readerFor(ListPOJO.class);
        ListPOJO resultList = detecting
                .withFormatDetection(detecting, xmlMapper.readerFor(ListPOJO.class))
                .readValue(utf8Bytes(xml));
        assertNotNull(resultList);
        assertEquals(2, resultList.v.size());
    }

    /*
    /**********************************************************
    /* Test methods, error handling
    /**********************************************************
     */
    
    public void testSimpleInvalid() throws Exception
    {
        DataFormatDetector detector = new DataFormatDetector(XML_F);
        final String NON_XML = "{\"foo\":\"bar\"}";
        DataFormatMatcher matcher = detector.findFormat(new ByteArrayInputStream(NON_XML.getBytes("UTF-8")));
        // should not have match
        assertFalse(matcher.hasMatch());
        // and thus:
        assertEquals(MatchStrength.INCONCLUSIVE, matcher.getMatchStrength());
        // also:
        assertNull(matcher.createParserWithMatch());
    }
}
