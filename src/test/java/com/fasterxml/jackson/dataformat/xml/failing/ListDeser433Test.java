package com.fasterxml.jackson.dataformat.xml.failing;

import java.math.BigDecimal;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

// [dataformat-xml#433]
public class ListDeser433Test extends XmlTestBase
{
    @JacksonXmlRootElement(localName = "Product")
    static class Product433 {
        @JsonProperty("Prices")
        public Prices prices;
    }

    static class Prices {
        @JsonProperty("Price")
        public List<Price> price;

        public List<Price> getPrice() {
            if (price == null) {
                price = new ArrayList<Price>();
            }
            return this.price;
        }
    }

    static class Price {
        @JsonProperty("Start")
        public Integer start;
        @JsonProperty("End")
        public Integer end;
        @JsonProperty("Price")
        public BigDecimal price;
    }

    private final XmlMapper MAPPER = mapperBuilder()
            .defaultUseWrapper(false)
//            .annotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()))
            .build();

    // [dataformat-xml#433]
    public void testIssue433() throws Exception {
        final String XML =
"<Product>\n" +
"    <Price>\n" +
"        <Start>50</Start>\n" +
"        <End>99</End>\n" +
"        <Price>2.53</Price>\n" +
"    </Price>\n" +
"</Product>";

        Product433 main = MAPPER.readValue(XML, Product433.class);
        assertNotNull(main);
    }
}
