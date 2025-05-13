package tools.jackson.dataformat.xml.deser;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.PropertyName;
import tools.jackson.dataformat.xml.JacksonXmlAnnotationIntrospector;
import tools.jackson.dataformat.xml.JacksonXmlAnnotationIntrospectorConfig;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlText;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DifferentDeserializationPropertyNameTest extends XmlTestUtil
{
    static class TestBean {
        @JacksonXmlProperty(localName = "wrong")
        String wrong;

        @JacksonXmlText
        String name;
    }

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    @Test
    public void testWithExplicitProperty() {
        final XmlMapper mapper = XmlMapper.builder()
                .annotationIntrospector(new JacksonXmlAnnotationIntrospector(false,
                        new JacksonXmlAnnotationIntrospectorConfig(false, new PropertyName("name"))))
                .build();

        String xmlInput = "<testBean>ABC123</testBean>";

        TestBean testBean = mapper.readValue(xmlInput, TestBean.class);

        assertEquals("ABC123", testBean.name);
    }

    @Test
    public void testWithInferName() {
        final XmlMapper mapper = XmlMapper.builder()
                .annotationIntrospector(new JacksonXmlAnnotationIntrospector(false,
                        new JacksonXmlAnnotationIntrospectorConfig(true, null)))
                .build();

        String xmlInput = "<testBean>DEF</testBean>";

        TestBean testBean = mapper.readValue(xmlInput, TestBean.class);

        assertEquals("DEF", testBean.name);
    }

    @Test
    public void testWithDuplicateExplicitProperty() {
        final XmlMapper mapper = XmlMapper.builder()
                .annotationIntrospector(new JacksonXmlAnnotationIntrospector(false,
                        new JacksonXmlAnnotationIntrospectorConfig(false, new PropertyName("wrong"))))
                .build();

        String xmlInput = "<testBean>DEF</testBean>";

        Exception result = assertThrows(DatabindException.class, () -> mapper.readValue(xmlInput, TestBean.class));

        assertTrue(result.getMessage().contains("Multiple fields representing property \"wrong\""));
    }
}
