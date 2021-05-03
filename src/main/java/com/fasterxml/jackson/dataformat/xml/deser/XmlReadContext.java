package com.fasterxml.jackson.dataformat.xml.deser;

import java.util.Set;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.CharTypes;
import com.fasterxml.jackson.core.io.ContentReference;

/**
 * Extension of {@link JsonStreamContext}, which implements
 * core methods needed, and adds small amount of additional
 * state data we need.
 *<p>
 * Almost same as standard <code>JsonReaderContext</code>, but
 * custom version needed to be able to keep track of names
 * of properties that need wrapping; this is needed to
 * support wrapped/unwrapped Collection/array values.
 */
public final class XmlReadContext
    extends JsonStreamContext
{
    // // // Configuration

    protected final XmlReadContext _parent;

    // // // Location information (minus source reference)

    protected int _lineNr;
    protected int _columnNr;

    protected String _currentName;

    /**
     * @since 2.9
     */
    protected Object _currentValue;

    protected Set<String> _namesToWrap;

    /**
     * Name of property that requires wrapping
     */
    protected String _wrappedName;

    /*
    /**********************************************************************
    /* Simple instance reuse slots; speeds up things a bit (10-15%)
    /* for docs with lots of small arrays/objects (for which allocation was
    /* visible in profile stack frames)
    /**********************************************************************
     */

    protected XmlReadContext _child = null;

    /*
    /**********************************************************************
    /* Instance construction, reuse
    /**********************************************************************
     */

    public XmlReadContext(XmlReadContext parent, int type, int lineNr, int colNr)
    {
        super();
        _type = type;
        _parent = parent;
        _lineNr = lineNr;
        _columnNr = colNr;
        _index = -1;
    }

    protected final void reset(int type, int lineNr, int colNr)
    {
        _type = type;
        _index = -1;
        _lineNr = lineNr;
        _columnNr = colNr;
        _currentName = null;
        _currentValue = null;
        _namesToWrap = null;
    }

    @Override
    public Object getCurrentValue() {
        return _currentValue;
    }

    @Override
    public void setCurrentValue(Object v) {
        _currentValue = v;
    }

    /*
    /**********************************************************************
    /* Factory methods
    /**********************************************************************
     */

    public static XmlReadContext createRootContext(int lineNr, int colNr) {
        return new XmlReadContext(null, TYPE_ROOT, lineNr, colNr);
    }

    public static XmlReadContext createRootContext() {
        return new XmlReadContext(null, TYPE_ROOT, 1, 0);
    }
    
    public final XmlReadContext createChildArrayContext(int lineNr, int colNr)
    {
        ++_index; // not needed for Object, but does not hurt so no need to check curr type
        XmlReadContext ctxt = _child;
        if (ctxt == null) {
            _child = ctxt = new XmlReadContext(this, TYPE_ARRAY, lineNr, colNr);
            return ctxt;
        }
        ctxt.reset(TYPE_ARRAY, lineNr, colNr);
        return ctxt;
    }

    public final XmlReadContext createChildObjectContext(int lineNr, int colNr)
    {
        ++_index; // not needed for Object, but does not hurt so no need to check curr type
        XmlReadContext ctxt = _child;
        if (ctxt == null) {
            _child = ctxt = new XmlReadContext(this, TYPE_OBJECT, lineNr, colNr);
            return ctxt;
        }
        ctxt.reset(TYPE_OBJECT, lineNr, colNr);
        return ctxt;
    }

    /*
    /**********************************************************************
    /* Abstract method implementation, overrides
    /**********************************************************************
     */

    @Override
    public final String getCurrentName() { return _currentName; }

    // @since 2.9
    @Override public boolean hasCurrentName() { return _currentName != null; }

    @Override
    public final XmlReadContext getParent() { return _parent; }

    /**
     * @return Location pointing to the point where the context
     *   start marker was found
     */
    @Override
    public final JsonLocation startLocation(ContentReference srcRef) {
        // We don't keep track of offsets at this level (only reader does)
        long totalChars = -1L;

        return new JsonLocation(srcRef, totalChars, _lineNr, _columnNr);
    }

    /*
    /**********************************************************************
    /* Extended API
    /**********************************************************************
     */

    /**
     * Method called to mark start of new value, mostly to update `index`
     * for Array and Root contexts.
     *
     * @since 2.12
     */
    public final void valueStarted() {
        ++_index;
    }

    public void setCurrentName(String name) {
        _currentName = name;
    }

    public void setNamesToWrap(Set<String> namesToWrap) {
        _namesToWrap = namesToWrap;
    }

    // @since 2.11.1
    public boolean shouldWrap(String localName) {
        return (_namesToWrap != null) && _namesToWrap.contains(localName);
    }

    protected void convertToArray() {
        _type = TYPE_ARRAY;
    }
    
    /*
    /**********************************************************************
    /* Overridden standard methods
    /**********************************************************************
     */

    /**
     * Overridden to provide developer readable "JsonPath" representation
     * of the context.
     */
    @Override
    public final String toString()
    {
        StringBuilder sb = new StringBuilder(64);
        switch (_type) {
        case TYPE_ROOT:
            sb.append("/");
            break;
        case TYPE_ARRAY:
            sb.append('[');
            sb.append(getCurrentIndex());
            sb.append(']');
            break;
        case TYPE_OBJECT:
            sb.append('{');
            if (_currentName != null) {
                sb.append('"');
                CharTypes.appendQuoted(sb, _currentName);
                sb.append('"');
            } else {
                sb.append('?');
            }
            sb.append('}');
            break;
        }
        return sb.toString();
    }
}
