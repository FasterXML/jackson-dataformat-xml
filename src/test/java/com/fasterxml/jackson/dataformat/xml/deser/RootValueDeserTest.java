package com.fasterxml.jackson.dataformat.xml.deser;

import java.math.BigInteger;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

// New tests (2.12) for root-level values
public class RootValueDeserTest extends XmlTestBase
{
    private final XmlMapper MAPPER = newMapper();

    /*
    /**********************************************************
    /* Test methods, scalars
    /**********************************************************
     */

    public void testNumbers() throws Exception
    {
        _testScalar(Integer.valueOf(42), "<Integer>42</Integer>");
        _testScalar(Long.valueOf(-137L), "<Long>-137</Long>");
        _testScalar(Double.valueOf(0.25), "<Double>0.25</Double>");
        _testScalar(BigInteger.valueOf(31337), "<BigInteger>31337</BigInteger>");
    }

    private void _testScalar(Object input, String exp) throws Exception {
        _testScalar(input, input.getClass(), exp);
    }

    private void _testScalar(Object input, Class<?> type, String exp) throws Exception
    {
        String xml = MAPPER.writeValueAsString(input).trim();
        assertEquals(exp, xml);
        Object result = MAPPER.readValue(xml, input.getClass());
        assertEquals(input, result);
    }
}
