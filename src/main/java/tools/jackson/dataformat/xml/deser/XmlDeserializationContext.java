package tools.jackson.dataformat.xml.deser;

import tools.jackson.core.FormatSchema;
import tools.jackson.core.JacksonException;
import tools.jackson.core.TokenStreamFactory;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.*;
import tools.jackson.databind.deser.DeserializationContextExt;
import tools.jackson.databind.deser.DeserializerCache;
import tools.jackson.databind.deser.DeserializerFactory;

/**
 * XML-specific {@link DeserializationContext} needed to override certain
 * handlers.
 */
public class XmlDeserializationContext
    extends DeserializationContextExt
{
    public XmlDeserializationContext(TokenStreamFactory tsf,
            DeserializerFactory deserializerFactory, DeserializerCache cache,
            DeserializationConfig config, FormatSchema schema,
            InjectableValues values) {
        super(tsf, deserializerFactory, cache,
                config, schema, values);
    }

    /*
    /**********************************************************
    /* Overrides we need
    /**********************************************************
     */

    @Override
    public Object readRootValue(JsonParser p, JavaType valueType,
            ValueDeserializer<Object> deser, Object valueToUpdate)
        throws JacksonException
    {
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
            // empty element. So for now let's simply hard-code check for empty String
            // as marker and hope for best
            final String propName = p.currentName();
            JsonToken t = p.nextToken();
            if (t == JsonToken.VALUE_STRING) {
                if (propName.equals("")) {
                    text = p.getString();
                }
            } else {
                p.skipChildren();
            }
        }
        return text;
    }
}
