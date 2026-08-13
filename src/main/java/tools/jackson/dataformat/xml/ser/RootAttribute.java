package tools.jackson.dataformat.xml.ser;

import java.util.Objects;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamWriter2;

import tools.jackson.dataformat.xml.util.ArgUtil;

/**
 * Value container to represent an attribute to add to the root XML element
 * being written, to be registered via
 * {@link XmlGeneratorInitializer#addRootAttribute(QName, String)}
 * (or its String-name overload).
 *<p>
 * Typical use case is adding XML Schema instance attributes such as
 * {@code xsi:schemaLocation} or {@code xsi:noNamespaceSchemaLocation},
 * but any attribute can be added.
 *<p>
 * NOTE: root attributes are only emitted when the root value being serialized
 * produces a structured (object) start element; scalar root values (e.g.
 * a bare {@code String}) do not currently get root attributes attached.
 *
 * @since 3.2
 */
public record RootAttribute(QName name, String value)
    implements XmlGeneratorWritable
{
    public RootAttribute {
        Objects.requireNonNull(name, "name");
        ArgUtil.nonEmptyNonNull("name.localPart", name.getLocalPart());
        value = ArgUtil.nullToEmpty(value);
    }

    @Override
    public void write(ToXmlGenerator xmlGen, XMLStreamWriter2 sw) throws XMLStreamException {
        final String ns = name.getNamespaceURI();
        if (ns == null || ns.isEmpty()) {
            sw.writeAttribute(name.getLocalPart(), value);
        } else {
            // Use prefix from QName if present; Stax will fall back to a bound
            // prefix when prefix is empty but namespace URI is registered
            // (e.g. via XmlGeneratorInitializer.addNamespace("xsi", ...)).
            sw.writeAttribute(name.getPrefix(), ns, name.getLocalPart(), value);
        }
    }
}
