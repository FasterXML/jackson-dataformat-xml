package com.fasterxml.jackson.dataformat.xml.deser;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import static org.junit.jupiter.api.Assertions.*;

// [dataformat-xml#634]
public class XsiTypeReadTest extends XmlTestUtil
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

    @Test
    public void testExplicitXsiTypeReadEnabled() throws Exception
    {
        final String XML = XSI_ENABLED_MAPPER.writeValueAsString(new TypeBean("type0"));
        TypeBean result = XSI_ENABLED_MAPPER.readValue(XML, TypeBean.class);
        assertEquals("type0", result.typeId);
    }

    @Test
    public void testXsiTypeAsTypeReadeEnabled() throws Exception
    {
        final String XML = XSI_ENABLED_MAPPER.writeValueAsString(new PolyBean(42));
        PolyBean result = XSI_ENABLED_MAPPER.readValue(XML, PolyBean.class);
        assertEquals(42, result.value);
    }
}
