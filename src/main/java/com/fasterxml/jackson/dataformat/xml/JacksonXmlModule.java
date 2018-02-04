package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.deser.XmlBeanDeserializerModifier;
import com.fasterxml.jackson.dataformat.xml.ser.XmlBeanSerializerModifier;

/**
 * Module that implements most functionality needed to support producing and
 * consuming XML instead of JSON.
 */
public class JacksonXmlModule
    extends SimpleModule
    implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    protected final boolean _cfgDefaultUseWrapper;

    protected String _cfgNameForTextElement;

    /*
    /**********************************************************************
    /* Life-cycle: construction
    /**********************************************************************
     */
    
    public JacksonXmlModule(String name, boolean w)
    {
        super("JacksonXmlModule", PackageVersion.VERSION);
        _cfgNameForTextElement = name;
        _cfgDefaultUseWrapper = w;
    }

    @Override
    public void setupModule(SetupContext context)
    {
        // Need to modify BeanDeserializer, BeanSerializer that are used
        context.addBeanSerializerModifier(new XmlBeanSerializerModifier());
        context.addBeanDeserializerModifier(new XmlBeanDeserializerModifier(_cfgNameForTextElement));

        // as well as AnnotationIntrospector
        context.insertAnnotationIntrospector(_constructIntrospector());

        // and finally inform XmlFactory about overrides, if need be:
        if (_cfgNameForTextElement != FromXmlParser.DEFAULT_UNNAMED_TEXT_PROPERTY) {
            XmlMapper m = (XmlMapper) context.getOwner();
            m.setXMLTextElementName(_cfgNameForTextElement);
        }

        // Usually this would be the first call; but here anything added will
        // be stuff user may has added, so do it afterwards instead.
        super.setupModule(context);
    }    

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    protected AnnotationIntrospector _constructIntrospector() {
        return new JacksonXmlAnnotationIntrospector(_cfgDefaultUseWrapper);
    }
}
