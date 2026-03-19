package tools.jackson.dataformat.xml.tofix;

import org.junit.jupiter.api.Test;

import jakarta.xml.bind.annotation.*;

import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;
import tools.jackson.dataformat.xml.*;
import tools.jackson.dataformat.xml.testutil.failure.JacksonTestFailureExpected;
import tools.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;

import static org.junit.jupiter.api.Assertions.assertEquals;

// [dataformat-xml#559] JAXB @XmlValue deserializing not working with records
//
// Root cause: @XmlValue targets only FIELD and METHOD, not PARAMETER.
// For records, Java doesn't propagate the annotation to the constructor parameter,
// so Jackson can't match the property-based creator param to the @XmlValue property.
// Additionally, the JAXB introspector assigns implicit name "value" to @XmlValue
// properties, causing a property definition split (constructor param keeps "name",
// while field/getter get renamed to "value").
public class JaxbXmlValueRecord559Test extends XmlTestUtil
{
    @XmlRootElement(name = "TestObject")
    record TestObject(
            @XmlValue String name,
            @XmlAttribute int age) {}

    // POJO equivalent — works fine because field/getter/setter all merge
    // under the JAXB-assigned "value" name (no constructor param involved)
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

    // Record: fails
    @JacksonTestFailureExpected
    @Test
    public void testDeserializeRecord559() throws Exception {
        String xml = "<TestObject age=\"12\">foo</TestObject>";
        TestObject obj = MAPPER.readValue(xml, TestObject.class);
        assertEquals("foo", obj.name());
        assertEquals(12, obj.age());
    }
}
