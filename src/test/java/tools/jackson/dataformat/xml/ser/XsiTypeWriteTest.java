package tools.jackson.dataformat.xml.ser;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import tools.jackson.databind.node.ObjectNode;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.XmlWriteFeature;

import static org.junit.jupiter.api.Assertions.assertEquals;

// [dataformat-xml#324]
public class XsiTypeWriteTest extends XmlTestUtil
{
    @JsonRootName("Typed")
    static class TypeBean {
        @JsonProperty("xsi:type")
        public String typeId = "abc";
    }

    @JsonRootName("Poly")
    @JsonTypeInfo(use = Id.SIMPLE_NAME, include = As.PROPERTY, property="xsi:type")
    static class PolyBean {
        public int value = 42;
    }

    private final XmlMapper NO_XSI_MAPPER = XmlMapper.builder()
            .configure(XmlWriteFeature.AUTO_DETECT_XSI_TYPE, false)
            .build();

    private final XmlMapper XSI_ENABLED_MAPPER = XmlMapper.builder()
            .configure(XmlWriteFeature.AUTO_DETECT_XSI_TYPE, true)
            .build();

    @Test
    public void testExplicitXsiTypeWriteDisabled() throws Exception
    {
        assertEquals("<Typed><xsi:type>abc</xsi:type></Typed>",
                NO_XSI_MAPPER.writeValueAsString(new TypeBean()));
    }

    @Test
    public void testExplicitXsiTypeWriteEnabled() throws Exception
    {
        assertEquals(
                a2q("<Typed xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:type='abc'/>"),
                a2q(XSI_ENABLED_MAPPER.writeValueAsString(new TypeBean())));
    }

    @Test
    public void testXsiTypeAsTypeIdWriteDisabled() throws Exception
    {
        // not legal XML but with explicitly specified name is what caller wants
        // (note: not 100% sure how xsi:type is written as attribute)
        assertEquals(
                a2q("<Poly xsi:type='PolyBean'><value>42</value></Poly>"),
                a2q(NO_XSI_MAPPER.writeValueAsString(new PolyBean())));
    }

    @Test
    public void testXsiTypeAsTypeIdWriteEnabled() throws Exception
    {
        assertEquals(
                a2q("<Poly xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:type='PolyBean'>"
                        +"<value>42</value></Poly>"),
                a2q(XSI_ENABLED_MAPPER.writeValueAsString(new PolyBean())));
    }

    // Content-driven names (Map/JsonNode) that happen to include an "xsi:type"
    // key must not leave following siblings in attribute mode / the XSI namespace.
    // Before the fix the forced state leaked, so siblings became xsi:-prefixed
    // attributes -- and a colliding "type" key produced a duplicate xsi:type
    // attribute, i.e. XML this module could no longer read back.
    @Test
    public void testXsiTypeKeyDoesNotLeakOntoSibling() throws Exception
    {
        XmlMapper mapper = newMapper();
        ObjectNode tree = mapper.createObjectNode();
        tree.put("xsi:type", "A");
        tree.put("type", "B");
        String xml = mapper.writeValueAsString(tree);
        assertEquals(
                a2q("<ObjectNode xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'"
                        +" xsi:type='A'><type>B</type></ObjectNode>"),
                a2q(xml));
        // and the emitted document must round-trip through the same module
        assertEquals(tree, mapper.readTree(xml));
    }

    @Test
    public void testXsiTypeKeyFollowedByNilKey() throws Exception
    {
        XmlMapper mapper = newMapper();
        ObjectNode tree = mapper.createObjectNode();
        tree.put("xsi:type", "T");
        tree.put("nil", "true");
        String xml = mapper.writeValueAsString(tree);
        assertEquals(
                a2q("<ObjectNode xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'"
                        +" xsi:type='T'><nil>true</nil></ObjectNode>"),
                a2q(xml));
        // previously the leaked attribute mode emitted xsi:nil='true', collapsing
        // the whole document to null on read
        assertEquals(tree, mapper.readTree(xml));
    }
}
