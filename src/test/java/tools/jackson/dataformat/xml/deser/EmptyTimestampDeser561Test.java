package tools.jackson.dataformat.xml.deser;

import java.util.Date;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonFormat;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static org.junit.jupiter.api.Assertions.*;

// [dataformat-xml#561] Empty timestamp/date attribute should deserialize as null
public class EmptyTimestampDeser561Test extends XmlTestUtil
{
    static class DateAttrBean {
        @JacksonXmlProperty(isAttribute = true)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        public Date timestampDate;
    }

    static class DateElementBean {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        public Date timestampDate;
    }

    static class DateBean {
        @JacksonXmlProperty(isAttribute = true)
        @JsonFormat(pattern = "yyyy-MM-dd")
        public Date dateValue;
    }

    private final XmlMapper MAPPER = newMapper();

    // [dataformat-xml#561]: empty attribute
    @Test
    public void testEmptyDateAttributeWithTimestampFormat() throws Exception
    {
        DateAttrBean result = MAPPER.readValue(
                "<DateAttrBean timestampDate=\"\" />",
                DateAttrBean.class);
        assertNotNull(result);
        assertNull(result.timestampDate);
    }

    // [dataformat-xml#561]: empty element
    @Test
    public void testEmptyDateElement() throws Exception
    {
        DateElementBean result = MAPPER.readValue(
                "<DateElementBean><timestampDate></timestampDate></DateElementBean>",
                DateElementBean.class);
        assertNotNull(result);
        assertNull(result.timestampDate);
    }

    // [dataformat-xml#561]: empty Date attribute
    @Test
    public void testEmptyDateAttribute() throws Exception
    {
        DateBean result = MAPPER.readValue(
                "<DateBean dateValue=\"\" />",
                DateBean.class);
        assertNotNull(result);
        assertNull(result.dateValue);
    }
}
