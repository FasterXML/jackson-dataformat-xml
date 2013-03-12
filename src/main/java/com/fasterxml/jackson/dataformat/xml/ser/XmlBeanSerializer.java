package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.IOException;

import javax.xml.namespace.QName;

import com.fasterxml.jackson.core.*;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.fasterxml.jackson.dataformat.xml.util.XmlInfo;

/**
 * Specific sub-class of {@link BeanSerializer} needed to take care
 * of some xml-specific aspects, such as distinction between attributes
 * and elements.
 */
public class XmlBeanSerializer extends BeanSerializer
{
    /**
     * Marker used for storing associated internal data with {@link BeanPropertyWriter}
     * instances; to mark instances that are to be written out as attributes.
     * Created as separate non-interned String to ensure there are no collisions.
     */
    public final static String KEY_XML_INFO = new String("xmlInfo");

    /**
     * Number of attributes to write; these will have been ordered to be the first
     * properties to write.
     */
    protected final int _attributeCount;

    /**
     * Index of "text value" property we have, if any; can have at most
     * one such property.
     */
    protected final int _textPropertyIndex;

    /**
     * Array that contains namespace URIs associated with properties, if any;
     * null if no namespace definitions have been assigned
     */
    protected final QName[] _xmlNames;

    public XmlBeanSerializer(BeanSerializerBase src)
    {
        super(src);
        
        /* Then make sure attributes are sorted before elements, keep track
         * of how many there are altogether
         */
        int attrCount = 0;
        for (BeanPropertyWriter bpw : _props) {
            if (_isAttribute(bpw)) { // Yup: let's build re-ordered list then
                attrCount = _orderAttributesFirst(_props, _filteredProps);
                break;
            }
        }
        _attributeCount = attrCount;

        // And then collect namespace information
        _xmlNames = new QName[_props.length];
        int textIndex = -1;
        for (int i = 0, len = _props.length; i < len; ++i) {
            BeanPropertyWriter bpw = _props[i];
            XmlInfo info = (XmlInfo) bpw.getInternalSetting(KEY_XML_INFO);
            String ns = null;
            if (info != null) {
                ns = info.getNamespace();
                if (textIndex < 0 && info.isText()) {
                    textIndex = i;
                }
            }
            _xmlNames[i] = new QName((ns == null) ? "" : ns, bpw.getName());
        }
        _textPropertyIndex = textIndex;
    }

    protected XmlBeanSerializer(XmlBeanSerializer src, ObjectIdWriter objectIdWriter)
    {
        super(src, objectIdWriter);
        _attributeCount = src._attributeCount;
        _textPropertyIndex = src._textPropertyIndex;
        _xmlNames = src._xmlNames;
    }

    /*
    /**********************************************************
    /* Life-cycle: factory methods, fluent factories
    /**********************************************************
     */

    @Override
    public JsonSerializer<Object> unwrappingSerializer(NameTransformer unwrapper) {
        // 19-Feb-2012, tatu: Should support unwrapping for XML too... but not yet done
//        return new UnwrappingBeanSerializer(this, unwrapper);
        throw new UnsupportedOperationException("Unwrapping serialization not yet supported for XML");
    }

    @Override
    public BeanSerializer withObjectIdWriter(ObjectIdWriter objectIdWriter) {
        return new XmlBeanSerializer(this, objectIdWriter);
    }
    
    /*
    /**********************************************************
    /* Overridden serialization methods
    /**********************************************************
     */
    
