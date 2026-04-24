package tools.jackson.dataformat.xml.ser;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamWriter2;

import tools.jackson.dataformat.xml.util.ArgUtil;

/**
 * Immutable entity representing an XML Declaration to write (e.g.
 * {@code <?xml version="1.0" encoding="ISO-8859-1" standalone="yes"?>}).
 *<p>
 * Normal entry point is {@link XmlGeneratorInitializer#addXmlDeclaration(String, String)}
 * (and its overloads) rather than constructing this class directly.
 *
 * @since 3.2
 */
public class XmlDeclaration
    implements XmlGeneratorWritable
{
    private final String _version;

    private final String _encoding;

    private final Boolean _standalone;

    /**
     * @param version XML version: must be non-null, typically "1.0" or "1.1"
     * @param encoding Encoding to declare; may be {@code null} (or empty, treated
     *   as {@code null}) to omit the encoding pseudo-attribute.
     *   Note: when {@code standalone} is non-null, a null {@code encoding} will
     *   be silently substituted with "UTF-8" since the underlying Stax
     *   {@code writeStartDocument(version, encoding, standalone)} overload
     *   requires a non-null encoding.
     * @param standalone {@code standalone} pseudo-attribute value; {@code null}
     *   means omit the attribute
     */
    public XmlDeclaration(String version, String encoding, Boolean standalone) {
        _version = ArgUtil.nonEmptyNonNull("version", version);
        _encoding = ArgUtil.emptyToNull(encoding);
        _standalone = standalone;
    }

    @Override
    public void write(ToXmlGenerator xmlGen, XMLStreamWriter2 sw) throws XMLStreamException {
        // NOTE: Stax `writeStartDocument` overloads use differing argument
        // orders -- 2-arg form is (encoding, version) but 3-arg standalone
        // form is (version, encoding, standAlone).
        if (_standalone == null) {
            if (_encoding == null) {
                sw.writeStartDocument(_version);
            } else {
                sw.writeStartDocument(_encoding, _version);
            }
        } else {
            String encoding = (_encoding == null) ? "UTF-8" : _encoding;
            sw.writeStartDocument(_version, encoding, _standalone.booleanValue());
        }
    }
}
