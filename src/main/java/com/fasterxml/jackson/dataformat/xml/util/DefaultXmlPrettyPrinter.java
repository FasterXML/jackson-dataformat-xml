package com.fasterxml.jackson.dataformat.xml.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamWriter2;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.Instantiatable;

import com.fasterxml.jackson.dataformat.xml.XmlPrettyPrinter;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

/**
 * Indentation to use with XML is different from JSON, because JSON
 * requires use of separator characters and XML just basic whitespace.
 *<p>
 * Note that only a subset of methods of {@link PrettyPrinter} actually
 * get called by {@link ToXmlGenerator}; because of this, implementation
 * is bit briefer (and uglier...).
 */
public class DefaultXmlPrettyPrinter
    implements XmlPrettyPrinter, Instantiatable<DefaultXmlPrettyPrinter>,
        java.io.Serializable
{
    private static final long serialVersionUID = -1811120944652457526L;

    /**
     * Interface that defines objects that can produce indentation used
     * to separate object entries and array values. Indentation in this
     * context just means insertion of white space, independent of whether
     * linefeeds are output.
     */
    public interface Indenter
    {
        public void writeIndentation(JsonGenerator jg, int level)
            throws IOException, JsonGenerationException;

        public void writeIndentation(XMLStreamWriter2 sw, int level)
            throws XMLStreamException;
        
        /**
         * @return True if indenter is considered inline (does not add linefeeds),
         *   false otherwise
         */
        public boolean isInline();
    }

    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    /**
     * By default, let's use only spaces to separate array values.
     */
    protected Indenter _arrayIndenter = new FixedSpaceIndenter();

    /**
     * By default, let's use linefeed-adding indenter for separate
     * object entries. We'll further configure indenter to use
     * system-specific linefeeds, and 2 spaces per level (as opposed to,
     * say, single tabs)
     */
    protected Indenter _objectIndenter = new Lf2SpacesIndenter();

    // // // Config, other white space configuration

    /**
     * By default we will add spaces around colons used to
     * separate object fields and values.
     * If disabled, will not use spaces around colon.
     */
    protected boolean _spacesInObjectEntries = true;

    /*
    /**********************************************************
    /* State
    /**********************************************************
    */
    
    /**
     * Number of open levels of nesting. Used to determine amount of
     * indentation to use.
     */
    protected transient int _nesting = 0;
    
    /*
    /**********************************************************
    /* Life-cycle (construct, configure)
    /**********************************************************
    */

    public DefaultXmlPrettyPrinter() { }

    public void indentArraysWith(Indenter i)
    {
        _arrayIndenter = (i == null) ? new NopIndenter() : i;
    }

    public void indentObjectsWith(Indenter i)
    {
        _objectIndenter = (i == null) ? new NopIndenter() : i;
    }

    public void spacesInObjectEntries(boolean b) { _spacesInObjectEntries = b; }

    /*
    /**********************************************************
    /* Instantiatable impl
    /**********************************************************
     */
    
    @Override
    public DefaultXmlPrettyPrinter createInstance() {
        return new DefaultXmlPrettyPrinter();
    }

    /*
    /**********************************************************
    /* PrettyPrinter impl
    /**********************************************************
     */

    @Override
    public void writeRootValueSeparator(JsonGenerator jgen) throws IOException, JsonGenerationException {
        // Not sure if this should ever be applicable; but if multiple roots were allowed, we'd use linefeed
        jgen.writeRaw('\n');
    }
    
    /*
    /**********************************************************
    /* Array values
    /**********************************************************
     */
    
    @Override
    public void beforeArrayValues(JsonGenerator jgen) throws IOException, JsonGenerationException {
        // never called for ToXmlGenerator
    }

    @Override
    public void writeStartArray(JsonGenerator jgen)
    		throws IOException, JsonGenerationException
    {
        // anything to do here?
    }

    @Override
    public void writeArrayValueSeparator(JsonGenerator jgen)  throws IOException, JsonGenerationException {
        // never called for ToXmlGenerator
    }

    @Override
    public void writeEndArray(JsonGenerator jgen, int nrOfValues)
    		throws IOException, JsonGenerationException
    {
        // anything to do here?
    }
    
    /*
    /**********************************************************
    /* Object values
    /**********************************************************
     */

    @Override
    public void beforeObjectEntries(JsonGenerator jgen)
        throws IOException, JsonGenerationException
    {
        // never called for ToXmlGenerator
    }

    @Override
    public void writeStartObject(JsonGenerator jgen) throws IOException, JsonGenerationException
    {
        if (!_objectIndenter.isInline()) {
            if (_nesting > 0) {
                _objectIndenter.writeIndentation(jgen, _nesting);
            }
            ++_nesting;
        }
        ((ToXmlGenerator) jgen)._handleStartObject();
    }

    @Override
    public void writeObjectEntrySeparator(JsonGenerator jgen)
            throws IOException, JsonGenerationException
    {
        // never called for ToXmlGenerator
    }

    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator jgen) throws IOException, JsonGenerationException {
        // never called for ToXmlGenerator
    }
    
    @Override
    public void writeEndObject(JsonGenerator jgen, int nrOfEntries) throws IOException, JsonGenerationException
    {
        if (!_objectIndenter.isInline()) {
            --_nesting;
        }
        // for empty elements, no need for linefeeds etc:
        if (nrOfEntries > 0) {
            _objectIndenter.writeIndentation(jgen, _nesting);
        }
        ((ToXmlGenerator) jgen)._handleEndObject();
    }
    
    /*
    /**********************************************************
    /* XML-specific additions
    /**********************************************************
     */

    @Override
    public void writeStartElement(XMLStreamWriter2 sw,
            String nsURI, String localName) throws XMLStreamException
    {
        if (!_objectIndenter.isInline()) {
            if (_nesting > 0) {
                _objectIndenter.writeIndentation(sw, _nesting);
            }
            ++_nesting;
        }
        sw.writeStartElement(nsURI, localName);
    }

    @Override
    public void writeEndElement(XMLStreamWriter2 sw, int nrOfEntries) throws XMLStreamException
    {
        if (!_objectIndenter.isInline()) {
            --_nesting;
        }
        // for empty elements, no need for linefeeds etc:
        if (nrOfEntries > 0) {
            _objectIndenter.writeIndentation(sw, _nesting);
        }
        sw.writeEndElement();
    }
    
    @Override
    public void writeLeafElement(XMLStreamWriter2 sw,
    		String nsURI, String localName, String text)
  		throws XMLStreamException
    {
        if (!_objectIndenter.isInline()) {
            _objectIndenter.writeIndentation(sw, _nesting);
        }
        sw.writeStartElement(nsURI, localName);
        sw.writeCharacters(text);
        sw.writeEndElement();
    }

    @Override
    public void writeLeafElement(XMLStreamWriter2 sw,
    		String nsURI, String localName,
    		char[] buffer, int offset, int len)
        throws XMLStreamException
    {
        if (!_objectIndenter.isInline()) {
            _objectIndenter.writeIndentation(sw, _nesting);
        }
        sw.writeStartElement(nsURI, localName);
        sw.writeCharacters(buffer, offset, len);
        sw.writeEndElement();
    }
	
    @Override
    public void writeLeafElement(XMLStreamWriter2 sw,
    		String nsURI, String localName, boolean value)
  		throws XMLStreamException
    {
        if (!_objectIndenter.isInline()) {
            _objectIndenter.writeIndentation(sw, _nesting);
        }
        sw.writeStartElement(nsURI, localName);
        sw.writeBoolean(value);
        sw.writeEndElement();
    }
    
    @Override
    public void writeLeafElement(XMLStreamWriter2 sw,
            String nsURI, String localName, int value)
        throws XMLStreamException
    {
        if (!_objectIndenter.isInline()) {
            _objectIndenter.writeIndentation(sw, _nesting);
        }
        sw.writeStartElement(nsURI, localName);
        sw.writeInt(value);
        sw.writeEndElement();
    }

    @Override
    public void writeLeafElement(XMLStreamWriter2 sw,
            String nsURI, String localName, long value)
        throws XMLStreamException
    {
        if (!_objectIndenter.isInline()) {
            _objectIndenter.writeIndentation(sw, _nesting);
        }
        sw.writeStartElement(nsURI, localName);
        sw.writeLong(value);
        sw.writeEndElement();
    }

    @Override
    public void writeLeafElement(XMLStreamWriter2 sw,
            String nsURI, String localName, double value)
  		throws XMLStreamException
    {
        if (!_objectIndenter.isInline()) {
            _objectIndenter.writeIndentation(sw, _nesting);
        }
        sw.writeStartElement(nsURI, localName);
        sw.writeDouble(value);
        sw.writeEndElement();
    }

    @Override
    public void writeLeafElement(XMLStreamWriter2 sw,
    		String nsURI, String localName, float value)
  		throws XMLStreamException
    {
        if (!_objectIndenter.isInline()) {
            _objectIndenter.writeIndentation(sw, _nesting);
        }
        sw.writeStartElement(nsURI, localName);
        sw.writeFloat(value);
        sw.writeEndElement();
    }
	
    @Override
    public void writeLeafElement(XMLStreamWriter2 sw,
            String nsURI, String localName, BigInteger value)
        throws XMLStreamException
    {
        if (!_objectIndenter.isInline()) {
            _objectIndenter.writeIndentation(sw, _nesting);
        }
        sw.writeStartElement(nsURI, localName);
        sw.writeInteger(value);
        sw.writeEndElement();
    }

    @Override
    public void writeLeafElement(XMLStreamWriter2 sw,
    		String nsURI, String localName, BigDecimal value)
  		throws XMLStreamException
    {
        if (!_objectIndenter.isInline()) {
            _objectIndenter.writeIndentation(sw, _nesting);
        }
        sw.writeStartElement(nsURI, localName);
        sw.writeDecimal(value);
        sw.writeEndElement();
    }

    @Override
    public void writeLeafElement(XMLStreamWriter2 sw,
    		String nsURI, String localName,
    		byte[] data, int offset, int len)
        throws XMLStreamException
    {
        if (!_objectIndenter.isInline()) {
            _objectIndenter.writeIndentation(sw, _nesting);
        }
        sw.writeStartElement(nsURI, localName);
        sw.writeBinary(data, offset, len);
        sw.writeEndElement();
    }

    @Override
    public void writeLeafNullElement(XMLStreamWriter2 sw,
    		String nsURI, String localName)
        throws XMLStreamException
    {
        if (!_objectIndenter.isInline()) {
            _objectIndenter.writeIndentation(sw, _nesting);
        }
        sw.writeEmptyElement(nsURI, localName);
    }
    
    /*
    /**********************************************************
    /* Helper classes
    /* (note: copied from jackson-core to avoid dependency;
    /* allow local changes)
    /**********************************************************
     */

    /**
     * Dummy implementation that adds no indentation whatsoever
     */
    protected static class NopIndenter
        implements Indenter
    {
        public NopIndenter() { }
        @Override public void writeIndentation(JsonGenerator jg, int level) { }
        @Override public boolean isInline() { return true; }
        @Override public void writeIndentation(XMLStreamWriter2 sw, int level) { }
    }

    /**
     * This is a very simple indenter that only every adds a
     * single space for indentation. It is used as the default
     * indenter for array values.
     */
    protected static class FixedSpaceIndenter
        implements Indenter
    {
        public FixedSpaceIndenter() { }

        @Override
        public void writeIndentation(XMLStreamWriter2 sw, int level)
            throws XMLStreamException
        {
            sw.writeRaw(" ");
        }
        
        @Override
        public void writeIndentation(JsonGenerator jg, int level)
            throws IOException, JsonGenerationException
        {
            jg.writeRaw(' ');
        }

        @Override
        public boolean isInline() { return true; }
    }

    /**
     * Default linefeed-based indenter uses system-specific linefeeds and
     * 2 spaces for indentation per level.
     */
    protected static class Lf2SpacesIndenter
        implements Indenter
    {
        final static String SYSTEM_LINE_SEPARATOR;
        static {
            String lf = null;
            try {
                lf = System.getProperty("line.separator");
            } catch (Throwable t) { } // access exception?
            SYSTEM_LINE_SEPARATOR = (lf == null) ? "\n" : lf;
        }

        final static int SPACE_COUNT = 64;
        final static char[] SPACES = new char[SPACE_COUNT];
        static {
            Arrays.fill(SPACES, ' ');
        }

        public Lf2SpacesIndenter() { }

        @Override
        public boolean isInline() { return false; }

        @Override
        public void writeIndentation(XMLStreamWriter2 sw, int level)
            throws XMLStreamException
        {
            sw.writeRaw(SYSTEM_LINE_SEPARATOR);
            level += level; // 2 spaces per level
            while (level > SPACE_COUNT) { // should never happen but...
            	sw.writeRaw(SPACES, 0, SPACE_COUNT); 
                level -= SPACES.length;
            }
            sw.writeRaw(SPACES, 0, level);
        }
        
        @Override
        public void writeIndentation(JsonGenerator jg, int level)
            throws IOException, JsonGenerationException
        {
            jg.writeRaw(SYSTEM_LINE_SEPARATOR);
            level += level; // 2 spaces per level
            while (level > SPACE_COUNT) { // should never happen but...
                jg.writeRaw(SPACES, 0, SPACE_COUNT); 
                level -= SPACES.length;
            }
            jg.writeRaw(SPACES, 0, level);
        }
    }
}
