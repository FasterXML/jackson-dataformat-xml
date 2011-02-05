package com.fasterxml.jackson.xml.ser;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.ser.BeanPropertyWriter;
import org.codehaus.jackson.map.ser.BeanSerializer;
import org.codehaus.jackson.map.ser.BeanSerializerModifier;
import org.codehaus.jackson.type.JavaType;

import com.fasterxml.jackson.xml.XmlAnnotationIntrospector;
import com.fasterxml.jackson.xml.util.XmlInfo;

/**
 * We need a {@link BeanSerializerModifier} to replace default <code>BeanSerializer</code>
 * with XML-specific one; mostly to ensure that attribute properties are output
 * before element properties.
 */
public class XmlBeanSerializerModifier extends BeanSerializerModifier
{
    /*
    /**********************************************************
    /* Overridden methods
    /**********************************************************
     */

    /**
     * First thing to do is to find annotations regarding XML serialization,
     * and wrap collection serializers.
     */
    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
            BasicBeanDescription beanDesc, List<BeanPropertyWriter> beanProperties)
    {
        AnnotationIntrospector intr = config.getAnnotationIntrospector();
        for (int i = 0, len = beanProperties.size(); i < len; ++i) {
            BeanPropertyWriter bpw = beanProperties.get(i);
            final AnnotatedMember member = bpw.getMember();
            String ns = findNamespaceAnnotation(intr, member);
            Boolean isAttribute = findIsAttributeAnnotation(intr, member);
            bpw.setInternalSetting(XmlBeanSerializer.KEY_XML_INFO, new XmlInfo(isAttribute, ns));

            // Actually: if we have a Collection type, easiest place to add wrapping would be here...
            if (_isContainerType(bpw.getType())) {
                String localName = null, wrapperNs = null;

                QName wrappedName = new QName(ns, bpw.getName());
                QName wrapperName = findWrapperName(intr, member);
                if (wrapperName != null) {
                    localName = wrapperName.getLocalPart();
                    wrapperNs = wrapperName.getNamespaceURI();
                }
                /* Empty/missing localName means "use property name as wrapper"; later on
                 * should probably make missing (null) mean "don't add a wrapper"
                 */
                if (localName == null || localName.length() == 0) {
                    wrapperName = wrappedName;
                } else {
                    wrapperName = new QName((wrapperNs == null) ? "" : wrapperNs, localName);
                }
                beanProperties.set(i, new XmlBeanPropertyWriter(bpw, wrapperName, wrappedName));
            }
        }
        return beanProperties;
    }
    
    @Override
    public JsonSerializer<?> modifySerializer(SerializationConfig config,
            BasicBeanDescription beanDesc, JsonSerializer<?> serializer)
    {
        /* First things first: we can only handle real BeanSerializers; question
         * is, what to do if it's not one: throw exception or bail out?
         * For now let's do latter.
         */
        if (!(serializer instanceof BeanSerializer)) {
            return serializer;
        }
        return new XmlBeanSerializer((BeanSerializer) serializer);
    }

    /*
    /**********************************************************
    /* Helper methods
    /**********************************************************
     */
    
    /**
     * Helper method used for figuring out if given raw type is a collection ("indexed") type;
     * in which case a wrapper element is typically added.
     */
    private static boolean _isContainerType(JavaType type)
    {
        if (type.isContainerType()) {
            Class<?> cls = type.getRawClass();
            // One special case; byte[] will be serialized as base64-encoded String, not real array, so:
            // (actually, ditto for char[]; thought to be a String)
            if (cls == byte[].class || cls == byte[].class) {
                return false;
            }
            // issue#5: also, should not add wrapping for Maps
            if (Map.class.isAssignableFrom(cls)) {
                return false;
            }
            return true;
        }
        return false;
    }
    
    private static String findNamespaceAnnotation(AnnotationIntrospector ai, AnnotatedMember prop)
    {
        for (AnnotationIntrospector intr : ai.allIntrospectors()) {
            if (intr instanceof XmlAnnotationIntrospector) {
                String ns = ((XmlAnnotationIntrospector) intr).findNamespace(prop);
                if (ns != null) {
                    return ns;
                }
            }
        }
        return null;
    }

    private static Boolean findIsAttributeAnnotation(AnnotationIntrospector ai, AnnotatedMember prop)
    {
        for (AnnotationIntrospector intr : ai.allIntrospectors()) {
            if (intr instanceof XmlAnnotationIntrospector) {
                Boolean b = ((XmlAnnotationIntrospector) intr).isOutputAsAttribute(prop);
                if (b != null) {
                    return b;
                }
            }
        }
        return null;
    }

    private static QName findWrapperName(AnnotationIntrospector ai, AnnotatedMember prop)
    {
        for (AnnotationIntrospector intr : ai.allIntrospectors()) {
            if (intr instanceof XmlAnnotationIntrospector) {
                QName n = ((XmlAnnotationIntrospector) intr).findWrapperElement(prop);
                if (n != null) {
                    return n;
                }
            }
        }
        return null;
    }
    
}
