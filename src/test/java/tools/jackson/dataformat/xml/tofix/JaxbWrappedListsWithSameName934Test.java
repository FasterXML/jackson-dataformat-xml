package tools.jackson.dataformat.xml.tofix;

import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.testutil.failure.JacksonTestFailureExpected;
import tools.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// [dataformat-xml#934]: Separately wrapped JAXB lists can share an item name.
public class JaxbWrappedListsWithSameName934Test extends XmlTestUtil
{
    record Wrapper(
            @XmlElementWrapper(name = "ones") @XmlElement(name = "name") List<String> ones,
            @XmlElementWrapper(name = "twos") @XmlElement(name = "name") List<String> twos) { }

    @JacksonTestFailureExpected
    @Test
    public void testTwoWrappedListsWithSameItemName() throws Exception
    {
        XmlMapper mapper = XmlMapper.builder()
                .addModule(new JakartaXmlBindAnnotationModule())
                .build();
        Wrapper input = new Wrapper(List.of("one1", "one2"), List.of("two1"));

        String xml = mapper.writeValueAsString(input);
        assertTrue(xml.contains("<ones><name>one1</name><name>one2</name></ones>"), xml);
        assertTrue(xml.contains("<twos><name>two1</name></twos>"), xml);
        assertEquals(input, mapper.readValue(xml, Wrapper.class));
    }
}
