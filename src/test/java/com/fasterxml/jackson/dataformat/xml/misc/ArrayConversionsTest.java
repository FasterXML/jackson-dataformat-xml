package com.fasterxml.jackson.dataformat.xml.misc;

import java.util.*;
import java.lang.reflect.Array;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

/* NOTE: copied from jackson-databind (with some pruning)
 */

/**
 * Conversion tests to ensure that standard ObjectMapper conversions
 * work despite XmlMapper having to add XML-specific work-arounds.
 */
public class ArrayConversionsTest extends XmlTestBase
{
    static class IntListWrapper {
        public List<Integer> values;
    }

    static class IntArrayWrapper {
        public int[] values;

        public IntArrayWrapper() { }
        public IntArrayWrapper(int[] v) { values = v; }
    }
    
    /*
    /********************************************************
    /* Test methods
    /********************************************************
     */

    public void testNullXform() throws Exception {
        _testNullXform(xmlMapper(true));
        _testNullXform(xmlMapper(false));
    }
    
    private void _testNullXform(ObjectMapper mapper) throws Exception
    {
        // when given null, null should be returned without conversion (Java null has no type)
        assertNull(mapper.convertValue(null, Integer.class));
        assertNull(mapper.convertValue(null, String.class));
        assertNull(mapper.convertValue(null, byte[].class));
    }

    /**
     * Tests to verify that primitive number arrays round-trip
     * correctly, i.e. type -> type gives equal (although
     * not necessarily same) output
     */
    public void testArrayIdentityTransforms() throws Exception {
        _testArrayIdentityTransforms(xmlMapper(true));
        _testArrayIdentityTransforms(xmlMapper(false));
    }

    private void _testArrayIdentityTransforms(ObjectMapper mapper) throws Exception
    {
        // first integral types
        // (note: byte[] is ok, even if it goes to base64 and back)
        verifyByteArrayConversion(mapper, bytes(), byte[].class);
        verifyShortArrayConversion(mapper, shorts(), short[].class);
        verifyIntArrayConversion(mapper, ints(), int[].class);
        verifyLongArrayConversion(mapper, longs(), long[].class);
        // then primitive decimal types
        verifyFloatArrayConversion(mapper, floats(), float[].class);
        verifyDoubleArrayConversion(mapper, doubles(), float[].class);
    }

    public void testByteArrayFrom() throws Exception {
        _testByteArrayFrom(xmlMapper(true));
        _testByteArrayFrom(xmlMapper(false));
    }

    private void _testByteArrayFrom(ObjectMapper mapper) throws Exception
    {
        /* Note: byte arrays are tricky, since they are considered
         * binary data primarily, not as array of numbers. Hence
         * output will be base64 encoded...
         */
        byte[] data = _convert(mapper, "c3VyZS4=", byte[].class);
        byte[] exp = "sure.".getBytes("Ascii");
        verifyIntegralArrays(exp, data, exp.length);
    }
    
    public void testShortArrayToX() throws Exception
    {
        final XmlMapper mapper = new XmlMapper();
        short[] data = shorts();
        verifyShortArrayConversion(mapper, data, byte[].class);
        verifyShortArrayConversion(mapper, data, int[].class);
        verifyShortArrayConversion(mapper, data, long[].class);
    }

    public void testIntArrayToX() throws Exception
    {
        final XmlMapper mapper = new XmlMapper();

        int[] data = ints();
        verifyIntArrayConversion(mapper, data, byte[].class);
        verifyIntArrayConversion(mapper, data, short[].class);
        verifyIntArrayConversion(mapper, data, long[].class);

        List<Number> expNums = _numberList(data, data.length);
        // Alas, due to type erasure, need to use TypeRef, not just class
        List<Integer> actNums = mapper.convertValue(data, new TypeReference<List<Integer>>() {});
        assertEquals(expNums, actNums);
    }

    public void testLongArrayToX() throws Exception
    {
        final XmlMapper mapper = new XmlMapper();
        long[] data = longs();
        verifyLongArrayConversion(mapper, data, byte[].class);
        verifyLongArrayConversion(mapper, data, short[].class);
        verifyLongArrayConversion(mapper, data, int[].class);
 
        List<Number> expNums = _numberList(data, data.length);
        List<Long> actNums = mapper.convertValue(data, new TypeReference<List<Long>>() {});
        assertEquals(expNums, actNums);        
    }

    public void testListToIntArray() throws Exception
    {
        _testListToIntArray(true);
        _testListToIntArray(false);
    }

    private void _testListToIntArray(boolean wrap) throws Exception
    {
        final XmlMapper mapper = xmlMapper(wrap);
        List<Integer> in = new ArrayList<Integer>();
        in.add(1);
        in.add(2);
        in.add(3);
        int[] out = mapper.convertValue(in, int[].class);
        assertEquals(3, out.length);
        for (int i = 0; i < out.length; ++i) {
            assertEquals(i+1, out[i]);
        }
    }
    
