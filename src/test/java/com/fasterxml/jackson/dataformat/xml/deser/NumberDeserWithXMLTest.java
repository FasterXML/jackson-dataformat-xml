package com.fasterxml.jackson.dataformat.xml.deser;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

// Tests copied from databind "JDKNumberDeserTest" (only a small subset)
public class NumberDeserWithXMLTest extends XmlTestBase
{
    // [databind#2644]
    @JsonRootName("Root")
    static class NodeRoot2644 {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes(value = {
                @JsonSubTypes.Type(value = NodeParent2644.class, name = "NodeParent")
        })
        public Node2644 node;
    }

    public static class NodeParent2644 extends Node2644 { }

    public static abstract class Node2644 {
        @JsonProperty("amount")
        BigDecimal val;

        public BigDecimal getVal() {
            return val;
        }

        public void setVal(BigDecimal val) {
            this.val = val;
        }
    }

    // [databind#2784]
    static class BigDecimalHolder2784 {
        public BigDecimal value;
    }

    static class DoubleHolder2784 {
        public Double value;
    }

    static class FloatHolder2784 {
        public Float value;
    }

    @JsonRootName("Nested")
    static class NestedBigDecimalHolder2784 {
        @JsonUnwrapped
        public BigDecimalHolder2784 holder;
    }

    @JsonRootName("Nested")
    static class NestedDoubleHolder2784 {
        @JsonUnwrapped
        public DoubleHolder2784 holder;
    }

    @JsonRootName("Nested")
    static class NestedFloatHolder2784 {
        @JsonUnwrapped
        public FloatHolder2784 holder;
    }

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    // [databind#2644]
    public void testBigDecimalSubtypes() throws Exception
    {
        ObjectMapper mapper = mapperBuilder()
                .registerSubtypes(NodeParent2644.class)
                .build();
        NodeRoot2644 root = mapper.readValue("<Root>\n"
                +"<type>NodeParent</type>"
                +"<node><amount>9999999999999999.99</amount></node>\n"
                +"</Root>\n",
                NodeRoot2644.class
        );

        assertEquals(new BigDecimal("9999999999999999.99"), root.node.getVal());
    }

    // [databind#2784]
    public void testBigDecimalUnwrapped() throws Exception
    {
        // mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        final String DOC = "<Nested><value>5.00</value></Nested>";
        NestedBigDecimalHolder2784 result = MAPPER.readValue(DOC, NestedBigDecimalHolder2784.class);
        assertEquals(new BigDecimal("5.00"), result.holder.value);
    }

    public void testDoubleUnwrapped() throws Exception
    {
        final String DOC = "<Nested><value>125.123456789</value></Nested>";
        NestedDoubleHolder2784 result = MAPPER.readValue(DOC, NestedDoubleHolder2784.class);
        assertEquals(Double.parseDouble("125.123456789"), result.holder.value);
    }

    public void testFloatUnwrapped() throws Exception
    {
        final String DOC = "<Nested><value>125.123</value></Nested>";
        NestedFloatHolder2784 result = MAPPER.readValue(DOC, NestedFloatHolder2784.class);
        assertEquals(Float.parseFloat("125.123"), result.holder.value);
    }

    public void testVeryBigDecimalUnwrapped() throws Exception
    {
        final int len = 1200;
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append("1");
        }
        final String value = sb.toString();
        final String DOC = "<Nested><value>" + value + "</value></Nested>";
        try {
            MAPPER.readValue(DOC, NestedBigDecimalHolder2784.class);
            fail("expected JsonMappingException");
        } catch (JsonMappingException jme) {
            assertTrue("unexpected exception message: " + jme.getMessage(),
                    jme.getMessage().startsWith("Number length (1200) exceeds the maximum length (1000)"));
        }
    }

    public void testVeryBigDecimalUnwrappedWithUnlimitedNumLength() throws Exception
    {
        final int len = 1200;
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append("1");
        }
        final String value = sb.toString();
        final String DOC = "<Nested><value>" + value + "</value></Nested>";
        XmlFactory f = streamFactoryBuilder()
                .streamReadConstraints(StreamReadConstraints.builder().maxNumberLength(Integer.MAX_VALUE).build())
                .build();
        NestedBigDecimalHolder2784 result = new XmlMapper(f).readValue(DOC, NestedBigDecimalHolder2784.class);
        assertEquals(new BigDecimal(value), result.holder.value);
    }
}
