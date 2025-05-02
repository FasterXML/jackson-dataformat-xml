package tools.jackson.dataformat.xml;

import tools.jackson.databind.PropertyName;
import tools.jackson.dataformat.xml.deser.FromXmlParser;

import java.io.Serializable;

public record JacksonXmlAnnotationIntrospectorConfig(
        boolean inferXmlTextPropertyName,
        PropertyName xmlTextPropertyName //Only honored if inferXmlTextPropertyName is false
) implements Serializable {

    /**
     * Constructs a JacksonXmlAnnotationIntrospectorConfig with the default configuration
     * Does not infer the XmlTextPropertyName by default and uses {@link FromXmlParser#DEFAULT_TEXT_PROPERTY} for the {@link PropertyName}.
     */
    public JacksonXmlAnnotationIntrospectorConfig() {
        this(false, PropertyName.construct(FromXmlParser.DEFAULT_TEXT_PROPERTY));
    }

    public JacksonXmlAnnotationIntrospectorConfig withInferXmlTextPropertyName(boolean inferXmlTextPropertyName) {
        return new JacksonXmlAnnotationIntrospectorConfig(inferXmlTextPropertyName, this.xmlTextPropertyName);
    }

    public JacksonXmlAnnotationIntrospectorConfig withXmlTextPropertyName(PropertyName xmlTextPropertyName) {
        return new JacksonXmlAnnotationIntrospectorConfig(this.inferXmlTextPropertyName, xmlTextPropertyName);
    }
}
