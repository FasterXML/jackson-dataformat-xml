package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.CacheProvider;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.DeserializerCache;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;

/**
 * XML-specific {@link DeserializationContext} needed to override certain
 * handlers.
 *
 * @since 2.12
 */
public class XmlDeserializationContext
    extends DefaultDeserializationContext
{
    private static final long serialVersionUID = 1L;

    /*
    /**********************************************************
    /* Life-cycle methods
    /**********************************************************
     */

    /**
     * Default constructor for a blueprint object, which will use the standard
     * {@link DeserializerCache}, given factory.
     */
    public XmlDeserializationContext(DeserializerFactory df) {
        // 04-Sep-2023, tatu: Not ideal (wrt not going via CacheProvider) but
        //     has to do for backwards compatibility:
        super(df, new DeserializerCache());
    }

    private XmlDeserializationContext(XmlDeserializationContext src,
            DeserializationConfig config, JsonParser p, InjectableValues values) {
        super(src, config, p, values);
    }

    private XmlDeserializationContext(XmlDeserializationContext src) { super(src); }

    private XmlDeserializationContext(XmlDeserializationContext src, DeserializerFactory factory) {
        super(src, factory);
    }

    private XmlDeserializationContext(XmlDeserializationContext src, DeserializationConfig config) {
        super(src, config);
    }

    // @since 2.16
    private XmlDeserializationContext(XmlDeserializationContext src, CacheProvider cp) {
        super(src, cp);
    }

    @Override
    public XmlDeserializationContext copy() {
        return new XmlDeserializationContext(this);
    }

    @Override
    public DefaultDeserializationContext createInstance(DeserializationConfig config,
            JsonParser p, InjectableValues values) {
        return new XmlDeserializationContext(this, config, p, values);
    }

    @Override
    public DefaultDeserializationContext createDummyInstance(DeserializationConfig config) {
        // need to be careful to create non-blue-print instance
        return new XmlDeserializationContext(this, config);
    }

    @Override
    public DefaultDeserializationContext with(DeserializerFactory factory) {
        return new XmlDeserializationContext(this, factory);
    }

    @Override
    public DefaultDeserializationContext withCaches(CacheProvider cp) {
        return new XmlDeserializationContext(this, cp);
    }

    /*
    /**********************************************************
    /* Overrides we need
    /**********************************************************
     */

    @Override // since 2.12
    public Object readRootValue(JsonParser p, JavaType valueType,
            JsonDeserializer<Object> deser, Object valueToUpdate)
        throws IOException
    {
        // 18-Sep-2021, tatu: Complicated mess; with 2.12, had [dataformat-xml#374]
        //    to disable handling. With 2.13, via [dataformat-xml#485] undid this change
        try {
            if (_config.useRootWrapping()) {
                return _unwrapAndDeserialize(p, valueType, deser, valueToUpdate);
            }
            if (valueToUpdate == null) {
                return deser.deserialize(p, this);
            }
            return deser.deserialize(p, this, valueToUpdate);
        } catch (IndexOutOfBoundsException e) {
            // If value is invalid without end character, the deserialize will
            //     read pass the array bound and throws IndexOutOfBoundException
            throw new JsonParseException(p, "Invalid value with missing JsonToken.END_OBJECT.", e);
        }
    }

    // To support case where XML element has attributes as well as CDATA, need
    // to "extract" scalar value (CDATA), after the fact
    @Override // since 2.12
    public String extractScalarFromObject(JsonParser p, JsonDeserializer<?> deser,
            Class<?> scalarType)
        throws IOException
    {
        // Only called on START_OBJECT, should not need to check, but JsonParser we
        // get may or may not be `FromXmlParser` so traverse using regular means
        String text = "";

        while (p.nextToken() == JsonToken.FIELD_NAME) {
            // Couple of ways to find "real" textual content. One is to look for
            // "XmlText"... but for that would need to know configuration. Alternatively
            // could hold on to last text seen -- but this might be last attribute, for
            // empty element. So for now let's simply hard-code check for empty String
            // as marker and hope for best
            final String propName = p.currentName();
            JsonToken t = p.nextToken();
            if (t == JsonToken.VALUE_STRING) {
                if (propName.equals("")) {
                    text = p.getText();
                }
            } else {
                p.skipChildren();
            }
        }
        return text;
    }

}
