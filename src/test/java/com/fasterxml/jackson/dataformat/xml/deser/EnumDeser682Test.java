package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class EnumDeser682Test extends XmlTestBase
{
    static enum Country {
        ITALY("Italy"),
        NETHERLANDS("Netherlands");

        private String value;

        Country(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static Country fromValue(String value) {
            for (Country b : Country.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    private final ObjectMapper MAPPER = newMapper();

    public void testEnumDeser682() throws Exception {
        String xml = MAPPER.writeValueAsString(Country.ITALY);
        assertEquals("<Country>Italy</Country>", xml);
        
        assertEquals(Country.ITALY, MAPPER.readValue(xml, Country.class));
    }
}
