package tools.jackson.dataformat.xml.deser.records;

import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.*;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlText;

import static org.junit.jupiter.api.Assertions.*;

// [dataformat-xml#735]
public class XmlRecordDeser735Test extends XmlTestUtil
{
    record Amount(@JacksonXmlText String value,
                  @JacksonXmlProperty(isAttribute = true, localName = "Ccy") String currency) {}

    static class Pojo735 {
        String value;
        String currency;

        public Pojo735(@JacksonXmlText String value,
                @JacksonXmlProperty(isAttribute = true, localName = "Ccy") String currency)
        {
            this.value = value;
            this.currency = currency;
        }
    }

    private final String XML = "<Amt Ccy='EUR'>1</Amt>";

    private final XmlMapper MAPPER = newMapper();

    @Test
    public void testPojoDeser() throws Exception {
        Pojo735 amt = MAPPER.readValue(XML, Pojo735.class);
        assertEquals("1", amt.value);
        assertEquals("EUR", amt.currency);
    }

    @Test
    public void testRecordDeser() throws Exception {
        Amount amt = MAPPER.readValue(XML, Amount.class);
        assertEquals("1", amt.value);
        assertEquals("EUR", amt.currency);
    }
}
