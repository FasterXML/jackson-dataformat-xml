package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlInclude.Include;

public class TestSerializationInclude extends XmlTestBase
{
	@JacksonXmlInclude(value = Include.NON_EMPTY)
	static class NonEmptyClassBean {
        public String a;
        public String b;
        public String c;

        public NonEmptyClassBean(String a, String b, String c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }

	static class NonEmptyPropertyBean {
        public String a;
        public String b;
    	@JacksonXmlInclude(value = Include.NON_EMPTY)
        public String c;

        public NonEmptyPropertyBean(String a, String b, String c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }

	@JacksonXmlInclude(value = Include.NON_EMPTY)
	static class MergeBean {
        public String a;
        @JacksonXmlInclude(value = Include.NON_NULL)
        public String b;
        public String c;

        public MergeBean(String a, String b, String c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }
	
	
    private final XmlMapper MAPPER = newMapper();

    public void testNonEmptyClass() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new NonEmptyClassBean("1", "", "3"));
        assertEquals("<NonEmptyClassBean><a>1</a><c>3</c></NonEmptyClassBean>", xml);
    }
    
    public void testNonEmptyProperty() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new NonEmptyPropertyBean("1", "", ""));
        assertEquals("<NonEmptyPropertyBean><a>1</a><b></b></NonEmptyPropertyBean>", xml);
    }
    
    public void testMerge() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new MergeBean("1", "", "3"));
        assertEquals("<MergeBean><a>1</a><b></b><c>3</c></MergeBean>", xml);
    }
    
}