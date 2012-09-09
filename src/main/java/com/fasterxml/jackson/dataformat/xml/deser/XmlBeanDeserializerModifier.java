package com.fasterxml.jackson.dataformat.xml.deser;

import java.util.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.dataformat.xml.util.AnnotationUtil;

/**
 * The main reason for a modifier is to support handling of
 * 'wrapped' Collection types.
 */
public class XmlBeanDeserializerModifier
    extends BeanDeserializerModifier
{
    @Override
    public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config,
            BeanDescription beanDesc, List<BeanPropertyDefinition> propDefs)
    {
        final AnnotationIntrospector intr = config.getAnnotationIntrospector();
        int changed = 0;
        for (int i = 0, len = propDefs.size(); i < len; ++i) {
            BeanPropertyDefinition prop = propDefs.get(i);
            AnnotatedMember acc = prop.getAccessor();
            // should not be null, but just in case:
            if (acc == null) {
                continue;
            }
            // first: do we need to handle wrapping (for Lists)?
            PropertyName wrapperName = intr.findWrapperName(acc);
            if (wrapperName != null) {
                String localName = wrapperName.getSimpleName();
                if ((localName != null && localName.length() > 0)
                        && !localName.equals(prop.getName())) {
                    // make copy-on-write as necessary
                    if (changed == 0) {
                        propDefs = new ArrayList<BeanPropertyDefinition>(propDefs);
                    }
                    ++changed;
                    propDefs.set(i, prop.withName(localName));
                    continue;
                }
            } else {
                /* If not, how about "as text" unwrapping? Such properties
                 * are exposed as values of 'unnamed' fields; so one way to
                 * map them is to rename property to have name ""... (and
                 * hope this does not break other parts...)
                 */
                Boolean b = AnnotationUtil.findIsTextAnnotation(intr, acc);
                if (b != null && b.booleanValue()) {
                    // unwrapped properties will appear as 'unnamed' (empty String)
                    propDefs.set(i, prop.withName(""));
                    continue;
                }
            }
            // otherwise unwrapped; needs handling but later on
        }
        return propDefs;
    }

    /*
    @Override
    public BeanDeserializerBuilder updateBuilder(DeserializationConfig config,
            BeanDescription beanDesc, BeanDeserializerBuilder builder)
    {
        Iterator<SettableBeanProperty> it = builder.getProperties();
        while (it.hasNext()) {
            SettableBeanProperty prop = it.next();
            System.out.println("Builder, prop '"+prop.getName()+"', type "+prop.getType()+", hasSer "+prop.hasValueDeserializer());
        }
        return builder;
    }
    */

    @Override
    public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
            BeanDescription beanDesc, JsonDeserializer<?> deserializer)
    {
        if (!(deserializer instanceof BeanDeserializerBase)) {
            return deserializer;
        }
        return new WrapperHandlingDeserializer((BeanDeserializerBase) deserializer);
    }
}
