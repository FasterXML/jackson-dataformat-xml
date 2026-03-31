package tools.jackson.dataformat.xml.deser;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonRootName;

import tools.jackson.databind.DatabindException;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlReadFeature;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for [dataformat-xml#247]: verification of root element name
 * against expected (from annotation or class name) when
 * {@link XmlReadFeature#ENFORCE_ROOT_ELEMENT_NAME} is enabled.
 */
public class RootElementNameValidation247Test extends XmlTestUtil
{
    static class Root {
        public int value;
    }

    @JsonRootName("MyRoot")
    static class AnnotatedRoot {
        public int value;
    }

    @JacksonXmlRootElement(localName = "XmlRoot")
    static class XmlAnnotatedRoot {
        public int value;
    }

    private final XmlMapper ENFORCING_MAPPER = XmlMapper.builder()
            .enable(XmlReadFeature.ENFORCE_ROOT_ELEMENT_NAME)
            .build();

    private final XmlMapper DEFAULT_MAPPER = newMapper();

    // Matching class name should pass
    @Test
    public void testMatchingClassNameSucceeds() throws Exception
    {
        Root root = ENFORCING_MAPPER.readValue(
                "<Root><value>42</value></Root>", Root.class);
        assertEquals(42, root.value);
    }

    // Mismatched root name should fail when feature is enabled
    @Test
    public void testMismatchedNameFails() throws Exception
    {
        DatabindException e = assertThrows(DatabindException.class, () ->
            ENFORCING_MAPPER.readValue(
                    "<Boot><value>42</value></Boot>", Root.class));
        verifyException(e, "Root name");
        verifyException(e, "Boot");
        verifyException(e, "Root");
    }

    // Mismatched root name should succeed when feature is disabled (default)
    @Test
    public void testMismatchedNameSucceedsWhenDisabled() throws Exception
    {
        Root root = DEFAULT_MAPPER.readValue(
                "<Boot><value>42</value></Boot>", Root.class);
        assertEquals(42, root.value);
    }

    // @JsonRootName annotation should be used for expected name
    @Test
    public void testJsonRootNameAnnotation() throws Exception
    {
        AnnotatedRoot root = ENFORCING_MAPPER.readValue(
                "<MyRoot><value>42</value></MyRoot>", AnnotatedRoot.class);
        assertEquals(42, root.value);
    }

    @Test
    public void testJsonRootNameAnnotationMismatch() throws Exception
    {
        DatabindException e = assertThrows(DatabindException.class, () ->
            ENFORCING_MAPPER.readValue(
                    "<AnnotatedRoot><value>42</value></AnnotatedRoot>",
                    AnnotatedRoot.class));
        verifyException(e, "Root name");
        verifyException(e, "AnnotatedRoot");
        verifyException(e, "MyRoot");
    }

    // @JacksonXmlRootElement annotation should be used for expected name
    @Test
    public void testJacksonXmlRootElementAnnotation() throws Exception
    {
        XmlAnnotatedRoot root = ENFORCING_MAPPER.readValue(
                "<XmlRoot><value>42</value></XmlRoot>", XmlAnnotatedRoot.class);
        assertEquals(42, root.value);
    }

    @Test
    public void testJacksonXmlRootElementAnnotationMismatch() throws Exception
    {
        DatabindException e = assertThrows(DatabindException.class, () ->
            ENFORCING_MAPPER.readValue(
                    "<WrongName><value>42</value></WrongName>",
                    XmlAnnotatedRoot.class));
        verifyException(e, "Root name");
        verifyException(e, "WrongName");
        verifyException(e, "XmlRoot");
    }

    // Empty element should also be validated
    @Test
    public void testMismatchedEmptyElement() throws Exception
    {
        assertThrows(DatabindException.class, () ->
            ENFORCING_MAPPER.readValue("<Wrong/>", Root.class));
    }
}
