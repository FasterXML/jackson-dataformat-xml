package tools.jackson.dataformat.xml.misc;

import java.io.*;
import java.util.Map;

import org.junit.jupiter.api.Test;

import tools.jackson.core.exc.StreamReadException;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertThrows;

// [dataformat-xml]: entity/DTD hardening must survive JDK serialization of the mapper
public class DTDAfterSerializationTest extends XmlTestUtil
{
    // Internal entity that only expands when DTD processing is enabled
    private static final String ENTITY_XML =
            "<?xml version='1.0'?><!DOCTYPE foo [ <!ENTITY x \"HELLO\"> ]>\n"
            + "<foo>&x;</foo>";

    private XmlMapper jdkRoundtrip(XmlMapper mapper) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream os = new ObjectOutputStream(bytes)) {
            os.writeObject(mapper);
        }
        try (ObjectInputStream is = new ObjectInputStream(
                new ByteArrayInputStream(bytes.toByteArray()))) {
            return (XmlMapper) is.readObject();
        }
    }

    @Test
    public void testDTDStaysDisabledAfterRoundtrip() throws Exception
    {
        XmlMapper mapper = jdkRoundtrip(new XmlMapper());
        // Must fail specifically because the parser refuses the DTD-declared
        // entity (DTD support off), leaving `&x;` unexpanded -- not for some
        // unrelated binding reason. Before the fix this expanded to "HELLO".
        StreamReadException e = assertThrows(StreamReadException.class,
                () -> mapper.readValue(ENTITY_XML, Map.class));
        verifyException(e, "Undeclared general entity", "entity");
    }
}
