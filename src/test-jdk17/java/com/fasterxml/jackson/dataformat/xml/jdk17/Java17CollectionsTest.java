package com.fasterxml.jackson.dataformat.xml.jdk17;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class Java17CollectionsTest extends XmlTestBase
{
    private final XmlMapper _xmlMapper = new XmlMapper();

    public void testStreamOf()
            throws Exception
    {
        List<String> input = Stream.of("a", "b", "c").collect(Collectors.toList());

        String ser = _xmlMapper.writeValueAsString(input);
        assertEquals("<ArrayList><item>a</item><item>b</item><item>c</item></ArrayList>", ser);

        List<?> deser = _xmlMapper.readValue(ser, List.class);
        assertEquals(input, deser);

        input = Stream.of("a", "b", "c").toList();
        ser = _xmlMapper.writeValueAsString(input);
        deser = _xmlMapper.readValue(ser, List.class);
        assertEquals(input, deser);
    }

}
