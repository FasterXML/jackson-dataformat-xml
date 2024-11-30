package tools.jackson.dataformat.xml;

import javax.xml.XMLConstants;

import tools.jackson.core.FormatFeature;

/**
 * Enumeration that defines all togglable extra XML-specific features.
 *<p>
 * NOTE: in Jackson 2.x this was named {@code ToXmlGenerator.Feature}.
 */
public enum XmlWriteFeature implements FormatFeature
{
    /**
     * Feature that controls whether XML declaration should be written before
     * when generator is initialized (true) or not (false)
     */
    WRITE_XML_DECLARATION(false),

    /**
     * Feature that controls whether output should be done as XML 1.1; if so,
     * certain aspects may differ from default (1.0) processing: for example,
     * XML declaration will be automatically added (regardless of setting
     * <code>WRITE_XML_DECLARATION</code>) as this is required for reader to
     * know to use 1.1 compliant handling. XML 1.1 can be used to allow quoted
     * control characters (Ascii codes 0 through 31) as well as additional linefeeds
     * and name characters.
     */
    WRITE_XML_1_1(false),

    /**
     * Feature that controls whether serialization of Java {@code null} values adds
     * XML attribute of `xsi:nil`, as defined by XML Schema (see
     * <a href="https://www.oreilly.com/library/view/xml-in-a/0596007647/re166.html">this article</a>
     * for details) or not.
     * If enabled, `xsi:nil` attribute will be added to the empty element; if disabled,
     * it will not.
     *<p>
     * Feature is disabled by default for backwards compatibility.
     *
     * @since 2.10
     */
    WRITE_NULLS_AS_XSI_NIL(false),

    /**
     * Feature that determines writing of root values of type {@code ObjectNode}
     * ({@code JsonNode} subtype that represents Object content values),
     * regarding XML output.
     * If enabled and {@code ObjectNode} has exactly one entry (key/value pair),
     * then key of that entry is used as the root element name (and value
     * is written as contents. Otherwise (if feature disabled, or if root
     * {@code ObjectNode} has any other number of key/value entries,
     * root element name is determined using normal logic (either explicitly
     * configured, or {@code ObjectNode} otherwise).
     *<p>
     * Default setting is {@code disabled} in Jackson 2.x, for backwards compatibility:
     * likely to be changed in 3.0 to {@code enabled}.
     *
     * @since 2.13
     */
    UNWRAP_ROOT_OBJECT_NODE(false),

    /**
     * Feature that enables automatic conversion of logical property
     * name {@code "xsi:type"} into matching XML name where "type"
     * is the local name and "xsi" prefix is bound to URI
     * {@link XMLConstants#W3C_XML_SCHEMA_INSTANCE_NS_URI},
     * and output is indicated to be done as XML Attribute.
     * This is mostly desirable for Polymorphic handling where it is difficult
     * to specify XML Namespace for type identifier
     *
     * @since 2.17
     */
    AUTO_DETECT_XSI_TYPE(false),

    /**
     * Feature that determines how floating-point infinity values are
     * serialized.
     *<p>
     * By default, {@link Float#POSITIVE_INFINITY} and
     * {@link Double#POSITIVE_INFINITY} are serialized as {@code Infinity},
     * and {@link Float#NEGATIVE_INFINITY} and
     * {@link Double#NEGATIVE_INFINITY} are serialized as
     * {@code -Infinity}. This is the representation that Java normally
     * uses for these values (see {@link Float#toString(float)} and
     * {@link Double#toString(double)}), but JAXB and other XML
     * Schema-conforming readers won't understand it.
     *<p>
     * With this feature enabled, these values are instead serialized as
     * {@code INF} and {@code -INF}, respectively. This is the
     * representation that XML Schema and JAXB use (see the XML Schema
     * primitive types
     * <a href="https://www.w3.org/TR/xmlschema-2/#float"><code>float</code></a>
     * and
     * <a href="https://www.w3.org/TR/xmlschema-2/#double"><code>double</code></a>).
     *<p>
     * When deserializing, Jackson always understands both representations,
     * so there is no corresponding
     * {@link tools.jackson.dataformat.xml.XmlReadFeature}.
     *<p>
     * Feature is disabled by default for backwards compatibility.
     */
    WRITE_XML_SCHEMA_CONFORMING_FLOATS(false),
    ;

    private final boolean _defaultState;
    private final int _mask;

    /**
     * Method that calculates bit set (flags) of all features that
     * are enabled by default.
     */
    public static int collectDefaults()
    {
        int flags = 0;
        for (XmlWriteFeature f : values()) {
            if (f.enabledByDefault()) {
                flags |= f.getMask();
            }
        }
        return flags;
    }

    private XmlWriteFeature(boolean defaultState) {
        _defaultState = defaultState;
        _mask = (1 << ordinal());
    }

    @Override public boolean enabledByDefault() { return _defaultState; }
    @Override public int getMask() { return _mask; }
    @Override public boolean enabledIn(int flags) { return (flags & getMask()) != 0; }
}
