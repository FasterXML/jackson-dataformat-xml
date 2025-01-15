package tools.jackson.dataformat.xml.deser;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.xml.*;
import tools.jackson.dataformat.xml.testutil.NoCheckSubTypeValidator;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultTyping325Test extends XmlTestUtil
{
    static class Simple325 {
        protected String[] list;

        public String[] getList( ) { return list; }
        public void setList(String[] l) { list = l; }
    }

    // [dataformat-xml#325]
    @Test
    public void testDefaultTypingWithInnerClass() throws IOException
    {
        ObjectMapper mapper = mapperBuilder()
                .activateDefaultTyping(NoCheckSubTypeValidator.instance,
                        DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_OBJECT)
                .build();
        Simple325 s = new Simple325();
        s.setList(new String[] { "foo", "bar" });

        String doc = mapper.writeValueAsString(s);
        Simple325 result = mapper.readValue(doc, Simple325.class);
        assertNotNull(result.list);
        assertEquals(2, result.list.length);
    }
}
