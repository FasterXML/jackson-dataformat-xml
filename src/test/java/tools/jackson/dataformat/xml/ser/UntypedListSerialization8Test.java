package tools.jackson.dataformat.xml.ser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonRootName;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

// [dataformat-xml#8]: Serialization of List incorrect if property declared as Object
public class UntypedListSerialization8Test extends XmlTestUtil
{
    @JsonRootName("L")
    static class UntypedListBean
    {
        public final Object list;

        public UntypedListBean() {
            ArrayList<String> l= new ArrayList<String>();
            l.add("first");
            l.add("second");
            list = l;
        }
    }

    @JsonRootName("L")
    static class TypedListBean
    {
        public final List<String> list;

        public TypedListBean() {
            ArrayList<String> l= new ArrayList<String>();
            l.add("first");
            l.add("second");
            list = l;
        }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    // [dataformat-xml#8]
    @Test
    public void testListAsObject() throws IOException
    {
        String xmlForUntyped = MAPPER.writeValueAsString(new UntypedListBean());
        String xmlForTyped = MAPPER.writeValueAsString(new TypedListBean());

        assertEquals(xmlForTyped, xmlForUntyped);
    }
}
