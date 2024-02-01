package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

// [dataformat-xml#634]
public class XsiTypeReadTest extends XmlTestBase
{
    @JsonRootName("Typed")
    static class TypeBean {
        @JsonProperty("xsi:type")
        public String typeId;

        protected TypeBean() { }
        public TypeBean(String typeId) {
            this.typeId = typeId;
        }
    }

    @JsonRootName("Poly")
    @JsonTypeInfo(use = Id.SIMPLE_NAME, include = As.PROPERTY, property="xsi:type")
    static class PolyBean {
        public int value;

        protected PolyBean() { }
        public PolyBean(int v) { value = v; }
    }

    private final XmlMapper XSI_ENABLED_MAPPER = XmlMapper.builder()
            .configure(ToXmlGenerator.Feature.AUTO_DETECT_XSI_TYPE, true)
            .configure(FromXmlParser.Feature.AUTO_DETECT_XSI_TYPE, true)
            .build();

    public void testExplicitXsiTypeReadEnabled() throws Exception
    {
        final String XML = XSI_ENABLED_MAPPER.writeValueAsString(new TypeBean("type0"));
        TypeBean result = XSI_ENABLED_MAPPER.readValue(XML, TypeBean.class);
        assertEquals("type0", result.typeId);
    }

    public void testXsiTypeAsTypeReadeEnabled() throws Exception
    {
        final String XML = XSI_ENABLED_MAPPER.writeValueAsString(new PolyBean(42));
        PolyBean result = XSI_ENABLED_MAPPER.readValue(XML, PolyBean.class);
        assertEquals(42, result.value);
    }
}
