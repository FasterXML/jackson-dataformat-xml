package tools.jackson.dataformat.xml.lists;

import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.SerializationFeature;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnwrappedListWithEmptyCData129Test extends XmlTestUtil
{
    static class ListValues {
        @XmlElement(name = "value", required = true)
        @JacksonXmlElementWrapper(useWrapping=false)
        public List<String> value;
    }


    private final XmlMapper MAPPER = XmlMapper.builder()
            // easier for eye:
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();

    // for [dataformat-xml#129]
    @Test
    public void testListWithEmptyCData() throws Exception
    {
        _testListWithEmptyCData(" ");
        _testListWithEmptyCData("");
    }
        
    private void _testListWithEmptyCData(String cdata) throws Exception
    {
        ListValues result = MAPPER.readValue("<root>\n"
                + "<value>A</value>\n"
//                + "<value><![CDATA["+SECOND+"]]></value>\n"
                + "<value>"+cdata+"</value>\n"
                + "<value>C</value>\n"
                + "</root>", ListValues.class);

        List<String> values = result.value;

        assertEquals(3, values.size()); // expecting 3 values
        assertEquals("A", values.get(0));
        assertEquals(cdata, values.get(1)); // expecting empty string in second position
        assertEquals("C", values.get(2));
    }    
}
