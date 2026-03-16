package tools.jackson.dataformat.xml.deser;

import java.util.Set;

import tools.jackson.core.JsonParser;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.util.JsonParserDelegate;

import tools.jackson.databind.util.TokenBuffer;

/**
 * XML-specific {@link TokenBuffer} sub-class that ensures parsers created
 * from buffered content implement {@link ElementWrappable}, allowing
 * virtual wrapping to be configured on the underlying XML parser even
 * when content has been buffered (e.g., during polymorphic type resolution).
 *
 * @since 3.2
 */
public class XmlTokenBuffer extends TokenBuffer
{
    /**
     * Reference to the original XML parser that implements {@link ElementWrappable},
     * if one was found when this buffer was created.
     */
    protected final ElementWrappable _wrappableParser;

    protected XmlTokenBuffer(JsonParser p, ObjectReadContext ctxt)
    {
        super(p, ctxt);
        // Find the ElementWrappable parser by unwrapping delegates
        JsonParser unwrapped = p;
        while (unwrapped instanceof JsonParserDelegate del) {
            unwrapped = del.delegate();
        }
        _wrappableParser = (unwrapped instanceof ElementWrappable ew) ? ew : null;
    }

    public static XmlTokenBuffer xmlBufferForInputBuffering(JsonParser p,
            ObjectReadContext ctxt) {
        return new XmlTokenBuffer(p, ctxt);
    }

    /*
    /**********************************************************************
    /* Parser construction overrides
    /**********************************************************************
     */

    @Override
    public JsonParser asParser(ObjectReadContext readCtxt)
    {
        return _wrapIfNeeded(super.asParser(readCtxt));
    }

    @Override
    public JsonParser asParser(ObjectReadContext readCtxt, JsonParser p0)
    {
        return _wrapIfNeeded(super.asParser(readCtxt, p0));
    }

    protected JsonParser _wrapIfNeeded(JsonParser p) {
        return (_wrappableParser == null) ? p
                : new ElementWrappableParser(p, _wrappableParser);
    }

    /*
    /**********************************************************************
    /* Helper classes
    /**********************************************************************
     */

    /**
     * A thin parser delegate that implements {@link ElementWrappable} by
     * forwarding wrapping configuration to the original XML parser.
     * This allows {@link WrapperHandlingDeserializer} to find and configure
     * virtual wrapping even when the active parser is reading from a
     * {@link TokenBuffer}.
     */
    static class ElementWrappableParser extends JsonParserDelegate
        implements ElementWrappable
    {
        protected final ElementWrappable _wrappable;

        ElementWrappableParser(JsonParser delegate, ElementWrappable wrappable) {
            super(delegate);
            _wrappable = wrappable;
        }

        @Override
        public void addVirtualWrapping(Set<String> namesToWrap, boolean caseInsensitive) {
            _wrappable.addVirtualWrapping(namesToWrap, caseInsensitive);
        }
    }
}
