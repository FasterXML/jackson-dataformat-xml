package tools.jackson.dataformat.xml.failing;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import tools.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.*;

// related to [dataformat-xml#9] (and possibly others)
public class EnumIssue9Test extends XmlTestUtil
{
    static enum TestEnum { A, B, C; }
    
    static class UntypedEnumBean
    {
       @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="__type")
// this would actually work:
//        @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.WRAPPER_OBJECT)
        public Object value;

        public UntypedEnumBean() { }
        public UntypedEnumBean(TestEnum v) { value = v; }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    @Test
    public void testUntypedEnum() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new UntypedEnumBean(TestEnum.B));
        
        UntypedEnumBean result = MAPPER.readValue(xml, UntypedEnumBean.class);
        assertNotNull(result);
        assertNotNull(result.value);
        Object ob = result.value;
        
        if (TestEnum.class != ob.getClass()) {
            fail("Failed to deserialize TestEnum (got "+ob.getClass().getName()+") from: "+xml);
        }

        assertEquals(TestEnum.B, result.value);
    }
}
