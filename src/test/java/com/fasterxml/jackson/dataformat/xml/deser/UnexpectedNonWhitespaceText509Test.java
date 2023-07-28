package com.fasterxml.jackson.dataformat.xml.deser;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.Test;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlMixed;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

public class UnexpectedNonWhitespaceText509Test {
	@XmlRootElement(name = "data")
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "Data", propOrder = { "key", "content" })
	public static class Data {
		@XmlMixed
		@XmlAnyElement(lax = true)
		protected List<Object> content;
		@XmlAttribute(name = "key", required = true)
		protected java.lang.String key;

		public List<Object> getContent() {
			if (content == null) {
				content = new ArrayList<>();
			}
			return this.content;
		}

		public void setContent(List<Object> content) {
			this.content = content;
		}

		public java.lang.String getKey() {
			return key;
		}

		public void setKey(java.lang.String value) {
			this.key = value;
		}
	}

	@XmlRootElement(name = "metaData")
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "MetaData", propOrder = { "data" })
	public static class MetaData {
		@XmlElement(required = true)
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

	@Test
	public void testDeSerData() throws JacksonException {
		Data value = deSer("<data key=\"MadeWith\">Text Editor</data>", Data.class);
		assertEquals("\"key\" attribute not correctly deserialized", value.getKey(), "MadeWith");
		assertEquals("\"content\" not correctly deserialized", value.getContent(), List.of("Text Editor"));
	}

	@Test
	public void testDeSerMetaData() throws JacksonException {
		MetaData value = deSer("<metaData>\n" //
				+ "    <data key=\"MadeWith\">Text Editor</data>\n" //
				+ "    <data key=\"Version\">1.0.0</data>\n" //
				+ "</metaData>", MetaData.class);
		List<Data> entries = value.getData();
		assertEquals("\"data\" not correctly deserialized", entries.size(), 2);
		Data entry = entries.get(0);
		assertEquals("\"key\" attribute not correctly deserialized", entry.getKey(), "MadeWith");
		assertEquals("\"content\" not correctly deserialized", entry.getContent(), List.of("Text Editor"));
		entry = entries.get(1);
		assertEquals("\"key\" attribute not correctly deserialized", entry.getKey(), "Version");
		assertEquals("\"content\" not correctly deserialized", entry.getContent(), List.of("1.0.0"));
	}

	private <T> T deSer(String xmlString, Class<T> clazz) throws JacksonException {
		return new XmlMapper().readValue(xmlString, clazz);
	}
}
