package com.fasterxml.jackson.dataformat.xml.ser;

import java.util.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.ser.*;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.dataformat.xml.util.AnnotationUtil;
import com.fasterxml.jackson.dataformat.xml.util.TypeUtil;
import com.fasterxml.jackson.dataformat.xml.util.XmlInfo;

/**
 * We need a {@link BeanSerializerModifier} to replace default <code>BeanSerializer</code>
 * with XML-specific one; mostly to ensure that attribute properties are output
 * before element properties.
 */
public class XmlBeanSerializerModifier
    extends BeanSerializerModifier
    implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    public XmlBeanSerializerModifier() { }

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
            String ns = AnnotationUtil.findNamespaceAnnotation(config, intr, member);
            Boolean isAttribute = AnnotationUtil.findIsAttributeAnnotation(config, intr, member);
            Boolean isText = AnnotationUtil.findIsTextAnnotation(config, intr, member);
            Boolean isCData = AnnotationUtil.findIsCDataAnnotation(config, intr, member);
            bpw.setInternalSetting(XmlBeanSerializerBase.KEY_XML_INFO,
            		new XmlInfo(isAttribute, ns, isText, isCData));

            // Actually: if we have a Collection type, easiest place to add wrapping would be here...
            //  or: let's also allow wrapping of "untyped" (Object): assuming it is a dynamically
            //   typed Collection...
            if (!TypeUtil.isIndexedType(bpw.getType())) {
                continue;
            }
            PropertyName wrappedName = PropertyName.construct(bpw.getName(), ns);
            PropertyName wrapperName = bpw.getWrapperName();

            // first things first: no wrapping?
            if (wrapperName == null || wrapperName == PropertyName.NO_NAME) {
                continue;
            }
            // no local name? Just double the wrapped name for wrapper
            String localName = wrapperName.getSimpleName();
            if (localName == null || localName.length() == 0) {
                wrapperName = wrappedName;
            }
            beanProperties.set(i, new XmlBeanPropertyWriter(bpw, wrapperName, wrappedName));
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
}
