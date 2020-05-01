package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.core.Version;

import com.fasterxml.jackson.databind.module.SimpleDeserializers;

import com.fasterxml.jackson.dataformat.xml.deser.XmlBeanDeserializerModifier;
import com.fasterxml.jackson.dataformat.xml.deser.XmlBeanInstantiator;
import com.fasterxml.jackson.dataformat.xml.deser.XmlStringDeserializer;
import com.fasterxml.jackson.dataformat.xml.ser.XmlBeanSerializerModifier;

public class XmlModule
    extends com.fasterxml.jackson.databind.Module
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
        // First: special handling for String, to allow "String in Object"
        XmlStringDeserializer deser = new XmlStringDeserializer();
        context.addDeserializers(new SimpleDeserializers()
                .addDeserializer(String.class, deser)
                .addDeserializer(CharSequence.class, deser));
        context.addSerializerModifier(new XmlBeanSerializerModifier());
        // [dataformat-xml#318]:
        context.addValueInstantiators(new XmlBeanInstantiator.Provider());

        // and then bit trickier, need to add a modifier...
        // Need to modify BeanDeserializer, BeanSerializer that are used
        XmlMapper.Builder builder = (XmlMapper.Builder) context.getOwner();
        final String textElemName = builder.nameForTextElement();
        context.addDeserializerModifier(new XmlBeanDeserializerModifier(textElemName));
    }
}
