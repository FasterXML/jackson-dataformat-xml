package com.fasterxml.jackson.dataformat.xml;

import java.io.*;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

public class MapperCopyTest extends XmlTestBase
{
    @JsonRootName("AnnotatedName")
    static class Pojo282
    {
        public int a = 3;
    }

    public void testMapperCopy()
    {
        XmlMapper mapper1 = mapperBuilder()
                .nameForTextElement("foo")
                .enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .build();

        XmlMapper mapper2 = mapper1.rebuild().build();
        assertNotSame(mapper1, mapper2);

        XmlFactory xf1 = mapper1.tokenStreamFactory();
        XmlFactory xf2 = mapper2.tokenStreamFactory();
        // 21-May-2018, tatu: with 3.x, factories are immutable so complete fine to
        //    share
        assertSame(xf1, xf2);
        assertEquals(XmlFactory.class, xf2.getClass());

        // and incomplete copy as well
        assertEquals(xf1.getXMLTextElementName(), xf2.getXMLTextElementName());
        assertEquals(xf1.getFormatWriteFeatures(), xf2.getFormatWriteFeatures());
        assertEquals(xf1.getFormatReadFeatures(), xf2.getFormatReadFeatures());

        SerializationConfig sc1 = mapper1.serializationConfig();
        SerializationConfig sc2 = mapper2.serializationConfig();
        assertNotSame(sc1, sc2);
        assertEquals(
            "serialization features did not get copied",
            sc1.getSerializationFeatures(),
            sc2.getSerializationFeatures()
        );
    }

    public void testMapperSerialization() throws Exception
    {
        XmlMapper mapper1 = mapperBuilder()
                .nameForTextElement("foo")
                .build();
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

    // [dataformat-xml#282]
    @SuppressWarnings("deprecation")
    public void testCopyWith() throws Exception
    {
        XmlMapper xmlMapper = newMapper();
        final ObjectMapper xmlMapperNoAnno = xmlMapper.rebuild()
                .disable(MapperFeature.USE_ANNOTATIONS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .build();

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
