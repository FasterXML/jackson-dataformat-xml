package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.core.Version;

import com.fasterxml.jackson.dataformat.xml.deser.XmlBeanDeserializerModifier;
import com.fasterxml.jackson.dataformat.xml.ser.XmlBeanSerializerModifier;

public class XmlModule
    extends com.fasterxml.jackson.databind.JacksonModule
    implements java.io.Serializable
{
    private static final long serialVersionUID = 3L;

    public XmlModule() { }

    @Override
    public String getModuleName() {
        return getClass().getName();
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    @Override
    public void setupModule(SetupContext context)
    {
        context.addSerializerModifier(new XmlBeanSerializerModifier());

        // and then bit trickier, need to add a modifier...
        // Need to modify BeanDeserializer, BeanSerializer that are used
        XmlMapper.Builder builder = (XmlMapper.Builder) context.getOwner();
        final String textElemName = builder.nameForTextElement();
        context.addDeserializerModifier(new XmlBeanDeserializerModifier(textElemName));
    }
}
