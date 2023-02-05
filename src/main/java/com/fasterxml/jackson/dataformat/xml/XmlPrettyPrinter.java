package com.fasterxml.jackson.dataformat.xml;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamWriter2;

import com.fasterxml.jackson.core.PrettyPrinter;

/**
 * There are some XML-specific quirks that need extra TLC when
 * indenting: so we will use a refinement of general purpose one.
 */
public interface XmlPrettyPrinter extends PrettyPrinter
{
    /**
     * Method for forcibly writing a start element, without going
     * through Jackson generator (and thus, without updating currently
     * active element stack)
     * 
     * @since 2.1
     */
    public void writeStartElement(XMLStreamWriter2 sw,
            String nsURI, String localName) throws XMLStreamException;

    /**
     * Method for forcibly writing an end element, without going
     * through Jackson generator (and thus, without updating currently
     * active element stack)
     * 
     * @since 2.1
     */
    public void writeEndElement(XMLStreamWriter2 sw, int nrOfEntries) throws XMLStreamException;

    /**
     * Method for trying to write a linefeed to separate entities outside of the
     * root element (that is, in prolog or epilog), most often called to separate
     * XML declaration from the root element.
     *
     * @since 2.7
     */
    public void writePrologLinefeed(XMLStreamWriter2 sw) throws XMLStreamException;
    
    public void writeLeafElement(XMLStreamWriter2 sw,
    		String nsURI, String localName, String text, boolean isCData)
        throws XMLStreamException;

    public void writeLeafElement(XMLStreamWriter2 sw,
    		String nsURI, String localName,
    		char[] buffer, int offset, int len, boolean isCData)
        throws XMLStreamException;
    
    public void writeLeafElement(XMLStreamWriter2 sw,
    		String nsURI, String localName, boolean value)
  		throws XMLStreamException;

    // Lots of numbers...
    
    public void writeLeafElement(XMLStreamWriter2 sw,
    		String nsURI, String localName, int value)
		throws XMLStreamException;

    public void writeLeafElement(XMLStreamWriter2 sw,
    		String nsURI, String localName, long value)
  		throws XMLStreamException;

    public void writeLeafElement(XMLStreamWriter2 sw,
    		String nsURI, String localName, double value)
  		throws XMLStreamException;

    public void writeLeafElement(XMLStreamWriter2 sw,
    		String nsURI, String localName, float value)
  		throws XMLStreamException;

    public void writeLeafElement(XMLStreamWriter2 sw,
    		String nsURI, String localName, BigInteger value)
  		throws XMLStreamException;

    public void writeLeafElement(XMLStreamWriter2 sw,
    		String nsURI, String localName, BigDecimal value)
  		throws XMLStreamException;
    
    // binary element
    public void writeLeafElement(XMLStreamWriter2 sw,
            String nsURI, String localName,
            org.codehaus.stax2.typed.Base64Variant base64variant,
            byte[] data, int offset, int len)
        throws XMLStreamException;

    // empty element to represent null
    public void writeLeafNullElement(XMLStreamWriter2 sw,
            String nsURI, String localName)
        throws XMLStreamException;

    // TODO: demote from "DefaultXmlPrettyPrinter" in 2.13 or later:
//    public void writeLeafXsiNilElement(XMLStreamWriter2 sw,
//            String nsURI, String localName)
//        throws XMLStreamException;
}
