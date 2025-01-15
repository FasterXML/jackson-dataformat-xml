package tools.jackson.dataformat.xml.deser;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import tools.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.*;

public class EnumDeserTest extends XmlTestUtil
{
    static enum TestEnum { A, B, C; }

    static class EnumBean
    {
        public TestEnum value;

        public EnumBean() { }
        public EnumBean(TestEnum v) { value = v; }
    }

    // [dataformat-xml#682]
    static enum Country682 {
        ITALY("Italy"),
        NETHERLANDS("Netherlands");

        @JsonValue
        final String value;

        Country682(String value) {
            this.value = value;
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static Country682 fromValue(String value) {
            for (Country682 b : Country682.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();

    @Test
    public void testEnumInBean() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new EnumBean(TestEnum.B));
        EnumBean result = MAPPER.readValue(xml, EnumBean.class);
        assertNotNull(result);
        assertEquals(TestEnum.B, result.value);
    }

    // [dataformat-xml#121]
    @Test
    public void testRootEnum() throws Exception
    {
        String xml = MAPPER.writeValueAsString(TestEnum.B);
        TestEnum result = MAPPER.readValue(xml, TestEnum.class);
        assertNotNull(result);
        assertEquals(TestEnum.B, result);
    }

    // [dataformat-xml#682]
    @Test
    public void testEnumDeser682() throws Exception {
        String xml = MAPPER.writeValueAsString(Country682.ITALY);
        assertEquals("<Country682>Italy</Country682>", xml);
        
        assertEquals(Country682.ITALY, MAPPER.readValue(xml, Country682.class));
    }    
}
