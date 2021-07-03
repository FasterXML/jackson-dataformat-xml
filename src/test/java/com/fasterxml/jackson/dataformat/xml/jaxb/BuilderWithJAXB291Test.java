package com.fasterxml.jackson.dataformat.xml.jaxb;

import jakarta.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import com.fasterxml.jackson.dataformat.xml.XmlAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

// Test for [dataformat-xml#291]: works via field, not constructor
// (name mismatch to fix in test case)
public class BuilderWithJAXB291Test extends XmlTestBase
{
    @JsonDeserialize(builder = Address.AddressBuilder.class)
    static class Address
    {
        private final String address1;
        private final String city;
        private final String stateProvince;
        private final String postalCode;
        private final String country;
        private final String county;

        Address(AddressBuilder addressbuilder) {
            this.address1 = addressbuilder.address1;
            this.city = addressbuilder.city;
            this.stateProvince = addressbuilder.stateProvince;
            this.postalCode = addressbuilder.postalCode;
            this.country = addressbuilder.country;
            this.county = addressbuilder.county;
        }

        public String getAddress1() {
            return address1;
        }
        public String getCity() {
            return city;
        }

        public String getStateProvince() {
            return stateProvince;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public String getCountry() {
            return country;
        }

        public String getCounty() {
            return county;
        }

        @JsonPOJOBuilder(withPrefix = "set")
        static class AddressBuilder {
         
            @XmlElement(name = "Address1")
            final String address1;

            @XmlElement(name = "City")
            final String city;

            @XmlElement(name = "StateProvince")
            final String stateProvince;

            @XmlElement(name = "PostalCode")
            final String postalCode;

            @XmlElement(name = "Country")
            final String country;

            @XmlElement(name = "County")
            String county;

            @JsonCreator
            public AddressBuilder(@JsonProperty("address1") String address1, @JsonProperty("city") String city, @JsonProperty("stateProvince") String stateProvince,
                    @JsonProperty("postalCode") String postalCode, @JsonProperty("country") String country) {
                this.address1 = address1;
                this.city = city;
                this.stateProvince = stateProvince;
                this.postalCode = postalCode;
                this.country = country;
            }

            public AddressBuilder setCounty(String county) {
                this.county = county;
                return this;
            }

            public Address build() {
                return new Address(this);
            }
        }
    }

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    public void testBuilder291() throws Exception
    {
        final String DOC = "<Address>\n" + 
                "    <Address1>111 Foo Bar</Address1>\n" + 
                "    <City>San Francisco</City>\n" + 
                "    <Country>USA</Country>\n" + 
                "    <County>San Francisco</County>\n" + 
                "    <StateProvince>CA</StateProvince>\n" + 
                "    <PostalCode>94132</PostalCode>\n" + 
                "</Address>";

        AnnotationIntrospector xmlIntr = jakartaXMLBindAnnotationIntrospector();
        AnnotationIntrospector intr = XmlAnnotationIntrospector.Pair.instance
                (xmlIntr, new JacksonAnnotationIntrospector());
        XmlMapper mapper = mapperBuilder()
                .annotationIntrospector(intr)
                .build();
        Address value = mapper.readValue(DOC, Address.class);
        assertNotNull(value);
        assertEquals("San Francisco", value.getCity());
    }
}
