package tools.jackson.dataformat.xml.stream;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;

import tools.jackson.core.*;
import tools.jackson.databind.*;
import tools.jackson.databind.annotation.JsonDeserialize;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.deser.FromXmlParser;

import static org.junit.jupiter.api.Assertions.*;

// [dataformat-xml#496] Root element name not accessible from custom deserializer
// when root has no attributes
public class RootElementName496Test extends XmlTestUtil
{
    @JsonDeserialize(using = RootNameDeserializer.class)
    static class RootNameHolder {
        public QName rootName;

        RootNameHolder(QName rootName) {
            this.rootName = rootName;
        }
    }

    static class RootNameDeserializer extends ValueDeserializer<RootNameHolder> {
        @Override
        public RootNameHolder deserialize(JsonParser p, DeserializationContext ctxt)
        {
            QName rootName = ((FromXmlParser) p).getRootElementName();
            // consume the rest
            while (p.nextToken() != null) { }
            return new RootNameHolder(rootName);
        }
    }

    private final XmlMapper MAPPER = newMapper();

    // [dataformat-xml#496]: root name accessible without attributes
    @Test
    public void testRootNameWithoutAttributes() throws Exception
    {
        RootNameHolder result = MAPPER.readValue(
                "<root><field>value</field></root>", RootNameHolder.class);
        assertEquals("root", result.rootName.getLocalPart());
    }

    // [dataformat-xml#496]: root name accessible with attributes
    @Test
    public void testRootNameWithAttributes() throws Exception
    {
        RootNameHolder result = MAPPER.readValue(
                "<root foo='bar'><field>value</field></root>", RootNameHolder.class);
        assertEquals("root", result.rootName.getLocalPart());
    }

    // [dataformat-xml#496]: verify via parser directly, stable across full parse
    @Test
    public void testRootNameViaParser() throws Exception
    {
        try (JsonParser p = MAPPER.createParser("<myRoot><child>text</child></myRoot>")) {
            FromXmlParser xp = (FromXmlParser) p;
            QName rootName = xp.getRootElementName();
            assertEquals("myRoot", rootName.getLocalPart());
            // Advance past all tokens
            while (p.nextToken() != null) { }
            // Still accessible after parsing
            assertEquals("myRoot", xp.getRootElementName().getLocalPart());
        }
    }

    // [dataformat-xml#496]: empty root element
    @Test
    public void testRootNameEmptyElement() throws Exception
    {
        try (JsonParser p = MAPPER.createParser("<emptyRoot/>")) {
            FromXmlParser xp = (FromXmlParser) p;
            assertEquals("emptyRoot", xp.getRootElementName().getLocalPart());
        }
    }

    // [dataformat-xml#496]: root with text-only content (scalar root value)
    @Test
    public void testRootNameTextOnly() throws Exception
    {
        try (JsonParser p = MAPPER.createParser("<textRoot>hello</textRoot>")) {
            FromXmlParser xp = (FromXmlParser) p;
            assertEquals("textRoot", xp.getRootElementName().getLocalPart());
        }
    }

    // [dataformat-xml#496]: root with namespace — verify all QName components
    @Test
    public void testRootNameWithNamespace() throws Exception
    {
        try (JsonParser p = MAPPER.createParser(
                "<ns:root xmlns:ns='http://example.com'><ns:child>val</ns:child></ns:root>")) {
            FromXmlParser xp = (FromXmlParser) p;
            QName rootName = xp.getRootElementName();
            assertEquals("root", rootName.getLocalPart());
            assertEquals("http://example.com", rootName.getNamespaceURI());
            assertEquals("ns", rootName.getPrefix());
        }
    }

    // [dataformat-xml#496]: root with default namespace (no prefix)
    @Test
    public void testRootNameWithDefaultNamespace() throws Exception
    {
        try (JsonParser p = MAPPER.createParser(
                "<root xmlns='http://example.com'><child>val</child></root>")) {
            FromXmlParser xp = (FromXmlParser) p;
            QName rootName = xp.getRootElementName();
            assertEquals("root", rootName.getLocalPart());
            assertEquals("http://example.com", rootName.getNamespaceURI());
            assertEquals("", rootName.getPrefix());
        }
    }

    // [dataformat-xml#496]: root with multiple children (no attributes)
    @Test
    public void testRootNameMultipleChildren() throws Exception
    {
        RootNameHolder result = MAPPER.readValue(
                "<document><a>1</a><b>2</b><c>3</c></document>", RootNameHolder.class);
        assertEquals("document", result.rootName.getLocalPart());
    }
}
