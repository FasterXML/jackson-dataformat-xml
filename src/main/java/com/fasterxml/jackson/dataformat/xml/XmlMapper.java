package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.dataformat.xml.ser.XmlSerializerProvider;
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;


/**
 * Customized {@link ObjectMapper} that will read and write XML instead of JSON,
 * using XML-backed {@link com.fasterxml.jackson.core.JsonFactory}
 * implementation ({@link XmlFactory}).
 *<p>
 * Mapper itself overrides some aspects of functionality to try to handle
 * data binding aspects as similar to JAXB as possible.
 */
public class XmlMapper extends ObjectMapper
{
    private static final long serialVersionUID = -724333029147285918L;

    protected final static JacksonXmlModule DEFAULT_XML_MODULE = new JacksonXmlModule();
    
    // need to hold on to module instance just in case copy() is used
    protected final JacksonXmlModule _xmlModule;
    
    /*
    /**********************************************************
    /* Life-cycle: construction, configuration
    /**********************************************************
     */

    public XmlMapper() {
        this(new XmlFactory());
    }
    
    public XmlMapper(XmlFactory xmlFactory) {
        this(xmlFactory, DEFAULT_XML_MODULE);
    }
    
    public XmlMapper(JacksonXmlModule module)
    {
        this(new XmlFactory(), module);
    }

    public XmlMapper(XmlFactory xmlFactory, JacksonXmlModule module)
    {
        /* Need to override serializer provider (due to root name handling);
         * deserializer provider fine as is
         */
        super(xmlFactory, new XmlSerializerProvider(new XmlRootNameLookup()), null);
        _xmlModule = module;
        // but all the rest is done via Module interface!
        if (module != null) {
            registerModule(module);
        }
    }
    
    // @since 2.1
    @Override
    public XmlMapper copy()
    {
        _checkInvalidCopy(XmlMapper.class);
        return new XmlMapper((XmlFactory) _jsonFactory.copy(), _xmlModule);
    }
    
    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    /*
    /**********************************************************
    /* Additional XML-specific configurations
    /**********************************************************
     */

    /**
     * Method called by {@link JacksonXmlModule} to pass configuration
     * information to {@link XmlFactory}, during registration.
     * 
     * @since 2.1
     */
    protected void setXMLTextElementName(String name)
    {
        ((XmlFactory) _jsonFactory).setXMLTextElementName(name);
    }
    
    /*
    /**********************************************************
    /* Access to configuration settings
    /**********************************************************
     */

    /**
     * @deprecated Since 2.1, use {@link #getFactory} instead
     */
    @Override
    @Deprecated
    public XmlFactory getJsonFactory() {
        return (XmlFactory) _jsonFactory;
    }

    @Override
    public XmlFactory getFactory() {
        return (XmlFactory) _jsonFactory;
    }
    
    public ObjectMapper configure(ToXmlGenerator.Feature f, boolean state) {
        ((XmlFactory)_jsonFactory).configure(f, state);
        return this;
    }

    public ObjectMapper configure(FromXmlParser.Feature f, boolean state) {
        ((XmlFactory)_jsonFactory).configure(f, state);
        return this;
    }

    public ObjectMapper enable(ToXmlGenerator.Feature f) {
        ((XmlFactory)_jsonFactory).enable(f);
        return this;
    }

    public ObjectMapper enable(FromXmlParser.Feature f) {
        ((XmlFactory)_jsonFactory).enable(f);
        return this;
    }

    public ObjectMapper disable(ToXmlGenerator.Feature f) {
        ((XmlFactory)_jsonFactory).disable(f);
        return this;
    }

    public ObjectMapper disable(FromXmlParser.Feature f) {
        ((XmlFactory)_jsonFactory).disable(f);
        return this;
    }
    
    /*
    /**********************************************************
    /* Overridden methods
    /**********************************************************
     */

    /**
     * XML indentation differs from JSON indentation, thereby
     * need to change default pretty-printer
     */
    @Override
    protected PrettyPrinter _defaultPrettyPrinter() {
        return new DefaultXmlPrettyPrinter();
    }
}
