package tools.jackson.dataformat.xml.ser;

import org.junit.jupiter.api.Test;

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

    @Test
    public void testCDataWithEndMarkerRoundTrip() throws Exception
    {
        _roundTrip("a]]>b");
        _roundTrip("]]>");
        _roundTrip("]]>]]>");
        _roundTrip("trailing]]>");
        _roundTrip("<![CDATA[nested]]>tail");
    }

    @Test
    public void testCDataWithoutEndMarkerUnchanged() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new CDataBean("plain"));
        assertEquals("<CDataBean><value><![CDATA[plain]]></value></CDataBean>", xml);
    }

    private void _roundTrip(String value) throws Exception
    {
        String xml = MAPPER.writeValueAsString(new CDataBean(value));
        CDataBean result = MAPPER.readValue(xml, CDataBean.class);
        assertEquals(value, result.value, "round-trip failed for XML: " + xml);
    }
}
