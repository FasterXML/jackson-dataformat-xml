package com.fasterxml.jackson.dataformat.xml.deser.convert;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;

import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

// 2020-12-18, tatu: Modified from "jackson-databind" version: XML
//  backend MUST NOT prevent coercion from String since XML has no
//  native number representation (although TBH JsonParser.isExpectedNumberInt()
//  can work around that in many cases)
public class CoerceStringToIntsTest
    extends XmlTestBase
{
    private final ObjectMapper DEFAULT_MAPPER = newMapper();
    private final ObjectMapper MAPPER_LEGACY_FAIL = mapperBuilder()
            .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
            .build();

    private final ObjectMapper MAPPER_TO_EMPTY; {
        MAPPER_TO_EMPTY = newMapper();
        MAPPER_TO_EMPTY.coercionConfigFor(LogicalType.Integer)
            .setCoercion(CoercionInputShape.String, CoercionAction.AsEmpty);
    }

    private final ObjectMapper MAPPER_TRY_CONVERT; {
        MAPPER_TRY_CONVERT = newMapper();
        MAPPER_TRY_CONVERT.coercionConfigFor(LogicalType.Integer)
            .setCoercion(CoercionInputShape.String, CoercionAction.TryConvert);
    }

    private final ObjectMapper MAPPER_TO_NULL; {
        MAPPER_TO_NULL = newMapper();
        MAPPER_TO_NULL.coercionConfigFor(LogicalType.Integer)
            .setCoercion(CoercionInputShape.String, CoercionAction.AsNull);
    }

    private final ObjectMapper MAPPER_TO_FAIL; {
        MAPPER_TO_FAIL = newMapper();
        MAPPER_TO_FAIL.coercionConfigFor(LogicalType.Integer)
            .setCoercion(CoercionInputShape.String, CoercionAction.Fail);
    }

    protected static class BooleanWrapper {
        public Boolean b;

        public BooleanWrapper() { }
        public BooleanWrapper(Boolean value) { b = value; }
    }

    protected static class IntWrapper {
        public int i;

        public IntWrapper() { }
        public IntWrapper(int value) { i = value; }
    }

    protected static class LongWrapper {
        public long l;

        public LongWrapper() { }
        public LongWrapper(long value) { l = value; }
    }

    protected static class DoubleWrapper {
        public double d;

        public DoubleWrapper() { }
        public DoubleWrapper(double value) { d = value; }
    }

    /*
    /********************************************************
    /* Test methods, legacy setting
    /********************************************************
     */

    // Works by default (as per databind defaulting); but also works
    // even if seemingly prevented -- this because XML has no native
    // number type and Strings present all scalar values, essentially

    public void testDefaultStringToIntCoercion() throws Exception {
        _verifyLegacyFromStringSucceeds(DEFAULT_MAPPER);
    }

    public void testLegacyFailStringToInt() throws Exception {
        _verifyLegacyFromStringSucceeds(MAPPER_LEGACY_FAIL);
    }

    private void _verifyLegacyFromStringSucceeds(ObjectMapper mapper) throws Exception
    {
        // by default, should be ok
        Integer I = DEFAULT_MAPPER.readValue("<Integer>28</Integer>", Integer.class);
        assertEquals(28, I.intValue());
        {
            IntWrapper w = DEFAULT_MAPPER.readValue("<IntWrapper><i>37</i></IntWrapper>",
                    IntWrapper.class);
            assertEquals(37, w.i);
        }

        Long L = DEFAULT_MAPPER.readValue("<Long>39</Long>", Long.class);
        assertEquals(39L, L.longValue());
        {
            LongWrapper w = DEFAULT_MAPPER.readValue("<LongWrapper><l>-13</l></LongWrapper>",
                    LongWrapper.class);
            assertEquals(-13L, w.l);
        }

        Short S = DEFAULT_MAPPER.readValue("<Short>42</Short>", Short.class);
        assertEquals(42, S.intValue());

        BigInteger biggie = DEFAULT_MAPPER.readValue("<BigInteger>95007</BigInteger>", BigInteger.class);
        assertEquals(95007, biggie.intValue());

        AtomicLong atom = DEFAULT_MAPPER.readValue("<AtomicLong>25236</AtomicLong>", AtomicLong.class);
        assertEquals(25236L, atom.get());
    }

    /*
    /********************************************************
    /* Test methods, CoerceConfig, integers-from-String
    /********************************************************
     */

    // When explicitly enabled, should pass

    public void testCoerceConfigStringToNull() throws Exception {
        _verifyCoercionFromStringSucceeds(MAPPER_TO_NULL);
    }

    // But even if blocked, or changed to null, should pass since with
    // XML, "String" is a native representation of numbers

    public void testCoerceConfigStringToEmpty() throws Exception {
        _verifyCoercionFromStringSucceeds(MAPPER_TO_EMPTY);
    }

    public void testCoerceConfigStringConvert() throws Exception {
        _verifyCoercionFromStringSucceeds(MAPPER_TRY_CONVERT);
    }

    public void testCoerceConfigFailFromString() throws Exception {
        _verifyCoercionFromStringSucceeds(MAPPER_TO_FAIL);
    }

    private void _verifyCoercionFromStringSucceeds(ObjectMapper mapper) throws Exception
    {
        assertEquals(Integer.valueOf(12), mapper.readValue("<Integer>12</Integer>", Integer.class));
        assertEquals(Integer.valueOf(34), mapper.readValue("<int>34</int>", Integer.TYPE));
        {
            IntWrapper w = mapper.readValue( "<IntWrapper i='-225' />", IntWrapper.class);
            assertEquals(-225, w.i);
        }

        assertEquals(Long.valueOf(34), mapper.readValue("<Long>34</Long>", Long.class));
        assertEquals(Long.valueOf(534), mapper.readValue("<long>534</long>", Long.TYPE));
        {
            LongWrapper w = mapper.readValue("<LongWrapper><l>-225</l></LongWrapper>",
                    LongWrapper.class);
            assertEquals(-225L, w.l);
        }

        assertEquals(Short.valueOf((short)12), mapper.readValue("<Short>12</Short>", Short.class));
        assertEquals(Short.valueOf((short) 344), mapper.readValue("<short>344</short>", Short.TYPE));

        assertEquals(Byte.valueOf((byte)12), mapper.readValue("<Byte>12</Byte>", Byte.class));
        assertEquals(Byte.valueOf((byte) -99), mapper.readValue("<byte>-99</byte>", Byte.TYPE));

        assertEquals(BigInteger.valueOf(1242L),
                mapper.readValue("<BigInteger>1242</BigInteger>", BigInteger.class));
    }
}
