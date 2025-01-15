package com.fasterxml.jackson.dataformat.xml.deser;

import java.util.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import static org.junit.jupiter.api.Assertions.*;

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
		@JacksonXmlElementWrapper(useWrapping = false)
		@JacksonXmlProperty(localName = "data")
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
		assertEquals(value.getKey(), "MadeWith", "\"key\" attribute not correctly deserialized");
	}

	@Test
	public void testDeSerMetaData() throws Exception {
		MetaData value = deSer("<metaData>\n" //
				+ "    <data key=\"MadeWith\">Text Editor</data>\n" //
				+ "    <data key=\"Version\">1.0.0</data>\n" //
				+ "</metaData>", MetaData.class);
		List<Data> entries = value.getData();
		assertEquals(2, entries.size(), "\"data\" not correctly deserialized");
		Data entry = entries.get(0);
		assertEquals(entry.getKey(), "MadeWith", "\"key\" attribute not correctly deserialized");
		entry = entries.get(1);
		assertEquals(entry.getKey(), "Version", "\"key\" attribute not correctly deserialized");
	}

	private <T> T deSer(String xmlString, Class<T> clazz) throws Exception {
	    return XML_MAPPER.readerFor(clazz).readValue(xmlString);
	}
}
