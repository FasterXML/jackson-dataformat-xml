package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import static com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser.Feature.PROCESS_XSI_NIL;
import static com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator.Feature.WRITE_NULLS_AS_XSI_NIL;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// [dataformat-xml#584]
public class StringListRoundtripTest {
	private final static XmlMapper MAPPER = new XmlMapper();

	private final static String[] TEST_DATA = new String[] {"", "test", null, "test2"};

	@Test
	public void testStringArray() throws Exception
	{
		// default mode, should get back empty string
		MAPPER.disable(WRITE_NULLS_AS_XSI_NIL);
		MAPPER.enable(PROCESS_XSI_NIL);
		stringArrayRoundtrip(false);

		// xsi null enabled, should get back null
		MAPPER.enable(WRITE_NULLS_AS_XSI_NIL);
		MAPPER.enable(PROCESS_XSI_NIL);
		stringArrayRoundtrip(true);

		// xsi null write enabled but processing disabled, should get back empty string
		MAPPER.enable(WRITE_NULLS_AS_XSI_NIL);
		MAPPER.disable(PROCESS_XSI_NIL);
		stringArrayRoundtrip(false);
	}

	private void stringArrayRoundtrip(boolean shouldBeNull) throws Exception
	{
		// serialize to string
		String xml = MAPPER.writeValueAsString(TEST_DATA);
		assertNotNull(xml);

		// then bring it back
		String[] result = MAPPER.readValue(xml, String[].class);
		assertEquals(4, result.length);
		assertEquals("", result[0]);
		assertEquals("test", result[1]);
		if (shouldBeNull)
		{
			assertNull(result[2]);
		} else
		{
			assertEquals("", result[2]);
		}
		assertEquals("test2", result[3]);
	}

	@Test
	public void testStringArrayPojo() throws Exception
	{
		// default mode, should get back empty string
		MAPPER.disable(WRITE_NULLS_AS_XSI_NIL);
		MAPPER.enable(PROCESS_XSI_NIL);
		stringArrayPojoRoundtrip(false);

		// xsi null enabled, should get back null
		MAPPER.enable(WRITE_NULLS_AS_XSI_NIL);
		MAPPER.enable(PROCESS_XSI_NIL);
		stringArrayPojoRoundtrip(true);

		// xsi null write enabled but processing disabled, should get back empty string
		MAPPER.enable(WRITE_NULLS_AS_XSI_NIL);
		MAPPER.disable(PROCESS_XSI_NIL);
		stringArrayPojoRoundtrip(false);
	}

	private void stringArrayPojoRoundtrip(boolean shouldBeNull) throws Exception
	{
		ArrayPojo arrayPojo = new ArrayPojo();
		arrayPojo.setArray(TEST_DATA);

		// serialize to string
		String xml = MAPPER.writeValueAsString(arrayPojo);
		assertNotNull(xml);

		// then bring it back
		ArrayPojo result = MAPPER.readValue(xml, ArrayPojo.class);
		assertEquals(4, result.array.length);
		assertEquals("", result.array[0]);
		assertEquals("test", result.array[1]);
		if (shouldBeNull)
		{
			assertNull(result.array[2]);
		} else
		{
			assertEquals("", result.array[2]);
		}
		assertEquals("test2", result.array[3]);
	}

	private static class ArrayPojo {
		private String[] array;

		public String[] getArray() {
			return array;
		}

		public void setArray(String[] array) {
			this.array = array;
		}
	}

	@Test
	public void testStringList() throws Exception
	{
		// default mode, should get back empty string
		MAPPER.disable(WRITE_NULLS_AS_XSI_NIL);
		MAPPER.enable(PROCESS_XSI_NIL);
		stringListRoundtrip(false);

		// xsi null enabled, should get back null
		MAPPER.enable(WRITE_NULLS_AS_XSI_NIL);
		MAPPER.enable(PROCESS_XSI_NIL);
		stringListRoundtrip(true);

		// xsi null write enabled but processing disabled, should get back empty string
		MAPPER.enable(WRITE_NULLS_AS_XSI_NIL);
		MAPPER.disable(PROCESS_XSI_NIL);
		stringListRoundtrip(false);
	}

	private void stringListRoundtrip(boolean shouldBeNull) throws Exception
	{
		List<String> list = asList(TEST_DATA);

		// serialize to string
		String xml = MAPPER.writeValueAsString(list);
		assertNotNull(xml);

		// then bring it back
		List<String> result = MAPPER.readValue(xml, new TypeReference<List<String>>() {});
		assertEquals(4, result.size());
		assertEquals("", result.get(0));
		assertEquals("test", result.get(1));
		if (shouldBeNull)
		{
			assertNull(result.get(2));
		} else
		{
			assertEquals("", result.get(2));
		}
		assertEquals("test2", result.get(3));
	}

