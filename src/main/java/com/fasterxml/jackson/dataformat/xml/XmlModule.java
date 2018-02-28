package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.core.Version;

import com.fasterxml.jackson.databind.module.SimpleDeserializers;

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
    }
}
