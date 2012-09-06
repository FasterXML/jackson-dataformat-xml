package com.fasterxml.jackson.dataformat.xml.ser;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.ser.*;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.dataformat.xml.util.AnnotationUtil;
import com.fasterxml.jackson.dataformat.xml.util.XmlInfo;

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
            BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties)
    {
        final AnnotationIntrospector intr = config.getAnnotationIntrospector();
        for (int i = 0, len = beanProperties.size(); i < len; ++i) {
            BeanPropertyWriter bpw = beanProperties.get(i);
            final AnnotatedMember member = bpw.getMember();
            String ns = AnnotationUtil.findNamespaceAnnotation(intr, member);
            Boolean isAttribute = AnnotationUtil.findIsAttributeAnnotation(intr, member);
            Boolean isText = AnnotationUtil.findIsTextAnnotation(intr, member);
            bpw.setInternalSetting(XmlBeanSerializer.KEY_XML_INFO,
            		new XmlInfo(isAttribute, ns, isText));

            // Actually: if we have a Collection type, easiest place to add wrapping would be here...
            //  or: let's also allow wrapping of "untyped" (Object): assuming it is a dynamically
            //   typed Collection...
            if (_isContainerType(bpw.getType())) {
                String localName = null, wrapperNs = null;

                PropertyName wrappedName = PropertyName.construct(bpw.getName(), ns);
                PropertyName wrapperName = intr.findWrapperName(member);
                
                if (wrapperName != null) {
                    localName = wrapperName.getSimpleName();
                    wrapperNs = wrapperName.getNamespace();
                }
                /* 21-Aug-2012, tatu: Missing localName means "use property name as wrapper",
                 *   empty "no wrapper"
                 */
                if (localName == null) {
                    wrapperName = wrappedName;
                } else if (localName.length() == 0) {
                    // Empty wrapper name is explicit "DO NOT wrap" Lists indicator, so:
                    continue;
                } else {
                    wrapperName = PropertyName.construct(localName, (wrapperNs == null) ? "" : wrapperNs);
                }
                beanProperties.set(i, new XmlBeanPropertyWriter(bpw, wrapperName, wrappedName));
            }
        }
        return beanProperties;
    }
    
    @Override
    public JsonSerializer<?> modifySerializer(SerializationConfig config,
            BeanDescription beanDesc, JsonSerializer<?> serializer)
    {
        /* First things first: we can only handle real BeanSerializers; question
         * is, what to do if it's not one: throw exception or bail out?
         * For now let's do latter.
         */
        if (!(serializer instanceof BeanSerializerBase)) {
            return serializer;
        }
        return new XmlBeanSerializer((BeanSerializerBase) serializer);
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
}
