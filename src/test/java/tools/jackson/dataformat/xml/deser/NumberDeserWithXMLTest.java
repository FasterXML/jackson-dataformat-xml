package tools.jackson.dataformat.xml.deser;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;

import tools.jackson.core.StreamReadConstraints;
import tools.jackson.core.exc.StreamConstraintsException;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.*;

// Tests copied from databind "JDKNumberDeserTest" (only a small subset)
public class NumberDeserWithXMLTest extends XmlTestUtil
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

    static class DeserializationIssue4917 {
        public DecimalHolder4917 decimalHolder;
        public double number;
    }

    static class DecimalHolder4917 {
        public BigDecimal value;

        private DecimalHolder4917(BigDecimal value) {
            this.value = value;
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        static DecimalHolder4917 of(BigDecimal value) {
            return new DecimalHolder4917(value);
        }
    }

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    // [databind#2644]
    @Test
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
    @Test
    public void testBigDecimalUnwrapped() throws Exception
    {
        // mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        final String DOC = "<Nested><value>5.00</value></Nested>";
        NestedBigDecimalHolder2784 result = MAPPER.readValue(DOC, NestedBigDecimalHolder2784.class);
        assertEquals(new BigDecimal("5.00"), result.holder.value);
    }

    @Test
    public void testDoubleUnwrapped() throws Exception
    {
        final String DOC = "<Nested><value>125.123456789</value></Nested>";
        NestedDoubleHolder2784 result = MAPPER.readValue(DOC, NestedDoubleHolder2784.class);
        assertEquals(Double.parseDouble("125.123456789"), result.holder.value);
    }

    @Test
    public void testFloatUnwrapped() throws Exception
    {
        final String DOC = "<Nested><value>125.123</value></Nested>";
        NestedFloatHolder2784 result = MAPPER.readValue(DOC, NestedFloatHolder2784.class);
        assertEquals(Float.parseFloat("125.123"), result.holder.value);
    }

    @Test
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
            fail("expected DatabindException");
        } catch (StreamConstraintsException e) {
            assertTrue(
                    e.getMessage().startsWith("Number value length (1200) exceeds the maximum allowed"),
                    "unexpected exception message: " + e.getMessage());
        }
    }

    @Test
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

    // [databind#4917]
    @Test
    public void bigDecimal4917() throws Exception
    {
        DeserializationIssue4917 issue = MAPPER.readValue(
                "<root><decimalHolder>100.00</decimalHolder><number>50</number></root>",
                DeserializationIssue4917.class);
        assertEquals(new BigDecimal("100.00"), issue.decimalHolder.value);
        assertEquals(50.0, issue.number);
    }
}
