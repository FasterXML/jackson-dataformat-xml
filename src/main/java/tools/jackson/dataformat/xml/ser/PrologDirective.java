package tools.jackson.dataformat.xml.ser;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamWriter2;

/**
 * Common API for XML nodes -- Comments, DTDs, Processing Instructions -- to
 * be written in Document Prolog (before actual XML Document (root node),
 * after optional XML Declaration).
 *
 * @since 3.2
 */
public interface PrologDirective
{
    /**
     * Method to call to actually write out the directive using given
     * {@link XMLStreamWriter2}. {@link ToXmlGenerator} is only passed
     * in case access to configuration was needed.
     *
     * @param xmlGen Generator that called this method: MUST NOT call
     *   its output methods, only to be used for configuration access
     *   @param sw Writer to use for actual output of XML event
     */
    public void write(ToXmlGenerator xmlGen, XMLStreamWriter2 sw)
        throws XMLStreamException;
}
