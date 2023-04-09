package tools.jackson.dataformat.xml.deser;

import java.math.BigDecimal;
import java.math.BigInteger;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestBase;

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

    public void testNumbersWithENotation() throws Exception
    {
        BigInteger bigInteger = new BigDecimal("2e308").toBigInteger();
        _testScalar(bigInteger, "<BigInteger>" + bigInteger + "</BigInteger>");
        BigDecimal bigDecimal = new BigDecimal("2e308");
        _testScalar(bigDecimal, "<BigDecimal>" + bigDecimal + "</BigDecimal>");
    }

    private void _testScalar(Object input, String exp) throws Exception {
        _testScalar(input, input.getClass(), exp);
    }

    private void _testScalar(Object input, Class<?> type, String exp) throws Exception
    {
        String xml = MAPPER.writeValueAsString(input).trim();
        assertEquals(exp, xml);
        Object result = MAPPER.readValue(xml, type);
        assertEquals(input, result);
    }
}
