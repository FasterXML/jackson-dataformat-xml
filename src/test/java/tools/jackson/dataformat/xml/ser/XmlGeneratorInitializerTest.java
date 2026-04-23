package tools.jackson.dataformat.xml.ser;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonRootName;

import tools.jackson.databind.ObjectWriter;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.XmlWriteFeature;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class XmlGeneratorInitializerTest extends XmlTestUtil
{
    // For [dataformat-xml#207]: namespace prefix binding
    @JsonRootName("Ingredients")
    static class Ingredients {
        public String eggs = "12";
        @JacksonXmlProperty(namespace = "urn:produce:fruit")
        public String bananas = "6";
    }

    @JsonRootName("Root")
    static class NsAttrBean {
        @JacksonXmlProperty(isAttribute = true, namespace = "urn:attr:x", localName = "lang")
        public String lang = "en";
    }

    private final XmlMapper MAPPER = newMapper();

    // // [dataformat-xml#150]: DTD writing -- ok cases

    @Test
    public void testDTDWithOnlyRootElement() throws Exception
    {
        ObjectWriter w = _writer(new XmlGeneratorInitializer()
                        .addDTD("StringBean", null, null, null));
        assertEquals(a2q("<!DOCTYPE StringBean>\n"
                +"<StringBean><text>test</text></StringBean>"),
                w.writeValueAsString(new StringBean("test")));
    }

    @Test
    public void testDTDWithPublicId() throws Exception
    {
        ObjectWriter w = _writer(new XmlGeneratorInitializer()
                        .addDTD("StringBean", "system", "http://foo.bar", ""));
        assertEquals(a2q("<!DOCTYPE StringBean PUBLIC 'http://foo.bar' 'system'>\n"
                +"<StringBean><text>test</text></StringBean>"),
                w.writeValueAsString(new StringBean("test")));
    }

    @Test
    public void testDTDWithSystemIdOnly() throws Exception
    {
        ObjectWriter w = _writer(new XmlGeneratorInitializer()
                        .addDTD("StringBean", "system", "", null));
        assertEquals(a2q("<!DOCTYPE StringBean SYSTEM 'system'>\n"
                +"<StringBean><text>test</text></StringBean>"),
                w.writeValueAsString(new StringBean("test")));
    }

    @Test
    public void testDTDWithInternalSubset() throws Exception
    {
        ObjectWriter w = _writer(new XmlGeneratorInitializer()
                        .addDTD("StringBean", "system", "http://foo.bar", "<!ELEMENT root (#PCDATA)>"));
        assertEquals(a2q("<!DOCTYPE StringBean PUBLIC 'http://foo.bar' 'system' "
                +"[<!ELEMENT root (#PCDATA)>]>\n"
                +"<StringBean><text>test</text></StringBean>"),
                w.writeValueAsString(new StringBean("test")));
    }

    // Verify prolog ordering: XML declaration must come before DOCTYPE
    @Test
    public void testDTDWithXmlDeclaration() throws Exception
    {
        XmlMapper mapper = XmlMapper.builder()
                .configure(XmlWriteFeature.WRITE_XML_DECLARATION, true)
                .build();
        ObjectWriter w = _writer(mapper, new XmlGeneratorInitializer()
                        .addDTD("StringBean", "system", "http://foo.bar", null));
        // XML declaration is emitted with single quotes, DOCTYPE with double quotes,
        // so cannot use a2q() on the whole string here.
        assertEquals("<?xml version='1.0' encoding='UTF-8'?>\n"
                +"<!DOCTYPE StringBean PUBLIC \"http://foo.bar\" \"system\">\n"
                +"<StringBean><text>test</text></StringBean>",
                w.writeValueAsString(new StringBean("test")));
    }

    // // [dataformat-xml#150]: DTD writing -- failing cases
    @Test
    public void testDTDInvalidNoRoot() throws Exception
    {
        try {
            /*ObjectWriter w =*/ _writer(new XmlGeneratorInitializer()
                    .addDTD("", null, null, null));
            fail("Should not pass");
        } catch (IllegalArgumentException e) {
            verifyException(e, "Illegal argument for 'rootName': must be");
        }
    }

    // // [dataformat-xml#849]: Comment writing -- ok cases

    @Test
    public void testSimpleComment() throws Exception
    {
        ObjectWriter w = _writer(new XmlGeneratorInitializer()
                        .addComment("Comment content!"));
        assertEquals(a2q("<!--Comment content!-->\n"
                +"<StringBean><text>test</text></StringBean>"),
                w.writeValueAsString(new StringBean("test")));
    }

    // Verify ordering: XML declaration must come before Comment
    @Test
    public void testCommentWithXmlDeclaration() throws Exception
    {
        XmlMapper mapper = XmlMapper.builder()
                .configure(XmlWriteFeature.WRITE_XML_DECLARATION, true)
                .build();
        ObjectWriter w = _writer(mapper, new XmlGeneratorInitializer()
                        .addComment("Hello"));
        // XML declaration is emitted with single quotes, so cannot use a2q() here.
        assertEquals("<?xml version='1.0' encoding='UTF-8'?>\n"
                +"<!--Hello-->\n"
                +"<StringBean><text>test</text></StringBean>",
                w.writeValueAsString(new StringBean("test")));
    }

    // Verify "position added" ordering contract: Comment registered before DTD
    @Test
    public void testCommentBeforeDTD() throws Exception
    {
        ObjectWriter w = _writer(new XmlGeneratorInitializer()
                        .addComment("before dtd")
                        .addDTD("StringBean", null, null, null));
        assertEquals(a2q("<!--before dtd-->\n"
                +"<!DOCTYPE StringBean>\n"
                +"<StringBean><text>test</text></StringBean>"),
                w.writeValueAsString(new StringBean("test")));
    }

    // Verify "position added" ordering contract: DTD registered before Comment
    @Test
    public void testDTDBeforeComment() throws Exception
    {
        ObjectWriter w = _writer(new XmlGeneratorInitializer()
                        .addDTD("StringBean", null, null, null)
                        .addComment("after dtd"));
        assertEquals(a2q("<!DOCTYPE StringBean>\n"
                +"<!--after dtd-->\n"
                +"<StringBean><text>test</text></StringBean>"),
                w.writeValueAsString(new StringBean("test")));
    }

    // Ensure multiple comments are all written (no accidental dedup)
    @Test
    public void testMultipleComments() throws Exception
    {
        ObjectWriter w = _writer(new XmlGeneratorInitializer()
                        .addComment("first")
                        .addComment("second")
                        .addComment("third"));
        assertEquals(a2q("<!--first-->\n"
                +"<!--second-->\n"
                +"<!--third-->\n"
                +"<StringBean><text>test</text></StringBean>"),
                w.writeValueAsString(new StringBean("test")));
    }

    // Empty-string content is accepted and produces an empty comment
    @Test
    public void testEmptyComment() throws Exception
    {
        ObjectWriter w = _writer(new XmlGeneratorInitializer()
                        .addComment(""));
        assertEquals(a2q("<!---->\n"
                +"<StringBean><text>test</text></StringBean>"),
                w.writeValueAsString(new StringBean("test")));
    }

    // Null content is coerced to empty by PrologComment(String) constructor
    @Test
    public void testNullComment() throws Exception
    {
        ObjectWriter w = _writer(new XmlGeneratorInitializer()
                        .addComment(null));
        assertEquals(a2q("<!---->\n"
                +"<StringBean><text>test</text></StringBean>"),
                w.writeValueAsString(new StringBean("test")));
    }

    // [dataformat-xml#849]: verify `linefeedsBetweenPrologDirectives(false)`
    // suppresses both the post-declaration lf and inter-directive lfs
    @Test
    public void testLinefeedsBetweenPrologDirectivesDisabled() throws Exception
    {
        XmlMapper mapper = XmlMapper.builder()
                .configure(XmlWriteFeature.WRITE_XML_DECLARATION, true)
                .build();
        ObjectWriter w = _writer(mapper, new XmlGeneratorInitializer()
                        .linefeedsBetweenPrologDirectives(false)
                        .addDTD("StringBean", null, null, null)
                        .addComment("squished"));
        // XML declaration uses single quotes, DOCTYPE uses double, so cannot use a2q().
        assertEquals("<?xml version='1.0' encoding='UTF-8'?>"
                +"<!DOCTYPE StringBean>"
                +"<!--squished-->"
                +"<StringBean><text>test</text></StringBean>",
                w.writeValueAsString(new StringBean("test")));
    }

    // // [dataformat-xml#452]: PI writing -- ok cases

    @Test
    public void testSimplePIs() throws Exception
    {
        ObjectWriter w = _writer(new XmlGeneratorInitializer()
                        .addPI("target", "data"));
        assertEquals(a2q("<?target data?>\n"
                +"<StringBean><text>test</text></StringBean>"),
                w.writeValueAsString(new StringBean("test")));

        // Then empty/null
        final String EXP_WITH_NO_DATA = a2q("<?target?>\n"
                +"<StringBean><text>test</text></StringBean>");

        w = _writer(new XmlGeneratorInitializer()
                        .addPI("target", ""));
        assertEquals(EXP_WITH_NO_DATA,
                w.writeValueAsString(new StringBean("test")));

        w = _writer(new XmlGeneratorInitializer()
                        .addPI("target", null));
        assertEquals(EXP_WITH_NO_DATA,
                w.writeValueAsString(new StringBean("test")));
    }

    // Ensure multiple PIs are all written in insertion order
    @Test
    public void testMultiplePIs() throws Exception
    {
        ObjectWriter w = _writer(new XmlGeneratorInitializer()
                        .addPI("xml-stylesheet", "type=\"text/xsl\" href=\"style.xsl\"")
                        .addPI("target2", "data2"));
        assertEquals(a2q("<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>\n"
                +"<?target2 data2?>\n"
                +"<StringBean><text>test</text></StringBean>"),
                w.writeValueAsString(new StringBean("test")));
    }

    // Verify ordering: XML declaration must come before PI
    @Test
    public void testPIWithXmlDeclaration() throws Exception
    {
        XmlMapper mapper = XmlMapper.builder()
                .configure(XmlWriteFeature.WRITE_XML_DECLARATION, true)
                .build();
        ObjectWriter w = _writer(mapper, new XmlGeneratorInitializer()
                        .addPI("target", "data"));
        // XML declaration is emitted with single quotes, so cannot use a2q() here.
        assertEquals("<?xml version='1.0' encoding='UTF-8'?>\n"
                +"<?target data?>\n"
                +"<StringBean><text>test</text></StringBean>",
                w.writeValueAsString(new StringBean("test")));
    }

    // Verify "position added" ordering contract: Comment before PI
    @Test
    public void testCommentBeforePI() throws Exception
    {
        ObjectWriter w = _writer(new XmlGeneratorInitializer()
                        .addComment("before pi")
                        .addPI("target", "data"));
        assertEquals(a2q("<!--before pi-->\n"
                +"<?target data?>\n"
                +"<StringBean><text>test</text></StringBean>"),
                w.writeValueAsString(new StringBean("test")));
    }

    // Verify "position added" ordering contract: PI before Comment
    @Test
    public void testPIBeforeComment() throws Exception
    {
        ObjectWriter w = _writer(new XmlGeneratorInitializer()
                        .addPI("target", "data")
                        .addComment("after pi"));
        assertEquals(a2q("<?target data?>\n"
                +"<!--after pi-->\n"
                +"<StringBean><text>test</text></StringBean>"),
                w.writeValueAsString(new StringBean("test")));
    }

    // // [dataformat-xml#452]: PI writing -- failing cases

    @Test
    public void testInvalidPINullTarget() throws Exception
    {
        try {
            /*ObjectWriter w =*/ _writer(new XmlGeneratorInitializer()
                    .addPI(null, "data"));
            fail("Should not pass");
        } catch (IllegalArgumentException e) {
            verifyException(e, "Illegal argument for 'target': must be");
        }
    }

    @Test
    public void testInvalidPIEmptyTarget() throws Exception
    {
        try {
            /*ObjectWriter w =*/ _writer(new XmlGeneratorInitializer()
                    .addPI("", "data"));
            fail("Should not pass");
        } catch (IllegalArgumentException e) {
            verifyException(e, "Illegal argument for 'target': must be");
        }
    }

    // // [dataformat-xml#207]: namespace prefix bindings

    // Without binding, Woodstox emits a synthetic "wstxns1" prefix; with
    // `addNamespace(prefix, uri)` the generator should use the caller-supplied prefix
    //
    // NOTE: somewhat fragile since its Woodstox-specific
    @Test
    public void testNamespacePrefixBinding() throws Exception
    {
        // Without binding: auto-generated prefix (sanity baseline)
        assertEquals(a2q("<Ingredients>"
                +"<wstxns1:bananas xmlns:wstxns1='urn:produce:fruit'>6</wstxns1:bananas>"
                +"<eggs>12</eggs>"
                +"</Ingredients>"),
                MAPPER.writeValueAsString(new Ingredients()));

        ObjectWriter w = _writer(new XmlGeneratorInitializer()
                .addNamespace("fruit", "urn:produce:fruit"));
        assertEquals(a2q("<Ingredients>"
                +"<fruit:bananas xmlns:fruit='urn:produce:fruit'>6</fruit:bananas>"
                +"<eggs>12</eggs>"
                +"</Ingredients>"),
                w.writeValueAsString(new Ingredients()));
    }

    // `addDefaultNamespace(uri)` binds URI as the (unprefixed) default namespace
    @Test
    public void testDefaultNamespaceBinding() throws Exception
    {
        ObjectWriter w = _writer(new XmlGeneratorInitializer()
                .addDefaultNamespace("urn:produce:fruit"));
        assertEquals(a2q("<Ingredients>"
                +"<bananas xmlns='urn:produce:fruit'>6</bananas>"
                +"<eggs>12</eggs>"
                +"</Ingredients>"),
                w.writeValueAsString(new Ingredients()));
    }

    // Empty prefix must behave identically to `addDefaultNamespace(uri)` (per ArgUtil.emptyToNull)
    @Test
    public void testNamespacePrefixEmptyTreatedAsDefault() throws Exception
    {
        ObjectWriter w = _writer(new XmlGeneratorInitializer()
                .addNamespace("", "urn:produce:fruit"));
        assertEquals(a2q("<Ingredients>"
                +"<bananas xmlns='urn:produce:fruit'>6</bananas>"
                +"<eggs>12</eggs>"
                +"</Ingredients>"),
                w.writeValueAsString(new Ingredients()));
    }

    // Multiple bindings can be registered; only those actually referenced should appear in output
    @Test
    public void testMultipleNamespaceBindings() throws Exception
    {
        ObjectWriter w = _writer(new XmlGeneratorInitializer()
                .addNamespace("fruit", "urn:produce:fruit")
                .addNamespace("veg", "urn:produce:veg"));
        assertEquals(a2q("<Ingredients>"
                +"<fruit:bananas xmlns:fruit='urn:produce:fruit'>6</fruit:bananas>"
                +"<eggs>12</eggs>"
                +"</Ingredients>"),
                w.writeValueAsString(new Ingredients()));
    }

    // Namespace binding should also apply to attributes (prefix moves to root element)
    @Test
    public void testNamespacePrefixBindingOnAttribute() throws Exception
    {
        ObjectWriter w = _writer(new XmlGeneratorInitializer()
                .addNamespace("x", "urn:attr:x"));
        assertEquals(a2q("<Root xmlns:x='urn:attr:x' x:lang='en'/>"),
                w.writeValueAsString(new NsAttrBean()));
    }

    // Registering a namespace URI that isn't referenced by any written
    // element/attribute should not affect output
    @Test
    public void testUnusedNamespaceBindingHasNoEffect() throws Exception
    {
        final String EXPECTED = MAPPER.writeValueAsString(new Ingredients());
        ObjectWriter w = _writer(new XmlGeneratorInitializer()
                .addNamespace("unused", "urn:nobody:cares"));
        assertEquals(EXPECTED, w.writeValueAsString(new Ingredients()));
    }

    // // [dataformat-xml#207]: namespace binding -- failing cases

    @Test
    public void testInvalidNamespaceBindings() throws Exception
    {
        try {
            new XmlGeneratorInitializer().addNamespace("p", null);
            fail("Should not pass");
        } catch (IllegalArgumentException e) {
            verifyException(e, "Illegal argument for 'namespaceURI': must be");
        }
        try {
            new XmlGeneratorInitializer().addNamespace("p", "");
            fail("Should not pass");
        } catch (IllegalArgumentException e) {
            verifyException(e, "Illegal argument for 'namespaceURI': must be");
        }
    }

    @Test
    public void testInvalidDefaultNamespaceNullURI() throws Exception
    {
        try {
            new XmlGeneratorInitializer().addDefaultNamespace(null);
            fail("Should not pass");
        } catch (IllegalArgumentException e) {
            verifyException(e, "Illegal argument for 'namespaceURI': must be");
        }
    }

    // // Other tests

    private ObjectWriter _writer(XmlGeneratorInitializer initializer) {
        return _writer(MAPPER, initializer);
    }

    private ObjectWriter _writer(XmlMapper mapper, XmlGeneratorInitializer initializer) {
        return mapper.writer().with(initializer);
    }
}
