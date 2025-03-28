package com.fasterxml.jackson.dataformat.xml;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RoundtripContentTest extends XmlTestUtil
{
    private final XmlMapper MAPPER = new XmlMapper();

    @Test
    public void testRoundtrip() throws Exception
    {
        MediaItem.Content content = new MediaItem.Content();
        content.setTitle("content");
        content.addPerson("William");
        content.addPerson("Robert");

        MediaItem input = new MediaItem(content);
        input.addPhoto(new MediaItem.Photo("http://a", "title1", 200, 100, MediaItem.Size.LARGE));
        input.addPhoto(new MediaItem.Photo("http://b", "title2", 640, 480, MediaItem.Size.SMALL));

        ObjectWriter w = MAPPER.writerFor(MediaItem.class);

        // two variants; first without indentation
        _verifyRoundtrip(w.writeValueAsString(input), input);

        // and then with indentation
        _verifyRoundtrip(w.withDefaultPrettyPrinter()
                .writeValueAsString(input), input);
    }

    private void _verifyRoundtrip(String xml, MediaItem exp) throws Exception
    {
        ObjectReader r = MAPPER.readerFor(MediaItem.class);
        MediaItem result = r.readValue(xml);
        assertNotNull(result);
        assertEquals(exp.getContent().getTitle(), result.getContent().getTitle());
    }
}
