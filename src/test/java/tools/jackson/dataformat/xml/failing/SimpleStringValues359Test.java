package tools.jackson.dataformat.xml.failing;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestBase;

public class SimpleStringValues359Test extends XmlTestBase
{
    static class Issue167Bean {
        public String d;
    }

    private final XmlMapper MAPPER = newMapper();

    // [dataformat-xml#359]
    public void testEmptyElementToString() throws Exception
    {
        final String XML =
"<a xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>\n"+
"<d xsi:nil='true'/>\n"+
"</a>\n";
        Issue167Bean result = MAPPER.readValue(XML, Issue167Bean.class);
        assertNotNull(result);
        // 06-Sep-2019, tatu: As per [dataformat-xml#354] this should now (2.10)
        //    produce real `null`:
//        assertEquals("", result.d);
        assertNull(result.d);
    }
}
