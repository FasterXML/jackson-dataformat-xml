package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.deser.XmlBeanDeserializerModifier;
import com.fasterxml.jackson.dataformat.xml.ser.XmlBeanSerializerModifier;


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
        // Need to modify BeanDeserializer, BeanSerializer that are used
        context.addBeanSerializerModifier(new XmlBeanSerializerModifier());
        context.addBeanDeserializerModifier(new XmlBeanDeserializerModifier());

        // as well as AnnotationIntrospector
        context.insertAnnotationIntrospector(XML_ANNOTATION_INTROSPECTOR);
    }    
}
