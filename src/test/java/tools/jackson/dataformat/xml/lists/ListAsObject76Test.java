package tools.jackson.dataformat.xml.lists;

import java.util.*;

import org.junit.jupiter.api.Test;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonFormat;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Test originally from [dataformat-xml#76].
//
// Should pass on JDK17+ too on Jackson 2.15+
@SuppressWarnings("serial")
public class ListAsObject76Test extends XmlTestUtil
{
    static final class Value {
        @XmlElement(name = "v")
        public String v;

        public String getV() { return v; }
        public void setV(final String v) { this.v = v; }
    }

    // 17-May-2018, tatu: In 3.0, need to use POJO
    @JsonFormat(shape=JsonFormat.Shape.POJO)
    static final class Values extends LinkedList<Value>
    {
        @XmlAttribute(name = "type")
        private String type;
    
        @JacksonXmlElementWrapper(localName = "value", useWrapping = false)
        @JacksonXmlProperty(localName = "value")
        List<Value> values = new ArrayList<Value>();

        String getType() { return type; }

        void setType(final String type) { this.type = type; }

        List<Value> getValues() { return values; }

        void setValues(final List<Value> values) { this.values = values; }
    }

    @Test
    public void testCollection() throws Exception {
        final Values values = new XmlMapper().readValue("<values type=\"array\">" +
                "  <value><v>c</v></value>" +
                "  <value><v>d</v></value>" +
                "</values>",
                Values.class);
        assertEquals(2, values.getValues().size(), 2);
        assertEquals("c", values.getValues().get(0).getV());
        assertEquals("d", values.getValues().get(1).getV());
    
        assertEquals("array", values.getType());
    }
}
