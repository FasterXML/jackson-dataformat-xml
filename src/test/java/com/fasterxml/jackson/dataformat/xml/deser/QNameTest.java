package com.fasterxml.jackson.dataformat.xml.deser;

import static junit.framework.TestCase.assertEquals;

import javax.xml.namespace.QName;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;

public class QNameTest {
    protected static class Parent {
        public Level1 level1;
    }

    protected static class Level1 {
        public QName name;
    }

    private final XmlMapper MAPPER = XmlMapper.builder()
            .defaultUseWrapper(false)
            .build();

    @Test
    public void testQNameParser() throws Exception
    {
        String xml =
                "<parent xmlns:t=\"urn:example:types:r1\">\n" +
                "    <level1 name=\"t:DateTime\" />\n" +
                "</parent>";

        Parent bean = MAPPER.readValue(xml, Parent.class);

        assertEquals("{urn:example:types:r1}DateTime", bean.level1.name.toString());
    }
}
