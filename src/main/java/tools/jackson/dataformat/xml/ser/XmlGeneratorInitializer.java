package tools.jackson.dataformat.xml.ser;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
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
 * </ul>
 *<p>
 * NOTE: instances are mutable, not thread-safe.
 *
 * @since 3.2
 */
public class XmlGeneratorInitializer
    implements GeneratorInitializer
{
    protected DTD _dtd;

    @Override
    public void initialize(SerializationConfig config, JsonGenerator g) throws JacksonException {
        if (g instanceof ToXmlGenerator xg) {
            xg.initConfig(_dtd);
        }
    }

    public XmlGeneratorInitializer setDTD(String rootName,
            String systemId, String publicId,
            String internalSubset) {
        return setDTD(new DTD(rootName, systemId, publicId, internalSubset));
    }

    public XmlGeneratorInitializer setDTD(DTD dtd) {
        _dtd = dtd;
        return this;
    }
}
