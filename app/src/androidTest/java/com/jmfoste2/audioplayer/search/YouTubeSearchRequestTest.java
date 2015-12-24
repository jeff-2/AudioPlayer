package com.jmfoste2.audioplayer.search;

import com.jmfoste2.audioplayer.model.SearchItem;

import junit.framework.TestCase;

import java.util.List;

public class YouTubeSearchRequestTest extends TestCase {

    public void testGetSearchResults() throws Exception {
        List<SearchItem> results = YouTubeSearchRequest.getSearchResults("the beatles", 2);
        assertEquals(results.size(), 2);
        for (SearchItem result : results) {
            checkSearchItem(result, "the beatles");
        }
    }

    public void testGetSearchResultsSingleQuery() throws Exception {
        List<SearchItem> results = YouTubeSearchRequest.getSearchResults("aerosmith", 1);
        assertEquals(1, results.size());
        checkSearchItem(results.get(0), "aerosmith");
    }

    private void checkSearchItem(SearchItem item, String query) {
        assertTrue(item.getTitle().toLowerCase().contains(query));
        assertFalse(item.getDuration().isEmpty());
        assertFalse(item.getDescription().isEmpty());
        assertFalse(item.getDuration().isEmpty());
        assertFalse(item.getVideoId().isEmpty());
    }
}
