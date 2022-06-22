package com.fasterxml.jackson.dataformat.xml;

import java.io.Serializable;

/**
 * XML tag name processor primarily used for dealing with tag names
 * containing invalid characters. Invalid characters in tags can,
 * for instance, easily appear in map keys.
 * <p>
 * Processors should be set in the {@link XmlMapper#setXmlTagProcessor(XmlTagProcessor)}
 * and/or the {@link XmlMapper.Builder#xmlTagProcessor(XmlTagProcessor)} methods.
 * <p>
 * See {@link XmlTagProcessors} for default processors.
 *
 * @since 2.14
 */
public interface XmlTagProcessor extends Serializable {

    /**
     * Representation of an XML tag name
     */
    class XmlTagName {
        public final String namespace;
        public final String localPart;

        public XmlTagName(String namespace, String localPart) {
            this.namespace = namespace;
            this.localPart = localPart;
        }
    }


    /**
     * Used during XML serialization.
     * <p>
     * This method should process the provided {@link XmlTagName} and
     * escape / encode invalid XML characters.
     *
     * @param tag The tag to encode
     * @return The encoded tag name
     */
    XmlTagName encodeTag(XmlTagName tag);


    /**
     * Used during XML deserialization.
     * <p>
     * This method should process the provided {@link XmlTagName} and
     * revert the encoding done in the {@link #encodeTag(XmlTagName)}
     * method.
     * <p>
     * Note: Depending on the use case, it is not always required (or
     * even possible) to reverse an encoding with 100% accuracy.
     *
     * @param tag The tag to encode
     * @return The encoded tag name
     */
    XmlTagName decodeTag(XmlTagName tag);

}
