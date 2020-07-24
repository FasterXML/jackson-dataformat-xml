package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.IOException;
import java.util.BitSet;
import java.util.Set;

import javax.xml.namespace.QName;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.impl.WritableObjectId;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.fasterxml.jackson.dataformat.xml.util.XmlInfo;

/**
 * Specific sub-class of {@link BeanSerializerBase} needed to take care
 * of some xml-specific aspects, such as distinction between attributes
 * and elements.
 */
@SuppressWarnings("serial")
public abstract class XmlBeanSerializerBase extends BeanSerializerBase
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

    /**
     * Optional set of indexes of properties that should be serialized as CDATA,
     * instead of regular XML text segment. Left as null in cases where none of
     * element values are to be written in such a way.
     */
    protected final BitSet _cdata;
    
    public XmlBeanSerializerBase(BeanSerializerBase src)
    {
        super(src);

        // Then make sure attributes are sorted before elements, keep track
        // of how many there are altogether
        int attrCount = 0;
        for (BeanPropertyWriter bpw : _props) {
            if (_isAttribute(bpw)) { // Yup: let's build re-ordered list then
                attrCount = _orderAttributesFirst(_props, _filteredProps);
                break;
            }
        }
        _attributeCount = attrCount;

        // also: pre-compute need, if any, for CDATA handling:
        BitSet cdata = null;
        for (int i = 0, len = _props.length; i < len; ++i) {
            BeanPropertyWriter bpw = _props[i];
            if (_isCData(bpw)) {
                if (cdata == null) {
                    cdata = new BitSet(len);
                }
                cdata.set(i);
            }
        }
        _cdata = cdata;
        
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

    protected XmlBeanSerializerBase(XmlBeanSerializerBase src, ObjectIdWriter objectIdWriter)
    {
        super(src, objectIdWriter);
        _attributeCount = src._attributeCount;
        _textPropertyIndex = src._textPropertyIndex;
        _xmlNames = src._xmlNames;
        _cdata = src._cdata;
    }

    protected XmlBeanSerializerBase(XmlBeanSerializerBase src, ObjectIdWriter objectIdWriter, Object filterId)
    {
        super(src, objectIdWriter, filterId);
        _attributeCount = src._attributeCount;
        _textPropertyIndex = src._textPropertyIndex;
        _xmlNames = src._xmlNames;
        _cdata = src._cdata;
    }

    protected XmlBeanSerializerBase(XmlBeanSerializerBase src,
            Set<String> toIgnore, Set<String> toInclude)
    {
        super(src, toIgnore, toInclude);
        _attributeCount = src._attributeCount;
        _textPropertyIndex = src._textPropertyIndex;
        _xmlNames = src._xmlNames;
        _cdata = src._cdata;
    }
    
    public XmlBeanSerializerBase(XmlBeanSerializerBase src, NameTransformer transformer)
    {
        super(src, transformer);
        _attributeCount = src._attributeCount;
        _textPropertyIndex = src._textPropertyIndex;
        _xmlNames = src._xmlNames;
        _cdata = src._cdata;
    }

    // @since 2.11.1
    protected XmlBeanSerializerBase(XmlBeanSerializerBase src,
            BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
        super(src, properties, filteredProperties);
        _attributeCount = src._attributeCount;
        _textPropertyIndex = src._textPropertyIndex;
        _xmlNames = src._xmlNames;
        _cdata = src._cdata;
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
    @Override
    protected void serializeFields(Object bean, JsonGenerator gen0, SerializerProvider provider)
        throws IOException
    {
        // 19-Aug-2013, tatu: During 'convertValue()', need to skip
        if (!(gen0 instanceof ToXmlGenerator)) {
            super.serializeFields(bean, gen0, provider);
            return;
        }
        final ToXmlGenerator xgen = (ToXmlGenerator) gen0;
        final BeanPropertyWriter[] props;
        if (_filteredProps != null && provider.getActiveView() != null) {
            props = _filteredProps;
        } else {
            props = _props;
        }

        final int attrCount = _attributeCount;
        final boolean isAttribute = xgen._nextIsAttribute;
        if (attrCount > 0) {
            xgen.setNextIsAttribute(true);
        }
        final int textIndex = _textPropertyIndex;
        final QName[] xmlNames = _xmlNames;
        int i = 0;
        final BitSet cdata = _cdata;

        try {
            for (final int len = props.length; i < len; ++i) {
                // 28-jan-2014, pascal: we don't want to reset the attribute flag if we are an unwrapping serializer 
                // that started with nextIsAttribute to true because all properties should be unwrapped as attributes too.
                if (i == attrCount && !(isAttribute && isUnwrappingSerializer())) {
                    xgen.setNextIsAttribute(false);
                }
                // also: if this is property to write as text ("unwrap"), need to:
                if (i == textIndex) {
                    xgen.setNextIsUnwrapped(true);
                }
                xgen.setNextName(xmlNames[i]);
                BeanPropertyWriter prop = props[i];
                if (prop != null) { // can have nulls in filtered list
                    if ((cdata != null) && cdata.get(i)) {
                        xgen.setNextIsCData(true);
                        prop.serializeAsField(bean, xgen, provider);
                        xgen.setNextIsCData(false);
                    } else {
                        prop.serializeAsField(bean, xgen, provider);
                    }
                }
                // Reset to avoid next value being written as unwrapped, 
                // for example when property is suppressed
                if (i == textIndex) {
                    xgen.setNextIsUnwrapped(false);
                }
            }
            if (_anyGetterWriter != null) {
                // For [#117]: not a clean fix, but with @JsonTypeInfo, we'll end up
                // with accidental attributes otherwise
                xgen.setNextIsAttribute(false);
                _anyGetterWriter.getAndSerialize(bean, xgen, provider);
            }
        } catch (Exception e) {
            String name = (i == props.length) ? "[anySetter]" : props[i].getName();
            wrapAndThrow(provider, e, bean, name);
        } catch (StackOverflowError e) { // Bit tricky, can't do more calls as stack is full; so:
            JsonMappingException mapE = JsonMappingException.from(gen0,
                    "Infinite recursion (StackOverflowError)");
            String name = (i == props.length) ? "[anySetter]" : props[i].getName();
            mapE.prependPath(new JsonMappingException.Reference(bean, name));
            throw mapE;
        }
    }

    @Override
    protected void serializeFieldsFiltered(Object bean, JsonGenerator gen0,
            SerializerProvider provider)
        throws IOException
    {
        // 19-Aug-2013, tatu: During 'convertValue()', need to skip
        if (!(gen0 instanceof ToXmlGenerator)) {
            super.serializeFieldsFiltered(bean, gen0, provider);
            return;
        }
        
        final ToXmlGenerator xgen = (ToXmlGenerator) gen0;
        
        final BeanPropertyWriter[] props;
        if (_filteredProps != null && provider.getActiveView() != null) {
            props = _filteredProps;
        } else {
            props = _props;
        }
        final PropertyFilter filter = findPropertyFilter(provider, _propertyFilterId, bean);
        // better also allow missing filter actually..
        if (filter == null) {
            serializeFields(bean, gen0, provider);
            return;
        }

        final boolean isAttribute = xgen._nextIsAttribute;
        final int attrCount = _attributeCount;
        if (attrCount > 0) {
            xgen.setNextIsAttribute(true);
        }
        final int textIndex = _textPropertyIndex;
        final QName[] xmlNames = _xmlNames;
        final BitSet cdata = _cdata;

        int i = 0;
        try {
            for (final int len = props.length; i < len; ++i) {
                // 28-jan-2014, pascal: we don't want to reset the attribute flag if we are an unwrapping serializer 
                // that started with nextIsAttribute to true because all properties should be unwrapped as attributes too.
                if (i == attrCount && !(isAttribute && isUnwrappingSerializer())) {
                    xgen.setNextIsAttribute(false);
                }
                // also: if this is property to write as text ("unwrap"), need to:
                if (i == textIndex) {
                    xgen.setNextIsUnwrapped(true);
                }
                xgen.setNextName(xmlNames[i]);
                BeanPropertyWriter prop = props[i];
                if (prop != null) { // can have nulls in filtered list
                    if ((cdata != null) && cdata.get(i)) {
                        xgen.setNextIsCData(true);
                        filter.serializeAsField(bean, xgen, provider, prop);
                        xgen.setNextIsCData(false);
                    } else {
                        filter.serializeAsField(bean, xgen, provider, prop);
                    }
                }
            }
            if (_anyGetterWriter != null) {
                // For [#117]: not a clean fix, but with @JsonTypeInfo, we'll end up
                // with accidental attributes otherwise
                xgen.setNextIsAttribute(false);
                // 24-Jul-2019, tatu: Fixed for [dataformat-xml#351]
                _anyGetterWriter.getAndFilter(bean, xgen, provider, filter);
            }
        } catch (Exception e) {
            String name = (i == props.length) ? "[anySetter]" : props[i].getName();
            wrapAndThrow(provider, e, bean, name);
        } catch (StackOverflowError e) {
            JsonMappingException mapE = JsonMappingException.from(gen0, "Infinite recursion (StackOverflowError)", e);
            String name = (i == props.length) ? "[anySetter]" : props[i].getName();
            mapE.prependPath(new JsonMappingException.Reference(bean, name));
            throw mapE;
        }
    }
    
    @Override
    public void serializeWithType(Object bean, JsonGenerator gen, SerializerProvider provider,
            TypeSerializer typeSer)
        throws IOException
    {
        if (_objectIdWriter != null) {
            _serializeWithObjectId(bean, gen, provider, typeSer);
            return;
        }
        /* Ok: let's serialize type id as attribute, but if (and only if!)
         * we are using AS_PROPERTY
         */
        if (typeSer.getTypeInclusion() == JsonTypeInfo.As.PROPERTY) {
            ToXmlGenerator xgen = (ToXmlGenerator)gen;
            xgen.setNextIsAttribute(true);
            super.serializeWithType(bean, gen, provider, typeSer);
            if (_attributeCount == 0) { // if no attributes, need to reset
                xgen.setNextIsAttribute(false);
            }
        } else {
            super.serializeWithType(bean, gen, provider, typeSer);
        }
    }
    
    @Override
    protected void _serializeObjectId(Object bean, JsonGenerator gen, SerializerProvider provider,
            TypeSerializer typeSer, WritableObjectId objectId) throws IOException
    {
        // Ok: let's serialize type id as attribute, but if (and only if!) we are using AS_PROPERTY
        if (typeSer.getTypeInclusion() == JsonTypeInfo.As.PROPERTY) {
            ToXmlGenerator xgen = (ToXmlGenerator)gen;
            xgen.setNextIsAttribute(true);
            super._serializeObjectId(bean, gen, provider, typeSer, objectId);
            if (_attributeCount == 0) { // if no attributes, need to reset
                xgen.setNextIsAttribute(false);
            }
        } else {
            super._serializeObjectId(bean, gen, provider, typeSer, objectId);
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

    protected static boolean _isCData(BeanPropertyWriter bpw)
    {
        XmlInfo info = (XmlInfo) bpw.getInternalSetting(KEY_XML_INFO);
        return (info != null) && info.isCData();
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
            
            // Move attribute a few places down as necessary
            int moveBy = i - attrCount;
            if (moveBy > 0) {
                System.arraycopy(properties, attrCount, properties, attrCount + 1, moveBy);
                properties[attrCount] = bpw;
                if (filteredProperties != null) {
                    BeanPropertyWriter fbpw = filteredProperties[i];
                    System.arraycopy(filteredProperties, attrCount, filteredProperties, attrCount+1, moveBy);
                    filteredProperties[attrCount] = fbpw;
                }
            }
            ++attrCount;
        }
        return attrCount;
    }
}
