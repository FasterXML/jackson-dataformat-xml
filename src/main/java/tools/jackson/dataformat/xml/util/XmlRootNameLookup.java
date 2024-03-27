package tools.jackson.dataformat.xml.util;

import javax.xml.namespace.QName;

import tools.jackson.databind.*;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.AnnotatedClass;
import tools.jackson.databind.type.ClassKey;
import tools.jackson.databind.util.SimpleLookupCache;

/**
 * Helper class used for efficiently finding root element name used with
 * XML serializations.
 */
public class XmlRootNameLookup
    implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * If all we get to serialize is a null, there's no way to figure out
     * expected root name; so let's just default to literal {@code "null"}.
     */
    public final static QName ROOT_NAME_FOR_NULL = new QName("null");

    /**
     * For efficient operation, let's try to minimize number of times we
     * need to introspect root element name to use.
     */
    protected final transient SimpleLookupCache<ClassKey,QName> _rootNames;

    public XmlRootNameLookup() {
        _rootNames = new SimpleLookupCache<>(40, 200);
    }

    protected Object readResolve() {
        // just need to make 100% sure it gets set to non-null, that's all
        // 05-Jan-2020, tatu: How is that possibly, you ask? JDK serialization, that's how
        //   (it by-passes calls to constructors, as well as initializers)
        //   ... and if you don't believe, try commenting it out and see test failure you get
        if (_rootNames == null) {
            return new XmlRootNameLookup();
        }
        return this;
    }

    public QName findRootName(DatabindContext ctxt, JavaType rootType) {
        return findRootName(ctxt, rootType.getRawClass());
    }

    public QName findRootName(DatabindContext ctxt, Class<?> rootType)
    {
        ClassKey key = new ClassKey(rootType);
        QName name = _rootNames.get(key);
        if (name != null) {
            return name;
        }
        name = _findRootName(ctxt, rootType);
        _rootNames.put(key, name);
        return name;
    }

    protected QName _findRootName(DatabindContext ctxt, Class<?> rootType)
    {
        final AnnotatedClass ac = ctxt.introspectClassAnnotations(rootType);
        final AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        String localName = null;
        String ns = null;

        PropertyName root = intr.findRootName(ctxt.getConfig(), ac);
        if (root != null) {
            localName = root.getSimpleName();
            ns = root.getNamespace();
        }
        // No answer so far? Let's just default to using simple class name
        if (localName == null || localName.length() == 0) {
            // Should we strip out enclosing class tho? For now, nope:
            // one caveat: array simple names end with "[]"; also, "$" needs replacing
            localName = StaxUtil.sanitizeXmlTypeName(rootType.getSimpleName());
            return _qname(ns, localName);
        }
        // Otherwise let's see if there's namespace, too (if we are missing it)
        if (ns == null || ns.isEmpty()) {
            ns = _findNamespace(ctxt, intr, ac);
        }
        return _qname(ns, localName);
    }

    private QName _qname(String ns, String localName) {
        if (ns == null) { // some QName impls barf on nulls...
            ns = "";
        }
        return new QName(ns, localName);
    }

    private String _findNamespace(DatabindContext ctxt, AnnotationIntrospector ai,
            AnnotatedClass ann)
    {
        final MapperConfig<?> config = ctxt.getConfig();
        for (AnnotationIntrospector intr : ai.allIntrospectors()) {
            if (intr instanceof AnnotationIntrospector.XmlExtensions) {
                String ns = ((AnnotationIntrospector.XmlExtensions) intr).findNamespace(config, ann);
                if (ns != null) {
                    return ns;
                }
            }
        }
        return null;
    }
}
