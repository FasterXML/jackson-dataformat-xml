package com.fasterxml.jackson.dataformat.xml.util;

import javax.xml.namespace.QName;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.type.ClassKey;
import com.fasterxml.jackson.databind.util.LRUMap;
import com.fasterxml.jackson.dataformat.xml.XmlAnnotationIntrospector;

/**
 * Helper class used for efficiently finding root element name used with
 * XML serializations.
 */
public class XmlRootNameLookup
    implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * For efficient operation, let's try to minimize number of times we
     * need to introspect root element name to use.
     *<p>
     * Note: changed to <code>transient</code> for 2.3; no point in serializing such
     * state
     */
    protected final transient LRUMap<ClassKey,QName> _rootNames = new LRUMap<ClassKey,QName>(40, 200);

    public XmlRootNameLookup() { }
    
    protected Object readResolve() {
        // just need to make 100% sure it gets set to non-null, that's all
        if (_rootNames == null) {
            return new XmlRootNameLookup();
        }
        return this;
    }

    public QName findRootName(JavaType rootType, MapperConfig<?> config) {
        return findRootName(rootType.getRawClass(), config);
    }

    public QName findRootName(Class<?> rootType, MapperConfig<?> config)
    {
        ClassKey key = new ClassKey(rootType);
        QName name;
        synchronized (_rootNames) {
            name = _rootNames.get(key);
        }
        if (name != null) {
            return name;
        }
        name = _findRootName(rootType, config);
        synchronized (_rootNames) {
            _rootNames.put(key, name);
        }
        return name;
    }
    
    // NOTE: needed to be synchronized in 2.6.4, but 2.7.0 adds a proper fix
    // for annotation introspection hence not needed any more
    protected QName _findRootName(Class<?> rootType, MapperConfig<?> config)
    {
        BeanDescription beanDesc = config.introspectClassAnnotations(rootType);
        AnnotationIntrospector intr = config.getAnnotationIntrospector();
        AnnotatedClass ac = beanDesc.getClassInfo();
        String localName = null;
        String ns = null;

        PropertyName root = intr.findRootName(ac);
        if (root != null) {
            localName = root.getSimpleName();
            ns = root.getNamespace();
        }
        // No answer so far? Let's just default to using simple class name
        if (localName == null || localName.length() == 0) {
            // Should we strip out enclosing class tho? For now, nope:
            // one caveat: array simple names end with "[]"; also, "$" needs replacing
            localName = StaxUtil.sanitizeXmlTypeName(rootType.getSimpleName());
            return new QName("", localName);
        }
        // Otherwise let's see if there's namespace, too (if we are missing it)
        if (ns == null || ns.length() == 0) {
            ns = findNamespace(intr, ac);
        }
        if (ns == null) { // some QName impls barf on nulls...
            ns = "";
        }
        return new QName(ns, localName);
    }

    private String findNamespace(AnnotationIntrospector ai, AnnotatedClass ann)
    {
        for (AnnotationIntrospector intr : ai.allIntrospectors()) {
            if (intr instanceof XmlAnnotationIntrospector) {
                String ns = ((XmlAnnotationIntrospector) intr).findNamespace(ann);
                if (ns != null) {
                    return ns;
                }
            }
        }
        return null;
    }
}
