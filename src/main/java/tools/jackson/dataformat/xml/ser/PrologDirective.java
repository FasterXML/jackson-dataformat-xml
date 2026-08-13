package tools.jackson.dataformat.xml.ser;

/**
 * Base API for XML nodes -- Comments, DTDs, Processing Instructions -- to
 * be written in Document Prolog (before actual XML Document (root node),
 * after optional XML Declaration).
 *
 * @since 3.2
 */
public interface PrologDirective
    extends XmlGeneratorWritable
{
    // All we need is so far in XmlGeneratorWritable
}
