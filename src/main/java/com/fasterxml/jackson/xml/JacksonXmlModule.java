package com.fasterxml.jackson.xml;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.module.SimpleModule;

import com.fasterxml.jackson.xml.ser.XmlBeanSerializerModifier;

/**
 * Module that implements most functionality needed to support producing and
 * consuming XML instead of JSON.
 */
public class JacksonXmlModule extends SimpleModule
{
    private final static AnnotationIntrospector XML_ANNOTATION_INTROSPECTOR = new JacksonXmlAnnotationIntrospector();

    // !!! TODO: how to externalize version?
    private final static Version VERSION = new Version(0, 1, 0, null);
    
    public JacksonXmlModule()
    {
        super("JackxonXmlModule", VERSION);
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
