package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class XsiNilNestingTest extends XmlTestBase
{
    // for [dataformat-xml#366]
    protected static class Parent366 {
        public Level1 level1;
    }

    protected static class Level1 {
        public Level2 level2;
    }

    protected static class Level2 {
        public String ignored;
        public String field;
    }

    private final XmlMapper MAPPER = newMapper();

    // for [dataformat-xml#366]
    public void testDoesNotAffectHierarchy() throws Exception
    {
        String xml = "<Parent xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + " <level1>"
                + "  <level2>"
                + "    <ignored xsi:nil=\"true\"/>"
                + "    <field>test-value</field>"
                + "  </level2>"
                + " </level1>"
                + "</Parent>";
        Parent366 bean = MAPPER.readValue(xml, Parent366.class);

        assertNotNull(bean);
        assertNotNull(bean.level1);
        Level2 l2 = bean.level1.level2;
        assertNotNull(l2);

        // should be null
        assertNull(l2.ignored);
        // and should not be null
        assertNotNull(l2.field);

        assertEquals("test-value", l2.field);
    }
}
