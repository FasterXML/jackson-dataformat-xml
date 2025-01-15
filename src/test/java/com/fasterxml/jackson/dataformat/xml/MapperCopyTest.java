package com.fasterxml.jackson.dataformat.xml;

import java.io.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.dataformat.xml.ser.XmlSerializerProvider;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;

import static org.junit.jupiter.api.Assertions.*;

public class MapperCopyTest extends XmlTestUtil
{
    @JsonRootName("AnnotatedName")
    static class Pojo282
    {
        public int a = 3;
    }

    @Test
    public void testMapperCopy()
    {
        XmlMapper mapper1 = mapperBuilder()
                .nameForTextElement("foo")
                .configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .build();

        XmlMapper mapper2 = mapper1.copy();
        assertNotSame(mapper1, mapper2);
        XmlFactory xf1 = mapper1.getFactory();
        XmlFactory xf2 = mapper2.getFactory();
        assertNotSame(xf1, xf2);
        assertEquals(XmlFactory.class, xf2.getClass());

        // and incomplete copy as well
        assertEquals(xf1.getXMLTextElementName(), xf2.getXMLTextElementName());
        assertEquals(xf1._xmlGeneratorFeatures, xf2._xmlGeneratorFeatures);
        assertEquals(xf1._xmlParserFeatures, xf2._xmlParserFeatures);

        SerializationConfig sc1 = mapper1.getSerializationConfig();
        SerializationConfig sc2 = mapper2.getSerializationConfig();
        assertNotSame(sc1, sc2);
        assertEquals(
            sc1.getSerializationFeatures(),
            sc2.getSerializationFeatures(),
            "serialization features did not get copied"
        );
    }

    @Test
    public void testSerializerProviderCopy() {
        DefaultSerializerProvider provider = new XmlSerializerProvider(new XmlRootNameLookup());
        DefaultSerializerProvider copy = provider.copy();
        assertNotSame(provider, copy);
    }

    @Test
    public void testMapperSerialization() throws Exception
    {
        XmlMapper mapper1 = mapperBuilder()
                .nameForTextElement("foo")
                .build();
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

    // [dataformat-xml#282]
    @SuppressWarnings("deprecation")
    @Test
    public void testCopyWith() throws Exception
    {
        XmlMapper xmlMapper = newMapper();
        final ObjectMapper xmlMapperNoAnno = xmlMapper.copy()
                .disable(MapperFeature.USE_ANNOTATIONS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        String xml1 = xmlMapper.writeValueAsString(new Pojo282());
        String xml2 = xmlMapperNoAnno.writeValueAsString(new Pojo282());

        if (!xml1.contains("AnnotatedName")) {
            fail("Should use name 'AnnotatedName', xml = "+xml1);
        }
        if (!xml2.contains("Pojo282")
                || xml2.contains("AnnotatedName")) {
            fail("Should NOT use name 'AnnotatedName' but 'Pojo282', xml = "+xml1);
        }
    }
}
