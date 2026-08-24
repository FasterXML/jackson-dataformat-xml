package tools.jackson.dataformat.xml.util;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.*;

import org.codehaus.stax2.XMLStreamWriter2;

import tools.jackson.core.*;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.core.exc.StreamWriteException;

public class StaxUtil
{
    public static <T> T throwAsReadException(XMLStreamException e,
            JsonParser p)
        throws JacksonException
    {
        Throwable t = _unwrap(e);
        throw new StreamReadException(p, _message(t, e), t);
    }

    public static <T> T throwAsWriteException(XMLStreamException e,
            JsonGenerator g)
        throws JacksonException
    {
        Throwable t = _unwrap(e);
        throw new StreamWriteException(g, _message(t, e), t);
    }

    private static Throwable _unwrap(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        if (t instanceof Error e) throw e;
        if (t instanceof RuntimeException re) throw re;
        return t;
    }

    /**
     * Writes {@code text} as one or more CDATA sections. XML does not allow the
     * sequence {@code "]]>"} inside a CDATA block, so where it occurs the value is
     * split: the {@code "]]"} ends one section and the {@code ">"} starts the next.
     * A coalescing reader (the default for this module) reads the pieces back as
     * the original text, so a value containing {@code "]]>"} round-trips instead of
     * making the underlying Stax writer reject it.
     */
    public static void writeCData(XMLStreamWriter2 sw, String text)
        throws XMLStreamException
    {
        int ix = text.indexOf("]]>");
        if (ix < 0) {
            sw.writeCData(text);
            return;
        }
        int start = 0;
        do {
            // keep the "]]" in this section, push the ">" into the next one
            sw.writeCData(text.substring(start, ix + 2));
            start = ix + 2;
            ix = text.indexOf("]]>", start);
        } while (ix >= 0);
        sw.writeCData(text.substring(start));
    }

    public static void writeCData(XMLStreamWriter2 sw, char[] buffer, int offset, int len)
        throws XMLStreamException
    {
        // Common case has no "]]>" in range, so avoid allocating a String for it
        final int end = offset + len;
        for (int i = offset; i < end - 2; ++i) {
            if (buffer[i] == ']' && buffer[i + 1] == ']' && buffer[i + 2] == '>') {
                writeCData(sw, new String(buffer, offset, len));
                return;
            }
        }
        sw.writeCData(buffer, offset, len);
    }

    private static String _message(Throwable t1, Throwable t2) {
        String msg = t1.getMessage();
        if (msg == null) {
            msg = t2.getMessage();
        }
        return msg;
    }

    /**
     * Since XML names can not contain all characters JSON names can, we may
     * need to replace characters. Let's start with trivial replacement of
     * ASCII characters that can not be included.
     */
    public static String sanitizeXmlTypeName(String name)
    {
        // [dataformat-xml#451]: with DEDUCTION, at least, won't have property name
        //   (but probably sensible to check for it anyway)
        if (name == null) {
            return null;
        }
        
        StringBuilder sb;
        int changes = 0;
        // First things first: remove array types' trailing[]...
        if (name.endsWith("[]")) {
            do {
                name = name.substring(0, name.length() - 2);
                ++changes;
            } while (name.endsWith("[]"));
            sb = new StringBuilder(name);
            // do trivial pluralization attempt
            if (name.endsWith("s")) {
                sb.append("es");
            } else {
                sb.append('s');
            }
        } else {
            sb = new StringBuilder(name);
        }
        for (int i = 0, len = name.length(); i < len; ++i) {
            char c = name.charAt(i);
            if (c > 127) continue;
            if (c >= 'a' && c <= 'z') continue;
            if (c >= 'A' && c <= 'Z') continue;
            if (c >= '0' && c <= '9') continue;
            if (c == '_' || c == '.' || c == '-') continue;
            // Ok, need to replace
            ++changes;
            if (c == '$') {
                sb.setCharAt(i, '.');
            } else {
                sb.setCharAt(i, '_');
            }
        }
        if (changes == 0) {
            return name;
        }
        return sb.toString();
    }

    /**
     * Helper method used to "convert" Jackson's {@link Base64Variant} into corresponding
     * Stax2 equivalent, to try to allow Jackson-style configuration for XML output as well.
     *
     * @param j64b Jackson base64 variant to find match for
     *
     * @return Stax2 Base64 variant that most closely resembles Jackson canonical Base64 variant
     *     passed in as argument
     */
    public static org.codehaus.stax2.typed.Base64Variant toStax2Base64Variant(Base64Variant j64b) {
        return Base64Mapper.instance.map(j64b);
    }

    private static class Base64Mapper {
        public final static Base64Mapper instance = new Base64Mapper();

        private final Map<String, org.codehaus.stax2.typed.Base64Variant> j2stax2
            = new HashMap<>();
        {
            j2stax2.put(Base64Variants.MIME.getName(), org.codehaus.stax2.typed.Base64Variants.MIME);
            j2stax2.put(Base64Variants.MIME_NO_LINEFEEDS.getName(),
                    org.codehaus.stax2.typed.Base64Variants.MIME_NO_LINEFEEDS);
            j2stax2.put(Base64Variants.MODIFIED_FOR_URL.getName(),
                    org.codehaus.stax2.typed.Base64Variants.MODIFIED_FOR_URL);
            j2stax2.put(Base64Variants.PEM.getName(), org.codehaus.stax2.typed.Base64Variants.PEM);
        }

        private Base64Mapper() {
        }

        public org.codehaus.stax2.typed.Base64Variant map(Base64Variant j64b) {
            org.codehaus.stax2.typed.Base64Variant result = j2stax2.get(j64b.getName());
            if (result == null) {
                // 13-May-2020, tatu: in unexpected case of no match, default to what Stax2
                //    considers default, not Jackson: this for backwards compatibility with
                //    Jackson 2.11 and earlier
                result = org.codehaus.stax2.typed.Base64Variants.getDefaultVariant();
            }
            return result;
        }
    }
}
