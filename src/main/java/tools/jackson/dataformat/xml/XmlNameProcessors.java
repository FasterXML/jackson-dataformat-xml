package tools.jackson.dataformat.xml;

import java.util.Base64;
import java.util.Objects;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Contains default {@link XmlNameProcessor} implementations.
 * <p>
 * Processors should be set using builder methods used in creating
 * {@link XmlFactory} instances; see {@link XmlFactoryBuilder}.
 */
public final class XmlNameProcessors
{
    private XmlNameProcessors() {
        // Nothing to do here; just to prevent instantiation
    }

    /**
     * Generates a new processor that does nothing and just passes through the
     * names as-is. Using this processor may generate invalid XML.
     * <p>
     * With this processor set, a map with the keys {@code "123"} and
     * {@code "$ I am <fancy>! &;"} will be written as:
     *
     * <pre>{@code
     * <DTO>
     *     <badMap>
     *         <$ I am <fancy>! &;>xyz</$ I am <fancy>! &;>
     *         <123>bar</123>
     *     </badMap>
     * </DTO>
     * }</pre>
     * <p>
     * This is the default behavior for backwards compatibility.
     */
    public static XmlNameProcessor newPassthroughProcessor() {
        return new PassthroughProcessor();
    }

    /**
     * Generates a new processor that replaces all characters that are NOT one of:
     *<ul>
     * <li>Lower- or upper-case ASCII letter (a to z, A to Z)
     *   </li>
     * <li>Digit (0 to 9) in position OTHER than the first character
     *   </li>
     * <li>Underscore
     *   </li>
     * <li>Hyphen ({@code -}) in position OTHER than the first character
     *   </li>
     * <li>Colon (only  exposed if underlying parser is in non-namespace-aware mode)
     *   </li>
     * </ul>
     * in an
     * XML name with a replacement string. This is a one-way processor, since
     * there is no way to reverse this replacement step.
     * <p>
     * With this processor set (and {@code "_"} as the replacement string), a map
     * with the keys {@code "123"} and {@code "$ I am <fancy>! &;"} will be written as:
     * <p>
     * NOTE: this processor works for US-ASCII based element and attribute names
     * but is unlikely to work well for many "international" use cases.
     *
     * <pre>{@code
     * <DTO>
     *     <badMap>
     *         <__I_am__fancy_____>xyz</__I_am__fancy_____>
     *         <_23>bar</_23>
     *     </badMap>
     * </DTO>
     * }</pre>
     *
     * @param replacement The replacement string to replace invalid characters with
     */
    public static XmlNameProcessor newReplacementProcessor(String replacement) {
        return new ReplaceNameProcessor(replacement);
    }

    /**
     * Convenience method 
     * equivalent to calling {@link #newReplacementProcessor(String)} with {@code "_"}
     */
    public static XmlNameProcessor newReplacementProcessor() {
        return newReplacementProcessor("_");
    }

    /**
     * Generates a new processor that escapes all names that contains characters
     * OTHER than following characters:
     *<ul>
     * <li>Lower- or upper-case ASCII letter (a to z, A to Z)
     *   </li>
     * <li>Digit (0 to 9) in position OTHER than the first characters
     *   </li>
     * <li>Underscore
     *   </li>
     * <li>Hyphen ({@code -}) in position OTHER than the first character
     *   </li>
     * <li>Colon (only  exposed if underlying parser is in non-namespace-aware mode)
     *   </li>
     * </ul>
     * with a base64-encoded version. Here the
     * <a href="https://datatracker.ietf.org/doc/html/rfc4648#section-5">base64url</a>
     * encoder and decoders are used. The {@code =} padding characters are
     * always omitted.
     * <p>
     * With this processor set, a map with the keys {@code "123"} and
     * {@code "$ I am <fancy>! &;"} will be written as:
     *
     * <pre>{@code
     * <DTO>
     *     <badMap>
     *         <base64_tag_JCBJIGFtIDxmYW5jeT4hICY7>xyz</base64_tag_JCBJIGFtIDxmYW5jeT4hICY7>
     *         <base64_tag_MTIz>bar</base64_tag_MTIz>
     *     </badMap>
     * </DTO>
     * }</pre>
     *<p>
     * NOTE: names that already start with {@code prefix} are escaped as well, even
     * though they are otherwise valid, so that decoding cannot confuse them with
     * names this processor encoded.
     *
     * @param prefix The prefix to use for name that are escaped
     */
    public static XmlNameProcessor newBase64Processor(String prefix) {
        return new Base64NameProcessor(prefix);
    }

