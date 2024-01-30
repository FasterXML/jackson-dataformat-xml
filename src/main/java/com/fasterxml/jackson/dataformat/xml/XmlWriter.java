package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.DataOutputAsStream;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.cfg.DatatypeFeature;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.DataOutput;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

public final class XmlWriter extends ObjectWriter {

    private final ObjectWriter _objectWriter;

    public XmlWriter(final ObjectWriter objectWriter) {
        super(objectWriter, objectWriter.getConfig());
        _objectWriter = objectWriter;
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    @Override
    public ObjectWriter with(SerializationFeature feature) {
        return new XmlWriter(_objectWriter.with(feature));
    }

    @Override
    public ObjectWriter with(SerializationFeature first, SerializationFeature... other) {
        return new XmlWriter(_objectWriter.with(first, other));
    }

    @Override
    public ObjectWriter withFeatures(SerializationFeature... features) {
        return new XmlWriter(_objectWriter.withFeatures(features));
    }

    @Override
    public ObjectWriter without(SerializationFeature feature) {
        return new XmlWriter(_objectWriter.without(feature));
    }

    @Override
    public ObjectWriter without(SerializationFeature first, SerializationFeature... other) {
        return new XmlWriter(_objectWriter.without(first, other));
    }

    @Override
    public ObjectWriter withoutFeatures(SerializationFeature... features) {
        return new XmlWriter(_objectWriter.withoutFeatures(features));
    }

    @Override
    public ObjectWriter with(DatatypeFeature feature) {
        return new XmlWriter(_objectWriter.with(feature));
    }

    @Override
    public ObjectWriter withFeatures(DatatypeFeature... features) {
        return new XmlWriter(_objectWriter.withFeatures(features));
    }

    @Override
    public ObjectWriter without(DatatypeFeature feature) {
        return new XmlWriter(_objectWriter.without(feature));
    }

    @Override
    public ObjectWriter withoutFeatures(DatatypeFeature... features) {
        return new XmlWriter(_objectWriter.withoutFeatures(features));
    }

    @Override
    public ObjectWriter with(JsonGenerator.Feature feature) {
        return new XmlWriter(_objectWriter.with(feature));
    }

    @Override
    public ObjectWriter withFeatures(JsonGenerator.Feature... features) {
        return new XmlWriter(_objectWriter.withFeatures(features));
    }

    @Override
    public ObjectWriter without(JsonGenerator.Feature feature) {
        return new XmlWriter(_objectWriter.without(feature));
    }

    @Override
    public ObjectWriter withoutFeatures(JsonGenerator.Feature... features) {
        return new XmlWriter(_objectWriter.withoutFeatures(features));
    }

    @Override
    public ObjectWriter with(StreamWriteFeature feature) {
        return super.with(feature);
    }

    @Override
    public ObjectWriter without(StreamWriteFeature feature) {
        return new XmlWriter(_objectWriter.without(feature));
    }

    @Override
    public ObjectWriter with(FormatFeature feature) {
        return new XmlWriter(_objectWriter.with(feature));
    }

    @Override
    public ObjectWriter withFeatures(FormatFeature... features) {
        return new XmlWriter(_objectWriter.withFeatures(features));
    }

    @Override
    public ObjectWriter without(FormatFeature feature) {
        return new XmlWriter(_objectWriter.without(feature));
    }

    @Override
    public ObjectWriter withoutFeatures(FormatFeature... features) {
        return new XmlWriter(_objectWriter.withoutFeatures(features));
    }

    @Override
    public ObjectWriter forType(JavaType rootType) {
        return new XmlWriter(_objectWriter.forType(rootType));
    }

    @Override
    public ObjectWriter forType(Class<?> rootType) {
        return new XmlWriter(_objectWriter.forType(rootType));
    }

    @Override
    public ObjectWriter forType(TypeReference<?> rootType) {
        return new XmlWriter(_objectWriter.forType(rootType));
    }

    @Override
    public ObjectWriter withType(JavaType rootType) {
        return new XmlWriter(_objectWriter.withType(rootType));
    }

    @Override
    public ObjectWriter withType(Class<?> rootType) {
        return new XmlWriter(_objectWriter.withType(rootType));
    }

    @Override
    public ObjectWriter withType(TypeReference<?> rootType) {
        return new XmlWriter(_objectWriter.withType(rootType));
    }

    @Override
    public ObjectWriter with(DateFormat df) {
        return new XmlWriter(_objectWriter.with(df));
    }

    @Override
    public ObjectWriter withDefaultPrettyPrinter() {
        return new XmlWriter(_objectWriter.withDefaultPrettyPrinter());
    }

    @Override
    public ObjectWriter with(FilterProvider filterProvider) {
        return new XmlWriter(_objectWriter.with(filterProvider));
    }

    @Override
    public ObjectWriter with(PrettyPrinter pp) {
        return new XmlWriter(_objectWriter.with(pp));
    }

    @Override
    public ObjectWriter withRootName(String rootName) {
        return new XmlWriter(_objectWriter.withRootName(rootName));
    }

    @Override
    public ObjectWriter withRootName(PropertyName rootName) {
        return new XmlWriter(_objectWriter.withRootName(rootName));
    }

    @Override
    public ObjectWriter withoutRootName() {
        return new XmlWriter(_objectWriter.withoutRootName());
    }

    @Override
    public ObjectWriter with(FormatSchema schema) {
        return new XmlWriter(_objectWriter.with(schema));
    }

    @Override
    public ObjectWriter withSchema(FormatSchema schema) {
        return new XmlWriter(_objectWriter.withSchema(schema));
    }

    @Override
    public ObjectWriter withView(Class<?> view) {
        return new XmlWriter(_objectWriter.withView(view));
    }

    @Override
    public ObjectWriter with(Locale l) {
        return new XmlWriter(_objectWriter.with(l));
    }

    @Override
    public ObjectWriter with(TimeZone tz) {
        return new XmlWriter(_objectWriter.with(tz));
    }

    @Override
    public ObjectWriter with(Base64Variant b64variant) {
        return new XmlWriter(_objectWriter.with(b64variant));
    }

    @Override
    public ObjectWriter with(CharacterEscapes escapes) {
        return new XmlWriter(_objectWriter.with(escapes));
    }

    @Override
    public ObjectWriter with(JsonFactory f) {
        return new XmlWriter(_objectWriter.with(f));
    }

    @Override
    public ObjectWriter with(ContextAttributes attrs) {
        return new XmlWriter(_objectWriter.with(attrs));
    }

    @Override
    public ObjectWriter withAttributes(Map<?, ?> attrs) {
        return new XmlWriter(_objectWriter.withAttributes(attrs));
    }

    @Override
    public ObjectWriter withAttribute(Object key, Object value) {
        return new XmlWriter(_objectWriter.withAttribute(key, value));
    }

    @Override
    public ObjectWriter withoutAttribute(Object key) {
        return new XmlWriter(_objectWriter.withoutAttribute(key));
    }

    @Override
    public ObjectWriter withRootValueSeparator(String sep) {
        return new XmlWriter(_objectWriter.withRootValueSeparator(sep));
    }

    @Override
    public ObjectWriter withRootValueSeparator(SerializableString sep) {
        return new XmlWriter(_objectWriter.withRootValueSeparator(sep));
    }

    @Override
    public JsonGenerator createGenerator(OutputStream out) throws IOException {
        return _objectWriter.createGenerator(out);
    }

    @Override
    public JsonGenerator createGenerator(OutputStream out, JsonEncoding enc) throws IOException {
        return _objectWriter.createGenerator(out, enc);
    }

    @Override
    public JsonGenerator createGenerator(Writer w) throws IOException {
        return _objectWriter.createGenerator(w);
    }

    @Override
    public JsonGenerator createGenerator(File outputFile, JsonEncoding enc) throws IOException {
        return _objectWriter.createGenerator(outputFile, enc);
    }

    @Override
    public JsonGenerator createGenerator(DataOutput out) throws IOException {
        return _objectWriter.createGenerator(out);
    }

    @Override
    public SequenceWriter writeValues(File out) throws IOException {
        return _objectWriter.writeValues(out);
    }

    public SequenceWriter writeValues(File out, Charset charset) throws IOException {
        return this._newSequenceWriter(false, this.createGenerator(out, charset), true);
    }

    @Override
    public SequenceWriter writeValues(JsonGenerator g) throws IOException {
        return _objectWriter.writeValues(g);
    }

    @Override
    public SequenceWriter writeValues(Writer out) throws IOException {
        return _objectWriter.writeValues(out);
    }

    @Override
    public SequenceWriter writeValues(OutputStream out) throws IOException {
        return _objectWriter.writeValues(out);
    }

    public SequenceWriter writeValues(OutputStream out, Charset charset) throws IOException {
        return this._newSequenceWriter(false, this.createGenerator(out, charset), true);
    }

    @Override
    public SequenceWriter writeValues(DataOutput out) throws IOException {
        return _objectWriter.writeValues(out);
    }

    public SequenceWriter writeValues(DataOutput out, Charset charset) throws IOException {
        return this._newSequenceWriter(false, this.createGenerator(out, charset), true);
    }

    @Override
    public SequenceWriter writeValuesAsArray(File out) throws IOException {
        return _objectWriter.writeValuesAsArray(out);
    }

    public SequenceWriter writeValuesAsArray(File out, Charset encoding) throws IOException {
        return this._newSequenceWriter(true, createGenerator(out, encoding), true);
    }

    @Override
    public SequenceWriter writeValuesAsArray(JsonGenerator gen) throws IOException {
        return _objectWriter.writeValuesAsArray(gen);
    }

    @Override
    public SequenceWriter writeValuesAsArray(Writer out) throws IOException {
        return _objectWriter.writeValuesAsArray(out);
    }

    @Override
    public SequenceWriter writeValuesAsArray(OutputStream out) throws IOException {
        return _objectWriter.writeValuesAsArray(out);
    }

    public SequenceWriter writeValuesAsArray(OutputStream out, Charset encoding) throws IOException {
        return this._newSequenceWriter(true, createGenerator(out, encoding), true);
    }

    @Override
    public SequenceWriter writeValuesAsArray(DataOutput out) throws IOException {
        return _objectWriter.writeValuesAsArray(out);
    }

    public SequenceWriter writeValuesAsArray(DataOutput out, Charset encoding) throws IOException {
        return this._newSequenceWriter(true, createGenerator(out, encoding), true);
    }

    @Override
    public boolean isEnabled(SerializationFeature f) {
        return _objectWriter.isEnabled(f);
    }

    @Override
    public boolean isEnabled(MapperFeature f) {
        return _objectWriter.isEnabled(f);
    }

    @Override
    public boolean isEnabled(DatatypeFeature f) {
        return _objectWriter.isEnabled(f);
    }

    @Override
    public boolean isEnabled(JsonParser.Feature f) {
        return _objectWriter.isEnabled(f);
    }

    @Override
    public boolean isEnabled(JsonGenerator.Feature f) {
        return _objectWriter.isEnabled(f);
    }

    @Override
    public boolean isEnabled(StreamWriteFeature f) {
        return _objectWriter.isEnabled(f);
    }

    @Override
    public SerializationConfig getConfig() {
        return _objectWriter.getConfig();
    }

    @Override
    public JsonFactory getFactory() {
        return _objectWriter.getFactory();
    }

    @Override
    public TypeFactory getTypeFactory() {
        return _objectWriter.getTypeFactory();
    }

    @Override
    public boolean hasPrefetchedSerializer() {
        return _objectWriter.hasPrefetchedSerializer();
    }

    @Override
    public ContextAttributes getAttributes() {
        return _objectWriter.getAttributes();
    }

    @Override
    public void writeValue(JsonGenerator g, Object value) throws IOException {
        _objectWriter.writeValue(g, value);
    }

    @Override
    public void writeValue(File resultFile, Object value) throws IOException, StreamWriteException, DatabindException {
        _objectWriter.writeValue(resultFile, value);
    }

    public void writeValue(File resultFile, Object value, Charset encoding)
            throws IOException, StreamWriteException, DatabindException {
        _writeValueAndClose(createGenerator(resultFile, encoding), value);
    }

    @Override
    public void writeValue(OutputStream out, Object value) throws IOException, StreamWriteException, DatabindException {
        _objectWriter.writeValue(out, value);
    }

    public void writeValue(OutputStream out, Object value, Charset encoding)
            throws IOException, StreamWriteException, DatabindException {
        _writeValueAndClose(createGenerator(out, encoding), value);
    }

    @Override
    public void writeValue(Writer w, Object value) throws IOException, StreamWriteException, DatabindException {
        _objectWriter.writeValue(w, value);
    }

    @Override
    public void writeValue(DataOutput out, Object value) throws IOException, StreamWriteException, DatabindException {
        _objectWriter.writeValue(out, value);
    }

    public void writeValue(DataOutput out, Object value, Charset encoding)
            throws IOException, StreamWriteException, DatabindException {
        _writeValueAndClose(createGenerator(out, encoding), value);
    }

    @Override
    public String writeValueAsString(Object value) throws JsonProcessingException {
        return _objectWriter.writeValueAsString(value);
    }

    @Override
    public byte[] writeValueAsBytes(Object value) throws JsonProcessingException {
        return _objectWriter.writeValueAsBytes(value);
    }

    /**
     * Method that can be used to serialize any Java value as
     * a byte array. Functionally equivalent to calling
     * {@link #writeValue(Writer,Object)} with {@link java.io.ByteArrayOutputStream}
     * and getting bytes, but more efficient.
     *
     * @param value value to serialize as XML bytes
     * @param encoding character encoding for the XML output
     * @return byte array representing the XML output
     * @throws JsonProcessingException
     */
    public byte[] writeValueAsBytes(Object value, Charset encoding)
            throws JsonProcessingException
    {
        // Although 'close()' is NOP, use auto-close to avoid lgtm complaints
        try (ByteArrayBuilder bb = new ByteArrayBuilder(_generatorFactory._getBufferRecycler())) {
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

    @Override
    public void acceptJsonFormatVisitor(JavaType type, JsonFormatVisitorWrapper visitor) throws JsonMappingException {
        _objectWriter.acceptJsonFormatVisitor(type, visitor);
    }

    @Override
    public void acceptJsonFormatVisitor(Class<?> type, JsonFormatVisitorWrapper visitor) throws JsonMappingException {
        _objectWriter.acceptJsonFormatVisitor(type, visitor);
    }

    @Override
    public boolean canSerialize(Class<?> type) {
        return _objectWriter.canSerialize(type);
    }

    @Override
    public boolean canSerialize(Class<?> type, AtomicReference<Throwable> cause) {
        return _objectWriter.canSerialize(type, cause);
    }

    private JsonGenerator createGenerator(OutputStream out, Charset charset) throws IOException {
        _assertNotNull("out", out);
        return _configureGenerator(((XmlFactory) _generatorFactory).createGenerator(out, charset));
    }

    private JsonGenerator createGenerator(DataOutput out, Charset charset) throws IOException {
        _assertNotNull("out", out);
        return _configureGenerator(((XmlFactory) _generatorFactory)
                .createGenerator(new DataOutputAsStream(out), charset));
    }

    private JsonGenerator createGenerator(File out, Charset charset) throws IOException {
        _assertNotNull("out", out);
        return _configureGenerator(((XmlFactory) _generatorFactory)
                .createGenerator(new FileOutputStream(out), charset));
    }
}
