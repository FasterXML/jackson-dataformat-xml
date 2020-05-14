package com.fasterxml.jackson.dataformat.xml;

import java.io.Closeable;
import java.io.IOException;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import com.fasterxml.jackson.databind.cfg.MapperBuilderState;
import com.fasterxml.jackson.databind.cfg.SerializationContexts;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;

import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.dataformat.xml.ser.XmlSerializationContexts;
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter;

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

        public Builder(XmlFactory f) {
            super(f);
            // 21-Jun-2017, tatu: Seems like there are many cases in XML where ability to coerce empty
            //    String into `null` (where it otherwise is an error) is very useful.
            enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
            _defaultUseWrapper = JacksonXmlAnnotationIntrospector.DEFAULT_USE_WRAPPER;
            _nameForTextElement = FromXmlParser.DEFAULT_UNNAMED_TEXT_PROPERTY;

            // as well as AnnotationIntrospector: note, however, that "use wrapper" may well
            // change later on
            annotationIntrospector(new JacksonXmlAnnotationIntrospector(_defaultUseWrapper));
            // Need to mangle Type Ids (to avoid invalid XML Name characters); one way is this:
            typeResolverProvider(new XmlTypeResolverProvider());
            // Some changes easiest to apply via Module
            addModule(new XmlModule());

            // 04-May-2018, tatu: Important! Let's also default `String` `null` handling to coerce
            //   to empty string -- this lets us induce `null` from empty tags first

            // 08-Sep-2019, tatu: This causes [dataformat-xml#359] (follow-up for #354), but
            //    can not simply be removed.

            _configOverrides.findOrCreateOverride(String.class)
                .setNullHandling(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));

            // 13-May-2020, tatu: [dataformat-xml#377] Need to ensure we will keep XML-specific
            //    Base64 default as "MIME" (not MIME-NO-LINEFEEDS), to preserve pre-2.12
            //    behavior
            defaultBase64Variant(Base64Variants.MIME);
        }

        @Override
        public XmlMapper build() {
            return new XmlMapper(this);
        }

        @Override
        protected MapperBuilderState _saveState() {
            return new XmlBuilderState(this);
        }

        public Builder(XmlBuilderState state) {
            super(state);
            _defaultUseWrapper = state._defaultUseWrapper;
            _nameForTextElement = state._nameForTextElement;
        }

        /*
        /******************************************************************
        /* Default value overrides
        /******************************************************************
         */

        @Override
        protected SerializationContexts _defaultSerializationContexts() {
            return new XmlSerializationContexts();
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
        /* Overrides for polymorphic type handling
        /******************************************************************
         */

        @Override
        protected TypeResolverBuilder<?> _defaultDefaultTypingResolver(PolymorphicTypeValidator ptv,
                DefaultTyping applicability,
                JsonTypeInfo.As includeAs) {
            return new DefaultingXmlTypeResolverBuilder(ptv, applicability, includeAs);
        }

        @Override
        protected TypeResolverBuilder<?> _defaultDefaultTypingResolver(PolymorphicTypeValidator ptv,
                DefaultTyping applicability,
                String propertyName) {
            return new DefaultingXmlTypeResolverBuilder(ptv, applicability, propertyName);
        }

        // Since WRAPPER_ARRAY does not work well, map to WRAPPER_OBJECT
        @Override
        public Builder activateDefaultTyping(PolymorphicTypeValidator ptv, DefaultTyping dti) {
            return activateDefaultTyping(ptv, dti, JsonTypeInfo.As.WRAPPER_OBJECT);
        }

        /*
        /******************************************************************
        /* XML format features
        /******************************************************************
         */

        public Builder enable(FromXmlParser.Feature... features) {
            for (FromXmlParser.Feature f : features) {
                _formatReadFeatures |= f.getMask();
            }
            return this;
        }

        public Builder disable(FromXmlParser.Feature... features) {
            for (FromXmlParser.Feature f : features) {
                _formatReadFeatures &= ~f.getMask();
            }
            return this;
        }

        public Builder configure(FromXmlParser.Feature feature, boolean state)
        {
            if (state) {
                _formatReadFeatures |= feature.getMask();
            } else {
                _formatReadFeatures &= ~feature.getMask();
            }
            return this;
        }

        public Builder enable(ToXmlGenerator.Feature... features) {
            for (ToXmlGenerator.Feature f : features) {
                _formatWriteFeatures |= f.getMask();
            }
            return this;
        }

        public Builder disable(ToXmlGenerator.Feature... features) {
            for (ToXmlGenerator.Feature f : features) {
                _formatWriteFeatures &= ~f.getMask();
            }
            return this;
        }

        public Builder configure(ToXmlGenerator.Feature feature, boolean state)
        {
            if (state) {
                _formatWriteFeatures |= feature.getMask();
            } else {
                _formatWriteFeatures &= ~feature.getMask();
            }
            return this;
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
            if (_defaultUseWrapper != b) {
                _defaultUseWrapper = b;

                AnnotationIntrospector ai0 = annotationIntrospector();
                for (AnnotationIntrospector ai : ai0.allIntrospectors()) {
                    if (ai instanceof XmlAnnotationIntrospector) {
                        ((XmlAnnotationIntrospector) ai).setDefaultUseWrapper(b);
                    }
                }
            }
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
            _streamFactory = ((XmlFactory) _streamFactory).withNameForTextElement(elem);
            return this;
        }
    }

    /**
     * Saved configuration entity to use with builder for {@link XmlMapper} instances.
     *
     * @since 3.0
     */
    protected static class XmlBuilderState extends MapperBuilderState
        implements java.io.Serializable // important!
    {
        private static final long serialVersionUID = 3L;

        protected final boolean _defaultUseWrapper;

        protected final String _nameForTextElement;
        
        public XmlBuilderState(Builder src) {
            super(src);
            _defaultUseWrapper = src._defaultUseWrapper;
            _nameForTextElement = src._nameForTextElement;
        }

        // We also need actual instance of state as base class can not implement logic
         // for reinstating mapper (via mapper builder) from state.
        @Override
        protected Object readResolve() {
            return new Builder(this).build();
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
    }

    public static XmlMapper.Builder xmlBuilder() {
        return new XmlMapper.Builder(new XmlFactory());
    }

    public static XmlMapper.Builder builder() {
        return new XmlMapper.Builder(new XmlFactory());
    }

    public static XmlMapper.Builder builder(XmlFactory streamFactory) {
        return new XmlMapper.Builder(streamFactory);
    }

    @SuppressWarnings("unchecked")
    @Override
    public XmlMapper.Builder rebuild() {
        return new XmlMapper.Builder((XmlBuilderState) _savedBuilderState);
    }

    /*
    /**********************************************************************
    /* Life-cycle: JDK serialization support
    /**********************************************************************
     */

    // 27-Feb-2018, tatu: Not sure why but it seems base class definitions
    //   are not sufficient alone; sub-classes must re-define.
    @Override
    protected Object writeReplace() {
        return _savedBuilderState;
    }

    @Override
    protected Object readResolve() {
        throw new IllegalStateException("Should never deserialize `"+getClass().getName()+"` directly");
    }

    /*
    /**********************************************************************
    /* Life-cycle: construction, legacy
    /**********************************************************************
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
    /**********************************************************************
    /* Life-cycle, shared "vanilla" (default configuration) instance
    /**********************************************************************
     */

    /**
     * Accessor method for getting globally shared "default" {@link XmlMapper}
     * instance: one that has default configuration, no (custom) modules registered, no
     * config overrides. Usable mostly when dealing "untyped" or Tree-style
     * content reading and writing.
     */
    public static XmlMapper shared() {
        return SharedWrapper.wrapped();
    }

    /*
    /**********************************************************************
    /* Access to configuration settings
    /**********************************************************************
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
    /**********************************************************************
    /* XML-specific access
    /**********************************************************************
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
        DeserializationContext ctxt = _deserializationContext();
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
        
        SerializationConfig config = serializationConfig();
        DefaultSerializerProvider prov = _serializerProvider(config);
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

    /*
    /**********************************************************
    /* Helper class(es)
    /**********************************************************
     */

    /**
     * Helper class to contain dynamically constructed "shared" instance of
     * mapper, should one be needed via {@link #shared}.
     */
    private final static class SharedWrapper {
        private final static XmlMapper MAPPER = XmlMapper.builder().build();

        public static XmlMapper wrapped() { return MAPPER; }
    }
}
