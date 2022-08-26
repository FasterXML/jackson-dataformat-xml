package tools.jackson.dataformat.xml.deser;

import javax.xml.namespace.QName;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class QNameDeserializer extends ValueDeserializer {
    ValueDeserializer<?> originalDeserializer;
    public QNameDeserializer(ValueDeserializer<?> deserializer) {
        originalDeserializer = deserializer;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
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
