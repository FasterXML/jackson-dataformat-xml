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
    /**
     * Determination of whether indexed properties (arrays, Lists) that are not explicitly
     * annotated (with {@link com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper}
     * or equivalent) should default to using implicit wrapper (with same name as property) or not.
     * If enabled, wrapping is used by default; if false, it is not.
     *<p>
     * Note that JAXB annotation introspector always assumes "do not wrap by default".
     * Jackson annotations have different default due to backwards compatibility.
     * 
     * @since 2.1
     */
    protected boolean _cfgDefaultUseWrapper = JacksonXmlAnnotationIntrospector.DEFAULT_USE_WRAPPER;
    
    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */
    
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
        context.insertAnnotationIntrospector(_constructIntrospector());
    }    

    /**
     * Method that can be used to define whether {@link AnnotationIntrospector}
     * we register will use wrapper for indexed (List, array) properties or not,
     * if there are no explicit annotations.
     * See {@link com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper}
     * for details.
     * 
     * @param state Whether to enable or disable "use wrapper for non-annotated List properties"
     */
    public void setDefaultUseWrapper(boolean state) {
        _cfgDefaultUseWrapper = state;
    }
    
    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */
    
    protected AnnotationIntrospector _constructIntrospector()
    {
        return new JacksonXmlAnnotationIntrospector(_cfgDefaultUseWrapper);
    }
}
