package tools.jackson.dataformat.xml.lists;

import java.util.*;

import org.junit.jupiter.api.Test;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.xml.XmlMapper;

import static org.junit.jupiter.api.Assertions.*;
import static tools.jackson.dataformat.xml.XmlReadFeature.PROCESS_XSI_NIL;
import static tools.jackson.dataformat.xml.XmlWriteFeature.WRITE_NULLS_AS_XSI_NIL;

// [dataformat-xml#584]
public class StringListRoundtripTest
{
    private final static String[] TEST_DATA = new String[] {"", "test", null, "test2"};

    private final static XmlMapper MAPPER_READ_WRITE_NULLS = XmlMapper.builder()
            .enable(PROCESS_XSI_NIL)
            .enable(WRITE_NULLS_AS_XSI_NIL)
            .build();
    private final static XmlMapper MAPPER_READ_NULLS = XmlMapper.builder()
            .enable(PROCESS_XSI_NIL)
            .disable(WRITE_NULLS_AS_XSI_NIL)
            .build();
    private final static XmlMapper MAPPER_WRITE_NULLS = XmlMapper.builder()
            .disable(PROCESS_XSI_NIL)
            .enable(WRITE_NULLS_AS_XSI_NIL)
            .build();

    @Test
    public void testStringArray() throws Exception
    {
        // default mode, should get back empty string
        _stringArrayRoundtrip(MAPPER_READ_NULLS, false);

        // xsi null enabled, should get back null
        _stringArrayRoundtrip(MAPPER_READ_WRITE_NULLS, true);

        // xsi null write enabled but processing disabled, should get back empty string
        _stringArrayRoundtrip(MAPPER_WRITE_NULLS, false);
    }

	private void _stringArrayRoundtrip(ObjectMapper mapper, boolean shouldBeNull) throws Exception
	{
		// serialize to string
		String xml = mapper.writeValueAsString(TEST_DATA);
		assertNotNull(xml);

		// then bring it back
		String[] result = mapper.readValue(xml, String[].class);
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
		_stringArrayPojoRoundtrip(MAPPER_READ_NULLS, false);

		// xsi null enabled, should get back null
		_stringArrayPojoRoundtrip(MAPPER_READ_WRITE_NULLS, true);

		// xsi null write enabled but processing disabled, should get back empty string
		_stringArrayPojoRoundtrip(MAPPER_WRITE_NULLS, false);
	}

	private void _stringArrayPojoRoundtrip(ObjectMapper mapper, boolean shouldBeNull) throws Exception
	{
		ArrayPojo arrayPojo = new ArrayPojo();
		arrayPojo.setArray(TEST_DATA);

		// serialize to string
		String xml = mapper.writeValueAsString(arrayPojo);
		assertNotNull(xml);

		// then bring it back
		ArrayPojo result = mapper.readValue(xml, ArrayPojo.class);
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

	static class ArrayPojo {
		String[] array;

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
		_stringListRoundtrip(MAPPER_READ_NULLS, false);

		// xsi null enabled, should get back null
		_stringListRoundtrip(MAPPER_READ_WRITE_NULLS, true);

		// xsi null write enabled but processing disabled, should get back empty string
		_stringListRoundtrip(MAPPER_WRITE_NULLS, false);
	}

	private void _stringListRoundtrip(ObjectMapper mapper, boolean shouldBeNull) throws Exception
	{
		List<String> list = Arrays.asList(TEST_DATA);

		// serialize to string
		String xml = mapper.writeValueAsString(list);
		assertNotNull(xml);

		// then bring it back
		List<String> result = mapper.readValue(xml, new TypeReference<List<String>>() {});
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
		_stringListPojoRoundtrip(MAPPER_READ_NULLS, false);

		// xsi null enabled, should get back null
		_stringListPojoRoundtrip(MAPPER_READ_WRITE_NULLS, true);

		// xsi null write enabled but processing disabled, should get back empty string
		_stringListPojoRoundtrip(MAPPER_WRITE_NULLS, false);
	}

	private void _stringListPojoRoundtrip(ObjectMapper mapper, boolean shouldBeNull) throws Exception
	{
		ListPojo listPojo =  new ListPojo();
		listPojo.setList(Arrays.asList(TEST_DATA));

		// serialize to string
		String xml = mapper.writeValueAsString(listPojo);
		assertNotNull(xml);

		// then bring it back
		ListPojo result = mapper.readValue(xml, ListPojo.class);
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

	static class ListPojo {
		List<String> list;

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
		_stringMapPojoRoundtrip(MAPPER_READ_NULLS, false);

		// xsi null enabled, should get back null
		_stringMapPojoRoundtrip(MAPPER_READ_WRITE_NULLS, true);

		// xsi null write enabled but processing disabled, should get back empty string
		_stringMapPojoRoundtrip(MAPPER_WRITE_NULLS, false);
	}

    private void _stringMapPojoRoundtrip(ObjectMapper mapper, boolean shouldBeNull) throws Exception
    {
        @SuppressWarnings("serial")
        Map<String, String> map = new HashMap<String, String>() {{
            put("a", "");
            put("b", "test");
            put("c", null);
            put("d", "test2");
        }};
        MapPojo mapPojo = new MapPojo();
        mapPojo.setMap(map);

        // serialize to string
        String xml = mapper.writeValueAsString(mapPojo);
        assertNotNull(xml);

        // then bring it back
        MapPojo result = mapper.readValue(xml, MapPojo.class);
        assertEquals(4, result.map.size());
        assertEquals("", result.map.get("a"));
        assertEquals("test", result.map.get("b"));
        if (shouldBeNull) {
            assertNull(result.map.get("c"));
        } else {
            assertEquals("", result.map.get("c"));
        }
        assertEquals("test2", result.map.get("d"));
    }

	static class MapPojo {
		Map<String, String> map;

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
		_stringPojoRoundtrip(MAPPER_READ_NULLS, false);

		// xsi null enabled, should get back null
		_stringPojoRoundtrip(MAPPER_READ_WRITE_NULLS, true);

		// xsi null write enabled but processing disabled, should get back empty string
		_stringPojoRoundtrip(MAPPER_WRITE_NULLS, false);
	}

	private void _stringPojoRoundtrip(ObjectMapper mapper, boolean shouldBeNull) throws Exception
	{
		StringPojo stringPojo = new StringPojo();
		stringPojo.setNormalString("test");
		stringPojo.setEmptyString("");
		stringPojo.setNullString(null);

		// serialize to string
		String xml = mapper.writeValueAsString(stringPojo);
		assertNotNull(xml);

		// then bring it back
		StringPojo result = mapper.readValue(xml, StringPojo.class);
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

	static class StringPojo {
		String normalString;
		String emptyString;
		String nullString;

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
