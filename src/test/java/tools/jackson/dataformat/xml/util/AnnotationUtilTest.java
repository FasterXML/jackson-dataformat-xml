package tools.jackson.dataformat.xml.util;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertNull;

public class AnnotationUtilTest extends XmlTestUtil
{
    // [dataformat-xml#884]: property definitions without any accessors do occur,
    // and have no annotations to find
    @Test
    public void testNullMemberAccepted() throws Exception {
        MapperConfig<?> config = new XmlMapper().serializationConfig();
        AnnotationIntrospector intr = config.getAnnotationIntrospector();

        assertNull(AnnotationUtil.findNamespaceAnnotation(config, intr, null));
        assertNull(AnnotationUtil.findIsAttributeAnnotation(config, intr, null));
        assertNull(AnnotationUtil.findIsTextAnnotation(config, intr, null));
        assertNull(AnnotationUtil.findIsCDataAnnotation(config, intr, null));
        assertNull(AnnotationUtil.findXmlPropertyInnerName(config, intr, null));
    }
}
