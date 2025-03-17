package tools.jackson.dataformat.xml.jdk17;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Java17CollectionsTest extends XmlTestUtil
{
    private final XmlMapper _xmlMapper = new XmlMapper();

    @Test
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
