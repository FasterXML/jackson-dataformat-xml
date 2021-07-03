package com.fasterxml.jackson.dataformat.xml;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import com.fasterxml.jackson.core.base.DecorableTSFactory.DecorableTSFBuilder;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

/**
 * {@link com.fasterxml.jackson.core.TokenStreamFactory.TSFBuilder}
 * implementation for constructing {@link XmlFactory}
 * instances.
 *
 * @since 3.0
 */
public class XmlFactoryBuilder extends DecorableTSFBuilder<XmlFactory, XmlFactoryBuilder>
{
    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    /**
     * Set of {@code FromXmlParser.Feature}s enabled, as bitmask.
     */
    protected int _formatParserFeatures;

    /**
     * Set of {@@code ToXmlGenerator.Feature}s enabled, as bitmask.
     */
    protected int _formatGeneratorFeatures;

    /**
     * Stax factory for creating underlying input stream readers;
     * `null` for "use default instance with default settings"
     */
    protected XMLInputFactory _xmlInputFactory;

    /**
     * Stax factory for creating underlying output stream writers;
     * `null` for "use default instance with default settings"
     */
    protected XMLOutputFactory _xmlOutputFactory;

    /**
     * In cases where a start element has both attributes and non-empty textual
     * value, we have to create a bogus property; we will use this as
     * the property name.
     *<p>
     * Name used for pseudo-property used for returning XML Text value (which does
     * not have actual element name to use). Defaults to empty String, but
     * may be changed for inter-operability reasons: JAXB, for example, uses
     * "value" as name.
     */
    protected String _nameForTextElement;

    /*
    /**********************************************************
    /* Life cycle
    /**********************************************************
     */
    
    protected XmlFactoryBuilder() {
        super(XmlFactory.DEFAULT_XML_PARSER_FEATURE_FLAGS,
                XmlFactory.DEFAULT_XML_GENERATOR_FEATURE_FLAGS);
    }

    public XmlFactoryBuilder(XmlFactory base) {
        super(base);
        _xmlInputFactory = base._xmlInputFactory;
        _xmlOutputFactory = base._xmlOutputFactory;
        _nameForTextElement = base._cfgNameForTextElement;
    }

    // // // Accessors

    public String nameForTextElement() { return _nameForTextElement; }

    public XMLInputFactory inputFactory() {
        if (_xmlInputFactory == null) {
            return defaultInputFactory();
        }
        return _xmlInputFactory;
    }

    protected static XMLInputFactory defaultInputFactory() {
        XMLInputFactory xmlIn = XMLInputFactory.newInstance();
        // as per [dataformat-xml#190], disable external entity expansion by default
        xmlIn.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        // and ditto wrt [dataformat-xml#211], SUPPORT_DTD
        xmlIn.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        return xmlIn;
    }

    public XMLOutputFactory outputFactory() {
        if (_xmlOutputFactory == null) {
            return defaultOutputFactory();
        }
        return _xmlOutputFactory;
    }

    protected static XMLOutputFactory defaultOutputFactory() {
        XMLOutputFactory xmlOut = XMLOutputFactory.newInstance();
        // [dataformat-xml#326]: Better ensure namespaces get built properly:
        xmlOut.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
        return xmlOut;
    }

    // // // Parser features

    public XmlFactoryBuilder enable(FromXmlParser.Feature f) {
        _formatParserFeatures |= f.getMask();
        return _this();
    }

    public XmlFactoryBuilder enable(FromXmlParser.Feature first, FromXmlParser.Feature... other) {
        _formatParserFeatures |= first.getMask();
        for (FromXmlParser.Feature f : other) {
            _formatParserFeatures |= f.getMask();
        }
        return _this();
    }

    public XmlFactoryBuilder disable(FromXmlParser.Feature f) {
        _formatParserFeatures &= ~f.getMask();
        return _this();
    }

    public XmlFactoryBuilder disable(FromXmlParser.Feature first, FromXmlParser.Feature... other) {
        _formatParserFeatures &= ~first.getMask();
        for (FromXmlParser.Feature f : other) {
            _formatParserFeatures &= ~f.getMask();
        }
        return _this();
    }

    public XmlFactoryBuilder configure(FromXmlParser.Feature f, boolean state) {
        return state ? enable(f) : disable(f);
    }

    // // // Generator features

    public XmlFactoryBuilder enable(ToXmlGenerator.Feature f) {
        _formatGeneratorFeatures |= f.getMask();
        return _this();
    }

    public XmlFactoryBuilder enable(ToXmlGenerator.Feature first, ToXmlGenerator.Feature... other) {
        _formatGeneratorFeatures |= first.getMask();
        for (ToXmlGenerator.Feature f : other) {
            _formatGeneratorFeatures |= f.getMask();
        }
        return _this();
    }

    public XmlFactoryBuilder disable(ToXmlGenerator.Feature f) {
        _formatGeneratorFeatures &= ~f.getMask();
        return _this();
    }
    
    public XmlFactoryBuilder disable(ToXmlGenerator.Feature first, ToXmlGenerator.Feature... other) {
        _formatGeneratorFeatures &= ~first.getMask();
        for (ToXmlGenerator.Feature f : other) {
            _formatGeneratorFeatures &= ~f.getMask();
        }
        return _this();
    }

    public XmlFactoryBuilder configure(ToXmlGenerator.Feature f, boolean state) {
        return state ? enable(f) : disable(f);
    }

    // // // Other config

    public XmlFactoryBuilder nameForTextElement(String name) {
        _nameForTextElement = name;
        return _this();
    }

    public XmlFactoryBuilder xmlInputFactory(XMLInputFactory xmlIn) {
        _xmlInputFactory = xmlIn;
        return _this();
    }

    public XmlFactoryBuilder xmlOutputFactory(XMLOutputFactory xmlOut) {
        _xmlOutputFactory = xmlOut;
        return _this();
    }

    // // // Actual construction

    @Override
    public XmlFactory build() {
        return new XmlFactory(this);
    }
}
