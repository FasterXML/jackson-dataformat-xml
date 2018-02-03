package com.fasterxml.jackson.dataformat.xml;

import java.io.*;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.dataformat.xml.ser.XmlSerializerProvider;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;

public class VersionInfoTest extends XmlTestBase
{
    public void testMapperVersions()
    {
        assertVersion(new XmlMapper());
        assertVersion(new XmlFactory());
    }

    // Test XmlMapper.copy()
    public void testMapperCopy()
    {
        XmlMapper mapper1 = new XmlMapper(XmlFactory.builder()
                .enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
                .nameForTextElement("foo")
                .build());
        mapper1.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        XmlMapper mapper2 = mapper1.copy();
        assertNotSame(mapper1, mapper2);
        XmlFactory xf1 = mapper1.tokenStreamFactory();
        XmlFactory xf2 = mapper2.tokenStreamFactory();
        // 31-Jan-2018, tatu: stream factories immutable with 3.0 so:
        assertSame(xf1, xf2);

        // and [dataformat-xml#48] as well, incomplete copy...
        assertEquals(xf1.getXMLTextElementName(), xf2.getXMLTextElementName());
        assertEquals(xf1._xmlGeneratorFeatures, xf2._xmlGeneratorFeatures);
        assertEquals(xf1._xmlParserFeatures, xf2._xmlParserFeatures);

        // and [dataformat-xml#233]
        SerializationConfig sc1 = mapper1.getSerializationConfig();
        SerializationConfig sc2 = mapper2.getSerializationConfig();
        assertNotSame(sc1, sc2);
        assertEquals(
            "serialization features did not get copied",
            sc1.getSerializationFeatures(),
            sc2.getSerializationFeatures()
        );
    }

    public void testSerializerProviderCopy() {
        DefaultSerializerProvider provider = new XmlSerializerProvider(new XmlFactory(),
                new XmlRootNameLookup());
        DefaultSerializerProvider copy = provider.copy();
        assertNotSame(provider, copy);
    }

    public void testMapperSerialization() throws Exception
    {
        XmlMapper mapper1 = new XmlMapper(XmlFactory.builder()
                .nameForTextElement("foo")
                .build());
        assertEquals("foo", mapper1.tokenStreamFactory().getXMLTextElementName());

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(bytes);
        objectStream.writeObject(mapper1);
        objectStream.close();
        
        ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        XmlMapper mapper2 = (XmlMapper) input.readObject();
        input.close();

        assertEquals("foo", mapper2.tokenStreamFactory().getXMLTextElementName());
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