	@Test
	public void testStringListPojo() throws Exception
	{
		// default mode, should get back empty string
		MAPPER.disable(WRITE_NULLS_AS_XSI_NIL);
		MAPPER.enable(PROCESS_XSI_NIL);
		stringListPojoRoundtrip(false);

		// xsi null enabled, should get back null
		MAPPER.enable(WRITE_NULLS_AS_XSI_NIL);
		MAPPER.enable(PROCESS_XSI_NIL);
		stringListPojoRoundtrip(true);

		// xsi null write enabled but processing disabled, should get back empty string
		MAPPER.enable(WRITE_NULLS_AS_XSI_NIL);
		MAPPER.disable(PROCESS_XSI_NIL);
		stringListPojoRoundtrip(false);
	}

	private void stringListPojoRoundtrip(boolean shouldBeNull) throws Exception
	{
		ListPojo listPojo =  new ListPojo();
		listPojo.setList(asList(TEST_DATA));

		// serialize to string
		String xml = MAPPER.writeValueAsString(listPojo);
		assertNotNull(xml);

		// then bring it back
		ListPojo result = MAPPER.readValue(xml, ListPojo.class);
		assertEquals(4, result.list.size());
		assertEquals("", result.list.get(0));
		assertEquals("test", result.list.get(1));
		if (shouldBeNull)
		{
			assertNull(result.list.get(2));
		} else
		{
			assertEquals("", result.list.get(2));
		}
		assertEquals("test2", result.list.get(3));
	}

	private static class ListPojo {
		private List<String> list;

		public List<String> getList() {
			return list;
		}

		public void setList(List<String> list) {
			this.list = list;
		}
	}

	@Test
	public void testStringMapPojo() throws Exception
	{
		// default mode, should get back empty string
		MAPPER.disable(WRITE_NULLS_AS_XSI_NIL);
		MAPPER.enable(PROCESS_XSI_NIL);
		stringMapPojoRoundtrip(false);

		// xsi null enabled, should get back null
		MAPPER.enable(WRITE_NULLS_AS_XSI_NIL);
		MAPPER.enable(PROCESS_XSI_NIL);
		stringMapPojoRoundtrip(true);

		// xsi null write enabled but processing disabled, should get back empty string
		MAPPER.enable(WRITE_NULLS_AS_XSI_NIL);
		MAPPER.disable(PROCESS_XSI_NIL);
		stringMapPojoRoundtrip(false);
	}

	private void stringMapPojoRoundtrip(boolean shouldBeNull) throws Exception
	{
		Map<String, String> map = new HashMap<String, String>() {{
			put("a", "");
			put("b", "test");
			put("c", null);
			put("d", "test2");
		}};
		MapPojo mapPojo = new MapPojo();
		mapPojo.setMap(map);

		// serialize to string
		String xml = MAPPER.writeValueAsString(mapPojo);
		assertNotNull(xml);

		// then bring it back
		MapPojo result = MAPPER.readValue(xml, MapPojo.class);
		assertEquals(4, result.map.size());
		assertEquals("", result.map.get("a"));
		assertEquals("test", result.map.get("b"));
		if (shouldBeNull)
		{
			assertNull(result.map.get("c"));
		} else
		{
			assertEquals("", result.map.get("c"));
		}
		assertEquals("test2", result.map.get("d"));
	}

	private static class MapPojo {
		private Map<String, String> map;

		public Map<String, String> getMap() {
			return map;
		}

		public void setMap(Map<String, String> map) {
			this.map = map;
		}
	}

	@Test
	public void testStringPojo() throws Exception
	{
		// default mode, should get back empty string
		MAPPER.disable(WRITE_NULLS_AS_XSI_NIL);
		MAPPER.enable(PROCESS_XSI_NIL);
		stringPojoRoundtrip(false);

		// xsi null enabled, should get back null
		MAPPER.enable(WRITE_NULLS_AS_XSI_NIL);
		MAPPER.enable(PROCESS_XSI_NIL);
		stringPojoRoundtrip(true);

		// xsi null write enabled but processing disabled, should get back empty string
		MAPPER.enable(WRITE_NULLS_AS_XSI_NIL);
		MAPPER.disable(PROCESS_XSI_NIL);
		stringPojoRoundtrip(false);
	}

	private void stringPojoRoundtrip(boolean shouldBeNull) throws Exception
	{
		StringPojo stringPojo = new StringPojo();
		stringPojo.setNormalString("test");
		stringPojo.setEmptyString("");
		stringPojo.setNullString(null);

		// serialize to string
		String xml = MAPPER.writeValueAsString(stringPojo);
		assertNotNull(xml);

		// then bring it back
		StringPojo result = MAPPER.readValue(xml, StringPojo.class);
		assertEquals("test", result.normalString);
		assertEquals("", result.emptyString);
		if (shouldBeNull)
		{
			assertNull(result.nullString);
		} else
		{
			assertEquals("", result.nullString);
		}
	}

	private static class StringPojo {
		private String normalString;
		private String emptyString;
		private String nullString;

		public String getNormalString() {
			return normalString;
		}

		public void setNormalString(String normalString) {
			this.normalString = normalString;
		}

		public String getEmptyString() {
			return emptyString;
		}

		public void setEmptyString(String emptyString) {
			this.emptyString = emptyString;
		}

		public String getNullString() {
			return nullString;
		}

		public void setNullString(String nullString) {
			this.nullString = nullString;
		}
	}
}
