package com.fasterxml.jackson.dataformat.xml;

import java.util.Base64;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Contains default XML tag name processors.
 * <p>
 * Processors should be set in the {@link XmlMapper#setXmlTagProcessor(XmlTagProcessor)}
 * and/or the {@link XmlMapper.Builder#xmlTagProcessor(XmlTagProcessor)} methods.
 *
 * @since 2.14
 */
public final class XmlTagProcessors {

    /**
     * Generates a new tag processor that does nothing and just passes through the
     * tag names. Using this processor may generate invalid XML.
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
     *
     * @since 2.14
     */
    public static XmlTagProcessor newPassthroughProcessor() {
        return new PassthroughTagProcessor();
    }

    /**
     * Generates a new tag processor that replaces all invalid characters in an
     * XML tag name with a replacement string. This is a one-way processor, since
     * there is no way to reverse this replacement step.
     * <p>
     * With this processor set (and {@code "_"} as the replacement string), a map
     * with the keys {@code "123"} and {@code "$ I am <fancy>! &;"} will be written as:
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
     *
     * @since 2.14
     */
    public static XmlTagProcessor newReplacementProcessor(String replacement) {
        return new ReplaceTagProcessor(replacement);
    }

    /**
     * Equivalent to calling {@link #newReplacementProcessor(String)} with {@code "_"}
     *
     * @since 2.14
     */
    public static XmlTagProcessor newReplacementProcessor() {
        return newReplacementProcessor("_");
    }

    /**
     * Generates a new tag processor that escapes all tag names containing invalid
     * characters with base64. Here the
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
     *
     * @param prefix The prefix to use for tags that are escaped
     *
     * @since 2.14
     */
    public static XmlTagProcessor newBase64Processor(String prefix) {
        return new Base64TagProcessor(prefix);
    }

    /**
     * Equivalent to calling {@link #newBase64Processor(String)} with {@code "base64_tag_"}
     *
     * @since 2.14
     */
    public static XmlTagProcessor newBase64Processor() {
        return newBase64Processor("base64_tag_");
    }

    /**
     * Similar to {@link #newBase64Processor(String)}, however, tag names will
     * <b>always</b> be escaped with base64. No magic prefix is required
     * for this case, since adding one would be redundant because all tags will
     * be base64 encoded.
     */
    public static XmlTagProcessor newAlwaysOnBase64Processor() {
        return new AlwaysOnBase64TagProcessor();
    }



    private static class PassthroughTagProcessor implements XmlTagProcessor {
        @Override
        public XmlTagName encodeTag(XmlTagName tag) {
            return tag;
        }

        @Override
        public XmlTagName decodeTag(XmlTagName tag) {
            return tag;
        }
    }

    private static class ReplaceTagProcessor implements XmlTagProcessor {
        private static final Pattern BEGIN_MATCHER = Pattern.compile("^[^a-zA-Z_:]");
        private static final Pattern MAIN_MATCHER = Pattern.compile("[^a-zA-Z0-9_:-]");

        private final String _replacement;

        private ReplaceTagProcessor(String replacement) {
            _replacement = replacement;
        }

        @Override
        public XmlTagName encodeTag(XmlTagName tag) {
            String newLocalPart = tag.localPart;
            newLocalPart = BEGIN_MATCHER.matcher(newLocalPart).replaceAll(_replacement);
            newLocalPart = MAIN_MATCHER.matcher(newLocalPart).replaceAll(_replacement);

            return new XmlTagName(tag.namespace, newLocalPart);
        }

        @Override
        public XmlTagName decodeTag(XmlTagName tag) {
            return tag;
        }
    }

    private static class Base64TagProcessor implements XmlTagProcessor {
        private static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();
        private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();
        private static final Pattern VALID_XML_TAG = Pattern.compile("[a-zA-Z_:]([a-zA-Z0-9_:.-])*");

        private final String _prefix;

        private Base64TagProcessor(String prefix) {
            _prefix = prefix;
        }

        @Override
        public XmlTagName encodeTag(XmlTagName tag) {
            if (VALID_XML_TAG.matcher(tag.localPart).matches()) {
                return tag;
            }
            final String encoded = new String(BASE64_ENCODER.encode(tag.localPart.getBytes(UTF_8)), UTF_8);
            return new XmlTagName(tag.namespace, _prefix + encoded);
        }

        @Override
        public XmlTagName decodeTag(XmlTagName tag) {
            if (!tag.localPart.startsWith(_prefix)) {
                return tag;
            }
            String localName = tag.localPart;
            localName = localName.substring(_prefix.length());
            localName = new String(BASE64_DECODER.decode(localName), UTF_8);
            return new XmlTagName(tag.namespace, localName);
        }
    }

    private static class AlwaysOnBase64TagProcessor implements XmlTagProcessor {
        private static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();
        private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();

        @Override
        public XmlTagName encodeTag(XmlTagName tag) {
            return new XmlTagName(tag.namespace, new String(BASE64_ENCODER.encode(tag.localPart.getBytes(UTF_8)), UTF_8));
        }

        @Override
        public XmlTagName decodeTag(XmlTagName tag) {
            return new XmlTagName(tag.namespace, new String(BASE64_DECODER.decode(tag.localPart), UTF_8));
        }
    }


    private XmlTagProcessors() {
        // Nothing to do here
    }
}
