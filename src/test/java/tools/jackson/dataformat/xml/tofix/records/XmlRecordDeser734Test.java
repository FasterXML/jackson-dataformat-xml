package tools.jackson.dataformat.xml.tofix.records;

import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlText;
import tools.jackson.dataformat.xml.testutil.failure.JacksonTestFailureExpected;

import static org.junit.jupiter.api.Assertions.assertEquals;

// [dataformat-xml#734]
public class XmlRecordDeser734Test extends XmlTestUtil
{

    record Amount(@JacksonXmlText String value,
                  @JacksonXmlProperty(isAttribute = true, localName = "Ccy") String currency) {}

    private final String XML =
            a2q("<Amt Ccy='EUR'>1</Amt>");

    @JacksonTestFailureExpected
    @Test
    public void testDeser() throws Exception {
        XmlMapper mapper = new XmlMapper();
        Amount amt = mapper.readValue(XML, Amount.class);
        assertEquals("1", amt.value);
        assertEquals("EUR", amt.currency);
    }
}
