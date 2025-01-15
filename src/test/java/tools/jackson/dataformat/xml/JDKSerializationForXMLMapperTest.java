package tools.jackson.dataformat.xml;

import java.io.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JDKSerializationForXMLMapperTest extends XmlTestUtil
{
    @Test
    public void testMapperSerialization() throws Exception
    {
        XmlMapper mapper1 = new XmlMapper(XmlFactory.builder()
                .nameForTextElement("foo")
                .build());
        assertEquals("foo", mapper1.tokenStreamFactory().getXMLTextElementName());

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(bytes);
        objectStream.writeObject(mapper1);
        objectStream.close();

        ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        Object ob = input.readObject();
        input.close();
        assertEquals(XmlMapper.class, ob.getClass());
        XmlMapper mapper2 = (XmlMapper) ob;

        assertEquals("foo", mapper2.tokenStreamFactory().getXMLTextElementName());
    }
}
