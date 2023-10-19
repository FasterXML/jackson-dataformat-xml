package tools.jackson.dataformat.xml.failing;

import java.util.*;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlText;

import static org.junit.Assert.assertEquals;

// For [dataformat-xml#509]
public class UnexpectedNonWhitespaceText509Test {
	@JsonPropertyOrder({ "key", "content" })
	static class Data {
	    @JacksonXmlText
		public String content;
		@JacksonXmlProperty(isAttribute=true)
		protected java.lang.String key;

		public java.lang.String getKey() {
			return key;
		}

		public void setKey(java.lang.String value) {
			this.key = value;
		}
	}

	static class MetaData {
		protected List<Data> data;

		public List<Data> getData() {
			if (data == null) {
				data = new ArrayList<>();
			}
			return this.data;
		}

		public void setData(List<Data> data) {
			this.data = data;
		}

		@Override
		public String toString() {
			return Objects.toString(data);
		}
	}

	private final XmlMapper XML_MAPPER = new XmlMapper();

	@Test
	public void testDeSerData() throws Exception {
		Data value = deSer("<data key=\"MadeWith\">Text Editor</data>", Data.class);
		assertEquals("\"key\" attribute not correctly deserialized", value.getKey(), "MadeWith");
	}

	@Test
	public void testDeSerMetaData() throws Exception {
		MetaData value = deSer("<metaData>\n" //
				+ "    <data key=\"MadeWith\">Text Editor</data>\n" //
				+ "    <data key=\"Version\">1.0.0</data>\n" //
				+ "</metaData>", MetaData.class);
		List<Data> entries = value.getData();
		assertEquals("\"data\" not correctly deserialized", entries.size(), 2);
		Data entry = entries.get(0);
		assertEquals("\"key\" attribute not correctly deserialized", entry.getKey(), "MadeWith");
		entry = entries.get(1);
		assertEquals("\"key\" attribute not correctly deserialized", entry.getKey(), "Version");
	}

	private <T> T deSer(String xmlString, Class<T> clazz) throws Exception {
	    return XML_MAPPER.readerFor(clazz).readValue(xmlString);
	}
}
