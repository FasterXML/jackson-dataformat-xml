package tools.jackson.dataformat.xml.deser;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlText;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static tools.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT;
import static tools.jackson.databind.cfg.DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS;

// [dataformat-xml#615]
public class EntityClassList615Test {

    static class EntityClass {
        @JacksonXmlText
        @JsonProperty("kto")
        public String kto;

        @JacksonXmlProperty(isAttribute = true, localName = "Hkto")
        @JsonProperty("hkto")
        public String hkto;
    }

    static class EntityClassList {

        @JacksonXmlElementWrapper(localName = "entities")
        @JsonProperty("entity")
        public List<EntityClass> entityClassList;
    }

    @Test
    void testXmlDeser() {
        XmlMapper xmlMapper = XmlMapper.builder()
                .defaultUseWrapper(false)
                .disable(WRITE_DATES_AS_TIMESTAMPS)
                .enable(ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .build();

        String xmlString =
                """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <EntityclassList>
                            <entities>
                                <kto Hkto="6543210000">4561230000</kto>
                                <kto Hkto="6543210000">5678910012</kto>
                                <kto Hkto="">5678910013</kto>
                                <kto Hkto="654321">567891</kto>
                                <kto>5678910014</kto>
                                <kto>567891</kto>
                            </entities>
                        </EntityclassList>""";

        EntityClassList entityClassList = xmlMapper.readValue(xmlString, EntityClassList.class);
        assertNotNull(entityClassList);
    }
}