    public void testListAsProperty() throws Exception
    {
        _testListAsProperty(true);
        _testListAsProperty(false);
    }

    private void _testListAsProperty(boolean wrap) throws Exception
    {
        final XmlMapper mapper = xmlMapper(wrap);
        IntListWrapper mid = mapper.convertValue(new IntArrayWrapper(new int[] { 1, 2, 3}),
                IntListWrapper.class);
        assertNotNull(mid);
        assertNotNull(mid.values);
        assertEquals(3, mid.values.size());

        IntArrayWrapper output = mapper.convertValue(mid, IntArrayWrapper.class);
        assertEquals(3, output.values.length);
        assertEquals(3, output.values[2]);
    }
    
    /*
    /********************************************************
    /* Helper methods
    /********************************************************
     */

    // note: all value need to be within byte range
    
    private byte[] bytes() { return new byte[] { 1, -1, 0, 98, 127 }; }
    private short[] shorts() { return new short[] { 1, -1, 0, 98, 127 }; }
    private int[] ints() { return new int[] { 1, -1, 0, 98, 127 }; }
    private long[] longs() { return new long[] { 1, -1, 0, 98, 127 }; }

    // note: use values that are exact in binary

    private double[] doubles() { return new double[] { 0.0, 0.25, -0.125, 10.5, 9875.0 }; }
    private float[] floats() { return new float[] {
            0.0f, 0.25f, -0.125f, 10.5f, 9875.0f };
    }

    private <T> void verifyByteArrayConversion(ObjectMapper mapper, byte[] data, Class<T> arrayType) {
        T result = _convert(mapper, data, arrayType);
        verifyIntegralArrays(data, result, data.length);
    }
    private <T> void verifyShortArrayConversion(ObjectMapper mapper, short[] data, Class<T> arrayType) {
        T result = _convert(mapper, data, arrayType);
        verifyIntegralArrays(data, result, data.length);
    }
    private <T> void verifyIntArrayConversion(ObjectMapper mapper, int[] data, Class<T> arrayType) {
        T result = _convert(mapper, data, arrayType);
        verifyIntegralArrays(data, result, data.length);
    }
    private <T> void verifyLongArrayConversion(ObjectMapper mapper, long[] data, Class<T> arrayType) {
        T result = _convert(mapper, data, arrayType);
        verifyIntegralArrays(data, result, data.length);
    }
    private <T> void verifyFloatArrayConversion(ObjectMapper mapper, float[] data, Class<T> arrayType) {
        T result = _convert(mapper, data, arrayType);
        verifyDoubleArrays(data, result, data.length);
    }
    private <T> void verifyDoubleArrayConversion(ObjectMapper mapper, double[] data, Class<T> arrayType) {
        T result = _convert(mapper, data, arrayType);
        verifyDoubleArrays(data, result, data.length);
    }
    
    private <T> T _convert(ObjectMapper mapper, Object input, Class<T> outputType)
    {
        // must be a primitive array, like "int[].class"
        if (!outputType.isArray()) throw new IllegalArgumentException();
        if (!outputType.getComponentType().isPrimitive()) throw new IllegalArgumentException();
        T result = mapper.convertValue(input, outputType);
        // sanity check first:
        assertNotNull(result);
        assertEquals(outputType, result.getClass());
        return result;
    }

    private List<Number> _numberList(Object numberArray, int size)
    {
        ArrayList<Number> result = new ArrayList<Number>(size);
        for (int i = 0; i < size; ++i) {
            result.add((Number) Array.get(numberArray, i));
        }
        return result;
    }
    
    /**
     * Helper method for checking that given collections contain integral Numbers
     * that essentially contain same values in same order
     */
    private void verifyIntegralArrays(Object inputArray, Object outputArray, int size)
    {
        for (int i = 0; i < size; ++i) {
            Number n1 = (Number) Array.get(inputArray, i);
            Number n2 = (Number) Array.get(outputArray, i);
            double value1 = ((Number) n1).longValue();
            double value2 = ((Number) n2).longValue();
            assertEquals("Entry #"+i+"/"+size+" not equal", value1, value2);
        }        
    }

    private void verifyDoubleArrays(Object inputArray, Object outputArray, int size)
    {
        for (int i = 0; i < size; ++i) {
            Number n1 = (Number) Array.get(inputArray, i);
            Number n2 = (Number) Array.get(outputArray, i);
            double value1 = ((Number) n1).doubleValue();
            double value2 = ((Number) n2).doubleValue();
            assertEquals("Entry #"+i+"/"+size+" not equal", value1, value2);
        }        
    }

}
