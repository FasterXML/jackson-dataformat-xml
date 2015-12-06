package com.fasterxml.jackson.dataformat.xml;

import java.io.*;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.dataformat.xml.PackageVersion;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

public class VersionInfoTest extends XmlTestBase
{
    public void testMapperVersions()
    {
        assertVersion(new XmlMapper());
        assertVersion(new XmlFactory());
    }

    // @since 2.1
    // [Issue#48]: ObjectMapper.copy()
    public void testMapperCopy()
    {
        XmlMapper mapper1 = new XmlMapper();
        mapper1.setXMLTextElementName("foo");
        mapper1.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        
        XmlMapper mapper2 = mapper1.copy();
        assertNotSame(mapper1, mapper2);
        XmlFactory xf1 = mapper1.getFactory();
        XmlFactory xf2 = mapper2.getFactory();
        assertNotSame(xf1, xf2);
        assertEquals(XmlFactory.class, xf2.getClass());

        // and [Issue#48] as well, incomplete copy...
        assertEquals(xf1.getXMLTextElementName(), xf2.getXMLTextElementName());
        assertEquals(xf1._xmlGeneratorFeatures, xf2._xmlGeneratorFeatures);
        assertEquals(xf1._xmlParserFeatures, xf2._xmlParserFeatures);
    }

    // Another test for [Issue#48]
    public void testMapperSerialization() throws Exception
    {
        XmlMapper mapper1 = new XmlMapper();
        mapper1.setXMLTextElementName("foo");
        assertEquals("foo", mapper1.getFactory().getXMLTextElementName());

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(bytes);
        objectStream.writeObject(mapper1);
        objectStream.close();
        
        ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        XmlMapper mapper2 = (XmlMapper) input.readObject();
        input.close();

        assertEquals("foo", mapper2.getFactory().getXMLTextElementName());
    }
    
    /*
    /**********************************************************
    /* Helper methods
    /**********************************************************
     */
    
    private void assertVersion(Versioned vers)
    {
        final Version v = vers.version();
        assertFalse("Should find version information (got "+v+")", v.isUnknownVersion());
        Version exp = PackageVersion.VERSION;
        assertEquals(exp.toFullString(), v.toFullString());
        assertEquals(exp, v);
    }
}

