package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonRootName;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

public class ListDeser393Test extends XmlTestBase
{
    @JsonRootName("prices")
    static class Prices393 {
        private List<Price393> price = new ArrayList<Price393>();

        public void setPrice(List<Price393> price) {
            this.price = price;
        }

        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Price393> getPrice() {
            return this.price;
        }
    }

    static class Price393 {
        private String price;
        private String num;

        protected Price393() { }
        public Price393(String p, String n) {
            price = p;
            num = n;
        }
        
        public void setPrice(String price) {
            this.price = price;
        }

        public String getPrice() {
            return this.price;
        }

        public void setNum(String num) {
            this.num = num;
        }

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

    // [dataformat-xml#393]
    public void testDeser393() throws Exception
    {
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
        Prices393 result = MAPPER.readValue(content, Prices393.class);
        assertNotNull(result);
        assertNotNull(result.getPrice());
        assertEquals(2, result.getPrice().size());
    }
}
