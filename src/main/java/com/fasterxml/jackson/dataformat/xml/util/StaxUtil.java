package com.fasterxml.jackson.dataformat.xml.util;

import java.io.IOException;

import javax.xml.stream.*;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;

public class StaxUtil
{
    /**
     * Adapter method used when only IOExceptions are declared to be thrown, but
     * a {@link XMLStreamException} was caught.
     *<p>
     * Note: dummy type variable is used for convenience, to allow caller to claim
     * that this method returns result of any necessary type.
     *
     * @deprecated Since 2.9
     */
    @Deprecated
    public static <T> T throwXmlAsIOException(XMLStreamException e) throws IOException
    {
        Throwable t = _unwrap(e);
        throw new IOException(t);
    }

    /**
     * @since 2.9
     */
    public static <T> T throwAsParseException(XMLStreamException e,
            JsonParser p) throws IOException
    {
        Throwable t = _unwrap(e);
        throw new JsonParseException(p, _message(t, e), t);
    }

    /**
     * @since 2.9
     */
    public static <T> T throwAsGenerationException(XMLStreamException e,
            JsonGenerator g) throws IOException
    {
        Throwable t = _unwrap(e);
        throw new JsonGenerationException(_message(t, e), t, g);
    }

    private static Throwable _unwrap(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        if (t instanceof Error) throw (Error) t;
        if (t instanceof RuntimeException) throw (RuntimeException) t;
        return t;
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
}
