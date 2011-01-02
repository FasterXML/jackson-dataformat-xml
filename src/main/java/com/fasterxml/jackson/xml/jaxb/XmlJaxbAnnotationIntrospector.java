package com.fasterxml.jackson.xml.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import com.fasterxml.jackson.xml.XmlAnnotationIntrospector;

/**
 * Alternative {@link AnnotationIntrospector} implementation that
 * builds on introspector from Jackson XC package that uses JAXB annotations,
 * not Jackson annotations.
 */
public class XmlJaxbAnnotationIntrospector
    extends JaxbAnnotationIntrospector
    implements XmlAnnotationIntrospector
{
    @Override
    public String findNamespace(Annotated ann)
    {
        String ns = null;

        /* 10-Oct-2009, tatus: I suspect following won't work quite
         *  as well as it should, wrt. defaulting to package.
         *  But it should work well enough to get things started --
         *  currently this method is not needed, and when it is,
         *  this can be improved.
         */

        if (ann instanceof AnnotatedClass) {
            /* For classes, it must be @XmlRootElement. Also, we do
             * want to use defaults from package, base class
             */
            XmlRootElement elem = findRootElementAnnotation((AnnotatedClass) ann);
            if (elem != null) {
                ns = elem.namespace();
            }
        } else {
            // For others, XmlElement or XmlAttribute work (anything else?)
            XmlElement elem = findAnnotation(XmlElement.class, ann, false, false, false);
            if (elem != null) {
                ns = elem.namespace();
            }
            if (ns == null || MARKER_FOR_DEFAULT.equals(ns)) {
                XmlAttribute attr = findAnnotation(XmlAttribute.class, ann, false, false, false);
                if (attr != null) {
                    ns = attr.namespace();
                }
            }
        }
        // JAXB uses marker for "not defined"
        if (MARKER_FOR_DEFAULT.equals(ns)) {
            ns = null;
        }
        return ns;
    }

    /**
     * Here we assume fairly simple logic; if there is <code>XmlAttribute</code> to be found,
     * we consider it an attibute; if <code>XmlElement</code>, not-an-attribute; and otherwise
     * we will consider there to be no information.
     * Caller is likely to default to considering things as elements.
     */
    @Override
    public Boolean isOutputAsAttribute(Annotated ann)
    {
        XmlAttribute attr = findAnnotation(XmlAttribute.class, ann, false, false, false);
        if (attr != null) {
            return Boolean.TRUE;
        }
        XmlElement elem = findAnnotation(XmlElement.class, ann, false, false, false);
        if (elem != null) {
            return Boolean.FALSE;
        }
        return null;
    }

    @Override
    public QName findWrapperElement(Annotated ann)
    {
        XmlElementWrapper w = findAnnotation(XmlElementWrapper.class, ann, false, false, false);
        if (w != null) {
            String ln = w.name();
            String ns = w.namespace();
            // if undefined, means "use property's name":
            if (MARKER_FOR_DEFAULT.equals(ln)) {
                ln = "";
            }
            return new QName(ns, ln);
        }
        return null;
    }

    /*
    /**********************************************************************
    /* Helper methods
    /**********************************************************************
     */
    
    private XmlRootElement findRootElementAnnotation(AnnotatedClass ac)
    {
        // Yes, check package, no class (already included), yes superclasses
        return findAnnotation(XmlRootElement.class, ac, true, false, true);
    }
}
