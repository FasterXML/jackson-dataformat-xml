package com.fasterxml.jackson.xml;

import java.io.ByteArrayInputStream;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.format.*;

public class TestXMLFormatDetection extends XmlTestBase
{
    public void testSimpleValidXmlDecl() throws Exception
    {
        XmlFactory f = new XmlFactory();
        DataFormatDetector detector = new DataFormatDetector(f);
        String XML = "<?xml version='1.0'?><root/>";
        DataFormatMatcher matcher = detector.findFormat(new ByteArrayInputStream(XML.getBytes("UTF-8")));
        assertTrue(matcher.hasMatch());
        assertEquals("XML", matcher.getMatchedFormatName());
        assertSame(f, matcher.getMatch());
        assertEquals(MatchStrength.FULL_MATCH, matcher.getMatchStrength());
        // ensure we could build a parser...
        JsonParser jp = matcher.createParserWithMatch();
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        jp.close();
    }

    public void testSimpleValidRoot() throws Exception
    {
        XmlFactory f = new XmlFactory();
        DataFormatDetector detector = new DataFormatDetector(f);
        String XML = "<root/>";
        DataFormatMatcher matcher = detector.findFormat(new ByteArrayInputStream(XML.getBytes("UTF-8")));
        assertTrue(matcher.hasMatch());
        assertEquals("XML", matcher.getMatchedFormatName());
        assertSame(f, matcher.getMatch());
        assertEquals(MatchStrength.SOLID_MATCH, matcher.getMatchStrength());
        // ensure we could build a parser...
        JsonParser jp = matcher.createParserWithMatch();
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        jp.close();
    }

    public void testSimpleValidDoctype() throws Exception
    {
        XmlFactory f = new XmlFactory();
        DataFormatDetector detector = new DataFormatDetector(f);
        String XML = "<!DOCTYPE root [ ]>   <root />";
        DataFormatMatcher matcher = detector.findFormat(new ByteArrayInputStream(XML.getBytes("UTF-8")));
        assertTrue(matcher.hasMatch());
        assertEquals("XML", matcher.getMatchedFormatName());
        assertSame(f, matcher.getMatch());
        assertEquals(MatchStrength.SOLID_MATCH, matcher.getMatchStrength());
        // ensure we could build a parser...
        JsonParser jp = matcher.createParserWithMatch();
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        jp.close();
    }
    
    public void testSimpleValidComment() throws Exception
    {
        XmlFactory f = new XmlFactory();
        DataFormatDetector detector = new DataFormatDetector(f);
        String XML = "  <!-- comment -->  <root></root>";
        DataFormatMatcher matcher = detector.findFormat(new ByteArrayInputStream(XML.getBytes("UTF-8")));
        assertTrue(matcher.hasMatch());
        assertEquals("XML", matcher.getMatchedFormatName());
        assertSame(f, matcher.getMatch());
        assertEquals(MatchStrength.SOLID_MATCH, matcher.getMatchStrength());
        // ensure we could build a parser...
        JsonParser jp = matcher.createParserWithMatch();
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        jp.close();
    }

    public void testSimpleValidPI() throws Exception
    {
        XmlFactory f = new XmlFactory();
        DataFormatDetector detector = new DataFormatDetector(f);
        String XML = "<?target foo?><root />";
        DataFormatMatcher matcher = detector.findFormat(new ByteArrayInputStream(XML.getBytes("UTF-8")));
        assertTrue(matcher.hasMatch());
        assertEquals("XML", matcher.getMatchedFormatName());
        assertSame(f, matcher.getMatch());
        assertEquals(MatchStrength.SOLID_MATCH, matcher.getMatchStrength());
        // ensure we could build a parser...
        JsonParser jp = matcher.createParserWithMatch();
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        jp.close();
    }
    
    public void testSimpleInvalid() throws Exception
    {
        DataFormatDetector detector = new DataFormatDetector(new XmlFactory());
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
