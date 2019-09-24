package com.fasterxml.jackson.dataformat.xml.testutil;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

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
    public Validity validateBaseType(MapperConfig<?> c, JavaType baseType) {
        return Validity.ALLOWED;
    }

    // Important! With Default Typing, "validateBaseType()" won't necessarily
    // be called, at least for root type, so need more than above method:
    
    @Override
    public Validity validateSubClassName(MapperConfig<?> config,
            JavaType baseType, String subClassName) {
        return Validity.ALLOWED;
    }

    /*
    @Override
    public Validity validateSubType(MapperConfig<?> config, JavaType baseType,
            JavaType subType) {
        return Validity.ALLOWED;
    }
    */
}
