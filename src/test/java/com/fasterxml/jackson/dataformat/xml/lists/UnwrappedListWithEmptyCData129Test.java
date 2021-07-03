package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

public class UnwrappedListWithEmptyCData129Test extends XmlTestBase
{
    static class ListValues {
        @XmlElement(name = "value", required = true)
        @JacksonXmlElementWrapper(useWrapping=false)
        public List<String> value;
    }


    private final XmlMapper MAPPER = new XmlMapper();
    {
        // easier for eye:
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    // for [dataformat-xml#129]
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
