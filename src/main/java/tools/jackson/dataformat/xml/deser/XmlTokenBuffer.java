package tools.jackson.dataformat.xml.deser;

import java.util.Set;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.SerializableString;
import tools.jackson.core.sym.PropertyNameMatcher;
import tools.jackson.core.util.JsonParserDelegate;

import tools.jackson.databind.util.TokenBuffer;

/**
 * XML-specific {@link TokenBuffer} sub-class that ensures parsers created
 * from buffered content implement {@link ElementWrappable}, allowing
 * virtual wrapping to be configured even when content has been buffered
 * (e.g., during polymorphic type resolution or {@code @JsonUnwrapped} handling).
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
     * Parser delegate that implements {@link ElementWrappable} to support
     * virtual wrapping on buffered token streams. For buffered content
     * (e.g., from {@code @JsonUnwrapped} handling), this parser injects
     * virtual {@code START_ARRAY}/{@code END_ARRAY} tokens around consecutive
     * properties with the same name that need wrapping, transforming
     * repeated XML elements into a JSON array structure.
     *<p>
     * For non-buffered content (e.g., polymorphic type resolution where
     * the XML parser is still active), wrapping is delegated to the
     * original XML parser.
     */
    static class ElementWrappableParser extends JsonParserDelegate
        implements ElementWrappable
    {
        // State machine constants
        private static final int STATE_NORMAL = 0;
        private static final int STATE_EMIT_START_ARRAY = 1;
        private static final int STATE_WRAPPING = 2;
        private static final int STATE_EMIT_PENDING = 3;

        protected final ElementWrappable _wrappable;

        // Virtual wrapping configuration
        protected Set<String> _namesToWrap;
        protected boolean _caseInsensitive;

        /**
         * Whether local wrapping is active. When true, this parser injects
         * virtual array tokens into the buffered token stream for names that
         * need wrapping. Set when {@link #addVirtualWrapping} is called.
         */
        protected boolean _localWrapping;

        private int _wrapState = STATE_NORMAL;
        private String _currentWrapName;
        private int _wrapDepth;

        // Pending token to emit after END_ARRAY
        private JsonToken _pendingToken;
        private String _pendingName;

        // Override for currentName/currentToken when emitting virtual tokens
        private JsonToken _virtualToken;
        private String _virtualName;

        ElementWrappableParser(JsonParser delegate, ElementWrappable wrappable) {
            super(delegate);
            _wrappable = wrappable;
        }

        @Override
        public void addVirtualWrapping(Set<String> namesToWrap, boolean caseInsensitive) {
            _namesToWrap = namesToWrap;
            _caseInsensitive = caseInsensitive;
            // Always delegate to the original XML parser: this handles the
            // polymorphic type resolution case where the parser is still live
            // and will read remaining (unbuffered) content.
            _wrappable.addVirtualWrapping(namesToWrap, caseInsensitive);
            // Also enable local wrapping on the buffered tokens: this handles
            // the @JsonUnwrapped case where all content is fully buffered and
            // the original parser has moved past it.
            _localWrapping = true;
        }

        private boolean _shouldWrap(String name) {
            if (_namesToWrap == null) {
                return false;
            }
            if (_caseInsensitive) {
                for (String n : _namesToWrap) {
                    if (n.equalsIgnoreCase(name)) {
                        return true;
                    }
                }
                return false;
            }
            return _namesToWrap.contains(name);
        }

        @Override
        public JsonToken currentToken() {
            if (_virtualToken != null) {
                return _virtualToken;
            }
            return delegate.currentToken();
        }

        @Override
        public int currentTokenId() {
            final JsonToken t = currentToken();
            return (t == null) ? JsonToken.NOT_AVAILABLE.id() : t.id();
        }

        @Override
        public String currentName() {
            if (_virtualName != null) {
                return _virtualName;
            }
            return delegate.currentName();
        }

        @Override
        public boolean isExpectedStartArrayToken() {
            return currentToken() == JsonToken.START_ARRAY;
        }

        @Override
        public boolean isExpectedStartObjectToken() {
            return currentToken() == JsonToken.START_OBJECT;
        }

        @Override
        public boolean hasToken(JsonToken t) {
            return currentToken() == t;
        }

        @Override
        public boolean hasTokenId(int id) {
            final JsonToken t = currentToken();
            return (t != null) && (t.id() == id);
        }

        @Override
        public boolean hasCurrentToken() {
            return currentToken() != null;
        }

        @Override
        public JsonToken nextToken() throws JacksonException {
            if (!_localWrapping) {
                return delegate.nextToken();
            }
            return _nextTokenWrapping();
        }

        @Override
        public JsonToken nextValue() throws JacksonException {
            if (!_localWrapping) {
                return delegate.nextValue();
            }
            JsonToken t = nextToken();
            if (t == JsonToken.PROPERTY_NAME) {
                t = nextToken();
            }
            return t;
        }

        @Override
        public String nextName() throws JacksonException {
            if (!_localWrapping) {
                return delegate.nextName();
            }
            return (nextToken() == JsonToken.PROPERTY_NAME) ? currentName() : null;
        }

        @Override
        public boolean nextName(SerializableString str) throws JacksonException {
            if (!_localWrapping) {
                return delegate.nextName(str);
            }
            return (nextToken() == JsonToken.PROPERTY_NAME) && str.getValue().equals(currentName());
        }

        @Override
        public int nextNameMatch(PropertyNameMatcher matcher) throws JacksonException {
            if (!_localWrapping) {
                return delegate.nextNameMatch(matcher);
            }
            String str = nextName();
            if (str != null) {
                return matcher.matchName(str);
            }
            if (hasToken(JsonToken.END_OBJECT)) {
                return PropertyNameMatcher.MATCH_END_OBJECT;
            }
            return PropertyNameMatcher.MATCH_ODD_TOKEN;
        }

        @Override
        public int currentNameMatch(PropertyNameMatcher matcher) {
            if (!_localWrapping || _virtualToken == null) {
                return delegate.currentNameMatch(matcher);
            }
            if (_virtualToken == JsonToken.PROPERTY_NAME && _virtualName != null) {
                return matcher.matchName(_virtualName);
            }
            if (_virtualToken == JsonToken.END_OBJECT) {
                return PropertyNameMatcher.MATCH_END_OBJECT;
            }
            return PropertyNameMatcher.MATCH_ODD_TOKEN;
        }

        private JsonToken _nextTokenWrapping() throws JacksonException {
            switch (_wrapState) {
            case STATE_EMIT_START_ARRAY:
                _wrapState = STATE_WRAPPING;
                _wrapDepth = 0;
                _virtualToken = JsonToken.START_ARRAY;
                _virtualName = null;
                return JsonToken.START_ARRAY;

            case STATE_EMIT_PENDING:
                JsonToken pt = _pendingToken;
                String pn = _pendingName;
                _pendingToken = null;
                _pendingName = null;
                _virtualToken = pt;
                _virtualName = pn;
                // Check if pending field is also a wrapped name
                if (pt == JsonToken.PROPERTY_NAME && _shouldWrap(pn)) {
                    _currentWrapName = pn;
                    _wrapState = STATE_EMIT_START_ARRAY;
                } else {
                    _wrapState = STATE_NORMAL;
                }
                return pt;

            case STATE_WRAPPING:
                return _nextWrapping();

            default: // STATE_NORMAL
                return _nextNormal();
            }
        }

        private JsonToken _nextNormal() throws JacksonException {
            _virtualToken = null;
            _virtualName = null;
            JsonToken t = delegate.nextToken();
            if (t == JsonToken.PROPERTY_NAME) {
                String name = delegate.currentName();
                if (_shouldWrap(name)) {
                    _currentWrapName = name;
                    _wrapState = STATE_EMIT_START_ARRAY;
                    // Return the PROPERTY_NAME, next call will emit START_ARRAY
                }
            }
            return t;
        }

        private JsonToken _nextWrapping() throws JacksonException {
            // When inside nested content (depth > 0), just pass through
            if (_wrapDepth > 0) {
                _virtualToken = null;
                _virtualName = null;
                JsonToken t = delegate.nextToken();
                if (t == JsonToken.START_OBJECT || t == JsonToken.START_ARRAY) {
                    ++_wrapDepth;
                } else if (t == JsonToken.END_OBJECT || t == JsonToken.END_ARRAY) {
                    --_wrapDepth;
                }
                return t;
            }

            // At wrapping level (depth == 0)
            _virtualToken = null;
            _virtualName = null;
            JsonToken t = delegate.nextToken();

            if (t == null) {
                // Unexpected end of buffer while in array — close the virtual array
                _currentWrapName = null;
                _wrapState = STATE_NORMAL;
                _virtualToken = JsonToken.END_ARRAY;
                return JsonToken.END_ARRAY;
            }
            if (t == JsonToken.PROPERTY_NAME) {
                String name = delegate.currentName();
                if (_matchesWrapName(name)) {
                    // Same collection element - skip the duplicate field name,
                    // return the value token directly
                    t = delegate.nextToken();
                    if (t == JsonToken.START_OBJECT || t == JsonToken.START_ARRAY) {
                        ++_wrapDepth;
                    }
                    return t;
                }
                // Different field - end the virtual array, save this token as pending
                _pendingToken = t;
                _pendingName = name;
                _currentWrapName = null;
                _wrapState = STATE_EMIT_PENDING;
                _virtualToken = JsonToken.END_ARRAY;
                _virtualName = null;
                return JsonToken.END_ARRAY;
            }
            if (t == JsonToken.END_OBJECT) {
                // End of containing object - end array, save END_OBJECT as pending
                _pendingToken = t;
                _pendingName = null;
                _currentWrapName = null;
                _wrapState = STATE_EMIT_PENDING;
                _virtualToken = JsonToken.END_ARRAY;
                _virtualName = null;
                return JsonToken.END_ARRAY;
            }
            // Other tokens (shouldn't normally happen at depth 0 in wrapping,
            // but handle gracefully)
            if (t == JsonToken.START_OBJECT || t == JsonToken.START_ARRAY) {
                ++_wrapDepth;
            }
            return t;
        }

        private boolean _matchesWrapName(String name) {
            if (_caseInsensitive) {
                return _currentWrapName.equalsIgnoreCase(name);
            }
            return _currentWrapName.equals(name);
        }
    }
}
