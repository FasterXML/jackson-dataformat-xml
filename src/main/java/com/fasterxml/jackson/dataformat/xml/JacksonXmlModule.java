package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;

import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.deser.XmlBeanDeserializerModifier;
import com.fasterxml.jackson.dataformat.xml.ser.XmlBeanSerializerModifier;

/**
 * Module that implements most functionality needed to support producing and
 * consuming XML instead of JSON, used by {@link XmlMapper} for registering
 * handlers for XML-specific processing.
 *<p>
 * NOTE: please do NOT register this directly on {@link XmlMapper}: mapper
 * registers an instance (either one explicitly given in constructor, or, if none,
 * one it configures) and attempts to re-register is unlikely to work as
 * you'd expect.
 */
public class JacksonXmlModule
    extends SimpleModule
    implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;

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

    /**
     * Name used for pseudo-property used for returning XML Text value (which does
     * not have actual element name to use). Defaults to empty String, but
     * may be changed for interoperability reasons: JAXB, for example, uses
     * "value" as name.
     * 
     * @since 2.1
     */
    protected String _cfgNameForTextElement = FromXmlParser.DEFAULT_UNNAMED_TEXT_PROPERTY;
    
    /*
    /**********************************************************************
    /* Life-cycle: construction
    /**********************************************************************
     */
    
    public JacksonXmlModule()
    {
        super("JacksonXmlModule", PackageVersion.VERSION);
    }

    @SuppressWarnings("deprecation")
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
    /* Life-cycle: configuration
    /**********************************************************************
     */

    /**
     * Method that can be used to define whether {@link AnnotationIntrospector}
     * we register will use wrapper for indexed (List, array) properties or not,
     * if there are no explicit annotations.
     * See {@link com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper}
     * for details.
     *<p>
     * Note that method MUST be called before registering the module; otherwise change
     * will not have any effect.
     * 
     * @param state Whether to enable or disable "use wrapper for non-annotated List properties"
     * 
     * @since 2.1
     */
    public void setDefaultUseWrapper(boolean state) {
        _cfgDefaultUseWrapper = state;
    }

    /**
     * Method that can be used to define alternate "virtual name" to use
     * for XML CDATA segments; that is, text values. Default name is empty String
     * (""); but some frameworks use other names: JAXB, for example, uses
     * "value".
     *<p>
     * Note that method MUST be called before registering the module; otherwise change
     * will not have any effect.
     * 
     * @param name Virtual name to use when exposing XML character data sections
     * 
     * @since 2.1
     */
    public void setXMLTextElementName(String name) {
        _cfgNameForTextElement = name;
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
