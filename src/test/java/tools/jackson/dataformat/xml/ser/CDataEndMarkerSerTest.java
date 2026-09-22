package tools.jackson.dataformat.xml.ser;

import java.io.StringWriter;
import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectWriter;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlCData;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Values written as CDATA must survive serialization even when they contain
// the "]]>" sequence that closes a CDATA section.
public class CDataEndMarkerSerTest extends XmlTestUtil
{
    static class CDataBean
    {
        @JacksonXmlCData
        public String value;

        public CDataBean() { }
        public CDataBean(String v) { value = v; }
    }

    private final XmlMapper MAPPER = newMapper();

    private final static String[] VALUES_WITH_END_MARKER = new String[] {
            "a]]>b",
            "]]>",
            "]]>]]>",
            "trailing]]>",
            "]]]>", // extra ']' before the marker
            "<![CDATA[nested]]>tail"
    };

    @Test
    public void testCDataWithEndMarkerRoundTrip() throws Exception
    {
        _roundTrip(MAPPER.writer());
    }

    // Pretty-printing writes leaf elements via `DefaultXmlPrettyPrinter`, a
    // separate set of CDATA write calls, so verify that path too
    @Test
    public void testCDataWithEndMarkerRoundTripPretty() throws Exception
    {
        _roundTrip(MAPPER.writerWithDefaultPrettyPrinter());
    }

    // And finally the `char[]`-taking variant, only reachable via low-level
    // generator access
    @Test
    public void testCDataWithEndMarkerAsCharArray() throws Exception
    {
        for (String value : VALUES_WITH_END_MARKER) {
            StringWriter out = new StringWriter();
            ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out);
            gen.setNextName(new QName("CDataBean"));
            gen.writeStartObject();
            gen.writeName("value");
            gen.setNextIsCData(true);
            // offset the content to verify offset/length handling
            char[] ch = ("##" + value + "##").toCharArray();
            gen.writeString(ch, 2, value.length());
            gen.writeEndObject();
            gen.close();

            String xml = removeSjsxpNamespace(out.toString());
            CDataBean result = MAPPER.readValue(xml, CDataBean.class);
            assertEquals(value, result.value, "round-trip failed for XML: " + xml);
        }
    }

    @Test
    public void testCDataWithoutEndMarkerUnchanged() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new CDataBean("plain"));
        assertEquals("<CDataBean><value><![CDATA[plain]]></value></CDataBean>", xml);
    }

    private void _roundTrip(ObjectWriter w) throws Exception
    {
        for (String value : VALUES_WITH_END_MARKER) {
            String xml = w.writeValueAsString(new CDataBean(value));
            CDataBean result = MAPPER.readValue(xml, CDataBean.class);
            assertEquals(value, result.value, "round-trip failed for XML: " + xml);
        }
    }
}
