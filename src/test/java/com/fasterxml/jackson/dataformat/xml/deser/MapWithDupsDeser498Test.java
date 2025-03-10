package com.fasterxml.jackson.dataformat.xml.deser;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapWithDupsDeser498Test extends XmlTestUtil
{
    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    private final XmlMapper XML_MAPPER = newMapper();

    private final ObjectMapper JSON_MAPPER = new JsonMapper();

    // [dataformat-xml#498]
    @Test
    public void testRootLevelMap() throws Exception
    {
        final String xml = "<result>\n"
                + "  <hello>world</hello>\n"
                + "  <lists>1</lists>\n"
                + "  <lists>2</lists>\n"
                + "  <lists></lists>\n"
                + "  <lists>\n"
                + "    <inner>internal</inner>\n"
                + "    <time>123</time>\n"
                + "  </lists>\n"
                + "  <lists>3</lists>\n"
                + "  <lists>test</lists>\n"
                + "  <lists></lists>\n"
                + "  <single>one</single>\n"
                + "</result>";

        Map<?,?> expMap = JSON_MAPPER.readValue(a2q(
"{'hello':'world','lists':['1','2','',"
+"{'inner':'internal','time':'123'},'3','test',''],'single':'one'}"),
                Map.class);

        Map<?,?> map = XML_MAPPER.readValue(xml, Map.class);

// Work around: nominal target type of Object
//        Map<?,?> map = (Map<?,?> ) XML_MAPPER.readValue(xml, Object.class);

        assertEquals(expMap, map);
    }
}
