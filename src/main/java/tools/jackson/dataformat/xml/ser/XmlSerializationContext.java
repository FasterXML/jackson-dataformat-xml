package tools.jackson.dataformat.xml.ser;

import java.io.IOException;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import tools.jackson.core.*;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.*;
import tools.jackson.databind.cfg.GeneratorSettings;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.ser.SerializerFactory;
import tools.jackson.databind.ser.SerializationContextExt;
import tools.jackson.databind.ser.SerializerCache;
import tools.jackson.databind.util.TokenBuffer;
import tools.jackson.dataformat.xml.util.StaxUtil;
import tools.jackson.dataformat.xml.util.TypeUtil;
import tools.jackson.dataformat.xml.util.XmlRootNameLookup;

/**
 * We need to override some parts of
 * {@link tools.jackson.databind.SerializationContext}
 * implementation to handle oddities of XML output, like "extra" root element.
 */
public class XmlSerializationContext extends SerializationContextExt
{
    protected final XmlRootNameLookup _rootNameLookup;

    public XmlSerializationContext(TokenStreamFactory streamFactory,
            SerializationConfig config, GeneratorSettings genSettings,
            SerializerFactory f, SerializerCache cache,
            XmlRootNameLookup rootLookup)
    {
        super(streamFactory, config, genSettings, f, cache);
        _rootNameLookup  = rootLookup;
    }

    /*
    /**********************************************************************
    /* Overridden methods
    /**********************************************************************
     */

    @SuppressWarnings("resource")
    @Override
    public void serializeValue(JsonGenerator gen, Object value) throws JacksonException
    {
        _generator = gen;
        if (value == null) {
            _serializeXmlNull(gen);
            return;
        }
        final Class<?> cls = value.getClass();
        final boolean asArray;
        final ToXmlGenerator xgen = _asXmlGenerator(gen);
        if (xgen == null) { // called by convertValue()
            asArray = false;
        } else {
            // [dataformat-xml#441]: allow ObjectNode unwrapping
            if (_shouldUnwrapObjectNode(xgen, value)) {
                _serializeUnwrappedObjectNode(xgen, value, null);
                return;
            }
            QName rootName = _rootNameFromConfig();
            if (rootName == null) {
                rootName = _rootNameLookup.findRootName(this, cls);
            }
            _initWithRootName(xgen, rootName);
            asArray = TypeUtil.isIndexedType(_config.constructType(cls));
            if (asArray) {
                _startRootArray(xgen, rootName);
            }
        }
        
        // From super-class implementation
        final ValueSerializer<Object> ser = findTypedValueSerializer(cls, true);
        try {
            ser.serialize(value, gen, this);
        } catch (Exception e) { // but wrap RuntimeExceptions, to get path information
            throw _wrapAsJacksonE(gen, e);
        }
        // end of super-class implementation

        if (asArray) {
            gen.writeEndObject();
        }
    }

    @Override
    public void serializeValue(JsonGenerator gen, Object value, JavaType rootType) throws JacksonException
    {
        serializeValue(gen, value, rootType, null);
    }

    @SuppressWarnings("resource")
    @Override
    public void serializeValue(JsonGenerator gen, Object value, JavaType rootType,
            ValueSerializer<Object> ser) throws JacksonException
    {
        _generator = gen;
        if (value == null) {
            _serializeXmlNull(gen);
            return;
        }
        // Let's ensure types are compatible at this point
        if ((rootType != null) && !rootType.getRawClass().isAssignableFrom(value.getClass())) {
            _reportIncompatibleRootType(value, rootType);
        }
        final boolean asArray;
        final ToXmlGenerator xgen = _asXmlGenerator(gen);
        if (xgen == null) { // called by convertValue()
            asArray = false;
        } else {
            // [dataformat-xml#441]: allow ObjectNode unwrapping
            if (_shouldUnwrapObjectNode(xgen, value)) {
                _serializeUnwrappedObjectNode(xgen, value, ser);
                return;
            }
            QName rootName = _rootNameFromConfig();
            if (rootName == null) {
                rootName = (rootType == null)
                        ? _rootNameLookup.findRootName(this, value.getClass())
                        : _rootNameLookup.findRootName(this, rootType);
            }
            _initWithRootName(xgen, rootName);
            asArray = (rootType == null)
                    ? TypeUtil.isIndexedType(_config.constructType(value.getClass()))
                        : TypeUtil.isIndexedType(rootType);
            if (asArray) {
                _startRootArray(xgen, rootName);
            }
        }
        if (ser == null) {
            ser = findTypedValueSerializer(rootType, true);
        }
        // From super-class implementation
        try {
            ser.serialize(value, gen, this);
        } catch (Exception e) { // but others do need to be, to get path etc
            throw _wrapAsJacksonE(gen, e);
        }
        // end of super-class implementation
        if (asArray) {
            gen.writeEndObject();
        }
    }

