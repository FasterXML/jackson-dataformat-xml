package tools.jackson.dataformat.xml.ser;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamWriter2;

import tools.jackson.dataformat.xml.util.ArgUtil;

/**
 * Value container to represent XML Comment within "prolog"
 * part of the Document (before XML Root element, after XML
 * declaration if one written),
 * to be written using {@link XmlGeneratorInitializer}.
 *
 * @since 3.2
 */
public record Comment(String content)
    implements XmlPrologDirective
{
    public Comment {
        content = ArgUtil.nullToEmpty(content);
    }

    @Override
    public void write(ToXmlGenerator xmlGen, XMLStreamWriter2 sw) throws XMLStreamException {
        sw.writeComment(content);
    }
}
