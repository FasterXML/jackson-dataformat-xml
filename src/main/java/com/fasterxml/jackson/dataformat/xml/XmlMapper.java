package com.fasterxml.jackson.dataformat.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.type.LogicalType;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.deser.XmlDeserializationContext;
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
 *<p>
 * Note that most of configuration should be done by pre-constructing
 * {@link JacksonXmlModule} explicitly, instead of relying on default settings.
 */
public class XmlMapper extends ObjectMapper
{
    // as of 2.6
    private static final long serialVersionUID = 1L;

    /**
     * Builder implementation for constructing {@link XmlMapper} instances.
     *
     * @since 2.10
     */
    public static class Builder extends MapperBuilder<XmlMapper, Builder>
    {
        public Builder(XmlMapper m) {
            super(m);
        }

        public Builder enable(FromXmlParser.Feature... features)  {
            for (FromXmlParser.Feature f : features) {
                _mapper.enable(f);
            }
            return this;
        }

        public Builder disable(FromXmlParser.Feature... features) {
            for (FromXmlParser.Feature f : features) {
                _mapper.disable(f);
            }
            return this;
        }

        public Builder configure(FromXmlParser.Feature feature, boolean state)
        {
            if (state) {
                _mapper.enable(feature);
            } else {
                _mapper.disable(feature);
            }
            return this;
        }

        public Builder enable(ToXmlGenerator.Feature... features) {
            for (ToXmlGenerator.Feature f : features) {
                _mapper.enable(f);
            }
            return this;
        }

        public Builder disable(ToXmlGenerator.Feature... features) {
            for (ToXmlGenerator.Feature f : features) {
                _mapper.disable(f);
            }
            return this;
        }

        public Builder configure(ToXmlGenerator.Feature feature, boolean state)
        {
            if (state) {
                _mapper.enable(feature);
            } else {
                _mapper.disable(feature);
            }
            return this;
        }
        
        public Builder nameForTextElement(String name) {
            _mapper.setXMLTextElementName(name);
            return this;
        }

        public Builder defaultUseWrapper(boolean state) {
            _mapper.setDefaultUseWrapper(state);
            return this;
        }

        /**
         * @since 2.14
         */
        public Builder xmlNameProcessor(XmlNameProcessor processor) {
            _mapper.setXmlNameProcessor(processor);
            return this;
        }
    }

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

    /**
     * @since 2.4
     */
    public XmlMapper(XMLInputFactory inputF, XMLOutputFactory outF) {
        this(new XmlFactory(inputF, outF));
    }

    /**
     * @since 2.4
     */
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
        // Need to override serializer provider (due to root name handling);
        // deserializer provider fine as is
        super(xmlFactory, new XmlSerializerProvider(new XmlRootNameLookup()),
                new XmlDeserializationContext(BeanDeserializerFactory.instance));
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

        // 13-May-2020, tatu: [dataformat-xml#377] Need to ensure we will keep XML-specific
        //    Base64 default as "MIME" (not MIME-NO-LINEFEEDS), to preserve pre-2.12
        //    behavior
        setBase64Variant(Base64Variants.MIME);

