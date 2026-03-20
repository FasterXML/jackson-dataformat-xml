package tools.jackson.dataformat.xml.deser;

import java.util.*;

import tools.jackson.databind.*;
import tools.jackson.databind.deser.*;
import tools.jackson.databind.deser.bean.BeanDeserializerBase;
import tools.jackson.databind.deser.std.DelegatingDeserializer;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.introspect.BeanPropertyDefinition;
import tools.jackson.dataformat.xml.util.AnnotationUtil;

/**
 * The main reason for a modifier is to support handling of
 * 'wrapped' Collection types.
 */
public class XmlBeanDeserializerModifier
    extends ValueDeserializerModifier
    implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Virtual name used for text segments.
     */
    protected final String _cfgNameForTextValue;

    public XmlBeanDeserializerModifier(String nameForTextValue)
    {
        _cfgNameForTextValue = nameForTextValue;
    }

    @Override
    public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config,
            BeanDescription.Supplier beanDescRef, List<BeanPropertyDefinition> propDefs)
    {
        final AnnotationIntrospector intr = config.getAnnotationIntrospector();
        int changed = 0;

        for (int i = 0, propCount = propDefs.size(); i < propCount; ++i) {
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
            Boolean b = AnnotationUtil.findIsTextAnnotation(config, intr, acc);
            if (b != null && b.booleanValue()) {
                // [dataformat-xml#559] For records with JAXB @XmlValue: the annotation
                // introspector may rename the property (e.g. to "value"), causing a
                // property definition split where the constructor parameter ends up in
                // a separate property under the original Java name. If we detect this
                // split, rename the constructor-parameter property to the text value
                // name and remove this (getter-only) one.
                String memberName = acc.getName();
                if (!memberName.equals(prop.getName())) {
                    int splitIdx = _findSplitProperty(propDefs, memberName, i);
                    if (splitIdx >= 0) {
                        // make copy-on-write as necessary
                        if (changed == 0) {
                            propDefs = new ArrayList<>(propDefs);
                        }
                        ++changed;
                        // Rename the split counterpart (which has the constructor param)
                        propDefs.set(splitIdx, propDefs.get(splitIdx).withSimpleName(_cfgNameForTextValue));
                        // Remove this getter-only property definition
                        propDefs.remove(i);
                        --propCount;
                        --i; // re-examine this index
                        continue;
                    }
                }
                // Default: just rename this property
                BeanPropertyDefinition newProp = prop.withSimpleName(_cfgNameForTextValue);
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
                    propDefs.set(i, prop.withSimpleName(localName));
                    continue;
                }
                // otherwise unwrapped; needs handling but later on
            }
        }
        return propDefs;
    }

    /**
     * [dataformat-xml#559] Find a property definition that was split from the isText
     * property due to annotation-introspector-driven renaming (e.g. JAXB @XmlValue
     * assigning implicit name "value" while the constructor param keeps the Java name).
     *
     * @param memberName Java member name of the isText property's primary member
     * @param excludeIdx index to skip (the isText property itself)
     * @return index of the split counterpart, or -1 if not found
     */
    private int _findSplitProperty(List<BeanPropertyDefinition> propDefs,
            String memberName, int excludeIdx)
    {
        for (int j = 0, len = propDefs.size(); j < len; ++j) {
            if (j == excludeIdx) continue;
            if (memberName.equals(propDefs.get(j).getName())) {
                return j;
            }
        }
        return -1;
    }

    @Override
    public ValueDeserializer<?> modifyDeserializer(DeserializationConfig config,
            BeanDescription.Supplier beanDescRef, ValueDeserializer<?> deser)
    {
        if (deser instanceof BeanDeserializerBase bdb) {
            return _modifyBeanDeserializer(config, bdb);
        }
        // [dataformat-xml#334]: If a user's DeserializerModifier has wrapped the
        //   BeanDeserializer in a DelegatingDeserializer, unwrap to find the
        //   underlying BeanDeserializerBase, process it, and rebuild the chain.
        if (deser instanceof DelegatingDeserializer dd) {
            return _modifyThroughDelegation(config, dd);
        }
        return deser;
    }

    // @since 3.2.0
    protected ValueDeserializer<?> _modifyBeanDeserializer(DeserializationConfig config,
            BeanDeserializerBase deser)
    {
        /* 17-Aug-2013, tatu: One important special case first: if we have one "XML Text"
         * property, it may be exposed as VALUE_STRING token (depending on whether any attribute
         * values are exposed): and to deserialize from that, we need special handling unless POJO
         * has appropriate single-string creator method.
         */
        // Heuristics are bit tricky; but for now let's assume that if POJO
        // can already work with VALUE_STRING, it's ok and doesn't need extra support
        ValueInstantiator inst = deser.getValueInstantiator();
        // 03-Aug-2017, tatu: [dataformat-xml#254] suggests we also should
        //    allow passing `int`/`Integer`/`long`/`Long` cases, BUT
        //    unfortunately we can not simply use default handling. Would need
        //    coercion.
        // 30-Apr-2020, tatu: Complication from [dataformat-xml#318] as we now
        //    have a delegate too...
        // [dataformat-xml#608]: relaxed from earlier check that required all
        // non-text properties to be attributes; now handles beans with
        // @JacksonXmlText alongside other element properties too.
        if (!inst.canCreateFromString()) {
            SettableBeanProperty textProp = _findTextProp(deser.properties());
            if (textProp != null) {
                return new XmlTextDeserializer(deser, textProp);
            }
        }
        return new WrapperHandlingDeserializer(deser);
    }

    // @since 3.2.0
    protected ValueDeserializer<?> _modifyThroughDelegation(DeserializationConfig config,
            DelegatingDeserializer deser)
    {
        ValueDeserializer<?> delegatee = deser.getDelegatee();
        ValueDeserializer<?> modifiedDelegatee;
        if (delegatee instanceof BeanDeserializerBase bdb) {
            modifiedDelegatee = _modifyBeanDeserializer(config, bdb);
        } else if (delegatee instanceof DelegatingDeserializer dd) {
            modifiedDelegatee = _modifyThroughDelegation(config, dd);
        } else {
            // Delegatee is not a type we can handle
            return deser;
        }
        if (modifiedDelegatee != delegatee) {
            return deser.replaceDelegatee(modifiedDelegatee);
        }
        return deser;
    }

    /**
     * Find the {@code @JacksonXmlText} property (renamed to {@code _cfgNameForTextValue}
     * by {@link #updateProperties}) if one exists.
     *<p>
     * NOTE: before [dataformat-xml#608] fix, this method also required all non-text
     * properties to be attributes; relaxed to allow element properties too.
     */
    private SettableBeanProperty _findTextProp(Iterator<SettableBeanProperty> propIt)
    {
        while (propIt.hasNext()) {
            SettableBeanProperty prop = propIt.next();
            PropertyName n = prop.getFullName();
            if (_cfgNameForTextValue.equals(n.getSimpleName())) {
                return prop;
            }
        }
        return null;
    }
}
