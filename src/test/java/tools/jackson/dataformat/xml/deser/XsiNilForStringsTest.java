package tools.jackson.dataformat.xml.deser;

import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.*;

public class XsiNilForStringsTest extends XmlTestUtil
{
    private final static String XSI_NS_DECL = "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'";

    protected static class StringPair {
        public String first, second;
    }

    private final XmlMapper MAPPER = newMapper();

    // [dataformat-xml#378]
    @Test
    public void testWithStringAsNull() throws Exception
    {
        StringPair bean;
        
        bean = MAPPER.readValue(
"<StringPair "+XSI_NS_DECL+"><first>not null</first><second xsi:nil='true' /></StringPair>",
            StringPair.class);
        assertNotNull(bean);

        assertEquals("not null", bean.first);

        assertNull(bean.second);
    }

    // [dataformat-xml#378]
    @Test
    public void testWithStringAsNull2() throws Exception
    {
        StringPair bean;

        bean = MAPPER.readValue(
"<StringPair "+XSI_NS_DECL+"><first xsi:nil='true' /><second>not null</second></StringPair>",
//"<StringPair "+XSI_NS_DECL+"><first xsi:nil='true'></first><second>not null</second></StringPair>",
            StringPair.class);
        assertNotNull(bean);
        assertEquals("not null", bean.second);

      assertNull(bean.first);
    }
}
