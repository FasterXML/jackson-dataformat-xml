package tools.jackson.dataformat.xml.stream;

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
        public String rootName;

        RootNameHolder(String rootName) {
            this.rootName = rootName;
        }
    }

    static class RootNameDeserializer extends ValueDeserializer<RootNameHolder> {
        @Override
        public RootNameHolder deserialize(JsonParser p, DeserializationContext ctxt)
        {
            String rootName = ((FromXmlParser) p).getRootElementLocalName();
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
        assertEquals("root", result.rootName);
    }

    // [dataformat-xml#496]: root name accessible with attributes
    @Test
    public void testRootNameWithAttributes() throws Exception
    {
        RootNameHolder result = MAPPER.readValue(
                "<root foo='bar'><field>value</field></root>", RootNameHolder.class);
        assertEquals("root", result.rootName);
    }

    // [dataformat-xml#496]: verify via parser directly
    @Test
    public void testRootNameViaParser() throws Exception
    {
        try (JsonParser p = MAPPER.createParser("<myRoot><child>text</child></myRoot>")) {
            FromXmlParser xp = (FromXmlParser) p;
            assertEquals("myRoot", xp.getRootElementLocalName());
            // Advance past all tokens
            while (p.nextToken() != null) { }
            // Still accessible after parsing
            assertEquals("myRoot", xp.getRootElementLocalName());
        }
    }
}
