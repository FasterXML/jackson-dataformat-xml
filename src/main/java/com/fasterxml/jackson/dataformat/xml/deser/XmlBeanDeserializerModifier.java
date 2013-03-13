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
    implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Virtual name used for text segments.
     */
    protected String _cfgNameForTextValue = "";

    public XmlBeanDeserializerModifier(String nameForTextValue)
    {
        _cfgNameForTextValue = nameForTextValue;
    }
    
    @Override
    public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config,
            BeanDescription beanDesc, List<BeanPropertyDefinition> propDefs)
    {
        final AnnotationIntrospector intr = config.getAnnotationIntrospector();
        int changed = 0;
        for (int i = 0, len = propDefs.size(); i < len; ++i) {
            BeanPropertyDefinition prop = propDefs.get(i);
            AnnotatedMember acc = prop.getPrimaryMember();
            // should not be null, but just in case:
            if (acc == null) {
                continue;
            }
            /* First: handle "as text"? Such properties
             * are exposed as values of 'unnamed' fields; so one way to
             * map them is to rename property to have name ""... (and
             * hope this does not break other parts...)
             */
            Boolean b = AnnotationUtil.findIsTextAnnotation(intr, acc);
            if (b != null && b.booleanValue()) {
                // unwrapped properties will appear as 'unnamed' (empty String)
                BeanPropertyDefinition newProp = prop.withName(_cfgNameForTextValue);
                if (newProp != prop) {
                    propDefs.set(i, newProp);
                }
                continue;
            }
            // second: do we need to handle wrapping (for Lists)?
            PropertyName wrapperName = prop.getWrapperName();
            
            if (wrapperName != null && wrapperName != PropertyName.NO_NAME) {
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
                // otherwise unwrapped; needs handling but later on
            }
        }
        return propDefs;
    }

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
