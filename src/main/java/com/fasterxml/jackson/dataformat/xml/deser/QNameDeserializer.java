package com.fasterxml.jackson.dataformat.xml.deser;

import javax.xml.namespace.QName;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;


public class QNameDeserializer extends JsonDeserializer {
    JsonDeserializer<?> originalDeserializer;
    public QNameDeserializer(JsonDeserializer<?> deserializer) {
        originalDeserializer = deserializer;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        QName qName = (QName) originalDeserializer.deserialize(p, ctxt);

        if (qName.getLocalPart().indexOf(":") > 0) {
            String prefix = qName.getLocalPart().split(":")[0];
            String localPart = qName.getLocalPart().split(":")[1];
            String namespace = ((FromXmlParser)ctxt.getParser()).getStaxReader().getNamespaceContext().getNamespaceURI(prefix);

            return new QName(namespace, localPart, prefix);
        }

        return qName;
    }
}
