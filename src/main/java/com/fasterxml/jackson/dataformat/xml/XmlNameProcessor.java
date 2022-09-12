package com.fasterxml.jackson.dataformat.xml;

import java.io.Serializable;

/**
 * API of processors primarily used for dealing with XML names
 * containing invalid characters. Invalid characters in names can,
 * for instance, easily appear in map keys.
 * <p>
 * Processors should be set in the {@link XmlMapper#setXmlNameProcessor}
 * and/or the {@link XmlMapper.Builder#xmlNameProcessor} methods.
 * <p>
 * See {@link XmlNameProcessors} for default processors.
 *
 * @since 2.14
 */
public interface XmlNameProcessor extends Serializable {

    /**
     * Representation of an XML element or attribute name
     */
    class XmlName {
        public String namespace;
        public String localPart;

        public XmlName() { }
    }

    /**
     * Used during XML serialization.
     * <p>
     * This method should process the provided {@link XmlName} and
     * escape / encode invalid XML characters.
     *
     * @param name The name to encode
     */
    void encodeName(XmlName name);


    /**
     * Used during XML deserialization.
     * <p>
     * This method should process the provided {@link XmlName} and
     * revert the encoding done in the {@link #encodeName(XmlName)}
     * method.
     * <p>
     * Note: Depending on the use case, it is not always required (or
     * even possible) to reverse an encoding with 100% accuracy.
     *
     * @param name The name to encode
     */
    void decodeName(XmlName name);

}
