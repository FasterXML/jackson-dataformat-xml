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
		String[] array = new String[] {"", "test", null, "test2"};

		// serialize to string
		String xml = MAPPER.writeValueAsString(array);
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
		List<String> list = asList("", "test", null, "test2");

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
	public void testStringMap() throws Exception
	{
		// default mode, should get back empty string
		MAPPER.disable(WRITE_NULLS_AS_XSI_NIL);
		MAPPER.enable(PROCESS_XSI_NIL);
		stringMapRoundtrip(false);

		// xsi null enabled, should get back null
		MAPPER.enable(WRITE_NULLS_AS_XSI_NIL);
		MAPPER.enable(PROCESS_XSI_NIL);
		stringMapRoundtrip(true);

		// xsi null write enabled but processing disabled, should get back empty string
		MAPPER.enable(WRITE_NULLS_AS_XSI_NIL);
		MAPPER.disable(PROCESS_XSI_NIL);
		stringMapRoundtrip(false);
	}

	private void stringMapRoundtrip(boolean shouldBeNull) throws Exception
	{
		Map<String, String> map = new HashMap<String, String>() {{
			put("a", "");
			put("b", "test");
			put("c", null);
			put("d", "test2");
		}};
		MapPojo mapPojo = new MapPojo();
		mapPojo.setMap( map );

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

		public MapPojo() {
		}

		public Map<String, String> getMap() {
			return map;
		}

		public void setMap(Map<String, String> map) {
			this.map = map;
		}
	}
}
