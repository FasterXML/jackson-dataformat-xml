package com.fasterxml.jackson.xml;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;

import com.fasterxml.jackson.xml.ser.XmlBeanSerializerModifier;

/**
 * Module that implements most functionality needed to support producing and
 * consuming XML instead of JSON.
 */
public class JacksonXmlModule extends SimpleModule
{
    private final static AnnotationIntrospector XML_ANNOTATION_INTROSPECTOR = new JacksonXmlAnnotationIntrospector();
    
    public JacksonXmlModule()
    {
        super("JackxonXmlModule", ModuleVersion.instance.version());
    }
    
    @Override
    public void setupModule(SetupContext context)
    {
        // Need to modify BeanSerializer that is used
        context.addBeanSerializerModifier(new XmlBeanSerializerModifier());
        // as well as AnnotationIntrospector
        context.insertAnnotationIntrospector(XML_ANNOTATION_INTROSPECTOR);
    }    
}
