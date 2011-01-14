package com.fasterxml.jackson.xml.ser;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.ser.BeanPropertyWriter;
import org.codehaus.jackson.map.ser.BeanSerializer;

import com.fasterxml.jackson.xml.util.XmlInfo;

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
     * Array that contains namespace URIs associated with properties, if any;
     * null if no namespace definitions have been assigned
     */
    protected final QName[] _xmlNames;

    public XmlBeanSerializer(BeanSerializer src)
    {
        super(src);

        // Ok, first: collect namespace information
        _xmlNames = new QName[_props.length];
        // First, find namespace information
        for (int i = 0, len = _props.length; i < len; ++i) {
            BeanPropertyWriter bpw = _props[i];
            XmlInfo info = (XmlInfo) bpw.getInternalSetting(KEY_XML_INFO);
            String ns = null;
            if (info != null) {
                ns = info.getNamespace();
            }
            _xmlNames[i] = new QName((ns == null) ? "" : ns, bpw.getName());
        }      
        
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
    }
    
    protected XmlBeanSerializer(XmlBeanSerializer src, BeanPropertyWriter[] filtered)
    {
        super(src._handledType, src._props, filtered, src._anyGetterWriter, src._propertyFilterId);
        _attributeCount = src._attributeCount;
        _xmlNames = src._xmlNames;
    }
    
    @Override
    public BeanSerializer withFiltered(BeanPropertyWriter[] filtered)
    {
        if (filtered == _filteredProps) {
            return this;
        }
        return new XmlBeanSerializer(this, filtered);
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
    protected void serializeFields(Object bean, JsonGenerator jgen0, SerializerProvider provider)
        throws IOException, JsonGenerationException
    {
        final ToXmlGenerator jgen = (ToXmlGenerator) jgen0;
        final BeanPropertyWriter[] props;
        if (_filteredProps != null && provider.getSerializationView() != null) {
            props = _filteredProps;
        } else {
            props = _props;
        }
        
        final int attrCount = _attributeCount;
        if (attrCount > 0) {
            jgen.setNextIsAttribute(true);
        }
        final QName[] xmlNames = _xmlNames;
        
        int i = 0;
        try {
            for (final int len = props.length; i < len; ++i) {
                if (i == attrCount) {
                    jgen.setNextIsAttribute(false);
                }
                jgen.setNextName(xmlNames[i]);
                BeanPropertyWriter prop = props[i];
                if (prop != null) { // can have nulls in filtered list
                    prop.serializeAsField(bean, jgen, provider);
                }
            }
            if (_anyGetterWriter != null) {
                _anyGetterWriter.getAndSerialize(bean, jgen, provider);
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

    @Override
    public void serializeWithType(Object bean, JsonGenerator jgen, SerializerProvider provider,
            TypeSerializer typeSer)
        throws IOException, JsonGenerationException
    {
        // Ok: let's serialize type id as attribute
        ToXmlGenerator xgen = (ToXmlGenerator)jgen;
        xgen.setNextIsAttribute(true);
        super.serializeWithType(bean, jgen, provider, typeSer);
        if (_attributeCount == 0) { // if no attributes, need to reset
            xgen.setNextIsAttribute(false);
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
