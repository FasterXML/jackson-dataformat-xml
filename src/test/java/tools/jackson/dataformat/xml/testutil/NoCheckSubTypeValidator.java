package tools.jackson.dataformat.xml.testutil;

import tools.jackson.databind.*;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

/**
 * Test-only {@link PolymorphicTypeValidator} used by tests that should not block
 * use of any subtypes.
 */
public final class NoCheckSubTypeValidator
    extends PolymorphicTypeValidator.Base
{
    private static final long serialVersionUID = 1L;

    public final static NoCheckSubTypeValidator instance = new NoCheckSubTypeValidator(); 

    @Override
    public Validity validateBaseType(DatabindContext c, JavaType baseType) {
        return Validity.ALLOWED;
    }

    // Important! With Default Typing, "validateBaseType()" won't necessarily
    // be called, at least for root type, so need more than above method:
    
    @Override
    public Validity validateSubClassName(DatabindContext ctxt, JavaType baseType, String subClassName) {
        return Validity.ALLOWED;
    }

    /*
    @Override
        public Validity validateSubType(DatabindContext ctxt, JavaType baseType, JavaType subType)
        return Validity.ALLOWED;
    }
    */
}
