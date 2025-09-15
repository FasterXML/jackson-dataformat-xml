package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// [dataformat-xml#735]
public class XmlClassDeser735Test extends XmlTestUtil
{
    public static class Amount {
        @JacksonXmlText
        private String value;

        @JacksonXmlProperty(isAttribute = true, localName = "Ccy")
        private String currency;

        // Need default constructor for deserialization (failure without it)
        public Amount() {

        }

        public Amount(String value, String currency) {
            this.value = value;
            this.currency = currency;
        }

        public String getValue() {
            return value;
        }

        public String getCurrency() {
            return currency;
        }
    }

    private final String XML =
            a2q("<Amt Ccy='EUR'>1</Amt>");

    @Test
    public void testDeser() throws Exception {
        XmlMapper mapper = new XmlMapper();
        Amount amt = mapper.readValue(XML, Amount.class);
        assertEquals("1", amt.value);
        assertEquals("EUR", amt.currency);
    }
}
