package tools.jackson.dataformat.xml.tofix.records;

import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;

// [dataformat-xml#734]
public class XmlRecordDeser734Test extends XmlTestUtil
{

    record Amount(@JacksonXmlProperty String value,
                  @JacksonXmlProperty(isAttribute = true, localName = "Ccy") String currency) {}

    private final String XML =
            a2q("<Amt Ccy='EUR'>1</Amt>");

    @Test
    public void testDeser() throws Exception {
        XmlMapper mapper = new XmlMapper();
        Amount amt0 = new Amount("1", "EUR");
        String xml0 = mapper.writeValueAsString(amt0);
        assertEquals(XML, xml0);
        Amount amt = mapper.readValue(XML, Amount.class);
        // on master, the next assert fails because the value is null
        assertEquals(1, amt.value);
        assertEquals("EUR", amt.currency);
    }
}
