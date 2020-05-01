package com.fasterxml.jackson.dataformat.xml.failing;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

public class Issue393DeserTest extends XmlTestBase
{
    @JacksonXmlRootElement(localName = "result")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class Value393 {
        private Prices prices = new Prices();

        public void setPrices(Prices prices) {
            this.prices = prices;
        }

        @JacksonXmlProperty(localName = "prices")
        public Prices getPrices() {
            return this.prices;
        }
    }

//    @JsonIgnoreProperties(ignoreUnknown = true)
    @JacksonXmlRootElement(localName = "prices")
    static class Prices {
        private List<Price> price = new ArrayList<Price>();

        public void setPrice(List<Price> price) {
            this.price = price;
        }

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "price")
        public List<Price> getPrice() {
            return this.price;
        }
    }

//    @JacksonXmlRootElement(localName = "price")
//    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Price {
        private String price;
        private String num;

        protected Price() { }
        public Price(String p, String n) {
            price = p;
            num = n;
        }
        
        public void setPrice(String price) {
            this.price = price;
        }

        @JacksonXmlProperty(localName = "price")
        public String getPrice() {
            return this.price;
        }

        public void setNum(String num) {
            this.num = num;
        }

        @JacksonXmlProperty(localName = "num")
        public String getNum() {
            return this.num;
        }
    }

    /*
    /********************************************************
    /* Test methods
    /********************************************************
     */

    private final ObjectMapper MAPPER = newMapper();

    // [dataform#393]
    public void testDeser393() throws Exception
    {
        // for debugging:
/*        
        Value393 input = new Value393();
        Prices prices = new Prices();
        prices.setPrice(Arrays.asList(
                new Price("100", "7.0"),
                new Price("100", "4.0")
        ));
        input.setPrices(prices);

        String xml = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(input);
        System.out.println("XML:\n"+xml);
*/
/*
        String content = //"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + "\n" +
                "<result>\n"
                + " <prices>\n"
                + "   <price>\n"
                + "     <num>100</num>\n"
                + "     <price>7.0</price>\n"
                + "   </price>\n"
                + "   <price>\n"
                + "     <num>100</num>\n"
                + "     <price>4.0</price>\n"
                + "   </price>"
                + " </prices>\n"
                + "</result>";
        Value393 result = MAPPER.readValue(content, Value393.class);
        assertNotNull(result);
*/

        String content =
                "<prices>\n"
                + " <price>\n"
                + "   <num>100</num>\n"
                + "   <price>7.0</price>\n"
                + " </price>\n"
                + " <price>\n"
                + "   <num>100</num>\n"
                + "   <price>4.0</price>\n"
                + " </price>"
                + "</prices>\n";
        Prices result = MAPPER.readValue(content, Prices.class);
        assertNotNull(result);
    }
}
