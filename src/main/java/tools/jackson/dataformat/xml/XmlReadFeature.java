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
     * Default setting is {@code false}.
     */
    AUTO_DETECT_XSI_TYPE(false),

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
