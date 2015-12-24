package com.jmfoste2.audioplayer.model;

import junit.framework.TestCase;

public class SearchItemTest extends TestCase {

    private SearchItem searchItem;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        searchItem = new SearchItem("videoID", "videoTitle", "videoDescription", "videoThumbnailURL", "PT24H23M7S");
    }

    public void testGetDurationInSeconds() {
        int expectedSeconds = 87787;
        int seconds = searchItem.getDurationInSeconds();
        assertEquals(expectedSeconds, seconds);
    }

    public void testGetFormattedStartDuration() {
        String expectedFormattedStartDuration = "00:00:00";
        String formattedStartDuration = searchItem.getFormattedStartDuration();
        assertEquals(expectedFormattedStartDuration, formattedStartDuration);
    }

    public void testGetFormattedDuration() {
        String expectedFormattedDuration = "24:23:07";
        String formattedDuration = searchItem.getFormattedDuration();
        assertEquals(expectedFormattedDuration, formattedDuration);
    }

    public void testGetFormattedEndDuration() {
        String expectedFormattedEndDuration = "24:23:06";
        String formattedEndDuration = searchItem.getFormattedEndDuration();
        assertEquals(expectedFormattedEndDuration, formattedEndDuration);
    }

    public void testGetFormattedDurationFromSeconds() {
        int seconds = 23461;
        String expectedFormattedDuration = "06:31:01";
        String formattedDuration = SearchItem.getFormattedDuration(seconds, searchItem.getFormattedDuration().length());
        assertEquals(expectedFormattedDuration, formattedDuration);
    }
}
