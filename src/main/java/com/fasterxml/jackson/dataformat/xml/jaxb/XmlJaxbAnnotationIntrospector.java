package com.fasterxml.jackson.dataformat.xml.jaxb;

import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/**
 * Alternative {@link com.fasterxml.jackson.databind.AnnotationIntrospector}
 * implementation that
 * builds on {@link JaxbAnnotationIntrospector}.
 *<p>
 * NOTE: since version 2.4, it may NOT be necessary to use this class;
 * instead, plain {@link JaxbAnnotationIntrospector} should fully work.
 * With previous versions some aspects were not fully working and this
 * class was necessary.
 */
public class XmlJaxbAnnotationIntrospector
    extends JaxbAnnotationIntrospector
    implements XmlAnnotationIntrospector
{
    private static final long serialVersionUID = 1L; // since 2.7

    @Deprecated
    public XmlJaxbAnnotationIntrospector() {
        super();
    }

    public XmlJaxbAnnotationIntrospector(TypeFactory typeFactory) {
        super(typeFactory);
    }
    
    /*
    /**********************************************************************
    /* XmlAnnotationIntrospector overrides
    /**********************************************************************
     */
    
    @Override
    public String findNamespace(Annotated ann) {
        return super.findNamespace(ann);
    }

    @Override
    public Boolean isOutputAsAttribute(Annotated ann) {
        return super.isOutputAsAttribute(ann);
    }
    
    @Override
    public Boolean isOutputAsText(Annotated ann) {
        return super.isOutputAsText(ann);
    }

    @Override
    public Boolean isOutputAsCData(Annotated ann) {
        //There is no CData annotation in JAXB
        return null;
    }

    @Override
    public void setDefaultUseWrapper(boolean b) {
        // nothing to do with JAXB
    }
}
