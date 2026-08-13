package tools.jackson.dataformat.xml.ser;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamWriter2;

/**
 * Common API for XML entities to write via {@link ToXmlGenerator}.
 *
 * @since 3.2
 */
public interface XmlGeneratorWritable
{
    /**
     * Method to call to actually write out the entity using given
     * {@link XMLStreamWriter2}. {@link ToXmlGenerator} is only passed
     * in case access to configuration was needed.
     *
     * @param xmlGen Generator that called this method: MUST NOT call
     *   its output methods, only to be used for configuration access
     * @param sw Writer to use for actual output of XML event
     */
    public void write(ToXmlGenerator xmlGen, XMLStreamWriter2 sw)
        throws XMLStreamException;
}