        // 04-Jun-2020, tatu: Use new (2.12) "CoercionConfigs" to support coercion
        //   from empty and blank Strings to "empty" POJOs etc
        coercionConfigDefaults()
            // To allow indentation without problems, need to accept blank String as empty:
            .setAcceptBlankAsEmpty(Boolean.TRUE)
            // and then coercion from empty String to empty value, in general
            .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsEmpty)
            ;
        // 03-May-2021, tatu: ... except make sure to keep "empty to Null" for
        //   scalar types...
        coercionConfigFor(LogicalType.Integer)
            .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
        coercionConfigFor(LogicalType.Float)
            .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
        coercionConfigFor(LogicalType.Boolean)
            .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
    }

    /**
     * @since 2.8.9
     */
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

    /**
     * @since 2.10
     */
    public static XmlMapper.Builder xmlBuilder() {
        return new XmlMapper.Builder(new XmlMapper());
    }

    /**
     * @since 2.10
     */
    public static XmlMapper.Builder builder() {
        return new XmlMapper.Builder(new XmlMapper());
    }

    /**
     * @since 2.10
     */
    public static XmlMapper.Builder builder(XmlFactory streamFactory) {
        return new XmlMapper.Builder(new XmlMapper(streamFactory));
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    /*
    /**********************************************************
    /* Factory method overrides
    /**********************************************************
     */

    @Override // since 2.10
    protected TypeResolverBuilder<?> _constructDefaultTypeResolverBuilder(DefaultTyping applicability,
            PolymorphicTypeValidator ptv) {
        return new DefaultingXmlTypeResolverBuilder(applicability, ptv);
    }

    /*
    /**********************************************************
    /* Additional XML-specific configurations
    /**********************************************************
     */

    /**
     * Method called by {@link JacksonXmlModule} to pass configuration
     * information to {@link XmlFactory}, during registration; NOT
     * exposed as public method since configuration should be done
     * via {@link JacksonXmlModule}.
     * 
     * @since 2.1
     *
     * @deprecated Since 2.10 use {@link Builder#nameForTextElement(String)} instead
     */
    @Deprecated
    protected void setXMLTextElementName(String name) {
        ((XmlFactory) _jsonFactory).setXMLTextElementName(name);
    }

    /**
     * Since 2.7
     * 
     * @deprecated Since 2.10 use {@link Builder#defaultUseWrapper(boolean)} instead
     */
    @Deprecated
    public XmlMapper setDefaultUseWrapper(boolean state) {
        // ser and deser configs should usually have the same introspector, so:
        AnnotationIntrospector ai0 = getDeserializationConfig().getAnnotationIntrospector();
        for (AnnotationIntrospector ai : ai0.allIntrospectors()) {
            if (ai instanceof JacksonXmlAnnotationIntrospector) {
                ((JacksonXmlAnnotationIntrospector) ai).setDefaultUseWrapper(state);
            }
        }
        return this;
    }

    /**
     * @since 2.14
     */
    public void setXmlNameProcessor(XmlNameProcessor processor) {
        ((XmlFactory)_jsonFactory).setXmlNameProcessor(processor);
    }

    /*
    /**********************************************************
    /* Access to configuration settings
    /**********************************************************
     */

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
    /* XML-specific access
    /**********************************************************
     */

    /**
     * Method for reading a single XML value from given XML-specific input
     * source; useful for incremental data-binding, combining traversal using
     * basic Stax {@link XMLStreamReader} with data-binding by Jackson.
     * 
     * @since 2.4
     */
    public <T> T readValue(XMLStreamReader r, Class<T> valueType) throws IOException {
        return readValue(r, _typeFactory.constructType(valueType));
    } 

    /**
     * Method for reading a single XML value from given XML-specific input
     * source; useful for incremental data-binding, combining traversal using
     * basic Stax {@link XMLStreamReader} with data-binding by Jackson.
     * 
     * @since 2.4
     */
    public <T> T readValue(XMLStreamReader r, TypeReference<T> valueTypeRef) throws IOException {
        return readValue(r, _typeFactory.constructType(valueTypeRef));
    } 

    /**
     * Method for reading a single XML value from given XML-specific input
     * source; useful for incremental data-binding, combining traversal using
     * basic Stax {@link XMLStreamReader} with data-binding by Jackson.
     * 
     * @since 2.4
     */
    @SuppressWarnings("resource")
    public <T> T readValue(XMLStreamReader r, JavaType valueType) throws IOException
    {
        FromXmlParser p = getFactory().createParser(r);
        return super.readValue(p,  valueType);
    } 

    /**
     * Method for serializing given value using specific {@link XMLStreamReader}:
     * useful when building large XML files by binding individual items, one at
     * a time.
     * 
     * @since 2.4
     */
    public void writeValue(XMLStreamWriter w0, Object value) throws IOException {
        @SuppressWarnings("resource")
        ToXmlGenerator g = getFactory().createGenerator(w0);
        super.writeValue(g, value);
        // NOTE: above call should do flush(); and we should NOT close here.
        // Finally, 'g' has no buffers to release.
    }

    /**
     * Method that can be used to serialize any Java value as
     * a byte array.
     *
     * @param value value to serialize as XML bytes
     * @param encoding character encoding for the XML output
     * @return byte array representing the XML output
     * @throws JsonProcessingException
     * @since 2.16
     */
    public byte[] writeValueAsBytes(Object value, String encoding) throws JsonProcessingException {
        try (ByteArrayBuilder bb = new ByteArrayBuilder(_jsonFactory._getBufferRecycler())) {
            _writeValueAndClose(createGenerator(bb, encoding), value);
            final byte[] result = bb.toByteArray();
            bb.release();
            return result;
        } catch (JsonProcessingException e) { // to support [JACKSON-758]
            throw e;
        } catch (IOException e) { // shouldn't really happen, but is declared as possibility so:
            throw JsonMappingException.fromUnexpectedIOE(e);
        }
    }

    /**
     * Method that can be used to serialize any Java value as
     * XML output, written to File provided.
     *
     * @param resultFile the file to write to
     * @param value the value to serialize
     * @param encoding character encoding for the XML output
     * @throws IOException
     * @throws StreamWriteException
     * @throws DatabindException
     * @since 2.16
     */
    public void writeValue(File resultFile, Object value, String encoding)
            throws IOException, StreamWriteException, DatabindException
    {
        _writeValueAndClose(createGenerator(resultFile, encoding), value);
    }

    /**
     * Method that can be used to serialize any Java value as
     * JSON output, using output stream provided (using encoding
     * {@link JsonEncoding#UTF8}).
     *<p>
     * Note: method does not close the underlying stream explicitly
     * here; however, {@link JsonFactory} this mapper uses may choose
     * to close the stream depending on its settings (by default,
     * it will try to close it when {@link JsonGenerator} we construct
     * is closed).
     *
     * @since 2.16
     */
    public void writeValue(OutputStream out, Object value, String encoding)
            throws IOException, StreamWriteException, DatabindException
    {
        _writeValueAndClose(createGenerator(out, encoding), value);
    }

    protected final JsonGenerator createGenerator(OutputStream out, String encoding) throws IOException {
        this._assertNotNull("out", out);
        JsonGenerator g = ((XmlFactory) _jsonFactory).createGenerator(out, encoding);
        this._serializationConfig.initialize(g);
        return g;
    }

    protected final JsonGenerator createGenerator(File outputFile, String encoding) throws IOException {
        _assertNotNull("outputFile", outputFile);
        JsonGenerator g = ((XmlFactory) _jsonFactory).createGenerator(
                new FileOutputStream(outputFile), encoding);
        _serializationConfig.initialize(g);
        return g;
    }
}
