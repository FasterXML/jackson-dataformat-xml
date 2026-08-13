package tools.jackson.dataformat.xml.ser;

import java.util.*;

import tools.jackson.databind.*;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.ser.*;
import tools.jackson.databind.ser.bean.BeanSerializerBase;
import tools.jackson.dataformat.xml.util.AnnotationUtil;
import tools.jackson.dataformat.xml.util.TypeUtil;
import tools.jackson.dataformat.xml.util.XmlInfo;

/**
 * We need a {@link ValueSerializerModifier} to replace default <code>BeanSerializer</code>
 * with XML-specific one; mostly to ensure that attribute properties are output
 * before element properties.
 */
public class XmlBeanSerializerModifier
    extends ValueSerializerModifier
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
            BeanDescription.Supplier beanDescRef, List<BeanPropertyWriter> beanProperties)
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

            // If we have a Collection/array type, the easiest place to add wrapping is here.
            // [dataformat-xml#8]: also allow wrapping of "untyped" (Object): assuming it may
            // be a dynamically typed Collection at runtime. Use dynamic wrapping so that
            // wrapping is only applied when runtime value is actually a Collection.
            final JavaType propType = bpw.getType();
            final boolean dynamicWrapping;

            if (TypeUtil.isIndexedType(propType)) {
                dynamicWrapping = false;
            } else if (propType.isJavaLangObject()
                    // [dataformat-xml#8]: for Object-typed properties, only wrap standard
                    // BeanPropertyWriters; skip virtual properties (e.g. @JsonAppend)
                    // and other custom subclasses whose get() won't survive wrapping
                    && bpw.getClass() == BeanPropertyWriter.class) {
                dynamicWrapping = true;
            } else {
                continue;
            }
            // [dataformat-xml#27]: Get inner element name via introspector
            // since property name may have been set to wrapper name
            // to avoid conflicts during bean introspection
            PropertyName wrappedName = AnnotationUtil.findXmlPropertyInnerName(config, intr, member);
            if (wrappedName == null) {
                wrappedName = PropertyName.construct(bpw.getName(), ns);
            }
            PropertyName wrapperName = bpw.getWrapperName();

            // first things first: no wrapping?
            if (wrapperName == null || wrapperName == PropertyName.NO_NAME) {
                // [dataformat-xml#627]/[dataformat-xml#871]: For an unwrapped
                // Collection/array property, a null value must be omitted (as in the
                // wrapped case below) rather than written as an `xsi:nil` element --
                // otherwise it is indistinguishable, on read, from a collection holding
                // a single null element. Only applies to concrete indexed types (not the
                // dynamic Object-typed case) and only to plain BeanPropertyWriters.
                if (!dynamicWrapping && bpw.getClass() == BeanPropertyWriter.class) {
                    beanProperties.set(i, new XmlNullSuppressingBeanPropertyWriter(bpw));
                }
                continue;
            }
            // no local name? Just double the wrapped name for wrapper
            String localName = wrapperName.getSimpleName();
            if (localName == null || localName.length() == 0) {
                wrapperName = wrappedName;
            }
            // [dataformat-xml#8]: for Object-typed properties, use dynamic wrapping
            // that checks at runtime if the value is actually a Collection
            beanProperties.set(i,
                    new XmlBeanPropertyWriter(bpw, wrapperName, wrappedName, dynamicWrapping));
        }
        return beanProperties;
    }
    
    @Override
    public ValueSerializer<?> modifySerializer(SerializationConfig config,
            BeanDescription.Supplier beanDescRefc, ValueSerializer<?> serializer)
    {
        // First things first: we can only handle real BeanSerializers; question
        // is, what to do if it's not one: throw exception or bail out?
        // For now let's do latter.
        if (!(serializer instanceof BeanSerializerBase)) {
            return serializer;
        }
        return new XmlBeanSerializer((BeanSerializerBase) serializer);
    }
}
