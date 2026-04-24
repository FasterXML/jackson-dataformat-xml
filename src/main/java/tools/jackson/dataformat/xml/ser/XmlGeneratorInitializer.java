package tools.jackson.dataformat.xml.ser;

import java.util.ArrayList;
import java.util.List;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.exc.StreamWriteException;

import tools.jackson.databind.*;
import tools.jackson.databind.cfg.GeneratorInitializer;

import tools.jackson.dataformat.xml.XmlWriteFeature;

/**
 * Default {@link GeneratorInitializer} implementation to use with
 * {@link ToXmlGenerator}, registered via
 * {@link ObjectWriter#with(GeneratorInitializer)}.
 * It allows output of various document-level things such as
 *<ul>
 * <li>Document Type Declarations (DTD); that is "&lt;!DOCTYPE>" directive
 *  </li>
 * <li>Comments (in Document prolog, before the root element)
 *  </li>
 * <li>Namespace bindings (prefix to URI mappings)
 *  </li>
 * <li>Processing Instructions (PIs; in Document prolog, before the root element)
 *  </li>
 * <li>XML Declaration (with custom version, encoding and/or standalone value)
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
    /**
     * Prolog Directives to pass for generator to write.
     */
    protected List<PrologDirective> _directives;

    /**
     * Namespace bindings (prefix to URI) to register with generator.
     */
    protected List<NamespaceBinding> _namespaceBindings;

    /**
     * Custom XML declaration to write.
     */
    protected XmlDeclaration _xmlDeclaration;

    protected boolean _addLfBetweenPrologDirectives = true;

    protected boolean _hasDTD;

    @Override
    public void initialize(SerializationConfig config, JsonGenerator g) throws JacksonException {
        if (g instanceof ToXmlGenerator xg) {
            xg.initDocument(_xmlDeclaration,
                    _addLfBetweenPrologDirectives, _directives,
                    _namespaceBindings);
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
     * Method for adding XML comment; to be written at position added
     * relative to other directives
     * (but always after XML Declaration which must come before any other output;
     * and before Document Root element)
     *
     * @param commentContent (optional) Comment content to include
     *
     * @return This initializer for call chaining
     */
    public XmlGeneratorInitializer addComment(String commentContent) {
        return _add(new PrologComment(commentContent));
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
     * be written at position added relative to other directives
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

    /**
     * Method for adding XML Processing Instruction (PI); to be written at
     * position added relative to other directives
     * (but always after XML Declaration which must come before any other output;
     * and before Document Root element)
     *
     * @param target Processing Instruction target: must not be {@code null} or
     *   empty String
     * @param data (optional) Processing Instruction data part, if any,
     *    separated by a space from target (if not null)
     *
     * @return This initializer for call chaining
     */
    public XmlGeneratorInitializer addPI(String target, String data) {
        return _add(new PrologPI(target, data));
    }

    /**
     * Method for specifying namespace URI to preferentially bind to the
     * "default namespace" (one used when element has no prefix).
     * This will guide underlying generator to add necessary
     * declarations when actually writing elements with matching
     * namespace URI.
     *
     * @param namespaceURI URI of the default namespace
     *
     * @return This initializer for call chaining
     */
    public XmlGeneratorInitializer addDefaultNamespace(String namespaceURI) {
        return addNamespace(null, namespaceURI);
    }

    /**
     * Method for adding a mapping (binding) between given prefix and matching
     * namespace URI. This will guide underlying generator to add necessary
     * declarations when actually writing namespaced attributes and elements.
     *
     * @param prefix Prefix to use for namespace
     * @param namespaceURI URI of the namespace
     *
     * @return This initializer for call chaining
     */
    public XmlGeneratorInitializer addNamespace(String prefix, String namespaceURI) {
        if (_namespaceBindings == null) {
            _namespaceBindings = new ArrayList<>();
        }
        _namespaceBindings.add(new NamespaceBinding(prefix, namespaceURI));
        return this;
    }

    /**
     * Method for specifying custom XML declaration to write.
     *<p>
     * When a custom XML declaration is registered it fully replaces output
     * that would otherwise be produced by
     * {@link XmlWriteFeature#WRITE_XML_DECLARATION},
     * {@link XmlWriteFeature#WRITE_XML_1_1} and
     * {@link XmlWriteFeature#WRITE_STANDALONE_YES_TO_XML_DECLARATION}:
     * those format features are ignored. Note that caller is responsible
     * for ensuring the declared encoding matches the encoding the
     * underlying {@code Writer} or {@code OutputStream} actually uses.
     *
     * @param version XML version: either "1.0" or "1.1"
     * @param encoding {@code encoding} content will be encoded in: usually "UTF-8"
     *
     * @return This initializer for call chaining
     */
    public XmlGeneratorInitializer addXmlDeclaration(String version, String encoding) {
        return addXmlDeclaration(new XmlDeclaration(version, encoding, null));
    }

    /**
     * Method for specifying custom XML declaration to write.
     *<p>
     * See {@link #addXmlDeclaration(String, String)} for details on how
     * this interacts with {@link XmlWriteFeature} flags.
     *
     * @param version XML version: either "1.0" or "1.1"
     * @param encoding {@code encoding} content will be encoded in: usually "UTF-8"
     * @param standalone {@code standalone} pseudo-attribute value to write
     *
     * @return This initializer for call chaining
     */
    public XmlGeneratorInitializer addXmlDeclaration(String version, String encoding,
            boolean standalone) {
        return addXmlDeclaration(new XmlDeclaration(version, encoding, standalone));
    }

    /**
     * Method for specifying custom XML declaration to write.
     *<p>
     * See {@link #addXmlDeclaration(String, String)} for details on how
     * this interacts with {@link XmlWriteFeature} flags.
     *
     * @param xmlDeclaration declaration to write
     *
     * @return This initializer for call chaining
     */
    public XmlGeneratorInitializer addXmlDeclaration(XmlDeclaration xmlDeclaration) {
        if (_xmlDeclaration != null) {
            throw new StreamWriteException(null,
                    "Cannot add another XML Declaration, initializer already has one");
        }
        _xmlDeclaration = xmlDeclaration;
        return this;
    }

    protected XmlGeneratorInitializer _add(PrologDirective d) {
        if (_directives == null) {
            _directives = new ArrayList<>();
        }
        _directives.add(d);
        return this;
    }
}
