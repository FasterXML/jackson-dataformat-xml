package com.fasterxml.jackson.dataformat.xml.testutil;

import com.fasterxml.jackson.databind.*;
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
    public Validity validateBaseType(DatabindContext c, JavaType baseType) {
        return Validity.ALLOWED;
    }
}