    /**
     * Main serialization method needs to be overridden to allow XML-specific
     * extra handling, such as indication of whether to write attributes or
     * elements.
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void serializeFields(Object bean, JsonGenerator jgen0, SerializerProvider provider)
        throws IOException, JsonGenerationException
    {
        final ToXmlGenerator xgen = (ToXmlGenerator) jgen0;
        final BeanPropertyWriter[] props;
        // !!! TODO: change to use non-deprecated version in 2.3
        if (_filteredProps != null && provider.getSerializationView() != null) {
            props = _filteredProps;
        } else {
            props = _props;
        }

        final int attrCount = _attributeCount;
        if (attrCount > 0) {
            xgen.setNextIsAttribute(true);
        }
        final int textIndex = _textPropertyIndex;
        final QName[] xmlNames = _xmlNames;
        int i = 0;

        try {
            for (final int len = props.length; i < len; ++i) {
                if (i == attrCount) {
                    xgen.setNextIsAttribute(false);
                }
                // also: if this is property to write as text ("unwrap"), need to:
                if (i == textIndex) {
                    xgen.setNextIsUnwrapped(true);
                }
                xgen.setNextName(xmlNames[i]);
                BeanPropertyWriter prop = props[i];
                if (prop != null) { // can have nulls in filtered list
                    prop.serializeAsField(bean, xgen, provider);
                }
            }
            if (_anyGetterWriter != null) {
                _anyGetterWriter.getAndSerialize(bean, xgen, provider);
            }
        } catch (Exception e) {
            String name = (i == props.length) ? "[anySetter]" : props[i].getName();
            wrapAndThrow(provider, e, bean, name);
        } catch (StackOverflowError e) { // Bit tricky, can't do more calls as stack is full; so:
            JsonMappingException mapE = new JsonMappingException("Infinite recursion (StackOverflowError)");
            String name = (i == props.length) ? "[anySetter]" : props[i].getName();
            mapE.prependPath(new JsonMappingException.Reference(bean, name));
            throw mapE;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void serializeFieldsFiltered(Object bean, JsonGenerator jgen0,
            SerializerProvider provider)
        throws IOException, JsonGenerationException
    {
        final ToXmlGenerator xgen = (ToXmlGenerator) jgen0;
        
        final BeanPropertyWriter[] props;
        // !!! TODO: change to use non-deprecated version in 2.3
        if (_filteredProps != null && provider.getSerializationView() != null) {
            props = _filteredProps;
        } else {
            props = _props;
        }
        final BeanPropertyFilter filter = findFilter(provider);
        // better also allow missing filter actually..
        if (filter == null) {
            serializeFields(bean, jgen0, provider);
            return;
        }

        final int attrCount = _attributeCount;
        if (attrCount > 0) {
            xgen.setNextIsAttribute(true);
        }
        final int textIndex = _textPropertyIndex;
        final QName[] xmlNames = _xmlNames;

        int i = 0;
        try {
            for (final int len = props.length; i < len; ++i) {
                if (i == attrCount) {
                    xgen.setNextIsAttribute(false);
                }
                // also: if this is property to write as text ("unwrap"), need to:
                if (i == textIndex) {
                    xgen.setNextIsUnwrapped(true);
                }
                xgen.setNextName(xmlNames[i]);
                BeanPropertyWriter prop = props[i];
                if (prop != null) { // can have nulls in filtered list
                    filter.serializeAsField(bean, xgen, provider, prop);
                }
            }
            if (_anyGetterWriter != null) {
                _anyGetterWriter.getAndSerialize(bean, xgen, provider);
            }
        } catch (Exception e) {
            String name = (i == props.length) ? "[anySetter]" : props[i].getName();
            wrapAndThrow(provider, e, bean, name);
        } catch (StackOverflowError e) {
            JsonMappingException mapE = new JsonMappingException("Infinite recursion (StackOverflowError)", e);
            String name = (i == props.length) ? "[anySetter]" : props[i].getName();
            mapE.prependPath(new JsonMappingException.Reference(bean, name));
            throw mapE;
        }
    }
    
    @Override
    public void serializeWithType(Object bean, JsonGenerator jgen, SerializerProvider provider,
            TypeSerializer typeSer)
        throws IOException, JsonGenerationException
    {
        /* Ok: let's serialize type id as attribute, but if (and only if!)
         * we are using AS_PROPERTY
         */
        if (typeSer.getTypeInclusion() == JsonTypeInfo.As.PROPERTY) {
            ToXmlGenerator xgen = (ToXmlGenerator)jgen;
            xgen.setNextIsAttribute(true);
            super.serializeWithType(bean, jgen, provider, typeSer);
            if (_attributeCount == 0) { // if no attributes, need to reset
                xgen.setNextIsAttribute(false);
            }
        } else {
            super.serializeWithType(bean, jgen, provider, typeSer);
        }
    }
    
    /*
    /**********************************************************
    /* Helper methods
    /**********************************************************
     */

    protected static boolean _isAttribute(BeanPropertyWriter bpw)
    {
        XmlInfo info = (XmlInfo) bpw.getInternalSetting(KEY_XML_INFO);
        return (info != null) && info.isAttribute();
    }

    /**
     * Method for re-sorting lists of bean properties such that attributes are strictly
     * written before elements.
     */
    protected static int _orderAttributesFirst(BeanPropertyWriter[] properties,
            BeanPropertyWriter[] filteredProperties)
    {
        int attrCount = 0;

        for (int i = 0, len = properties.length; i < len; ++i) {
            BeanPropertyWriter bpw = properties[i];
            
            if (!_isAttribute(bpw)) {
                continue;
            }
            // Swap if attribute and there are preceding elements:
            if (attrCount < i) {
                properties[i] = properties[attrCount];
                properties[attrCount] = bpw;
                if (filteredProperties != null) {
                    BeanPropertyWriter fbpw = filteredProperties[i];
                    filteredProperties[i] = filteredProperties[attrCount];
                    filteredProperties[attrCount] = fbpw;
                }
            }
            ++attrCount;
        }
        return attrCount;
    }
}
