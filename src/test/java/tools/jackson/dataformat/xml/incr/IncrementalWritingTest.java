package tools.jackson.dataformat.xml.incr;

import java.io.*;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.*;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import static org.junit.jupiter.api.Assertions.*;


public class IncrementalWritingTest extends XmlTestUtil
{
    private final XmlMapper MAPPER = xmlMapper(true);
    
    @Test
    public void testSimple() throws Exception
    {
        StringWriter strw = new StringWriter();
        XMLStreamWriter sw = MAPPER.tokenStreamFactory().getXMLOutputFactory().createXMLStreamWriter(strw);
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

    // @since 2.17
    @Test
    public void testWriteUsingXMLStreamWriter() throws Exception
    {
        XMLOutputFactory staxF = MAPPER.tokenStreamFactory().getXMLOutputFactory();
        final Point p = new Point(1, 2);

        // Serialize first using convenience method
        try (StringWriter w = new StringWriter()) {
            XMLStreamWriter sw = staxF.createXMLStreamWriter(w);
            MAPPER.writeValue(sw, p);
            assertEquals("<Point><x>1</x><y>2</y></Point>", w.toString());
        }

        // and then by explicit XMLStreamWriter
        try (StringWriter w = new StringWriter()) {
            XMLStreamWriter sw = staxF.createXMLStreamWriter(w);
            sw.writeStartDocument("US-ASCII", "1.1");
            try (ToXmlGenerator g = MAPPER.createGenerator(sw)) {
                MAPPER.writeValue(g, p);
                assertEquals("<?xml version='1.1' encoding='US-ASCII'?>"
                        +"<Point><x>1</x><y>2</y></Point>", w.toString());
            }
        }
    }
}
