package com.fasterxml.jackson.dataformat.xml.incr;

import java.io.*;

import javax.xml.stream.XMLStreamWriter;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class IncrementalWritingTest extends XmlTestBase
{
    private final XmlMapper MAPPER = xmlMapper(true);
    
    public void testSimple() throws Exception
    {
        StringWriter strw = new StringWriter();
        XMLStreamWriter sw = MAPPER.getFactory().getXMLOutputFactory().createXMLStreamWriter(strw);
        sw.writeStartElement("root");

        MAPPER.writeValue(sw, new NameBean(13, "Grizabella", "Glamour"));
        MAPPER.writeValue(sw, new NameBean(17, "Growl", "Tiger"));

        sw.writeEndElement();
        sw.writeEndDocument();
        sw.close();

        String xml = strw.toString().trim();

        assertEquals("<root>"
                +"<NameBean age=\"13\"><first>Grizabella</first><last>Glamour</last></NameBean>"
                +"<NameBean age=\"17\"><first>Growl</first><last>Tiger</last></NameBean></root>",
                xml);
    }
}
