package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class TestComplex extends XmlTestBase
{
    private final XmlMapper MAPPER = new XmlMapper();
    
    public void testRoundtrip() throws Exception
    {
        MediaItem.Content content = new MediaItem.Content();
        content.setTitle("content");
        content.addPerson("William");
        content.addPerson("Robert");

        MediaItem input = new MediaItem(content);
        input.addPhoto(new MediaItem.Photo("http://a", "title1", 200, 100, MediaItem.Size.LARGE));
        input.addPhoto(new MediaItem.Photo("http://b", "title2", 640, 480, MediaItem.Size.SMALL));

        ObjectWriter w = MAPPER.writerWithType(MediaItem.class);

        /*
        StringWriter sw = new StringWriter();
        try {
        w.writeValue(sw, input);
        } finally {
            System.err.println("So far -> ["+sw+"]");
        }
        */
        
        String xml = w.writeValueAsString(input);

//System.err.println("DEBUG: Xml == "+xml);

        ObjectReader r = MAPPER.reader(MediaItem.class);
        MediaItem result = r.readValue(xml);
        assertNotNull(result);
        assertEquals(content.getTitle(), result.getContent().getTitle());
    }
}
