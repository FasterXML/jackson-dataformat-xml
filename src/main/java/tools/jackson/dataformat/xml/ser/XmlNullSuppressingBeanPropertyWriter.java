package tools.jackson.dataformat.xml.ser;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.BeanPropertyWriter;

/**
 * Property writer sub-class used for unwrapped (no wrapper element) Collection/array
 * properties. It suppresses output of {@code null} values entirely, so that a null
 * collection is simply omitted -- consistent with the wrapped-collection case handled
 * by {@link XmlBeanPropertyWriter} (which also omits null collections).
 *<p>
 * Without this, an unwrapped null collection would be written via the standard null
 * handling as an {@code xsi:nil} element (when {@code WRITE_NULLS_AS_XSI_NIL} is enabled,
 * which it is by default in 3.x). That output is indistinguishable, on read, from a
 * collection containing a single null element (see [dataformat-xml#871]), so for null
 * collections we omit the element instead.
 *
 * @since 3.2.1
 */
public class XmlNullSuppressingBeanPropertyWriter
    extends BeanPropertyWriter
{
    public XmlNullSuppressingBeanPropertyWriter(BeanPropertyWriter wrapped)
    {
        super(wrapped);
    }

    @Override
    public void serializeAsProperty(Object bean, JsonGenerator g, SerializationContext ctxt)
        throws Exception
    {
        // [dataformat-xml#627]/[dataformat-xml#871]: omit a null unwrapped collection
        // (rather than writing it as an `xsi:nil` element), matching wrapped behavior.
        if (get(bean) == null) {
            return;
        }
        super.serializeAsProperty(bean, g, ctxt);
    }

    // NOTE: intentionally do NOT override `serializeAsElement()` (used for Array/tabular
    // shape, e.g. `@JsonFormat(shape = ARRAY)`): there, null entries are positional and
    // must be written (as `null`), not suppressed -- otherwise following entries would
    // shift. Only the Object-shape `serializeAsProperty()` path suppresses nulls here.
}
