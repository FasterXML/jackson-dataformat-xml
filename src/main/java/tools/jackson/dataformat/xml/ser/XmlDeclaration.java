package tools.jackson.dataformat.xml.ser;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamWriter2;

import tools.jackson.dataformat.xml.util.ArgUtil;

/**
 * Entity representing XML Declaration to write.
 *
 * @since 3.2
 */
public class XmlDeclaration
    implements XmlGeneratorWritable
{
    private final String _version;

    private final String _encoding;

    private final Boolean _standalone;

    public XmlDeclaration(String version, String encoding, Boolean standalone) {
        _version = ArgUtil.nonEmptyNonNull("version", version);
        _encoding = ArgUtil.emptyToNull(encoding);
        _standalone = standalone;
    }

    @Override
    public void write(ToXmlGenerator xmlGen, XMLStreamWriter2 sw) throws XMLStreamException {
        if (_standalone == null) {
            if (_encoding == null) {
                sw.writeStartDocument(_version);
            } else {
                sw.writeStartDocument(_version, _encoding);
            }
        } else {
            String encoding = (_encoding == null) ? "UTF-8" : _encoding;
            sw.writeStartDocument(_version, encoding, _standalone.booleanValue());
        }
    }
}
