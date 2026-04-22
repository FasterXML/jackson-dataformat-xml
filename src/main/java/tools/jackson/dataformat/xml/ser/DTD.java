package tools.jackson.dataformat.xml.ser;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamWriter2;

import tools.jackson.dataformat.xml.util.ArgUtil;

/**
 * Value container to represent XML Document Type Declaration,
 * to be written using {@link XmlGeneratorInitializer}.
 *
 * @since 3.2
 */
public record DTD(String rootName,
        String systemId, String publicId,
        String internalSubset)
    implements PrologDirective
{
    public DTD {
        rootName = ArgUtil.nonEmptyNonNull("rootName", rootName);
        systemId = ArgUtil.emptyToNull(systemId);
        publicId = ArgUtil.emptyToNull(publicId);
        internalSubset = ArgUtil.emptyToNull(internalSubset);
    }

    @Override
    public void write(ToXmlGenerator xmlGen, XMLStreamWriter2 xmlWriter)
        throws XMLStreamException
    {
        xmlWriter.writeDTD(rootName(), systemId(), publicId(), internalSubset());
    }
}
