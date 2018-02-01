package com.fasterxml.jackson.dataformat.xml;

import java.io.Closeable;
import java.io.IOException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.dataformat.xml.ser.XmlSerializerProvider;
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;

/**
 * Customized {@link ObjectMapper} that will read and write XML instead of JSON,
 * using XML-backed {@link com.fasterxml.jackson.core.TokenStreamFactory}
 * implementation ({@link XmlFactory}).
 *<p>
 * Mapper itself overrides some aspects of functionality to try to handle
 * data binding aspects as similar to JAXB as possible.
 *<p>
 * Note that most of configuration should be done by pre-constructing
 * {@link JacksonXmlModule} explicitly, instead of relying on default settings.
 */
public class XmlMapper extends ObjectMapper
{
    private static final long serialVersionUID = 1L;

    protected final static JacksonXmlModule DEFAULT_XML_MODULE = new JacksonXmlModule();

    protected final static DefaultXmlPrettyPrinter DEFAULT_XML_PRETTY_PRINTER = new DefaultXmlPrettyPrinter();

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

    public XmlMapper(XMLInputFactory inputF, XMLOutputFactory outF) {
        this(new XmlFactory(inputF, outF));
    }

    public XmlMapper(XMLInputFactory inputF) {
        this(new XmlFactory(inputF));
    }

    public XmlMapper(XmlFactory xmlFactory) {
        this(xmlFactory, DEFAULT_XML_MODULE);
    }

    public XmlMapper(JacksonXmlModule module) {
        this(new XmlFactory(), module);
    }

    public XmlMapper(XmlFactory xmlFactory, JacksonXmlModule module)
    {
        /* Need to override serializer provider (due to root name handling);
         * deserializer provider fine as is
         */
        super(xmlFactory, new XmlSerializerProvider(xmlFactory, new XmlRootNameLookup()), null);
        _xmlModule = module;
        // but all the rest is done via Module interface!
        if (module != null) {
            registerModule(module);
        }
        // 19-May-2015, tatu: Must ensure we use XML-specific indenter
        _serializationConfig = _serializationConfig.withDefaultPrettyPrinter(DEFAULT_XML_PRETTY_PRINTER);
        // 21-Jun-2017, tatu: Seems like there are many cases in XML where ability to coerce empty
        //    String into `null` (where it otherwise is an error) is very useful.
        enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    }

    protected XmlMapper(XmlMapper src) {
        super(src);
        _xmlModule = src._xmlModule;
    }

    @Override
    public XmlMapper copy()
    {
        _checkInvalidCopy(XmlMapper.class);
        return new XmlMapper(this);
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

    @Deprecated
    protected void setXMLTextElementName(String name) {
        ((XmlFactory) _streamFactory).setXMLTextElementName(name);
    }

    public XmlMapper setDefaultUseWrapper(boolean state) {
        // ser and deser configs should usually have the same introspector, so:
        AnnotationIntrospector ai0 = getDeserializationConfig().getAnnotationIntrospector();
        for (AnnotationIntrospector ai : ai0.allIntrospectors()) {
            if (ai instanceof XmlAnnotationIntrospector) {
                ((XmlAnnotationIntrospector) ai).setDefaultUseWrapper(state);
            }
        }
        return this;
    }

    /*
    /**********************************************************
    /* Access to configuration settings
    /**********************************************************
     */

    @Override
    public XmlFactory tokenStreamFactory() {
        return (XmlFactory) _streamFactory;
    }

    /*
    /**********************************************************
    /* XML-specific access
    /**********************************************************
     */

    /**
     * Method for reading a single XML value from given XML-specific input
     * source; useful for incremental data-binding, combining traversal using
     * basic Stax {@link XMLStreamReader} with data-binding by Jackson.
     */
    public <T> T readValue(XMLStreamReader r, Class<T> valueType) throws IOException {
        return readValue(r, _typeFactory.constructType(valueType));
    } 

    /**
     * Method for reading a single XML value from given XML-specific input
     * source; useful for incremental data-binding, combining traversal using
     * basic Stax {@link XMLStreamReader} with data-binding by Jackson.
     */
    public <T> T readValue(XMLStreamReader r, TypeReference<T> valueTypeRef) throws IOException {
        return readValue(r, _typeFactory.constructType(valueTypeRef));
    } 

    /**
     * Method for reading a single XML value from given XML-specific input
     * source; useful for incremental data-binding, combining traversal using
     * basic Stax {@link XMLStreamReader} with data-binding by Jackson.
     */
    @SuppressWarnings("resource")
    public <T> T readValue(XMLStreamReader r, JavaType valueType) throws IOException
    {
        DeserializationContext ctxt = createDeserializationContext();
        FromXmlParser p = tokenStreamFactory().createParser(ctxt, r);
        return super.readValue(p, valueType);
    } 

    /**
     * Method for serializing given value using specific {@link XMLStreamReader}:
     * useful when building large XML files by binding individual items, one at
     * a time.
     */
    @SuppressWarnings("resource")
    public void writeValue(XMLStreamWriter w0, Object value) throws IOException
    {
        // 04-Oct-2017, tatu: Unfortunately can not simply delegate to super-class implementation
        //   because we need the context first...
        
        SerializationConfig config = getSerializationConfig();
        DefaultSerializerProvider prov = _serializerProvider(getSerializationConfig());
        ToXmlGenerator g = tokenStreamFactory().createGenerator(prov, w0);

        if (config.isEnabled(SerializationFeature.CLOSE_CLOSEABLE) && (value instanceof Closeable)) {
            _writeCloseableValue(g, value, config);
        } else {
            _serializerProvider(config).serializeValue(g, value);
            if (config.isEnabled(SerializationFeature.FLUSH_AFTER_WRITE_VALUE)) {
                g.flush();
            }
        }
    }
}
