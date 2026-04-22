package tools.jackson.dataformat.xml.deser;

import javax.xml.namespace.QName;

import tools.jackson.core.FormatSchema;
import tools.jackson.core.JacksonException;
import tools.jackson.core.TokenStreamFactory;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.*;
import tools.jackson.databind.deser.DeserializationContextExt;
import tools.jackson.databind.deser.DeserializerCache;
import tools.jackson.databind.deser.DeserializerFactory;
import tools.jackson.databind.util.ClassUtil;
import tools.jackson.databind.util.TokenBuffer;
import tools.jackson.dataformat.xml.XmlFactory;
import tools.jackson.dataformat.xml.XmlReadFeature;
import tools.jackson.dataformat.xml.util.XmlRootNameLookup;

/**
 * XML-specific {@link DeserializationContext} needed to override certain
 * handlers.
 */
public class XmlDeserializationContext
    extends DeserializationContextExt
{
    private final String _xmlTextElementName;

    /**
     * @since 3.2
     */
    protected final XmlRootNameLookup _rootNameLookup;

    public XmlDeserializationContext(TokenStreamFactory tsf,
            DeserializerFactory deserializerFactory, DeserializerCache cache,
            DeserializationConfig config, FormatSchema schema,
            InjectableValues values,
            XmlRootNameLookup rootNameLookup) {
        super(tsf, deserializerFactory, cache,
                config, schema, values);
        _xmlTextElementName = ((XmlFactory) tsf).getXMLTextElementName();
        _rootNameLookup = rootNameLookup;
    }

    /*
    /**********************************************************************
    /* Overrides we need
    /**********************************************************************
     */

    @Override
    public Object readRootValue(JsonParser p, JavaType valueType,
            ValueDeserializer<Object> deser, Object valueToUpdate)
        throws JacksonException
    {
        // [dataformat-xml#247]: Verify root element name if feature enabled
        if (p instanceof FromXmlParser xp
                && xp.isEnabled(XmlReadFeature.ENFORCE_ROOT_ELEMENT_NAME)) {
            _verifyRootElementName(xp, valueType);
        }

        // 18-Sep-2021, tatu: Complicated mess; with 2.12, had [dataformat-xml#374]
        //    to disable handling. With 2.13, via [dataformat-xml#485] undid this change
        if (_config.useRootWrapping()) {
            return _unwrapAndDeserialize(p, valueType, deser, valueToUpdate);
        }
        if (valueToUpdate == null) {
            return deser.deserialize(p, this);
        }
        return deser.deserialize(p, this, valueToUpdate);
    }

    // To support case where XML element has attributes as well as CDATA, need
    // to "extract" scalar value (CDATA), after the fact
    @Override
    public String extractScalarFromObject(JsonParser p, ValueDeserializer<?> deser,
            Class<?> scalarType)
        throws JacksonException
    {
        // Only called on START_OBJECT, should not need to check, but JsonParser we
        // get may or may not be `FromXmlParser` so traverse using regular means
        String text = "";

        while (p.nextToken() == JsonToken.PROPERTY_NAME) {
            // Couple of ways to find "real" textual content. One is to look for
            // "XmlText"... but for that would need to know configuration. Alternatively
            // could hold on to last text seen -- but this might be last attribute, for
            // empty element. So for now let's simply hard-code check for expected
            // "text element" marker/placeholder and hope for best
            final String propName = p.currentName();
            JsonToken t = p.nextToken();
            if (t == JsonToken.VALUE_STRING) {
                if (propName.equals(_xmlTextElementName)) {
                    text = p.getString();
                }
            } else {
                p.skipChildren();
            }
        }
        return text;
    }

    /**
     * Override to return XML-aware {@link XmlTokenBuffer} that produces
     * parsers implementing {@link ElementWrappable}, allowing virtual wrapping
     * to be configured even after content has been buffered (e.g., during
     * polymorphic type resolution).
     *
     * @since 3.2
     */
    @Override
    public TokenBuffer bufferForInputBuffering(JsonParser p) {
        return XmlTokenBuffer.xmlBufferForInputBuffering(p, this);
    }

    /*
    /**********************************************************************
    /* Internal helper methods
    /**********************************************************************
     */

    /**
     * Helper method for [dataformat-xml#247]: verify that the root element name
     * matches the expected name when {@link XmlReadFeature#ENFORCE_ROOT_ELEMENT_NAME}
     * is enabled.
     *
     * @since 3.2
     */
    protected void _verifyRootElementName(FromXmlParser xp, JavaType valueType)
        throws JacksonException
    {
        QName rootName = xp.getRootElementName();
        if (rootName == null) {
            return;
        }
        String actualName = rootName.getLocalPart();
        QName expectedQName = _rootNameLookup.findRootName(this, valueType);
        String expectedName = expectedQName.getLocalPart();

        if (!expectedName.equals(actualName)) {
            reportPropertyInputMismatch(valueType, actualName,
                    "Root name \"%s\" does not match expected (\"%s\") for type %s",
                    actualName, expectedName, ClassUtil.getTypeDescription(valueType));
        }

        // Also verify namespace URI: must match both ways (unexpected namespace
        // present, or expected namespace missing)
        String expectedNs = expectedQName.getNamespaceURI();
        String actualNs = rootName.getNamespaceURI();
        boolean expectedEmpty = (expectedNs == null || expectedNs.isEmpty());
        boolean actualEmpty = (actualNs == null || actualNs.isEmpty());

        if (expectedEmpty != actualEmpty || (!expectedEmpty && !expectedNs.equals(actualNs))) {
            reportPropertyInputMismatch(valueType, actualName,
                    "Root namespace \"%s\" does not match expected (\"%s\") for type %s",
                    _nsDesc(actualNs), _nsDesc(expectedNs),
                    ClassUtil.getTypeDescription(valueType));
        }
    }

    private static String _nsDesc(String ns) {
        return (ns == null || ns.isEmpty()) ? "" : ns;
    }
}
