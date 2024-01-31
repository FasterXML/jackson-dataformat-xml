package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

// [dataformat-xml#324]
public class XsiTypeWriteTest extends XmlTestBase
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
            .configure(ToXmlGenerator.Feature.AUTO_DETECT_XSI_TYPE, false)
            .build();

    private final XmlMapper XSI_ENABLED_MAPPER = XmlMapper.builder()
            .configure(ToXmlGenerator.Feature.AUTO_DETECT_XSI_TYPE, true)
            .build();

    public void testExplicitXsiTypeWriteDisabled() throws Exception
    {
        assertEquals("<Typed><xsi:type>abc</xsi:type></Typed>",
                NO_XSI_MAPPER.writeValueAsString(new TypeBean()));
    }

    public void testExplicitXsiTypeWriteEnabled() throws Exception
    {
        assertEquals(
                a2q("<Typed xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:type='abc'/>"),
                a2q(XSI_ENABLED_MAPPER.writeValueAsString(new TypeBean())));
    }

    public void testXsiTypeAsTypeIdWriteDisabled() throws Exception
    {
        assertEquals("<Poly><xsi_type>abc</xsi_type><value>42</value></Poly>",
                NO_XSI_MAPPER.writeValueAsString(new PolyBean()));
    }

    public void testXsiTypeAsTypeIdWriteEnabled() throws Exception
    {
        assertEquals(
                a2q("<Poly xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:type='abc'>"
                        +"<value>42</value></Poly>"),
                a2q(XSI_ENABLED_MAPPER.writeValueAsString(new PolyBean())));
    }
}
