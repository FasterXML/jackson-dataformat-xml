package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

public class ListDeser307Test extends XmlTestBase
{
    @JacksonXmlRootElement(localName = "customer")
    public class CustomerWithoutWrapper {
        public Long customerId;
        public String customerName;

        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Account> account;
    }

    public class Account {
        public Long accountId;
        public String accountName;
        public String postcode;
    }    
    
    /*
    /********************************************************
    /* Test methods
    /********************************************************
     */

    private final ObjectMapper MAPPER = newMapper();

    // [dataformat-xml#307]
    public void testListDeser307() throws Exception
    {
        final String XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<customer xmlns=\"http://www.archer-tech.com/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "    <customerId>1</customerId>\n" +
                "    <customerName>Michael Judy</customerName>\n" +
                "    <account>\n" +
                "        <accountId>100</accountId>\n" +
                "        <accountName>Michael</accountName>\n" +
                "        <postcode xsi:nil=\"true\"></postcode>\n" +
                "    </account>\n" +
                "    <account>\n" +
                "        <accountId>200</accountId>\n" +
                "        <accountName>Judy</accountName>\n" +
                "        <postcode xsi:nil=\"true\"></postcode>\n" +
                "    </account> \n" +
                "</customer>";
        CustomerWithoutWrapper result =
                MAPPER.readValue(XML, CustomerWithoutWrapper.class);
        assertNotNull(result);
        assertNotNull(result.account);
        assertEquals(2, result.account.size());
    }
}
