package com.fasterxml.jackson.dataformat.xml.failing;

import com.ctc.wstx.stax.WstxInputFactory;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import org.junit.Test;

import javax.xml.stream.XMLInputFactory;
import java.util.List;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static javax.xml.stream.XMLInputFactory.IS_NAMESPACE_AWARE;

public class XsiNilPrecedence385Test extends XmlTestBase
{
    protected static class Items {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "item")
        public List<Item> itemList;
    }

    @JacksonXmlRootElement(localName = "Items")
    protected static class Item {
        @JacksonXmlProperty(isAttribute = true)
        public String attr1;
        @JacksonXmlProperty(isAttribute = true)
        public String type;
        @JacksonXmlProperty(isAttribute = true)
        public Integer idx;
        @JacksonXmlText
        public String value;
    }

    private XmlMapper MAPPER;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        XMLInputFactory ifactory = new WstxInputFactory();
        ifactory.setProperty(IS_NAMESPACE_AWARE, true);

        XmlFactory xf = new XmlFactory(ifactory);

        XmlMapper mapper = new XmlMapper(xf); // there are other overloads too
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

        MAPPER = mapper;
    }

    @Test
    public void test() throws Exception
    {
        String xml = "<items xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "<item xsi:nil=\"true\" attr1=\"a\" type=\"est\" idx=\"1\" />\n" +
                "<item attr1=\"b\" type=\"est\" idx=\"2\" xsi:nil=\"true\" />\n" +
                "</items>";
        Items bean = MAPPER.readValue(xml, Items.class);

        assertNotNull(bean);
        assertNotNull(bean.itemList);
        assertEquals(2, bean.itemList.size());
        
        assertEquals("a",bean.itemList.get(0).attr1);
        assertEquals("est",bean.itemList.get(0).type);
        assertEquals(Integer.valueOf("1"),bean.itemList.get(0).idx);

        assertEquals("b",bean.itemList.get(1).attr1);
        assertEquals("est",bean.itemList.get(1).type);
        assertEquals(Integer.valueOf("2"),bean.itemList.get(1).idx);
    }
}