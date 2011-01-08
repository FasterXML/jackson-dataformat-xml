package com.fasterxml.jackson.xml;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.util.VersionUtil;

import com.fasterxml.jackson.xml.deser.FromXmlParser;
import com.fasterxml.jackson.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.xml.ser.XmlSerializerProvider;
import com.fasterxml.jackson.xml.util.XmlRootNameLookup;

/**
 * Customized {@link ObjectMapper} that will read and write XML instead of JSON,
 * using XML-backed {@link JsonFactory} implementation ({@link XmlFactory}).
 *<p>
 * Mapper itself overrides some aspects of functionality to try to handle
 * data binding aspects as similar to JAXB as possible.
 * 
 * @since 1.7
 */
public class XmlMapper extends ObjectMapper
{
    /*
    /**********************************************************
    /* Life-cycle: construction, configuration
    /**********************************************************
     */

    public XmlMapper()
    {
        this(new XmlFactory());
    }
    
    public XmlMapper(XmlFactory xmlFactory)
    {
        /* Need to override serializer provider (due to root name handling);
         * deserializer provider fine as is
         */
        super(xmlFactory, new XmlSerializerProvider(new XmlRootNameLookup()), null);
        // but all the rest is done via Module interface!
        this.registerModule(new JacksonXmlModule());
    }

    /**
     * Method that will return version information stored in and read from jar
     * that contains this class.
     */
    @Override
    public Version version() {
        return VersionUtil.versionFor(getClass());
    }

    /*
    /**********************************************************
    /* Access to configuration settings
    /**********************************************************
     */

    public ObjectMapper configure(ToXmlGenerator.Feature f, boolean state) {
        ((XmlFactory)_jsonFactory).configure(f, state);
        return this;
    }

    public ObjectMapper configure(FromXmlParser.Feature f, boolean state) {
        ((XmlFactory)_jsonFactory).configure(f, state);
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
        return null; // new DefaultPrettyPrinter();
    }
}
