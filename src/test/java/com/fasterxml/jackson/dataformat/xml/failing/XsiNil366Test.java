package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class XsiNil366Test extends XmlTestBase
{
    // for [dataformat-xml#366]
    protected static class Parent366 {
        public Level1 level1;
    }

    protected static class Level1 {
        public Level2 level2;
        public String field; // this should not be needed, but an unknown element is thrown without it
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
                + "<level1>"
                + "<level2>"
                + "<ignored xsi:nil=\"true\"/>"
                + "<field>test-value</field>"
                + "</level2>"
                + "</level1>"
                + "</Parent>";
        Parent366 bean = MAPPER.readValue(xml, Parent366.class);

        assertNotNull(bean);

        // this should not be set, but having an xsi:nil field before it causes it to set the next field on the wrong class
        assertEquals("test-value", bean.level1.field);

        // fails because field is set on level1 instead of on level2
        assertEquals("test-value", bean.level1.level2.field);
    }
}
