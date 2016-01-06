package com.fasterxml.jackson.dataformat.xml.failing;

import java.util.*;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class NestedUnwrappedLists180Test  extends XmlTestBase
{
    static class Records {
        @JacksonXmlElementWrapper(useWrapping=false)
        public List<Record> records = new ArrayList<Record>();
    }

    static class Record {
        @JacksonXmlElementWrapper(useWrapping=false)
        public List<Field> fields = new ArrayList<Field>();
    }

    static class Field {
        @JacksonXmlProperty(isAttribute=true)
        public String name;

        protected Field() { }
        public Field(String n) { name = n; }
    }

    /*
    /**********************************************************************
    /* Unit tests
    /**********************************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();

    public void testNestedUnwrappedLists180() throws Exception
    {
        /*
        Records recs = new Records();
        recs.records.add(new Record());
        recs.records.add(new Record());
        recs.records.add(new Record());
        recs.records.get(0).fields.add(new Field("a"));
        recs.records.get(2).fields.add(new Field("b"));

        String xml = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(recs);
        */

        String xml =
"<Records>\n"
+"  <records>\n"
+"    <fields name='a'/>\n"
+"  </records>\n"

// Important: it's the empty CDATA here that causes breakage -- empty element would be fine

+"  <records>\n"
+"  </records>\n"
+"  <records>\n"
+"   <fields name='b'/>\n"
+"  </records>\n"
+"</Records>\n"
;
        
//System.out.println("XML: "+xml);

        Records result = MAPPER.readValue(xml, Records.class);
        assertNotNull(result.records);
        assertEquals(3, result.records.size());
        assertEquals(1, result.records.get(2).fields.size());
        assertEquals("b", result.records.get(2).fields.get(0).name);
    }
}
