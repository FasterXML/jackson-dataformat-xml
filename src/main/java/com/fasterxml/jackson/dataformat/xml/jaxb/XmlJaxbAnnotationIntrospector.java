package com.fasterxml.jackson.dataformat.xml.jaxb;

import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlAnnotationIntrospector;

/**
 * Alternative {@link com.fasterxml.jackson.databind.AnnotationIntrospector}
 * implementation that
 * that USED TO build on {@code JaxbAnnotationIntrospector} from
 * {@code jackson-module-jaxb-annotatins} package.
 * But as of Jackson 2.13, SHOULD NOT BE USED as it DOES NOT WORK.
 * Removal was necessary because XML format package does not (and can not)
 * depend on JAXB annotations.
 * Class WILL BE REMOVED from Jackson 2.14 or later on.
 *<p>
 *
 * @deprecated Since 2.12 (as per above notes): broken since 2.13
 */
@Deprecated
public class XmlJaxbAnnotationIntrospector
    extends NopAnnotationIntrospector
    implements XmlAnnotationIntrospector
{
    private static final long serialVersionUID = 1L; // since 2.7

    @Deprecated
    public XmlJaxbAnnotationIntrospector() {
        super();
    }

    public XmlJaxbAnnotationIntrospector(TypeFactory typeFactory) {
        super();
    }

    /*
    /**********************************************************************
    /* XmlAnnotationIntrospector overrides
    /**********************************************************************
     */

    @Override
    public String findNamespace(MapperConfig<?> config, Annotated ann) {
        return null;
    }

    @Override
    public Boolean isOutputAsAttribute(MapperConfig<?> config, Annotated ann) {
        return null;
    }

    @Override
    public Boolean isOutputAsText(MapperConfig<?> config, Annotated ann) {
        return null;
    }

    @Override
    public Boolean isOutputAsCData(MapperConfig<?> config, Annotated ann) {
        return null;
    }
}
