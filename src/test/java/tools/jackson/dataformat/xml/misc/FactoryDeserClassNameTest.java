package tools.jackson.dataformat.xml.misc;

import java.io.*;
import java.util.Arrays;

import com.ctc.wstx.stax.WstxInputFactory;
import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.XmlFactory;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

// [dataformat-xml#910]: the Stax factory class names read back during JDK
// deserialization of `XmlFactory` must be verified to be actual Stax factory
// types before the named class is initialized/instantiated. A tampered stream
// naming an unrelated class must not get to run that class.
public class FactoryDeserClassNameTest extends XmlTestUtil
{
    // A real `XMLInputFactory`, used only so the serialized class name has a
    // known length we can substitute without disturbing the stream framing.
    public static class StubInFactory extends WstxInputFactory { }

    // Non-factory stand-in for an arbitrary classpath class. Its FQN must match
    // StubInFactory's byte length so the substitution below keeps the
    // ObjectOutputStream block-data framing intact (the two class names are
    // written into one length-prefixed block).
    public static class TripwireClass {
        public static volatile boolean CONSTRUCTED = false;
        public TripwireClass() {
            CONSTRUCTED = true;
        }
    }

    @Test
    public void testUnexpectedFactoryClassNotInstantiated() throws Exception
    {
        final String realInName = StubInFactory.class.getName();
        final String fakeName = TripwireClass.class.getName();
        // Guard: equal length keeps the writeUTF block-data length header valid.
        assertEquals(realInName.length(), fakeName.length(),
                "test class names must be equal length");

        final XmlFactory f = XmlFactory.builder()
                .xmlInputFactory(new StubInFactory())
                .build();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream os = new ObjectOutputStream(bytes)) {
            os.writeObject(f);
        }
        byte[] tampered = replaceUtf(bytes.toByteArray(), realInName, fakeName);

        TripwireClass.CONSTRUCTED = false;
        assertThrows(Exception.class, () -> {
            try (ObjectInputStream is = new ObjectInputStream(
                    new ByteArrayInputStream(tampered))) {
                is.readObject();
            }
        });
        // Before the fix the named class was loaded and instantiated before the
        // (failing) cast to `XMLInputFactory`; after the fix the type check runs
        // on the uninitialized class, so it is never constructed.
        assertFalse(TripwireClass.CONSTRUCTED,
                "Unexpected class must not be instantiated during deserialization");
    }

    private static byte[] replaceUtf(byte[] data, String from, String to) throws IOException {
        final byte[] f = from.getBytes("UTF-8");
        final byte[] t = to.getBytes("UTF-8");
        for (int i = 0; i + 2 + f.length <= data.length; ++i) {
            int len = ((data[i] & 0xff) << 8) | (data[i + 1] & 0xff);
            if (len == f.length
                    && Arrays.equals(Arrays.copyOfRange(data, i + 2, i + 2 + f.length), f)) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                out.write(data, 0, i);
                out.write((t.length >> 8) & 0xff);
                out.write(t.length & 0xff);
                out.write(t);
                out.write(data, i + 2 + f.length, data.length - (i + 2 + f.length));
                return out.toByteArray();
            }
        }
        throw new IllegalStateException("Could not locate class name in serialized stream");
    }
}
