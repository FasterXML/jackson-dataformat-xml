package tools.jackson.dataformat.xml.deser;

import java.util.Set;

import tools.jackson.core.*;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.core.io.CharTypes;
import tools.jackson.core.io.ContentReference;
import tools.jackson.core.json.DupDetector;

/**
 * Extension of {@link TokenStreamContext}, which implements
 * core methods needed, and adds small amount of additional
 * state data we need.
 *<p>
 * Almost same as standard <code>JsonReaderContext</code>, but
 * custom version needed to be able to keep track of names
 * of properties that need wrapping; this is needed to
 * support wrapped/unwrapped Collection/array values.
 */
public final class XmlReadContext
    extends TokenStreamContext
{
    // // // Configuration

    protected final XmlReadContext _parent;

    /**
     * Object used for checking for duplicate field names, if enabled
     * (null if not enabled)
     */
    protected final DupDetector _dups;

    // // // Location information (minus source reference)

    protected int _lineNr;
    protected int _columnNr;

    protected String _currentName;
    protected Object _currentValue;

    protected Set<String> _namesToWrap;

    /**
     * Name of property that requires wrapping
     */
    protected String _wrappedName;

    /*
    /**********************************************************************
    /* Simple instance reuse slots; speeds up things a bit (10-15%)
    /* for docs with lots of small arrays/objects (for which allocation
    /* was visible in profile stack frames)
    /**********************************************************************
     */

    protected XmlReadContext _child = null;

    /*
    /**********************************************************************
    /* Instance construction, reuse
    /**********************************************************************
     */

    public XmlReadContext(int type, XmlReadContext parent, DupDetector dups,
            int nestingDepth,
            int lineNr, int colNr)
    {
        super();
        _type = type;
        _parent = parent;
        _dups = dups;
        _nestingDepth = nestingDepth;
        _lineNr = lineNr;
        _columnNr = colNr;
        _index = -1;
    }

    @Deprecated // @since 3.0.2
    public XmlReadContext(int type, XmlReadContext parent, int nestingDepth,
            int lineNr, int colNr)
    {
        this(type, parent, null, nestingDepth, lineNr, colNr);
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
        // _nestingDepth is fine since reused instance at same nesting level
        if (_dups != null) {
            _dups.reset();
        }
    }

    @Override
    public Object currentValue() {
        return _currentValue;
    }

    @Override
    public void assignCurrentValue(Object v) {
        _currentValue = v;
    }

    /*
    /**********************************************************************
    /* Factory methods
    /**********************************************************************
     */

    public static XmlReadContext createRootContext(DupDetector dups, int lineNr, int colNr) {
        return new XmlReadContext(TYPE_ROOT, null, dups, 0, lineNr, colNr);
    }

    @Deprecated // @since 3.0.2
    public static XmlReadContext createRootContext(int lineNr, int colNr) {
        return createRootContext(null, lineNr, colNr);
    }

    public final XmlReadContext createChildArrayContext(int lineNr, int colNr)
    {
        ++_index; // not needed for Object, but does not hurt so no need to check curr type
        XmlReadContext ctxt = _child;
        if (ctxt == null) {
            _child = ctxt = new XmlReadContext(TYPE_ARRAY, this,
                    (_dups == null) ? null : _dups.child(),
                            _nestingDepth+1, lineNr, colNr);
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
            _child = ctxt = new XmlReadContext(TYPE_OBJECT, this,
                    (_dups == null) ? null : _dups.child(),
                            _nestingDepth+1, lineNr, colNr);
            return ctxt;
        }
        ctxt.reset(TYPE_OBJECT, lineNr, colNr);
        return ctxt;
    }

    /*
    /**********************************************************************
    /* Abstract method implementation
    /**********************************************************************
     */

    @Override
    public final String currentName() { return _currentName; }

    @Override public boolean hasCurrentName() { return _currentName != null; }

    @Override
    public final XmlReadContext getParent() { return _parent; }

    /**
     * @return Location pointing to the point where the context
     *   start marker was found
     */
    @Override
    public final TokenStreamLocation startLocation(ContentReference srcRef) {
        // We don't keep track of offsets at this level (only reader does)
        long totalChars = -1L;

        return new TokenStreamLocation(srcRef, totalChars, _lineNr, _columnNr);
    }

    /*
    /**********************************************************************
    /* Extended API
    /**********************************************************************
     */

    /**
     * Method called to mark start of new value, mostly to update `index`
     * for Array and Root contexts.
     */
    public final void valueStarted() {
        ++_index;
    }

    public void setCurrentName(String name) throws StreamReadException {
        _currentName = name;
        if (_dups != null) {
            _checkDup(_dups, name);
        }
    }

    private static void _checkDup(DupDetector dd, String name) throws StreamReadException
    {
        if (dd.isDup(name)) {
            Object src = dd.getSource();
            throw new StreamReadException(((src instanceof JsonParser jsonParser) ? jsonParser : null),
                    "Duplicate Object property \""+name+"\"");
        }
    }

    public void setNamesToWrap(Set<String> namesToWrap) {
        _namesToWrap = namesToWrap;
    }

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
        case TYPE_ROOT -> sb.append("/");
        case TYPE_ARRAY -> {
            sb.append('[');
            sb.append(getCurrentIndex());
            sb.append(']');
        }
        case TYPE_OBJECT -> {
            sb.append('{');
            if (_currentName != null) {
                sb.append('"');
                CharTypes.appendQuoted(sb, _currentName);
                sb.append('"');
            } else {
                sb.append('?');
            }
            sb.append('}');
        }
        }
        return sb.toString();
    }
}
