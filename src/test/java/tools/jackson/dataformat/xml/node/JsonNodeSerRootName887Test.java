package tools.jackson.dataformat.xml.node;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.node.ObjectNode;

import tools.jackson.dataformat.xml.XmlFactory;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlNameProcessor;
import tools.jackson.dataformat.xml.XmlNameProcessors;
import tools.jackson.dataformat.xml.XmlReadFeature;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

// [dataformat-xml#887]: root name of unwrapped `ObjectNode` (see
// `XmlWriteFeature.UNWRAP_ROOT_OBJECT_NODE`) needs `XmlNameProcessor` handling
public class JsonNodeSerRootName887Test extends XmlTestUtil
{
    // The unwrapped root name comes from content, so it needs the same
    // XmlNameProcessor treatment the name gets when written as a child element.
    @Test
    public void testUnwrappedRootNameIsProcessed() throws Exception
    {
        final String BAD_NAME = "$ I am <fancy>! &;";
        XmlMapper mapper = mapperWith(XmlNameProcessors.newReplacementProcessor());

        ObjectNode oneProp = mapper.createObjectNode();
        oneProp.putObject(BAD_NAME).put("id", 13);

        ObjectNode twoProps = mapper.createObjectNode();
        twoProps.putObject(BAD_NAME).put("id", 13);
        twoProps.put("other", 1);

        assertEquals("<__I_am__fancy_____><id>13</id></__I_am__fancy_____>",
                mapper.writeValueAsString(oneProp));
        assertEquals("<ObjectNode><__I_am__fancy_____><id>13</id></__I_am__fancy_____>"
                +"<other>1</other></ObjectNode>",
                mapper.writeValueAsString(twoProps));
    }

    // ... which also makes the WRAP_ROOT_ELEMENT_NAME / UNWRAP_ROOT_OBJECT_NODE
    // pairing round-trip a root name that had to be escaped
    @Test
    public void testUnwrappedRootNameRoundTrip() throws Exception
    {
        XmlMapper mapper = mapperWith(XmlNameProcessors.newBase64Processor());
        // decodes to "$ I am <fancy>! &;"
        final String DOC = "<base64_tag_JCBJIGFtIDxmYW5jeT4hICY7><id>13</id>"
                +"</base64_tag_JCBJIGFtIDxmYW5jeT4hICY7>";

        assertEquals(DOC, mapper.writeValueAsString(mapper.readTree(DOC)));
    }

    private XmlMapper mapperWith(XmlNameProcessor proc) {
        return mapperBuilder(XmlFactory.builder().xmlNameProcessor(proc).build())
                .enable(XmlReadFeature.WRAP_ROOT_ELEMENT_NAME)
                .build();
    }
}
