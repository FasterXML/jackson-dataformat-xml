package tools.jackson.dataformat.xml;

import javax.xml.XMLConstants;

import tools.jackson.core.FormatFeature;
import tools.jackson.core.JsonToken;

/**
 * Enumeration that defines all togglable features for XML parsers.
 *<p>
 * NOTE: in Jackson 2.x this was named {@code FromXmlParser.Feature}.
 */
public enum XmlReadFeature implements FormatFeature
{
    /**
     * Feature that enables automatic conversion of incoming "xsi:type"
     * (where "type"  is the local name and "xsi" prefix is bound to URI
     * {@link XMLConstants#W3C_XML_SCHEMA_INSTANCE_NS_URI}),
     * into Jackson simple Property Name of {@code "xsi:type"}.
     * This is usually needed to read content written using
     * matching {@code ToXmlGenerator.Feature#AUTO_DETECT_XSI_TYPE} feature,
     * usually used for Polymorphic handling where it is difficult
     * to specify proper XML Namespace for type identifier.
     *<p>
     * Default setting is {@code true} (was {@code false} in Jackson 2.x).
     */
    AUTO_DETECT_XSI_TYPE(true),

    /**
     * Feature that indicates whether empty XML elements
     * (both empty tags like {@code <tag />} and {@code <tag></tag>}
     * (with no intervening cdata)
     * are exposed as {@link JsonToken#VALUE_NULL}) or not.
     * If they are not
     * returned as `null` tokens, they will be returned as {@link JsonToken#VALUE_STRING}
     * tokens with textual value of "" (empty String).
     *<p>
     * NOTE: in Jackson 2.x, only "true" empty tags were affected, not split ones.
     * With 3.x both cases handled uniformly.
     *<p>
     * Default setting is {@code false}.
     */
    EMPTY_ELEMENT_AS_NULL(false),

    /**
     * Feature that controls whether the name of the root XML element is
     * verified against the expected root name during deserialization. The expected
     * root name is determined from {@code @JsonRootName}, {@code @JacksonXmlRootElement},
     * or the simple class name of the target type (in that priority order).
     *<p>
     * When enabled, a mismatch between the actual root element name and the expected
     * name will result in a {@link tools.jackson.databind.exc.MismatchedInputException}.
     * When disabled (the default), any root element name is accepted.
     * Note that Fully-Qualified Names (FQN) comparison is used: that is, both local name
     * and namespace URI must match.
     *<p>
     * Default setting is {@code false} for backwards-compatibility.
     *
     * @since 3.2
     */
    ENFORCE_ROOT_ELEMENT_NAME(false),

    /**
     * Feature that indicates whether XML Schema Instance attribute
     * {@code xsi:nil} will be processed automatically -- to indicate {@code null}
     * values -- or not.
     * If enabled, {@code xsi:nil} attribute on any XML element will mark such
     * elements as "null values" and any other attributes or child elements they
     * might have to be ignored. If disabled this attribute will be exposed like
     * any other attribute.
     *<p>
     * Default setting is {@code true}.
     */
    PROCESS_XSI_NIL(true),

    /**
     * Feature that controls whether XML Schema Instance (XSI) namespace attributes
     * other than {@code xsi:nil} and {@code xsi:type} (which have their own handling
     * via {@link #PROCESS_XSI_NIL} and {@link #AUTO_DETECT_XSI_TYPE}) are silently
     * skipped during deserialization. Attributes affected include {@code xsi:schemaLocation}
     * and {@code xsi:noNamespaceSchemaLocation}.
     *<p>
     * When enabled, these attributes are ignored and will not cause
     * {@code UnrecognizedPropertyException}. When disabled (default), they are
     * exposed as regular attributes and may require matching POJO properties
     * or {@code DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES} to be disabled.
     *<p>
     * Default setting is {@code false}.
     *
     * @since 3.2
     */
    SKIP_UNKNOWN_XSI_ATTRIBUTES(false),

    /**
     * Feature that, when enabled, exposes the XML root element as an extra
     * outer Object wrapper whose single property is named after the root
     * element's local name. This preserves the root name in the resulting
     * token stream (and therefore in {@code JsonNode}, {@code Map}, etc.),
     * which is otherwise discarded.
     *<p>
     * Example: with this feature enabled,
     *<pre>
     *   &lt;root&gt;&lt;value&gt;3&lt;/value&gt;&lt;/root&gt;
     *</pre>
     * is exposed as token stream equivalent to
     *<pre>
     *   { "root" : { "value" : "3" } }
     *</pre>
     * instead of the default
     *<pre>
     *   { "value" : "3" }
     *</pre>
     * The wrapper is purely a token-stream-level addition; the body is exposed
     * exactly as it would be without wrap. Roots that the parser would otherwise
     * expose as {@code null} ({@code xsi:nil} or, with
     * {@link #EMPTY_ELEMENT_AS_NULL} enabled, empty elements) become
     * {@code { "root" : null }}.
     *<p>
     * Designed to pair with {@link XmlWriteFeature#UNWRAP_ROOT_OBJECT_NODE}
     * to allow lossless round-tripping of root element name via the Tree
     * Model ({@code JsonNode}) and {@code Map} bindings.
     *<p>
     * Notes:
     *<ul>
     * <li>The wrapper key uses the root element's <em>local name only</em>;
     *   namespace URI is not encoded into the key (consistent with how
     *   child element names are exposed throughout this parser). The full
     *   {@link javax.xml.namespace.QName} of the root remains accessible
     *   via {@code FromXmlParser.getRootElementName()}.</li>
     * <li>This feature modifies the token stream, so it affects all
     *   bindings (POJO, {@code Map}, {@code JsonNode}), not just Tree Model.</li>
     *</ul>
     *<p>
     * Default setting is {@code false} for backwards-compatibility.
     *
     * @since 3.2
     */
    WRAP_ROOT_ELEMENT_NAME(false),

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
        for (XmlReadFeature f : values()) {
            if (f.enabledByDefault()) {
                flags |= f.getMask();
            }
        }
        return flags;
    }
    
    private XmlReadFeature(boolean defaultState) {
        _defaultState = defaultState;
        _mask = (1 << ordinal());
    }

    @Override public boolean enabledByDefault() { return _defaultState; }
    @Override public int getMask() { return _mask; }
    @Override public boolean enabledIn(int flags) { return (flags & getMask()) != 0; }
}
