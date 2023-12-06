package tools.jackson.dataformat.xml.util;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.ri.Stax2ReaderAdapter;

import tools.jackson.databind.util.ClassUtil;

/**
 * Refinement of {@link Stax2ReaderAdapter} to override certain methods,
 * to patch over flaws of JDK-provided default Stax implementation, SJSXP
 *
 * @since 2.17
 */
public class Stax2JacksonReaderAdapter
    extends Stax2ReaderAdapter
{
    private final XMLStreamReader _delegate;

    public Stax2JacksonReaderAdapter(XMLStreamReader sr) {
        super(sr);
        _delegate = sr;
    }

    public static XMLStreamReader2 wrapIfNecessary(XMLStreamReader sr)
    {
        if (sr instanceof XMLStreamReader2) {
            return (XMLStreamReader2) sr;
        }
        return new Stax2JacksonReaderAdapter(sr);
    }

    // 04-Dec-2023, tatu: Needed to catch exceptions from buggy SJSXP decoder...
    @Override
    public int next() throws XMLStreamException
    {
        try {
            return super.next();
        } catch (ArrayIndexOutOfBoundsException e) {
            // Use IllegalStateException since that is guaranteed to be translated
            // appropriately into Jackson type by caller:
            throw new IllegalStateException(
                    "Internal processing error by `XMLStreamReader` of type "
                    +ClassUtil.classNameOf(_delegate)+" when calling `next()` ("
                    +"consider using Woodstox instead): "
                    +e.getMessage(), e);
        }
    }
}
