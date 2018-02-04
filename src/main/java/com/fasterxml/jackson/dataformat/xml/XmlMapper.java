package com.fasterxml.jackson.dataformat.xml;

import java.io.Closeable;
import java.io.IOException;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.deser.XmlBeanDeserializerModifier;
import com.fasterxml.jackson.dataformat.xml.deser.XmlStringDeserializer;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.dataformat.xml.ser.XmlBeanSerializerModifier;
import com.fasterxml.jackson.dataformat.xml.ser.XmlSerializerProvider;
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;

/**
 * Customized {@link ObjectMapper} that will read and write XML instead of JSON,
 * using XML-backed {@link com.fasterxml.jackson.core.TokenStreamFactory}
 * implementation ({@link XmlFactory}), operation on STAX
 * {@link javax.xml.stream.XMLStreamReader}s and
 * {@link javax.xml.stream.XMLStreamWriter}s.
 *<p>
 * Mapper itself overrides some aspects of functionality to try to handle
 * data binding aspects as similar to JAXB as possible.
 */
public class XmlMapper extends ObjectMapper
{
    private static final long serialVersionUID = 1L;

    protected final static DefaultXmlPrettyPrinter DEFAULT_XML_PRETTY_PRINTER = new DefaultXmlPrettyPrinter();

    /**
     * Builder implementation for constructing {@link XmlMapper} instances.
     *
     * @since 3.0
     */
    public static class Builder extends MapperBuilder<XmlMapper, Builder>
    {
        protected boolean _defaultUseWrapper;

        protected String _nameForTextElement;

        /*
        /******************************************************************
        /* Life-cycle
        /******************************************************************
         */

        public Builder(XmlFactory streamFactory) {
            super(streamFactory);
            // 21-Jun-2017, tatu: Seems like there are many cases in XML where ability to coerce empty
            //    String into `null` (where it otherwise is an error) is very useful.
            enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
            _defaultUseWrapper = JacksonXmlAnnotationIntrospector.DEFAULT_USE_WRAPPER;
            _nameForTextElement = FromXmlParser.DEFAULT_UNNAMED_TEXT_PROPERTY;
        }

        @Override
        public XmlMapper build() {
            return new XmlMapper(this);
        }

        /*
        /******************************************************************
        /* Default value overrides
        /******************************************************************
         */
        
        @Override
        protected DefaultSerializerProvider _defaultSerializerProvider() {
            return new XmlSerializerProvider((XmlFactory) _streamFactory, new XmlRootNameLookup());
        }

        /**
         * Overridden to (try to) ensure we use XML-compatible default indenter
         */
        @Override
        protected PrettyPrinter _defaultPrettyPrinter() {
            return DEFAULT_XML_PRETTY_PRINTER;
        }

        /*
        /******************************************************************
        /* XML specific additional config
        /******************************************************************
         */

        public boolean defaultUseWrapper() {
            return _defaultUseWrapper;
        }

        /**
         * Determination of whether indexed properties (arrays, Lists) that are not explicitly
         * annotated (with {@link com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper}
         * or equivalent) should default to using implicit wrapper (with same name as property) or not.
         * If enabled, wrapping is used by default; if false, it is not.
         *<p>
         * Note that JAXB annotation introspector always assumes "do not wrap by default".
         * Jackson annotations have different default due to backwards compatibility.
         */
        public Builder defaultUseWrapper(boolean b) {
            _defaultUseWrapper = b;
            return this;
        }

        public String nameForTextElement() {
            return _nameForTextElement;
        }

        /**
         * Name used for pseudo-property used for returning XML Text value (cdata within
         * element, which does not have actual element name to use) as a named value (since
         * JSON data model just has named values, except for arrays).
         * Defaults to empty String, but may be changed for interoperability reasons:
         * JAXB, for example, uses "value" as name.
         */
        public Builder nameForTextElement(String elem) {
            if (elem == null) {
                elem = "";
            }
            _nameForTextElement = elem;
            return this;
        }
    }

    /*
    /**********************************************************************
    /* Life-cycle: construction 3.0 style
    /**********************************************************************
     */

    public XmlMapper(Builder b)
    {
        super(b);

        // First: special handling for String, to allow "String in Object"
        {
            XmlStringDeserializer deser = new XmlStringDeserializer();
            SimpleDeserializers desers = new SimpleDeserializers()
                    .addDeserializer(String.class, deser)
                    .addDeserializer(CharSequence.class, deser);
            DeserializerFactory df = _deserializationContext.getFactory().withAdditionalDeserializers(desers);
            _deserializationContext = _deserializationContext.with(df);
        }
        final boolean w = b.defaultUseWrapper();
        // as well as AnnotationIntrospector
        {
            JacksonXmlAnnotationIntrospector intr = new JacksonXmlAnnotationIntrospector(w);
            _deserializationConfig = _deserializationConfig.withInsertedAnnotationIntrospector(intr);
            _serializationConfig = _serializationConfig.withInsertedAnnotationIntrospector(intr);
        }

        // Need to modify BeanDeserializer, BeanSerializer that are used
        _serializerFactory = _serializerFactory.withSerializerModifier(new XmlBeanSerializerModifier());
        final String textElemName = b.nameForTextElement();
        {
            XmlBeanDeserializerModifier mod =  new XmlBeanDeserializerModifier(textElemName);
            DeserializerFactory df = _deserializationContext.getFactory().withDeserializerModifier(mod);
            _deserializationContext = _deserializationContext.with(df);
        }
        
        // !!! TODO: 03-Feb-2018, tatu: remove last piece of mutability... 
        if (!FromXmlParser.DEFAULT_UNNAMED_TEXT_PROPERTY.equals(textElemName)) {
            ((XmlFactory) _streamFactory).setXMLTextElementName(textElemName);
        }
    }

    // 03-Feb-2018, tatu: Was needed in 2.x but should NOT be necessary as we construct
    //   introspector with proper settings.
/*
    private XmlMapper setDefaultUseWrapper(boolean state) {
        // ser and deser configs should usually have the same introspector, so:
        AnnotationIntrospector ai0 = getDeserializationConfig().getAnnotationIntrospector();
        for (AnnotationIntrospector ai : ai0.allIntrospectors()) {
            if (ai instanceof XmlAnnotationIntrospector) {
                ((XmlAnnotationIntrospector) ai).setDefaultUseWrapper(state);
            }
        }
        return this;
    }
*/

    public static XmlMapper.Builder xmlBuilder() {
        return new XmlMapper.Builder(new XmlFactory());
    }

    @SuppressWarnings("unchecked")
    public static XmlMapper.Builder builder() {
        return new XmlMapper.Builder(new XmlFactory());
    }

    public static XmlMapper.Builder builder(XmlFactory streamFactory) {
        return new XmlMapper.Builder(streamFactory);
    }

    /*
    /**********************************************************
    /* Life-cycle: construction, legacy
    /**********************************************************
     */

    public XmlMapper() {
        this(new XmlFactory());
    }

    public XmlMapper(XmlFactory xmlFactory)
    {
        this(new Builder(xmlFactory));
        
        /*
        // Need to override serializer provider (due to root name handling);
        // deserializer provider fine as is
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
        */
    }

    /*
    /**********************************************************
    /* Access to configuration settings
    /**********************************************************
     */

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

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
