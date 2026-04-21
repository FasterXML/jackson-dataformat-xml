package tools.jackson.dataformat.xml.ser;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectWriter;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.XmlWriteFeature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class XmlGeneratorInitializerTest extends XmlTestUtil
{
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

    // // Other tests

    private ObjectWriter _writer(XmlGeneratorInitializer initializer) {
        return _writer(MAPPER, initializer);
    }

    private ObjectWriter _writer(XmlMapper mapper, XmlGeneratorInitializer initializer) {
        return mapper.writer().with(initializer);
    }
}
