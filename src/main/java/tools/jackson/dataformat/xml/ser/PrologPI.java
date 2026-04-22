package tools.jackson.dataformat.xml.ser;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamWriter2;

import tools.jackson.dataformat.xml.util.ArgUtil;

/**
 * Value container to represent XML Processing Instruction (PI)
 * within Prolog part of the Document (before XML Root element,
 * after XML declaration if one written),
 * to be written using {@link XmlGeneratorInitializer}.
 *
 * @since 3.2
 */
public record PrologPI(String target, String data)
    implements PrologDirective
{
    public PrologPI {
        target = ArgUtil.nonEmptyNonNull("target", target);
        data = ArgUtil.emptyToNull(data);
    }

    @Override
    public void write(ToXmlGenerator xmlGen, XMLStreamWriter2 sw) throws XMLStreamException {
        if (data == null) {
            sw.writeProcessingInstruction(target);
        } else {
            sw.writeProcessingInstruction(target, data);
        }
    }
}
