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

    @JacksonXmlRootElement(localName = "NsRoot", namespace = "http://example.com/test")
    static class NamespacedRoot {
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

    // Namespace URI verification: matching namespace should pass
    @Test
    public void testMatchingNamespaceSucceeds() throws Exception
    {
        NamespacedRoot root = ENFORCING_MAPPER.readValue(
                "<ns:NsRoot xmlns:ns=\"http://example.com/test\"><ns:value>42</ns:value></ns:NsRoot>",
                NamespacedRoot.class);
        assertEquals(42, root.value);
    }

    // Namespace URI verification: wrong namespace should fail
    @Test
    public void testMismatchedNamespaceFails() throws Exception
    {
        DatabindException e = assertThrows(DatabindException.class, () ->
            ENFORCING_MAPPER.readValue(
                    "<ns:NsRoot xmlns:ns=\"http://example.com/wrong\"><ns:value>42</ns:value></ns:NsRoot>",
                    NamespacedRoot.class));
        verifyException(e, "Root namespace");
        verifyException(e, "http://example.com/wrong");
        verifyException(e, "http://example.com/test");
    }

    // No namespace in XML when one is expected should fail
    @Test
    public void testMissingNamespaceFails() throws Exception
    {
        DatabindException e = assertThrows(DatabindException.class, () ->
            ENFORCING_MAPPER.readValue(
                    "<NsRoot><value>42</value></NsRoot>",
                    NamespacedRoot.class));
        verifyException(e, "Root namespace");
    }

    // Unexpected namespace in XML when none is expected should fail
    @Test
    public void testUnexpectedNamespaceFails() throws Exception
    {
        DatabindException e = assertThrows(DatabindException.class, () ->
            ENFORCING_MAPPER.readValue(
                    "<ns:Root xmlns:ns=\"http://example.com/unexpected\"><ns:value>42</ns:value></ns:Root>",
                    Root.class));
        verifyException(e, "Root namespace");
    }

    // No namespace expected, none present should pass
    @Test
    public void testNoNamespaceExpectedNonePresentSucceeds() throws Exception
    {
        Root root = ENFORCING_MAPPER.readValue(
                "<Root><value>42</value></Root>", Root.class);
        assertEquals(42, root.value);
    }
}
