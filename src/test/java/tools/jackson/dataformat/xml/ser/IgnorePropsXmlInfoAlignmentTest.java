package tools.jackson.dataformat.xml.ser;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlCData;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlText;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IgnorePropsXmlInfoAlignmentTest extends XmlTestUtil
{
    @JsonPropertyOrder({ "attr", "e1", "e2" })
    static class AttrInner {
        @JacksonXmlProperty(isAttribute = true)
        public String attr = "A";
        public String e1 = "E1";
        public String e2 = "E2";
    }

    static class AttrOuter {
        @JsonIgnoreProperties("attr")
        public AttrInner inner = new AttrInner();
    }

    @JsonPropertyOrder({ "p0", "text", "p2" })
    static class TextInner {
        public String p0 = "P0";
        @JacksonXmlText
        public String text = "TEXT";
        public String p2 = "P2";
    }

    static class TextOuter {
        @JsonIgnoreProperties("p0")
        public TextInner inner = new TextInner();
    }

    @JsonPropertyOrder({ "x", "cd", "y" })
    static class CDataInner {
        public String x = "X";
        @JacksonXmlCData
        public String cd = "C";
        public String y = "Y";
    }

    static class CDataOuter {
        @JsonIgnoreProperties("x")
        public CDataInner inner = new CDataInner();
    }

    private final XmlMapper MAPPER = newMapper();

    @Test
    public void testAttributeStaysElementAfterIgnore() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new AttrOuter());
        assertEquals("<AttrOuter><inner><e1>E1</e1><e2>E2</e2></inner></AttrOuter>", xml);
    }

    @Test
    public void testTextIndexAfterIgnore() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new TextOuter());
        assertEquals("<TextOuter><inner>TEXT<p2>P2</p2></inner></TextOuter>", xml);
    }

    @Test
    public void testCDataIndexAfterIgnore() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new CDataOuter());
        assertEquals("<CDataOuter><inner><cd><![CDATA[C]]></cd><y>Y</y></inner></CDataOuter>", xml);
    }
}
