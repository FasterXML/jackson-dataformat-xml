package com.fasterxml.jackson.dataformat.xml.failing;

import javax.xml.stream.*;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.databind.*;

import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

// for [dataformat-xml#326]
public class FailingNamespace326Test extends XmlTestBase
{
    @JacksonXmlRootElement(localName = "new")
    static class Bean {
        @JacksonXmlProperty(isAttribute = true)
        public String source="ECOM";
        public Member member;

        public Bean(int id, String name) {
            this.member=new Member(id, name);
        }

        public Member getMember() {
            return member;
        }
    }

    static class Member {
        private final int id;
        private final String name;

        public Member(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public void testIssue311() throws Exception {
        XMLInputFactory xmlInputFactory = new WstxInputFactory();
        XMLOutputFactory xmlOutputFactory = new WstxOutputFactory();
        
        /* Setting this to true makes the application run but does not write namespace */
        xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, false);
        
        XmlMapper mapper = new XmlMapper(xmlInputFactory);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(System.out);

        startDocument(writer);
        Bean bean=new Bean(1, "Dude");
        mapper.writeValue(writer, bean);
        endDocument(writer);        
    }

    protected void startDocument(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartDocument("utf-8", "1.0");
        writer.writeCharacters("\n");
        writer.setDefaultNamespace("http://eClub.Schemas.ImportContact");
        writer.writeStartElement("contacts");
        writer.writeDefaultNamespace( "http://eClub.Schemas.ImportContact");
        writer.writeCharacters("\n");
    }

    protected void endDocument(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeCharacters("\n");
        writer.writeEndDocument();
    }
}
