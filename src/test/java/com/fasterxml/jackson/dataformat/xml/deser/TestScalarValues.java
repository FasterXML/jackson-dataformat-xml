package com.fasterxml.jackson.dataformat.xml.deser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class TestScalarValues {

	/*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();
    

    // Byte
    @Test
    public void testByteClass() throws IOException
    {
    	Byte b = MAPPER.readValue("<Byte>1</Byte>", Byte.class);
    	assertNotNull(b);
    	assertEquals(1, b.byteValue());
    }
    
    @Test
    public void testByte() throws IOException
    {
    	byte b = MAPPER.readValue("<Byte>1</Byte>", byte.class);
    	assertEquals(1, b);
    }
    
    @Test
    public void testByteWithAttribute() throws IOException
    {
    	byte b = MAPPER.readValue("<Byte xmlns=\"\">1</Byte>", byte.class);
    	assertEquals(1, b);
    }
    
    @Test
    public void testByteWithWhitespace() throws IOException
    {
    	byte b = MAPPER.readValue("<Byte\t>1</Byte>", byte.class);
    	assertEquals(1, b);
    }
    
    @Test
    public void testClassThatLooksLikeByte() throws IOException
    {
    	try
    	{
    		MAPPER.readValue("<Byteish>123</Byteish>", byte.class);
    		fail();
    	}
    	catch (JsonMappingException e) { }
    }

    
    // Character
    @Test
    public void testCharacterClass() throws IOException
    {
    	Character c = MAPPER.readValue("<Character>a</Character>", Character.class);
    	assertNotNull(c);
    	assertEquals('a', c.charValue());
    }
    
    @Test
    public void testChar() throws IOException
    {
    	char c = MAPPER.readValue("<Character>a</Character>", char.class);
    	assertEquals('a', c);
    }
    
    @Test
    public void testCharWithAttribute() throws IOException
    {
    	char c = MAPPER.readValue("<Character xmlns=\"\">a</Character>", char.class);
    	assertEquals('a', c);
    }
    
    @Test
    public void testCharWithWhitespace() throws IOException
    {
    	char c = MAPPER.readValue("<Character\t>a</Character>", char.class);
    	assertEquals('a', c);
    }
    
    @Test
    public void testClassThatLooksLikeCharacter() throws IOException
    {
    	try
    	{
    		MAPPER.readValue("<Characterish>a</Characterish>", char.class);
    		fail();
    	}
    	catch (JsonMappingException e) { }
    }

    
    // Short
    @Test
    public void testShortClass() throws IOException
    {
    	Short s = MAPPER.readValue("<Short>123</Short>", Short.class);
    	assertNotNull(s);
    	assertEquals(123, s.shortValue());
    }
    
    @Test
    public void testShort() throws IOException
    {
    	short s = MAPPER.readValue("<Short>123</Short>", short.class);
    	assertEquals(123, s);
    }
    
    @Test
    public void testShortWithAttribute() throws IOException
    {
    	short s = MAPPER.readValue("<Short xmlns=\"\">123</Short>", short. class);
    	assertEquals(123, s);
    }
    
    @Test
    public void testShortWithWhitespace() throws IOException
    {
    	short s = MAPPER.readValue("<Short\t>123</Short>", short.class);
    	assertEquals(123, s);
    }
    
    @Test
    public void testClassThatLooksLikeShort() throws IOException
    {
    	try
    	{
    		MAPPER.readValue("<Shortish>123</Shortish>", short.class);
    		fail();
    	}
    	catch (JsonMappingException e) { }
    }

    
    // Integer
    @Test
    public void testIntegerClass() throws IOException
    {
    	Integer i = MAPPER.readValue("<Integer>123</Integer>", Integer.class);
    	assertNotNull(i);
    	assertEquals(123, i.intValue());
    }
    
    @Test
    public void testInt() throws IOException
    {
    	int i = MAPPER.readValue("<Integer>123</Integer>", int.class);
    	assertEquals(123, i);
    }
    
    @Test
    public void testIntWithAttribute() throws IOException
    {
    	int i = MAPPER.readValue("<Integer xmlns=\"\">123</Integer>", int.class);
    	assertEquals(123, i);
    }
    
    @Test
    public void testIntWithWhitespace() throws IOException
    {
    	int i= MAPPER.readValue("<Integer\t>123</Integer>", int.class);
    	assertEquals(123, i);
    }
    
    @Test
    public void testClassThatLooksLikeInteger() throws IOException
    {
    	try
    	{
    		MAPPER.readValue("<Integerish>123</Integerish>", int.class);
    		fail();
    	}
    	catch (JsonMappingException e) { }
    }
    

    // Long
    @Test
    public void testLongClass() throws IOException
    {
    	Long l = MAPPER.readValue("<Long>123</Long>", Long.class);
    	assertNotNull(l);
    	assertEquals(123, l.longValue());
    }
    
    @Test
    public void testLong() throws IOException
    {
    	long l = MAPPER.readValue("<Long>123</Long>", long.class);
    	assertEquals(123, l);
    }
    
    @Test
    public void testLongWithAttribute() throws IOException
    {
    	long l = MAPPER.readValue("<Long xmlns=\"\">123</Long>", long.class);
    	assertEquals(123, l);
    }
    
    @Test
    public void testLongWithWhitespace() throws IOException
    {
    	long l = MAPPER.readValue("<Long\t>123</Long>", long.class);
    	assertEquals(123, l);
    }
    
    @Test
    public void testClassThatLooksLikeLong() throws IOException
    {
    	try
    	{
    		MAPPER.readValue("<Longish>123</Longish>", long.class);
    		fail();
    	}
    	catch (JsonMappingException e) { }
    }

    
    // Float
    @Test
    public void testFloatClass() throws IOException
    {
    	Float f = MAPPER.readValue("<Float>123.45</Float>", Float.class);
    	assertNotNull(f);
    	assertEquals(123.45, f.floatValue(), 0.001);
    }
    
    @Test
    public void testFloat() throws IOException
    {
    	float f = MAPPER.readValue("<Float>123.45</Float>", float.class);
    	assertEquals(123.45, f, 0.001);
    }
    
    @Test
    public void testFloatWithAttribute() throws IOException
    {
    	float f = MAPPER.readValue("<Float xmlns=\"\">123.45</Float>", float.class);
    	assertEquals(123.45, f, 0.001);
    }
    
    @Test
    public void testFloatWithWhitespace() throws IOException
    {
    	float f = MAPPER.readValue("<Float\t>123.45</Float>", float.class);
    	assertEquals(123.45, f, 0.001);
    }
    
    @Test
    public void testClassThatLooksLikeFloat() throws IOException
    {
    	try
    	{
    		MAPPER.readValue("<Floatish>123.45</Floatish>", float.class);
    		fail();
    	}
    	catch (JsonMappingException e) { }
    }

    
    // Double
    @Test
    public void testDoubleClass() throws IOException
    {
    	Double d = MAPPER.readValue("<Double>123.45</Double>", Double.class);
    	assertNotNull(d);
    	assertEquals(123.45, d.doubleValue(), 0.001);
    }
    
    @Test
    public void testDouble() throws IOException
    {
    	double d = MAPPER.readValue("<Double>123.45</Double>", double.class);
    	assertEquals(123.45, d, 0.001);
    }
    
    @Test
    public void testDoubleWithAttribute() throws IOException
    {
    	double d = MAPPER.readValue("<Double xmlns=\"\">123.45</Double>", double.class);
    	assertEquals(123.45, d, 0.001);
    }
    
    @Test
    public void testDoubleWithWhitespace() throws IOException
    {
    	double d= MAPPER.readValue("<Double\t>123.45</Double>", double.class);
    	assertEquals(123.45, d, 0.001);
    }
    
    @Test
    public void testClassThatLooksLikeDouble() throws IOException
    {
    	try
    	{
    		MAPPER.readValue("<Doubleish>123</Doubleish>", int.class);
    		fail();
    	}
    	catch (JsonMappingException e) { }
    }
}
