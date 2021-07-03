package com.fasterxml.jackson.dataformat.xml.deser;

import java.util.List;

import javax.xml.stream.XMLInputFactory;

import com.ctc.wstx.stax.WstxInputFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

// [dataformat-xml#422]
public class NonNamespaceAwareDeser422Test extends XmlTestBase
{
 // [dataformat-xml#422]
    @JsonIgnoreProperties(ignoreUnknown = true) // to skip `xmlns`
    static class RssDocument422 {
        public RssChannel channel;
    }

    @JsonIgnoreProperties(ignoreUnknown = true) // to skip `xmlns`
    static class RssChannel {
        public String title;
        public String description;
        @JacksonXmlProperty(localName = "link")
        public String siteUrl;
        @JacksonXmlProperty(localName = "lastBuildDate")
        public String updated;
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "item")
        public List<RssItem> items;
    }

    @JsonIgnoreProperties({ "category" })
    static class RssItem {
        @JacksonXmlProperty(localName = "guid")
        public String localId;
        @JacksonXmlProperty(localName = "pubDate")
        public String updated;
        public String title;
        @JacksonXmlProperty(localName = "link")
        public String articleUrl;
        @JacksonXmlProperty(localName = "dc:creator")
        public String author;
        @JacksonXmlProperty(localName = "content:encoded")
        public String encodedContent;
        @JacksonXmlProperty(localName = "description")
        public String content;
    }

    public void testBigDocIssue422() throws Exception
    {
        final XMLInputFactory xmlInputFactory = new WstxInputFactory();
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
        final XmlMapper xmlMapper = XmlMapper.builder(XmlFactory.builder()
                .xmlInputFactory(xmlInputFactory)
                .build()).build();

        final String XML =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<rss xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:content=\"http://purl.org/rss/1.0/modules/content/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:slash=\"http://purl.org/rss/1.0/modules/slash/\" xmlns:sy=\"http://purl.org/rss/1.0/modules/syndication/\" xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\" version=\"2.0\">\n" +
"  <channel>\n" +
"    <title>Ars Technica</title>\n" +
"    <link>https://arstechnica.com</link>\n" +
"    <description>Serving the Technologist for more than a decade. IT news, reviews, and analysis.</description>\n" +
"    <lastBuildDate>Sun, 23 Aug 2020 23:48:25 +0000</lastBuildDate>\n" +
"    <language>en-US</language>\n" +
"    <sy:updatePeriod>hourly</sy:updatePeriod>\n" +
"    <sy:updateFrequency>1</sy:updateFrequency>\n" +
"    <generator>https://wordpress.org/?v=4.9.15</generator>\n" +
"    <image>\n" +
"      <url>https://cdn.arstechnica.net/wp-content/uploads/2016/10/cropped-ars-logo-512_480-32x32.png</url>\n" +
"      <title>Ars Technica</title>\n" +
"      <link>https://arstechnica.com</link>\n" +
"      <width>32</width>\n" +
"      <height>32</height>\n" +
"    </image>\n" +
"    <atom10:link xmlns:atom10=\"http://www.w3.org/2005/Atom\" rel=\"self\" type=\"application/rss+xml\" href=\"http://feeds.arstechnica.com/arstechnica/index\" />\n" +
"    <feedburner:info xmlns:feedburner=\"http://rssnamespace.org/feedburner/ext/1.0\" uri=\"arstechnica/index\" />\n" +
"    <atom10:link xmlns:atom10=\"http://www.w3.org/2005/Atom\" rel=\"hub\" href=\"http://pubsubhubbub.appspot.com/\" />\n" +
"    <item>\n" +
"      <title>Trump announces a COVID-19 Emergency Use Authorization for blood plasma</title>\n" +
"      <link>https://arstechnica.com/?p=1700815</link>\n" +
"      <pubDate>Sun, 23 Aug 2020 23:26:43 +0000</pubDate>\n" +
"      <dc:creator><![CDATA[John Timmer]]></dc:creator>\n" +
"      <category><![CDATA[Policy]]></category>\n" +
"      <category><![CDATA[Science]]></category>\n" +
"      <category><![CDATA[antibodies]]></category>\n" +
"      <category><![CDATA[COVID-19]]></category>\n" +
"      <category><![CDATA[Emergency Use Authorization]]></category>\n" +
"      <category><![CDATA[fda]]></category>\n" +
"      <category><![CDATA[plasma]]></category>\n" +
"      <category><![CDATA[SARS-CoV-2]]></category>\n" +
"      <category><![CDATA[Trump]]></category>\n" +
"      <guid isPermaLink=\"false\">https://arstechnica.com/?p=1700815</guid>\n" +
"      <description><![CDATA[Move reportedly happens over objections of health officials about limited data.]]></description>\n" +
"      <content:encoded><![CDATA[<div id=\"rss-wrap\">\n" +
"<figure class=\"intro-image intro-left\"><img src=\"https://cdn.arstechnica.net/wp-content/uploads/2020/08/GettyImages-1220853125-800x604.jpg\" alt=\"Image of a man gesturing towards another.\"><p class=\"caption\" style=\"font-size:0.8em\"><a href=\"https://cdn.arstechnica.net/wp-content/uploads/2020/08/GettyImages-1220853125.jpg\" class=\"enlarge-link\" data-height=\"773\" data-width=\"1024\">Enlarge</a> <span class=\"sep\">/</span> Donald Trump gestures to Stephen Hahn, head of the FDA, at an earlier press conference. (credit: <a rel=\"nofollow\" class=\"caption-link\" href=\"https://www.gettyimages.com/detail/news-photo/president-donald-trump-and-stephen%C2%A0hahn-director-of-the-news-photo/1220853125?adppopup=true\">Drew Angerer</a>)</p>  </figure><div><a name=\"page-1\"></a></div>\n" +
"<p>Today, President Trump held a news conference to announce that the FDA has granted an Emergency Use Authorization for the treatment of COVID-19 cases using blood plasma from those formerly infected. The move comes despite significant uncertainty regarding just how effective this treatment is, and comes just days after Trump attacked the FDA for delaying its work as part of a plot to sabotage his re-election.</p>\n" +
"<h2>In the blood</h2>\n" + 
"<p>Plasma is the liquid portion of the blood, which (among other things) contains antibodies. It has been used to treat other infections, as some antibodies can be capable of neutralizing the infecting pathogen—binding to the bacteria or virus in a way that prevents it from entering cells. Early studies have indicated that it's relatively common for those who have had a SARS-CoV-2 infection to generate antibodies that can neutralize the virus in lab tests, although the antibody response to the virus is also <a href=\"https://arstechnica.com/science/2020/06/antibody-testing-suggests-immune-response-post-covid-is-very-variable/\">highly variable</a>.</p>\n" +
"<p>In the absence of any effective treatments, people started testing this \"convalescent plasma\" <a href=\"https://arstechnica.com/science/2020/03/hospitals-in-nyc-will-start-testing-therapy-using-plasma-of-those-infected/\">as early as March</a>, and testing has been expanded as the pool of post-infected individuals has continued to grow. But so far, the evidence has been mixed. One of the largest studies, led by researchers at the Mayo Clinic and including over 35,000 patients, <a href=\"https://doi.org/10.1101/2020.08.12.20169359\">did see an effect</a>, but it was a very mild one: mortality dropped from 11.9 percent in people who received plasma four days or more after starting treatment, compared with 8.7 percent if treatment was started earlier than that. But, critically, the study lacked a control group, leaving its authors talking about \"signatures of efficacy,\" rather than actual evidence of efficacy.</p></div><p><a href=\"https://arstechnica.com/?p=1700815#p3\">Read 7 remaining paragraphs</a> | <a href=\"https://arstechnica.com/?p=1700815&comments=1\">Comments</a></p><div class=\"feedflare\">\n" +
"<a href=\"http://feeds.arstechnica.com/~ff/arstechnica/index?a=P614tPda1Eo:cpKHHEXnQ5w:V_sGLiPBpWU\"><img src=\"http://feeds.feedburner.com/~ff/arstechnica/index?i=P614tPda1Eo:cpKHHEXnQ5w:V_sGLiPBpWU\" border=\"0\"></img></a> <a href=\"http://feeds.arstechnica.com/~ff/arstechnica/index?a=P614tPda1Eo:cpKHHEXnQ5w:F7zBnMyn0Lo\"><img src=\"http://feeds.feedburner.com/~ff/arstechnica/index?i=P614tPda1Eo:cpKHHEXnQ5w:F7zBnMyn0Lo\" border=\"0\"></img></a> <a href=\"http://feeds.arstechnica.com/~ff/arstechnica/index?a=P614tPda1Eo:cpKHHEXnQ5w:qj6IDK7rITs\"><img src=\"http://feeds.feedburner.com/~ff/arstechnica/index?d=qj6IDK7rITs\" border=\"0\"></img></a> <a href=\"http://feeds.arstechnica.com/~ff/arstechnica/index?a=P614tPda1Eo:cpKHHEXnQ5w:yIl2AUoC8zA\"><img src=\"http://feeds.feedburner.com/~ff/arstechnica/index?d=yIl2AUoC8zA\" border=\"0\"></img></a>\n" +
"</div>]]></content:encoded>\n" +
"    </item>\n" +
"  </channel>\n" +
"</rss>";

        final RssDocument422 doc = xmlMapper.readerFor(RssDocument422.class)
                .readValue(XML);
        assertNotNull(doc);
        RssChannel channel = doc.channel;
        assertNotNull(channel);
        assertEquals("https://arstechnica.com", channel.siteUrl);
        List<RssItem> items = channel.items;
        assertNotNull(items);
        assertEquals(1, items.size());
        RssItem item = items.get(0);
        assertTrue(item.encodedContent.contains("<h2>In the blood</h2>"));

        // for debugging:
/*        System.err.println("XML:\n"+xmlMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(doc));
                */
    }
}
