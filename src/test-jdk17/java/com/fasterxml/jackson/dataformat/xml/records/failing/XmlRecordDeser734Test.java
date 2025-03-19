package com.fasterxml.jackson.dataformat.xml.records.failing;

import org.junit.Test;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import static org.junit.Assert.assertEquals;

// [dataformat-xml#734]
public class XmlRecordDeser734Test extends XmlTestBase
{

    record Amount(@JacksonXmlText String value,
                  @JacksonXmlProperty(isAttribute = true, localName = "Ccy") String currency) {}

    private final String XML =
            a2q("<Amt Ccy='EUR'>1</Amt>");

    //@JacksonTestFailureExpected
    @Test
    public void testDeser() throws Exception {
        XmlMapper mapper = new XmlMapper();
        Amount amt = mapper.readValue(XML, Amount.class);
        assertEquals("1", amt.value);
        assertEquals("EUR", amt.currency);
    }
}
