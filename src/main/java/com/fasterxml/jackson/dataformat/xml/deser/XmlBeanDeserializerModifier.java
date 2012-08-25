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
            // first: do we need to handle wrapping (for Lists)?
            QName wrapperName = AnnotationUtil.findWrapperName(intr, acc);
            if (wrapperName != null) {
                String localName = wrapperName.getLocalPart();
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
            // Unwrapped. Ok -- requires special handling
            // !!! TODO
        }
        return propDefs;
    }

}