    @SuppressWarnings("resource")
    @Override
    public void serializePolymorphic(JsonGenerator gen, Object value, JavaType rootType,
            ValueSerializer<Object> valueSer, TypeSerializer typeSer)
        throws JacksonException
    {
        _generator = gen;
        if (value == null) {
            _serializeXmlNull(gen);
            return;
        }
        // Let's ensure types are compatible at this point
        if ((rootType != null) && !rootType.getRawClass().isAssignableFrom(value.getClass())) {
            _reportIncompatibleRootType(value, rootType);
        }
        final boolean asArray;
        final ToXmlGenerator xgen = _asXmlGenerator(gen);
        if (xgen == null) { // called by convertValue()
            asArray = false;
        } else {
            QName rootName = _rootNameFromConfig();
            if (rootName == null) {
                rootName = (rootType == null)
                        ? _rootNameLookup.findRootName(this, value.getClass())
                        : _rootNameLookup.findRootName(this, rootType);
            }
            _initWithRootName(xgen, rootName);
            asArray = (rootType == null)
                    ? TypeUtil.isIndexedType(_config.constructType(value.getClass()))
                    : TypeUtil.isIndexedType(rootType);
            if (asArray) {
                _startRootArray(xgen, rootName);
            }
        }
        // 21-May-2020: See comments in `jackson-databind/DefaultSerializationContext`
        if (valueSer == null) {
            if ((rootType != null) && rootType.isContainerType()) {
                valueSer = handleRootContextualization(findValueSerializer(rootType));
            } else {
                valueSer = handleRootContextualization(findValueSerializer(value.getClass()));
            }
        }
        // From super-class implementation
        try {
            valueSer.serializeWithType(value, gen, this, typeSer);
        } catch (Exception e) { // but others do need to be, to get path etc
            throw _wrapAsJacksonE(gen, e);
        }
        // end of super-class implementation
        if (asArray) {
            gen.writeEndObject();
        }
    }

    protected void _serializeXmlNull(JsonGenerator gen) throws JacksonException
    {
        // 14-Nov-2016, tatu: As per [dataformat-xml#213], we may have explicitly
        //    configured root name...
        QName rootName = _rootNameFromConfig();
        if (rootName == null) {
            rootName = XmlRootNameLookup.ROOT_NAME_FOR_NULL;
        }
        if (gen instanceof ToXmlGenerator) {
            _initWithRootName((ToXmlGenerator) gen, rootName);
        }
        super.serializeValue(gen, null);
    }

    protected void _startRootArray(ToXmlGenerator xgen, QName rootName) throws JacksonException
    {
        xgen.writeStartObject();
        // Could repeat root name, but what's the point? How to customize?
        xgen.writeName("item");
    }

    protected void _initWithRootName(ToXmlGenerator xgen, QName rootName) throws JacksonException
    {
        // 28-Nov-2012, tatu: We should only initialize the root name if no name has been
        //   set, as per [dataformat-xml#42], to allow for custom serializers to work.
        if (!xgen.setNextNameIfMissing(rootName)) {
            // however, if we are root, we... insist
            if (xgen.inRoot()) {
                xgen.setNextName(rootName);
            }
        }
        xgen.initGenerator();
        String ns = rootName.getNamespaceURI();
        // [dataformat-xml#26] If we just try writing root element with namespace,
        // we will get an explicit prefix. But we'd rather use the default
        // namespace, so let's try to force that.
        if (ns != null && ns.length() > 0) {
            try {
                xgen.getStaxWriter().setDefaultNamespace(ns);
            } catch (XMLStreamException e) {
                StaxUtil.throwAsWriteException(e, xgen);
            }
        }
    }

    protected QName _rootNameFromConfig()
    {
        PropertyName name = _config.getFullRootName();
        if (name == null) {
            return null;
        }
        String ns = name.getNamespace();
        if (ns == null || ns.isEmpty()) {
            return new QName(name.getSimpleName());
        }
        return new QName(ns, name.getSimpleName());
    }

    protected boolean _shouldUnwrapObjectNode(ToXmlGenerator xgen, Object value)
    {
        return xgen.isEnabled(ToXmlGenerator.Feature.UNWRAP_ROOT_OBJECT_NODE)
                && (value instanceof ObjectNode)
                && (((ObjectNode) value).size() == 1);
    }

    protected void _serializeUnwrappedObjectNode(ToXmlGenerator xgen, Object value,
            ValueSerializer<Object> ser) throws JacksonException
    {
        ObjectNode root = (ObjectNode) value;
        Map.Entry<String, JsonNode> entry = root.fields().next();
        final JsonNode newRoot = entry.getValue();

        // No namespace associated with JsonNode:
        _initWithRootName(xgen, new QName(entry.getKey()));
        if (ser == null) {
            ser = findTypedValueSerializer(newRoot.getClass(), true);
        }
        // From super-class implementation
        ser.serialize(newRoot, xgen, this);
    }

    protected ToXmlGenerator _asXmlGenerator(JsonGenerator gen)
    {
        if (!(gen instanceof ToXmlGenerator)) {
            // [dataformat-xml#71]: We sometimes get TokenBuffer, which is fine
            if (gen instanceof TokenBuffer) {
                return null;
            }
            // but verify
            throw DatabindException.from(gen,
                    "XmlMapper does not work with generators of type other than `ToXmlGenerator`; got: `"
                            +gen.getClass().getName()+"`");
        }
        return (ToXmlGenerator) gen;
    }    

    protected JacksonException _wrapAsJacksonE(JsonGenerator g, Exception e)
    {
        if (e instanceof IOException) {
            return JacksonIOException.construct((IOException) e);
        }
        // 17-Jan-2021, tatu: Should we do something else here? Presumably
        //    this exception has map set up
        if (e instanceof DatabindException) {
            throw (DatabindException) e;
        }
        String msg = e.getMessage();
        if (msg == null) {
            msg = "[no message for "+e.getClass().getName()+"]";
        }
        return DatabindException.from(g, msg, e);
    }
}
