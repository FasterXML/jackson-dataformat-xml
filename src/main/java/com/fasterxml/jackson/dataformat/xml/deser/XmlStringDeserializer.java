package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.IOException;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

/**
 * Custom variant used instead of "plain" {@code StringDeserializer} to handle
 * couple of edge cases that XML parser exposes.
 *<p>
 * NOTE: mostly copy-pasted from standard {@code StringDeserializer}
 *
 * @since 2.9.4
 */
public class XmlStringDeserializer
    extends StdScalarDeserializer<String>
{
    private static final long serialVersionUID = 1L;

    public XmlStringDeserializer() { super(String.class); }

    @Override
    public boolean isCachable() { return true; }

    @Override
    public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
        return "";
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        if (p.hasToken(JsonToken.VALUE_STRING)) {
            return p.getText();
        }
        JsonToken t = p.getCurrentToken();
        if (t == JsonToken.START_ARRAY) {
            return _deserializeFromArray(p, ctxt);
        }
        if (t == JsonToken.VALUE_EMBEDDED_OBJECT) {
            Object ob = p.getEmbeddedObject();
            if (ob == null) {
                return null;
            }
            if (ob instanceof byte[]) {
                return ctxt.getBase64Variant().encode((byte[]) ob, false);
            }
            // otherwise, try conversion using toString()...
            return ob.toString();
        }
        // allow coercions, as handled by `FromXmlParser.getValueAsString()`: this includes
        // START_OBJECT in some cases.
        String text = p.getValueAsString(null);
        if ((text != null) || (t == JsonToken.VALUE_NULL)) {
            return text;
        }
        return (String) ctxt.handleUnexpectedToken(_valueClass, p);
    }

    // Since we can never have type info ("natural type"; String, Boolean, Integer, Double):
    // (is it an error to even call this version?)
    @Override
    public String deserializeWithType(JsonParser p, DeserializationContext ctxt,
            TypeDeserializer typeDeserializer) throws IOException {
        return deserialize(p, ctxt);
    }
}
