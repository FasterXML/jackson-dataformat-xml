package com.fasterxml.jackson.dataformat.xml;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import com.fasterxml.jackson.core.TSFBuilder;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;

/**
 * {@link com.fasterxml.jackson.core.TSFBuilder} implementation
 * for constructing {@link XmlFactory} instances.
 */
public class XmlFactoryBuilder extends TSFBuilder<XmlFactory, XmlFactoryBuilder>
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
     * may be changed for interoperability reasons: JAXB, for example, uses
     * "value" as name.
     */
    protected String _nameForTextElement;

    /**
     * Optional {@link ClassLoader} to use for constructing
     * {@link XMLInputFactory} and {@kink XMLOutputFactory} instances if
     * not explicitly specified by caller. If not specified, will
     * default to {@link ClassLoader} that loaded this class.
     *
     * @since 2.13
     */
    protected ClassLoader _classLoaderForStax;

    /**
     * See {@link XmlNameProcessor} and {@link XmlNameProcessors}
     *
     * @since 2.14
     */
    protected XmlNameProcessor _nameProcessor;

    /*
    /**********************************************************
    /* Life cycle
    /**********************************************************
     */
    
    protected XmlFactoryBuilder() {
        _formatParserFeatures = XmlFactory.DEFAULT_XML_PARSER_FEATURE_FLAGS;
        _formatGeneratorFeatures = XmlFactory.DEFAULT_XML_GENERATOR_FEATURE_FLAGS;
        _classLoaderForStax = null;
        _nameProcessor = XmlNameProcessors.newPassthroughProcessor();
    }

    public XmlFactoryBuilder(XmlFactory base) {
        super(base);
        _formatParserFeatures = base._xmlParserFeatures;
        _formatGeneratorFeatures = base._xmlGeneratorFeatures;
        _xmlInputFactory = base._xmlInputFactory;
        _xmlOutputFactory = base._xmlOutputFactory;
        _nameForTextElement = base._cfgNameForTextElement;
        _nameProcessor = base._nameProcessor;
        _classLoaderForStax = null;
    }

    // // // Accessors

    public int formatParserFeaturesMask() { return _formatParserFeatures; }
    public int formatGeneratorFeaturesMask() { return _formatGeneratorFeatures; }

    public String nameForTextElement() { return _nameForTextElement; }

    public XMLInputFactory xmlInputFactory() {
        if (_xmlInputFactory == null) {
            return defaultInputFactory();
        }
        return _xmlInputFactory;
    }

    protected XMLInputFactory defaultInputFactory() {
        XMLInputFactory xmlIn = StaxUtil.defaultInputFactory(_classLoaderForStax);
        // as per [dataformat-xml#190], disable external entity expansion by default
        xmlIn.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        // and ditto wrt [dataformat-xml#211], SUPPORT_DTD
        xmlIn.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        return xmlIn;
    }

    public XMLOutputFactory xmlOutputFactory() {
        if (_xmlOutputFactory == null) {
            return defaultOutputFactory();
        }
        return _xmlOutputFactory;
    }

    protected XMLOutputFactory defaultOutputFactory() {
        XMLOutputFactory xmlOut = StaxUtil.defaultOutputFactory(_classLoaderForStax);
        // [dataformat-xml#326]: Better ensure namespaces get built properly:
        xmlOut.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
        return xmlOut;
    }

    // @since 2.13
    protected ClassLoader staxClassLoader() {
        return (_classLoaderForStax == null) ?
                getClass().getClassLoader() : _classLoaderForStax;
    }

    public XmlNameProcessor xmlNameProcessor() {
        return _nameProcessor;
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

    /**
     * @since 2.13 (was misnamed as {@code inputFactory(in) formerly})
     */
    public XmlFactoryBuilder xmlInputFactory(XMLInputFactory xmlIn) {
        _xmlInputFactory = xmlIn;
        return _this();
    }

    /**
     * @since 2.13 (was misnamed as {@code outputFactory(in) formerly})
     */
    public XmlFactoryBuilder xmlOutputFactory(XMLOutputFactory xmlOut)
    {
        _xmlOutputFactory = xmlOut;
        return _this();
    }

    /**
     * @deprecated Since 2.13 use {@link #xmlInputFactory()} instead
     */
    @Deprecated // since 2.13
    public XmlFactoryBuilder inputFactory(XMLInputFactory xmlIn) {
        return xmlInputFactory(xmlIn);
    }

    /**
     * @deprecated Since 2.13 use {@link #xmlOutputFactory()} instead
     */
    @Deprecated // since 2.13
    public XmlFactoryBuilder outputFactory(XMLOutputFactory xmlOut) {
        return xmlOutputFactory(xmlOut);
    }

    /**
     * Method that can be used to specific {@link ClassLoader} for creating
     * {@link XMLInputFactory} and {@link XMLOutputFactory} instances if
     * those are not explicitly defined by caller: passed to respective
     * {@code newFactory()} methods.
     *<br>
     * NOTE: recommended approach is to explicitly pass {@link XMLInputFactory}
     * and {@link XMLOutputFactory} methods instead of relying on JDK SPI
     * mechanism.
     *
     * @since 2.13
     */
    public XmlFactoryBuilder staxClassLoader(ClassLoader cl) {
        _classLoaderForStax = cl;
        return _this();
    }

    /**
     * @since 2.14
     */
    public XmlFactoryBuilder xmlNameProcessor(XmlNameProcessor nameProcessor) {
        _nameProcessor = nameProcessor;
        return _this();
    }
    
    // // // Actual construction

    @Override
    public XmlFactory build() {
        return new XmlFactory(this);
    }
}
