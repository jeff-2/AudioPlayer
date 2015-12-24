package com.jmfoste2.audioplayer.suggestion;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

public class SearchSuggestionRequestTest extends TestCase {

    private String URL = "http://web.engr.illinois.edu/~jmfoste2/";

    public void testGetHTTPResponse() throws Exception {
        String expectedText = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">\n" +
                "<html>\n" +
                " <head>\n" +
                "  <title>Index of /~jmfoste2</title>\n" +
                " </head>\n" +
                " <body>\n" +
                "<h1>Index of /~jmfoste2</h1>\n" +
                "<ul><li><a href=\"/\"> Parent Directory</a></li>\n" +
                "<li><a href=\"cgi-bin/\"> cgi-bin/</a></li>\n" +
                "<li><a href=\"date.php\"> date.php</a></li>\n" +
                "<li><a href=\"log.php\"> log.php</a></li>\n" +
                "<li><a href=\"log.txt\"> log.txt</a></li>\n" +
                "<li><a href=\"update_db.php\"> update_db.php</a></li>\n" +
                "</ul>\n" +
                "<address>Apache/2.2.29 (Unix) mod_ssl/2.2.29 OpenSSL/1.0.1e-fips mod_bwlimited/1.4 Server at web.engr.illinois.edu Port 80</address>\n" +
                "</body></html>\n";
        String responseText = SearchSuggestionRequest.getHTTPResponse(URL);
        assertEquals(expectedText, responseText);
    }

    public void testGetSuggestions() throws Exception {
        List<String> expectedSuggestions = Arrays.asList("the beatles", "the beatles hey jude",
                "the beatles here comes the sun", "the beatles let it be",
                "the beatles i want to hold your hand", "the beatles in my life",
                "the beatles yesterday", "the beatles twist and shout",
                "the beatles help", "the beatles full album");
        List<String> suggestions = SearchSuggestionRequest.getSuggestions("the beatles");
        assertEquals(expectedSuggestions, suggestions);
    }

    public void testGetSuggestionsSingleQuery() throws Exception {
        List<String> expectedSuggestions = Arrays.asList("aerosmith",
                "aerosmith i don't want miss a thing", "aerosmith dream on",
                "aerosmith sweet emotion", "aerosmith walk this way",
                "aerosmith crazy", "aerosmith angel", "aerosmith cryin",
                "aerosmith pink", "aerosmith amazing");
        List<String> suggestions = SearchSuggestionRequest.getSuggestions("aerosmith");
        assertEquals(expectedSuggestions, suggestions);
    }
}
