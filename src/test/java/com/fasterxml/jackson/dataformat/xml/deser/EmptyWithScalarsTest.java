package com.fasterxml.jackson.dataformat.xml.deser;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

// [dataformat-xml#473]: 2.11 -> 2.12 coercion of empty to "default"
// [dataformat-xml#474]: no failure for primitives, no null
public class EmptyWithScalarsTest extends XmlTestBase
{
    @JacksonXmlRootElement(localName = "w")
    static class NumbersPrimitive {
        public int i = 1;
        public long l = 2L;

        public double d = 0.5;
        public float f = 0.25f;
    }

    @JacksonXmlRootElement(localName = "w")
    static class NumbersWrapper {
        public Integer I = Integer.valueOf(1);
        public Long L = Long.valueOf(1L);

        public Double D = Double.valueOf(0.5);
        public Float F = Float.valueOf(0.5f);
    }

    @JacksonXmlRootElement(localName = "w")
    static class NumbersOther {
        public BigInteger bi = BigInteger.ONE;
        public BigDecimal bd = BigDecimal.ONE;
    }

    @JacksonXmlRootElement(localName = "w")
    static class MiscOther {
        public Boolean B = Boolean.TRUE;
    }

    private final XmlMapper MAPPER = newMapper();

    /*
    /**********************************************************************
    /* Test methods, Numbers / primitive
    /**********************************************************************
     */

    public void testPrimitiveIntsWithEmpty() throws Exception
    {
        NumbersPrimitive p = MAPPER.readValue(_emptyWrapped("i"),
                NumbersPrimitive.class);
        assertEquals(0, p.i);
        p = MAPPER.readValue(_emptyWrapped("l"),
                NumbersPrimitive.class);
        assertEquals(0L, p.l);
    }

    public void testPrimitiveFPsWithEmpty() throws Exception
    {
        NumbersPrimitive p = MAPPER.readValue(_emptyWrapped("d"),
                NumbersPrimitive.class);
        assertEquals(0d, p.d);
        p = MAPPER.readValue(_emptyWrapped("f"),
                NumbersPrimitive.class);
        assertEquals(0f, p.f);
    }

    // [dataformat-xml#474]: no failure for primitives, no null
    // (will try to fix in 2.13, but not 2.12)
    public void testPrimitivesNoNulls() throws Exception
    {
        ObjectReader r = MAPPER
                .readerFor(NumbersPrimitive.class)
                .with(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        _testPrimitivesNoNulls(r, _emptyWrapped("i"));
        _testPrimitivesNoNulls(r, _emptyWrapped("l"));
        _testPrimitivesNoNulls(r, _emptyWrapped("d"));
        _testPrimitivesNoNulls(r, _emptyWrapped("f"));
    }

    private void _testPrimitivesNoNulls(ObjectReader r, String doc) throws Exception
    {
        try {
            r.readValue(_emptyWrapped("i"));
            fail("Should not pass");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot coerce `null` to ");
        }
    }

    /*
    /**********************************************************************
    /* Test methods, Numbers / wrapper (or Object)
    /**********************************************************************
     */

    public void testIntegralsWithEmpty() throws Exception
    {
        NumbersWrapper w = MAPPER.readValue(_emptyWrapped("I"),
                NumbersWrapper.class);
        assertNull(w.I);
        w = MAPPER.readValue(_emptyWrapped("L"),
                NumbersWrapper.class);
        assertNull(w.L);

        NumbersOther o = MAPPER.readValue(_emptyWrapped("bi"),
                NumbersOther.class);
        assertNull(o.bi);
    }

    public void testFPWithEmpty() throws Exception
    {
        NumbersWrapper w = MAPPER.readValue(_emptyWrapped("D"),
                NumbersWrapper.class);
        assertNull(w.D);
        w = MAPPER.readValue(_emptyWrapped("F"),
                NumbersWrapper.class);
        assertNull(w.F);

        NumbersOther o = MAPPER.readValue(_emptyWrapped("bd"),
                NumbersOther.class);
        assertNull(o.bd);
    }

    /*
    /**********************************************************************
    /* Test methods, otber Scalars
    /**********************************************************************
     */

    public void testOtherScalarWithEmpty() throws Exception
    {
        MiscOther o = MAPPER.readValue(_emptyWrapped("B"),
                MiscOther.class);
        assertNull(o.B);
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    private String _emptyWrapped(String name) {
        return _simpleWrapped(name, "");
    }

    private String _simpleWrapped(String name, String value) {
        return "<w>\n"
                +"<"+name+">"+value+"</"+name+">\n"
                +"</w>\n";
    }
}
