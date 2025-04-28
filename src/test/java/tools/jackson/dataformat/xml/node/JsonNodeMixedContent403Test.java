package tools.jackson.dataformat.xml.node;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.deser.FromXmlParser;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonNodeMixedContent403Test extends XmlTestUtil
{
    final private ObjectMapper XML_MAPPER = newMapper();

    final private ObjectMapper JSON_MAPPER = new JsonMapper();

    @Test
    public void testMixedContentBefore() throws Exception
    {
        // First, before elements:
        assertEquals(JSON_MAPPER.readTree(a2q(String.format("{'%s':'before','a':'1','b':'2'}", FromXmlParser.DEFAULT_TEXT_PROPERTY))),
                XML_MAPPER.readTree("<root>before<a>1</a><b>2</b></root>"));
    }

    @Test
    public void testMixedContentBetween() throws Exception
    {
        // Second, between
        assertEquals(JSON_MAPPER.readTree(a2q(String.format("{'a':'1','%s':'between','b':'2'}", FromXmlParser.DEFAULT_TEXT_PROPERTY))),
                XML_MAPPER.readTree("<root><a>1</a>between<b>2</b></root>"));
    }

    @Test
    public void testMixedContentAfter() throws Exception
    {
        // and then after
        assertEquals(JSON_MAPPER.readTree(a2q(String.format("{'a':'1','b':'2','%s':'after'}", FromXmlParser.DEFAULT_TEXT_PROPERTY))),
                XML_MAPPER.readTree("<root><a>1</a><b>2</b>after</root>"));
    }

    @Test
    public void testMultipleMixedContent() throws Exception
    {
        // and then after
        assertEquals(JSON_MAPPER.readTree(
                a2q(String.format("{'%s':['first','second','third'],'a':'1','b':'2'}", FromXmlParser.DEFAULT_TEXT_PROPERTY))),
                XML_MAPPER.readTree("<root>first<a>1</a>second<b>2</b>third</root>"));
    }

    // [dataformat-xml#226]
    @Test
    public void testMixed226() throws Exception
    {
        final String XML = "<root>\n"
                +"<a>mixed1 <b>leaf</b>"
                +" mixed2</a>\n"
                +"</root>";
        JsonNode fromJson = JSON_MAPPER.readTree(
                a2q(String.format("{'a':{'%s':['mixed1 ',' mixed2'],'b':'leaf'}}", FromXmlParser.DEFAULT_TEXT_PROPERTY)));
        assertEquals(fromJson, XML_MAPPER.readTree(XML));
    }
}
