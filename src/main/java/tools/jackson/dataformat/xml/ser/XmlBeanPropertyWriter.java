package tools.jackson.dataformat.xml.ser;

import javax.xml.namespace.QName;

import com.fasterxml.jackson.annotation.JsonApplyView;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.*;
import tools.jackson.databind.ser.*;
import tools.jackson.databind.ser.impl.PropertySerializerMap;

/**
 * Property writer sub-class used for handling element wrapping needed for serializing
 * collection (array, Collection; not Map) types.
 */
public class XmlBeanPropertyWriter
    extends BeanPropertyWriter
{
    /*
    /**********************************************************************
    /* Config settings
    /**********************************************************************
     */

    /**
     * Element name used as wrapper for collection.
     */
    protected final QName _wrapperQName;

    /**
     * Element name used for items in the collection
     */
    protected final QName _wrappedQName;

    /**
     * Whether wrapping should be checked dynamically based on the runtime value type.
     * When {@code true}, wrapping is only applied if the runtime value is actually
     * a Collection, Iterable, or array. Used for properties declared as {@code Object}
     * that may or may not contain a collection at runtime.
     *
     * @since 3.2
     */
    protected final boolean _dynamicWrapping;

    /*
    /**********************************************************************
    /* Life-cycle: construction, configuration
    /**********************************************************************
     */

    /**
     * @deprecated Since 3.2
     */
    @Deprecated
    public XmlBeanPropertyWriter(BeanPropertyWriter wrapped,
            PropertyName wrapperName, PropertyName wrappedName) {
        this(wrapped, wrapperName, wrappedName, null, false);
    }

    /**
     * @deprecated Since 3.2
     */
    @Deprecated
    public XmlBeanPropertyWriter(BeanPropertyWriter wrapped,
            PropertyName wrapperName, PropertyName wrappedName,
            ValueSerializer<Object> serializer)
    {
        this(wrapped, wrapperName, wrappedName, serializer, false);
    }

    /**
     * @since 3.2
     */
    public XmlBeanPropertyWriter(BeanPropertyWriter wrapped,
            PropertyName wrapperName, PropertyName wrappedName,
            boolean dynamicWrapping)
    {
        this(wrapped, wrapperName, wrappedName, null, dynamicWrapping);
    }

    private XmlBeanPropertyWriter(BeanPropertyWriter wrapped,
            PropertyName wrapperName, PropertyName wrappedName,
            ValueSerializer<Object> serializer, boolean dynamicWrapping)
    {
        super(wrapped);
        _wrapperQName = _qname(wrapperName);
        _wrappedQName = _qname(wrappedName);
        _dynamicWrapping = dynamicWrapping;

        if (serializer != null) {
            assignSerializer(serializer);
        }
    }

    private QName _qname(PropertyName n)
    {
        String ns = n.getNamespace();
        if (ns == null) {
            ns = "";
        }
        return new QName(ns, n.getSimpleName());
    }

    /*
    /**********************************************************************
    /* Overridden methods
    /**********************************************************************
     */

    /**
     * Overridden version so that we can wrap output within wrapper element if
     * and as necessary.
     */
    @Override
    public void serializeAsProperty(Object bean, JsonGenerator g, SerializationContext ctxt)
        throws Exception
    {
        Object value = get(bean);

        // [dataformat-xml#8]: For dynamic wrapping (Object-typed properties),
        // check runtime type and delegate to standard handling if not a collection
        if (_dynamicWrapping && (value == null || !_isIndexedValue(value))) {
            super.serializeAsProperty(bean, g, ctxt);
            return;
        }

        /* 13-Feb-2014, tatu: As per [#103], default handling does not really
         *   work here. Rather, we need just a wrapping and should NOT call
         *   null handler, as it does not know what to do...
         *
         *   Question, however, is what should it be serialized as. We have two main
         *   choices; equivalent empty List, and "nothing" (missing). Let's start with
         *   empty List? But producing missing entry is non-trivial...
         */
        if (value == null) {
            // if (_nullSerializer != null) { ... }

            // For Empty List, we'd do this:
            /*
            @SuppressWarnings("resource")
            final ToXmlGenerator xmlGen = (jgen instanceof ToXmlGenerator) ? (ToXmlGenerator) jgen : null;
            if (xmlGen != null) {
                xmlGen.startWrappedValue(_wrapperQName, _wrappedQName);
                xmlGen.finishWrappedValue(_wrapperQName, _wrappedQName);
            }
            */
            // but for missing thing, well, just output nothing

            return;
        }

        // then find serializer to use
        ValueSerializer<Object> ser = _serializer;
        if (ser == null) {
            Class<?> cls = value.getClass();
            PropertySerializerMap map = _dynamicSerializers;
            ser = map.serializerFor(cls);
            if (ser == null) {
                ser = _findAndAddDynamic(map, cls, ctxt);
            }
        }
        // and then see if we must suppress certain values (default, empty)
        if (_suppressableValue != null) {
            if (MARKER_FOR_EMPTY == _suppressableValue) {
                if (ser.isEmpty(ctxt, value)) {
                    return;
                }
            } else if (_suppressableValue.equals(value)) {
                return;
            }
        }
        // For non-nulls: simple check for direct cycles
        if (value == bean) {
            // NOTE: method signature here change 2.3->2.4
            if (_handleSelfReference(bean, g, ctxt, ser)) {
                return;
            }
        }

        final ToXmlGenerator xmlGen = (g instanceof ToXmlGenerator) ? (ToXmlGenerator) g : null;
        // Ok then; addition we want to do is to add wrapper element, and that's what happens here
        // 19-Aug-2013, tatu: ... except for those nasty 'convertValue()' calls...
        if (xmlGen != null) {
            xmlGen.startWrappedValue(_wrapperQName, _wrappedQName);
        }
        // [dataformat-xml#27]: Use wrapped name (inner element name), not property
        // name which may be the wrapper name after introspector conflict resolution
        g.writeName(_wrappedQName.getLocalPart());
        // 18-Jun-2026, tatu: Need to apply active View, same as
        //    `BeanPropertyWriter.serializeAsProperty()` does
        if (_applyView == null) {
            _serialize(value, g, ctxt, ser);
        } else {
            final ValueSerializer<Object> actualSer = ser;
            ctxt.withActiveView(_applyView != JsonApplyView.NONE.class ? _applyView : null,
                    () -> _serialize(value, g, ctxt, actualSer));
        }
        if (xmlGen != null) {
            xmlGen.finishWrappedValue(_wrapperQName, _wrappedQName);
        }
    }

    private void _serialize(Object value, JsonGenerator g, SerializationContext ctxt,
            ValueSerializer<Object> ser)
    {
        if (_typeSerializer == null) {
            ser.serialize(value, g, ctxt);
        } else {
            ser.serializeWithType(value, g, ctxt, _typeSerializer);
        }
    }

    /**
     * Check if the runtime value is a Collection, array, or Iterable
     * (i.e. something that should get wrapper element handling).
     */
    private static boolean _isIndexedValue(Object value) {
        return (value instanceof java.util.Collection<?>)
                || (value instanceof Iterable<?>)
                || value.getClass().isArray();
    }
}