    /**
     * Convenience method
     * equivalent to calling {@link #newBase64Processor(String)} with {@code "base64_tag_"}
     */
    public static XmlNameProcessor newBase64Processor() {
        return newBase64Processor("base64_tag_");
    }

    /**
     * Similar to {@link #newBase64Processor(String)}, however, names will
     * <b>always</b> be escaped with base64. No magic prefix is required
     * for this case, since adding one would be redundant because all names
     * will be base64 encoded.
     */
    public static XmlNameProcessor newAlwaysOnBase64Processor() {
        return new AlwaysOnBase64NameProcessor();
    }

    static class PassthroughProcessor implements XmlNameProcessor {
        private static final long serialVersionUID = 1L;

        public PassthroughProcessor() { }

        @Override
        public void encodeName(XmlName name) { }

        @Override
        public void decodeName(XmlName name) { }
    }

    static class ReplaceNameProcessor implements XmlNameProcessor {
        private static final long serialVersionUID = 1L;

        private static final Pattern BEGIN_MATCHER = Pattern.compile("^[^a-zA-Z_:]");
        private static final Pattern MAIN_MATCHER = Pattern.compile("[^a-zA-Z0-9_:-]");

        private final String _replacement;

        public ReplaceNameProcessor(String replacement) {
            // Only used as replaceAll() argument, so a null one would otherwise
            // only fail on the first name written: fail here instead
            _replacement = Objects.requireNonNull(replacement, "replacement cannot be null");
        }

        @Override
        public void encodeName(XmlName name) {
            String newLocalPart = name.localPart;
            newLocalPart = BEGIN_MATCHER.matcher(newLocalPart).replaceAll(_replacement);
            name.localPart = MAIN_MATCHER.matcher(newLocalPart).replaceAll(_replacement);
        }

        @Override
        public void decodeName(XmlName name) {
            // One-way transformation; cannot decode
        }
    }

    static class Base64NameProcessor implements XmlNameProcessor {
        private static final long serialVersionUID = 1L;

        private static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();
        private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();
        private static final Pattern VALID_XML_NAME = Pattern.compile("[a-zA-Z_:]([a-zA-Z0-9_:.-])*");

        private final String _prefix;

        public Base64NameProcessor(String prefix) {
            // Both encodeName() and decodeName() test names against the prefix, so
            // a null one would only fail deep inside name handling: fail here instead
            _prefix = Objects.requireNonNull(prefix, "prefix cannot be null");
        }

        @Override
        public void encodeName(XmlName name) {
            // Names already starting with the prefix have to be escaped as well, even
            // when otherwise valid: decodeName() base64-decodes anything carrying the
            // prefix, so passing such a name through as-is would decode it into a
            // different name than was written.
            if (!VALID_XML_NAME.matcher(name.localPart).matches()
                    || name.localPart.startsWith(_prefix)) {
                name.localPart = _prefix + new String(BASE64_ENCODER.encode(name.localPart.getBytes(UTF_8)), UTF_8);
            }
        }

        @Override
        public void decodeName(XmlName name) {
            if (name.localPart.startsWith(_prefix)) {
                String localName = name.localPart;
                localName = localName.substring(_prefix.length());
                name.localPart = new String(BASE64_DECODER.decode(localName), UTF_8);
            }
        }
    }

    static class AlwaysOnBase64NameProcessor implements XmlNameProcessor {
        private static final long serialVersionUID = 1L;

        private static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();
        private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();

        public AlwaysOnBase64NameProcessor() { }

        @Override
        public void encodeName(XmlName name) {
            name.localPart = new String(BASE64_ENCODER.encode(name.localPart.getBytes(UTF_8)), UTF_8);
        }

        @Override
        public void decodeName(XmlName name) {
            name.localPart = new String(BASE64_DECODER.decode(name.localPart), UTF_8);
        }
    }
}
