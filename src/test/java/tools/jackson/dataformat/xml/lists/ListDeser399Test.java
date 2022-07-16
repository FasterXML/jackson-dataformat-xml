package tools.jackson.dataformat.xml.lists;

import java.util.*;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestBase;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class ListDeser399Test extends XmlTestBase
{
    static class Main {
        @JacksonXmlProperty(localName = "test")
        @JacksonXmlElementWrapper(useWrapping = false)
        List<Test> list = new ArrayList<Test>();
    }

    static class Test {
        @JacksonXmlProperty(localName = "test")
        @JacksonXmlElementWrapper(useWrapping = false)
        List<Test> list = new ArrayList<Test>();
    }

    private final XmlMapper MAPPER = newMapper();
    
    public void testIssue399() throws Exception {
        final String XML =
"<Main>\n" +
"    <test>\n" +
"        <test>\n" +
"            <test>\n" +
"            </test>\n" +
//"            <test>\n" +
//"            </test>\n" +
"        </test>\n" +
"    </test>\n" +
"    <test>\n" +
"    </test>\n" +
"</Main>";
        Main main = MAPPER.readValue(XML, Main.class);
        assertNotNull(main);
        assertNotNull(main.list);
        assertEquals(2, main.list.size());
        assertNotNull(main.list.get(0));
        assertNotNull(main.list.get(0).list);
        assertEquals(1, main.list.get(0).list.size());
        assertNotNull(main.list.get(1));
    }
}
