package tools.jackson.dataformat.xml.jaxb;

import org.junit.jupiter.api.Test;

import jakarta.xml.bind.annotation.*;

import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;
import tools.jackson.dataformat.xml.*;
import tools.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;

import static org.junit.jupiter.api.Assertions.assertEquals;

// [dataformat-xml#559] JAXB @XmlValue deserializing not working with records
public class JaxbXmlValueRecord559Test extends XmlTestUtil
{
    @XmlRootElement(name = "TestObject")
    record TestObject(
            @XmlValue String name,
            @XmlAttribute int age) {}

    // POJO equivalent
    @XmlRootElement(name = "TestObject")
    static class TestPojo {
        @XmlValue
        public String name;
        @XmlAttribute
        public int age;
    }

    private final XmlMapper MAPPER;
    {
        JakartaXmlBindAnnotationIntrospector jaxbIntr = new JakartaXmlBindAnnotationIntrospector();
        AnnotationIntrospector intr = XmlAnnotationIntrospector.Pair.instance(
                jaxbIntr,
                new JacksonAnnotationIntrospector());
        MAPPER = XmlMapper.builder()
                .annotationIntrospector(intr)
                .build();
    }

    // POJO: works
    @Test
    public void testDeserializePojo559() throws Exception {
        String xml = "<TestObject age=\"12\">foo</TestObject>";
        TestPojo obj = MAPPER.readValue(xml, TestPojo.class);
        assertEquals("foo", obj.name);
        assertEquals(12, obj.age);
    }

    // [dataformat-xml#559] Record: was failing, now fixed
    @Test
    public void testDeserializeRecord559() throws Exception {
        String xml = "<TestObject age=\"12\">foo</TestObject>";
        TestObject obj = MAPPER.readValue(xml, TestObject.class);
        assertEquals("foo", obj.name());
        assertEquals(12, obj.age());
    }
}
