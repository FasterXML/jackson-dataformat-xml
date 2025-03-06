package tools.jackson.dataformat.xml;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import tools.jackson.core.ErrorReportConfiguration;
import tools.jackson.core.StreamReadConstraints;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.StreamWriteConstraints;
import tools.jackson.core.base.DecorableTSFactory.DecorableTSFBuilder;

/**
 * {@link tools.jackson.core.TSFBuilder}
 * implementation for constructing {@link XmlFactory} instances.
 */
public class XmlFactoryBuilder extends DecorableTSFBuilder<XmlFactory, XmlFactoryBuilder>
{
    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
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
     */
    protected ClassLoader _classLoaderForStax;

    /**
     * See {@link XmlNameProcessor} and {@link XmlNameProcessors}
     */
    protected XmlNameProcessor _nameProcessor;

    /*
    /**********************************************************************
    /* Life cycle
    /**********************************************************************
     */
    
    protected XmlFactoryBuilder() {
        super(StreamReadConstraints.defaults(),
                StreamWriteConstraints.defaults(),
                ErrorReportConfiguration.defaults(),
                XmlFactory.DEFAULT_XML_PARSER_FEATURE_FLAGS,
                XmlFactory.DEFAULT_XML_GENERATOR_FEATURE_FLAGS);
        _classLoaderForStax = null;
        _nameProcessor = XmlNameProcessors.newPassthroughProcessor();
    }

    public XmlFactoryBuilder(XmlFactory base) {
        super(base);
        _xmlInputFactory = base._xmlInputFactory;
        _xmlOutputFactory = base._xmlOutputFactory;
        _nameForTextElement = base._cfgNameForTextElement;
        _nameProcessor = base._nameProcessor;
        _classLoaderForStax = null;
    }

    // // // Accessors

    public String nameForTextElement() { return _nameForTextElement; }

    public XMLInputFactory xmlInputFactory() {
        if (_xmlInputFactory == null) {
            return defaultXmlInputFactory(_classLoaderForStax);
        }
        return _xmlInputFactory;
    }

    protected XMLInputFactory defaultXmlInputFactory() {
        return defaultXmlInputFactory(staxClassLoader());
    }

    protected static XMLInputFactory defaultXmlInputFactory(ClassLoader cl) {
        // 05-Jul-2021, tatu: as per [dataformat-xml#483], consider ClassLoader
        XMLInputFactory xmlIn;
        try {
            xmlIn = XMLInputFactory.newFactory(XMLInputFactory.class.getName(), cl);
        } catch (FactoryConfigurationError | NoSuchMethodError e) {
            // 24-Oct-2022, tatu: as per [dataformat-xml#550] need extra care
            xmlIn = XMLInputFactory.newFactory();
        }
        // as per [dataformat-xml#190], disable external entity expansion by default
        xmlIn.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        // and ditto wrt [dataformat-xml#211], SUPPORT_DTD
        xmlIn.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        return xmlIn;
    }

    public XMLOutputFactory xmlOutputFactory() {
        if (_xmlOutputFactory == null) {
            return defaultXmlOutputFactory(_classLoaderForStax);
        }
        return _xmlOutputFactory;
    }

    protected XMLOutputFactory defaultXmlOutputFactory() {
        return defaultXmlOutputFactory(staxClassLoader());
    }

    protected static XMLOutputFactory defaultXmlOutputFactory(ClassLoader cl) {
        // 05-Jul-2021, tatu: as per [dataformat-xml#483], consider ClassLoader
        XMLOutputFactory xmlOut;
        try {
            xmlOut = XMLOutputFactory.newFactory(XMLOutputFactory.class.getName(), cl);
        } catch (FactoryConfigurationError | NoSuchMethodError e) {
            // 24-Oct-2022, tatu: as per [dataformat-xml#550] need extra care
            xmlOut = XMLOutputFactory.newFactory();
        }
        // [dataformat-xml#326]: Better ensure namespaces get built properly:
        xmlOut.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
        return xmlOut;
    }

    protected ClassLoader staxClassLoader() {
        return (_classLoaderForStax == null) ?
                getClass().getClassLoader() : _classLoaderForStax;
    }

    public XmlNameProcessor xmlNameProcessor() {
        return _nameProcessor;
    }

    // // // Parser features

    public XmlFactoryBuilder enable(XmlReadFeature f) {
        _formatParserFeatures |= f.getMask();
        return _this();
    }

    public XmlFactoryBuilder enable(XmlReadFeature first, XmlReadFeature... other) {
        _formatParserFeatures |= first.getMask();
        for (XmlReadFeature f : other) {
            _formatParserFeatures |= f.getMask();
        }
        return _this();
    }

    public XmlFactoryBuilder disable(XmlReadFeature f) {
        _formatParserFeatures &= ~f.getMask();
        return _this();
    }

    public XmlFactoryBuilder disable(XmlReadFeature first, XmlReadFeature... other) {
        _formatParserFeatures &= ~first.getMask();
        for (XmlReadFeature f : other) {
            _formatParserFeatures &= ~f.getMask();
        }
        return _this();
    }

    public XmlFactoryBuilder configure(XmlReadFeature f, boolean state) {
        return state ? enable(f) : disable(f);
    }

    // // // Generator features

    public XmlFactoryBuilder enable(XmlWriteFeature f) {
        _formatGeneratorFeatures |= f.getMask();
        return _this();
    }

    public XmlFactoryBuilder enable(XmlWriteFeature first, XmlWriteFeature... other) {
        _formatGeneratorFeatures |= first.getMask();
        for (XmlWriteFeature f : other) {
            _formatGeneratorFeatures |= f.getMask();
        }
        return _this();
    }

    public XmlFactoryBuilder disable(XmlWriteFeature f) {
        _formatGeneratorFeatures &= ~f.getMask();
        return _this();
    }
    
    public XmlFactoryBuilder disable(XmlWriteFeature first, XmlWriteFeature... other) {
        _formatGeneratorFeatures &= ~first.getMask();
        for (XmlWriteFeature f : other) {
            _formatGeneratorFeatures &= ~f.getMask();
        }
        return _this();
    }

    public XmlFactoryBuilder configure(XmlWriteFeature f, boolean state) {
        return state ? enable(f) : disable(f);
    }

    /**
     * The builder returned uses default settings more closely
     * matching the default configs used in Jackson 2.x versions.
     * <p>
     *     This method is still a work in progress and may not yet fully replicate the
     *     default settings of Jackson 2.x.
     * </p>
     */
    @Override
    public XmlFactoryBuilder configureForJackson2() {
        return super.configureForJackson2()
                .disable(XmlWriteFeature.WRITE_NULLS_AS_XSI_NIL)
                .disable(XmlWriteFeature.UNWRAP_ROOT_OBJECT_NODE)
                .disable(XmlWriteFeature.AUTO_DETECT_XSI_TYPE)
                .disable(XmlWriteFeature.WRITE_XML_SCHEMA_CONFORMING_FLOATS);
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

    /**
     * Method that can be used to specific {@link ClassLoader} for creating
     * {@link XMLInputFactory} and {@link XMLOutputFactory} instances if
     * those are not explicitly defined by caller: passed to respective
     * {@code newFactory()} methods.
     *<br>
     * NOTE: recommended approach is to explicitly pass {@link XMLInputFactory}
     * and {@link XMLOutputFactory} methods instead of relying on JDK SPI
     * mechanism.
     */
    public XmlFactoryBuilder staxClassLoader(ClassLoader cl) {
        _classLoaderForStax = cl;
        return _this();
    }

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
