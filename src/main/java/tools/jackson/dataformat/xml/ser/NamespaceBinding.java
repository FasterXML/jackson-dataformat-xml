package tools.jackson.dataformat.xml.ser;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamWriter2;

import tools.jackson.dataformat.xml.util.ArgUtil;

/**
 * Entity representing binding from prefix to namespace URI, used
 * to determine prefix to use for given namespace URI (and dynamically
 * adding necessary declarations)
 *
 * @since 3.2
 */
public class NamespaceBinding
    implements XmlGeneratorWritable
{
    private final String _prefix;

    private final String _namespaceURI;

    public NamespaceBinding(String prefix, String namespaceURI) {
        _prefix = ArgUtil.emptyToNull(prefix);
        _namespaceURI = ArgUtil.nonEmptyNonNull("namespaceURI", namespaceURI);
    }

    @Override
    public void write(ToXmlGenerator xmlGen, XMLStreamWriter2 sw) throws XMLStreamException {
        if (_prefix == null) {
            sw.setDefaultNamespace(_namespaceURI);
        } else {
            sw.setPrefix(_prefix, _namespaceURI);
        }
    }
}
