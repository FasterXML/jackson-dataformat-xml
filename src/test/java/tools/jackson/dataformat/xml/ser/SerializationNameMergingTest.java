package tools.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestBase;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

// [dataformat-xml#637]
public class SerializationNameMergingTest extends XmlTestBase
{
    // [dataformat-xml#637]
    static class NamesBean {
        // XML annotations have precedence over default/standard/json ones
        // but local name, namespace should be merged
        @JsonProperty(value="value", namespace="uri:ns1")
        @JacksonXmlProperty(isAttribute=true)
        public int valueDefault = 42;
    }

    private final XmlMapper MAPPER = newMapper();


    // [dataformat-xml#637]
    public void testNamespaceMerging637() throws Exception
    {
        assertEquals(a2q("<NamesBean xmlns:wstxns1='uri:ns1' wstxns1:value='42'/>"),
                MAPPER.writeValueAsString(new NamesBean()));
    }
}
