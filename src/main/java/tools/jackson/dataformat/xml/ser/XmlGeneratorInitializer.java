package tools.jackson.dataformat.xml.ser;

import java.util.ArrayList;
import java.util.List;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.exc.StreamWriteException;

import tools.jackson.databind.*;
import tools.jackson.databind.cfg.GeneratorInitializer;

/**
 * Default {@link GeneratorInitializer} implementation to use with
 * {@link ToXmlGenerator}, registered via
 * {@link ObjectWriter#with(GeneratorInitializer)}.
 * It allows output of various document-level things such as
 *<ul>
 * <li>Document Type Declarations (DTD); that is "&lt;!DOCTYPE>" directive
 *  </li>
 * <li>XML Comments (in Document prolog, before the root element)
 *  </li>
 * </ul>
 *<p>
 * NOTE: instances are mutable, not thread-safe.
 *
 * @since 3.2
 */
public class XmlGeneratorInitializer
    implements GeneratorInitializer
{
    protected List<XmlPrologDirective> _directives;

    protected boolean _addLfBetweenPrologDirectives = true;

    protected boolean _hasDTD;

    @Override
    public void initialize(SerializationConfig config, JsonGenerator g) throws JacksonException {
        if (g instanceof ToXmlGenerator xg) {
            xg.initProlog(_addLfBetweenPrologDirectives, _directives);
        }
    }

    /**
     * Method to change whether line-feeds are to be added between Prolog directives
     * or not: default being they are (enabled).
     *
     * @param addLFs Whether line-feeds are to be added or not (default: {@code true})
     *
     * @return This initializer for call chaining
     */
    public XmlGeneratorInitializer linefeedsBetweenPrologDirectives(boolean addLFs) {
        _addLfBetweenPrologDirectives = addLFs;
        return this;
    }

    /**
     * Method for adding XML comment; to be written in position added
     * with respective to other directives
     * (but always after XML Declaration which must come before any other output;
     * and before Document Root element)
     *
     * @param commentContent (optional) Comment content to include
     *
     * @return This initializer for call chaining
     */
    public XmlGeneratorInitializer addComment(String commentContent) {
        return _add(new Comment(commentContent));
    }

    /**
     * Convenience method that constructs {@link DTD} out of arguments
     * and calls {@link #addDTD(DTD)}.
     *
     * @param rootName (required) Root name for DTD
     * @param systemId (optional) System Id for DTD
     * @param publicId (optional) Public Id for DTD
     * @param internalSubset (optional) Internal subset for DTD (not including
     *   surrounding brackets
     *
     * @return This initializer for call chaining
     */
    public XmlGeneratorInitializer addDTD(String rootName,
            String systemId, String publicId,
            String internalSubset) {
        return addDTD(new DTD(rootName, systemId, publicId, internalSubset));
    }

    /**
     * Method for adding Document Type Declaration (DTD) directive; to
     * be written in position added with respective to other directives
     * (but always after XML Declaration which must come before any other output;
     * and before Document Root element)
     *
     * @param dtd DTD to write
     *
     * @return This initializer for call chaining
     */
    public XmlGeneratorInitializer addDTD(DTD dtd) {
        if (_hasDTD) {
            throw new StreamWriteException(null, "Cannot add another `DTD`, initializer already has one");
        }
        _hasDTD = true;
        return _add(dtd);
    }

    protected XmlGeneratorInitializer _add(XmlPrologDirective d) {
        if (_directives == null) {
            _directives = new ArrayList<>();
        }
        _directives.add(d);
        return this;
    }
}
