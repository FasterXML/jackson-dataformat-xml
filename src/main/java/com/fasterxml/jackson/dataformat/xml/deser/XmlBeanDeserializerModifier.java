package com.fasterxml.jackson.dataformat.xml.deser;

import java.util.*;

import javax.xml.namespace.QName;

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
            QName wrapperName = AnnotationUtil.findWrapperName(intr, acc);
            if (wrapperName == null) {
                continue;
            }
            String localName = wrapperName.getLocalPart();
            if ((localName == null || localName.length() == 0)
                   || localName.equals(prop.getName())) {
                continue;
            }
            // make copy-on-write as necessary
            if (changed == 0) {
                propDefs = new ArrayList<BeanPropertyDefinition>(propDefs);
            }
            ++changed;
            // Also, must do upcast unfortunately
            propDefs.set(i, prop.withName(localName));
        }
        return propDefs;
    }

}
