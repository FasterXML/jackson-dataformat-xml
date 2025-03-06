package tools.jackson.dataformat.xml.stream;

import java.io.File;
import java.io.StringWriter;
import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.XmlWriteFeature;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XmlGeneratorTest extends XmlTestUtil
{
    private final XmlMapper MAPPER = mapperBuilder(true)
            .disable(XmlWriteFeature.WRITE_NULLS_AS_XSI_NIL)
            .build();

    @Test
    public void testSimpleElement() throws Exception
    {
        StringWriter out = new StringWriter();
        ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out);
        // root name is special, need to be fed first:
        gen.setNextName(new QName("root"));
        gen.writeStartObject();
        gen.writeName("elem");
        gen.writeString("value");
        gen.writeEndObject();
        gen.close();
        String xml = out.toString();
        // one more thing: remove that annoying 'xmlns' decl, if it's there:
        xml = removeSjsxpNamespace(xml);
        assertEquals("<root><elem>value</elem></root>", xml);
    }

    @Test
    public void testNullValuedElement() throws Exception
    {
        // First explicitly written
        StringWriter out = new StringWriter();
        ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out);
        gen.setNextName(new QName("root"));
        gen.writeStartObject();
        gen.writeName("elem");
        gen.writeNull();
        gen.writeEndObject();
        gen.close();
        String xml = removeSjsxpNamespace(out.toString());
        assertEquals("<root><elem/></root>", xml);

        // and then indirectly (see [dataformat-xml#413])
        out = new StringWriter();
        gen = (ToXmlGenerator) MAPPER.createGenerator(out);
        gen.setNextName(new QName("root"));
        gen.writeStartObject();
        gen.writeName("elem");
        gen.writeString((String) null);
        gen.writeEndObject();
        gen.close();
        xml = removeSjsxpNamespace(out.toString());
        assertEquals("<root><elem/></root>", xml);
    }
    
    @Test
    public void testSimpleAttribute() throws Exception
    {
        StringWriter out = new StringWriter();
        ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out);
        // root name is special, need to be fed first:
        gen.setNextName(new QName("root"));
        gen.writeStartObject();
        // and also need to force attribute
        gen.setNextIsAttribute(true);
        gen.writeName("attr");
        gen.writeString("value");
        gen.writeEndObject();
        gen.close();
        String xml = out.toString();
        // one more thing: remove that annoying 'xmlns' decl, if it's there:
        xml = removeSjsxpNamespace(xml);
        assertEquals("<root attr=\"value\"/>", xml);
    }

    @Test
    public void testSecondLevelAttribute() throws Exception
    {
        StringWriter out = new StringWriter();
        ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out);
        gen.setNextName(new QName("root"));
        gen.writeStartObject();
        gen.writeName("elem");
        gen.writeStartObject();
        // and also need to force attribute
        gen.setNextIsAttribute(true);
        gen.writeName("attr");
        gen.writeString("value");
        gen.writeEndObject();
        gen.writeEndObject();
        gen.close();
        String xml = out.toString();
        // one more thing: remove that annoying 'xmlns' decl, if it's there:
        xml = removeSjsxpNamespace(xml);
        assertEquals("<root><elem attr=\"value\"/></root>", xml);
    }

    @Test
    public void testAttrAndElem() throws Exception
    {
        StringWriter out = new StringWriter();
        ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out);
        gen.setNextName(new QName("root"));
        gen.writeStartObject();
        // and also need to force attribute
        gen.writeName("attr");
        gen.setNextIsAttribute(true);
        gen.writeNumber(-3);

        // Also let's add child element as well
        gen.setNextIsAttribute(false);
        gen.writeName("elem");
        gen.writeNumber(13);
        gen.writeEndObject();
        gen.close();
        String xml = removeSjsxpNamespace(out.toString());
        assertEquals("<root attr=\"-3\"><elem>13</elem></root>", xml);
    }

    // [Issue#6], missing overrides for File-backed generator
    @Test
    public void testWriteToFile() throws Exception
    {
        File f = File.createTempFile("test", ".tst");
        MAPPER.writeValue(f, new IntWrapper(42));

        String xml = readAll(f).trim();

        assertEquals("<IntWrapper><i>42</i></IntWrapper>", xml);
        f.delete();
    }

    @Test
    public void testRawSimpleValue() throws Exception
    {
        StringWriter out = new StringWriter();
        ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out);
        // root name is special, need to be fed first:
        gen.setNextName(new QName("root"));
        gen.writeStartObject();
        gen.writeName("elem");
        gen.writeRawValue("value");
        gen.writeEndObject();
        gen.close();
        String xml = out.toString();
        // one more thing: remove that annoying 'xmlns' decl, if it's there:
        xml = removeSjsxpNamespace(xml);
        assertEquals("<root><elem>value</elem></root>", xml);
    }

    @Test
    public void testRawOffsetValue() throws Exception
    {
        StringWriter out = new StringWriter();
        ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out);
        // root name is special, need to be fed first:
        gen.setNextName(new QName("root"));
        gen.writeStartObject();
        gen.writeName("elem");
        gen.writeRawValue("NotAValue_value_NotAValue", 10, 5);
        gen.writeEndObject();
        gen.close();
        String xml = out.toString();
        // one more thing: remove that annoying 'xmlns' decl, if it's there:
        xml = removeSjsxpNamespace(xml);
        assertEquals("<root><elem>value</elem></root>", xml);
    }

    @Test
    public void testRawCharArrayValue() throws Exception
    {
        StringWriter out = new StringWriter();
        ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out);
        // root name is special, need to be fed first:
        gen.setNextName(new QName("root"));
        gen.writeStartObject();
        gen.writeName("elem");
        gen.writeRawValue(new char[] {'!', 'v', 'a', 'l', 'u', 'e', '!'}, 1, 5);
        gen.writeEndObject();
        gen.close();
        String xml = out.toString();
        // one more thing: remove that annoying 'xmlns' decl, if it's there:
        xml = removeSjsxpNamespace(xml);
        assertEquals("<root><elem>value</elem></root>", xml);
    }

    @Test
    public void testRawSimpleValueUnwrapped() throws Exception
    {
        StringWriter out = new StringWriter();
        ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out);
        // root name is special, need to be fed first:
        gen.setNextName(new QName("root"));
        gen.writeStartObject();
        gen.setNextIsUnwrapped(true);
        gen.writeName("elem");
        gen.writeRawValue("value");
        gen.writeEndObject();
        gen.close();
        String xml = out.toString();
        // one more thing: remove that annoying 'xmlns' decl, if it's there:
        xml = removeSjsxpNamespace(xml);
        assertEquals("<root>value</root>", xml);
    }

    @Test
    public void testRawOffsetValueUnwrapped() throws Exception
    {
        StringWriter out = new StringWriter();
        ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out);
        // root name is special, need to be fed first:
        gen.setNextName(new QName("root"));
        gen.writeStartObject();
        gen.setNextIsUnwrapped(true);
        gen.writeName("elem");
        gen.writeRawValue("NotAValue_value_NotAValue", 10, 5);
        gen.writeEndObject();
        gen.close();
        String xml = out.toString();
        // one more thing: remove that annoying 'xmlns' decl, if it's there:
        xml = removeSjsxpNamespace(xml);
        assertEquals("<root>value</root>", xml);
    }

    @Test
    public void testRawCharArrayValueUnwrapped() throws Exception
    {
        StringWriter out = new StringWriter();
        ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out);
        // root name is special, need to be fed first:
        gen.setNextName(new QName("root"));
        gen.writeStartObject();
        gen.setNextIsUnwrapped(true);
        gen.writeName("elem");
        gen.writeRawValue(new char[] {'!', 'v', 'a', 'l', 'u', 'e', '!'}, 1, 5);
        gen.writeEndObject();
        gen.close();
        String xml = out.toString();
        // one more thing: remove that annoying 'xmlns' decl, if it's there:
        xml = removeSjsxpNamespace(xml);
        assertEquals("<root>value</root>", xml);
    }

    @Test
    public void testRawSimpleAttribute() throws Exception
    {
        StringWriter out = new StringWriter();
        ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out);
        // root name is special, need to be fed first:
        gen.setNextName(new QName("root"));
        gen.writeStartObject();
        // and also need to force attribute
        gen.setNextIsAttribute(true);
        gen.writeName("attr");
        gen.writeRawValue("value");
        gen.writeEndObject();
        gen.close();
        String xml = out.toString();
        // one more thing: remove that annoying 'xmlns' decl, if it's there:
        xml = removeSjsxpNamespace(xml);
        assertEquals("<root attr=\"value\"/>", xml);
    }

    @Test
    public void testRawOffsetAttribute() throws Exception
    {
        StringWriter out = new StringWriter();
        ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out);
        // root name is special, need to be fed first:
        gen.setNextName(new QName("root"));
        gen.writeStartObject();
        // and also need to force attribute
        gen.setNextIsAttribute(true);
        gen.writeName("attr");
        gen.writeRawValue("NotAValue_value_NotAValue", 10, 5);
        gen.writeEndObject();
        gen.close();
        String xml = out.toString();
        // one more thing: remove that annoying 'xmlns' decl, if it's there:
        xml = removeSjsxpNamespace(xml);
        assertEquals("<root attr=\"value\"/>", xml);
    }

    @Test
    public void testRawCharArratAttribute() throws Exception
    {
        StringWriter out = new StringWriter();
        ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out);
        // root name is special, need to be fed first:
        gen.setNextName(new QName("root"));
        gen.writeStartObject();
        // and also need to force attribute
        gen.setNextIsAttribute(true);
        gen.writeName("attr");
        gen.writeRawValue(new char[]{'!', 'v', 'a', 'l', 'u', 'e', '!'}, 1, 5);
        gen.writeEndObject();
        gen.close();
        String xml = out.toString();
        // one more thing: remove that annoying 'xmlns' decl, if it's there:
        xml = removeSjsxpNamespace(xml);
        assertEquals("<root attr=\"value\"/>", xml);
    }
}
