package com.jmfoste2.audioplayer.search;

import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchResultSnippet;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.jmfoste2.audioplayer.model.SearchItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A request for searching for videos which match a
 * query on YouTube.
 */
public class YouTubeSearchRequest {

    private static final String TAG = "YouTubeSearchRequest";

    private static final String API_KEY = "AIzaSyBdwpm-I0cryygudizz_xFxJF8uXYDEy_k";
    private static final long DEFAULT_MAX_NUM_VIDEOS = 10;

    private static YouTube YOUTUBE;

    /**
     * Searches for videos which match the specified query
     * and as search item results.
     *
     * @param query The query indicating videos to search for (must be non-empty).
     * @return The search items corresponding to the search query.
     * @throws IOException
     */
    public static List<SearchItem> getSearchResults(String query) throws IOException {
        return getSearchResults(query, DEFAULT_MAX_NUM_VIDEOS);
    }

    /**
     * Searches for videos which match the specified query
     * and return up to maxNumResults as search item results.
     *
     * @param query The query indicating videos to search for (must be non-empty).
     * @param maxNumResults The maximum number of results that should be returned.
     * @return The search items corresponding to the search query.
     * @throws IOException
     */
    public static List<SearchItem> getSearchResults(String query, long maxNumResults) throws IOException {
        if (YOUTUBE == null) {
            YOUTUBE = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {}
            }).setApplicationName("app-name").build();
        }

        // build search request for videos with the specified data being returned
        YouTube.Search.List search = YOUTUBE.search().list("id,snippet");
        search.setKey(API_KEY);
        search.setQ(query);
        search.setType("video");
        search.setFields("items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url,snippet/thumbnails/high/url)");
        search.setMaxResults(maxNumResults);

        SearchListResponse searchResponse = search.execute();
        List<SearchResult> searchResults = searchResponse.getItems();

        List<SearchItem> searchItems = extractSearchItems(searchResults);

        Log.d(TAG, searchResults.toString());
        Log.d(TAG, searchItems.toString());

        return searchItems;
    }

    /**
     * Extracts the relevant fields from the searchResults to create SearchItems.
     *
     * @param searchResults The results of the youtube search for videos.
     * @return The list of SearchItems extracted from the searchResults.
     * @throws IOException
     */
    private static List<SearchItem> extractSearchItems(List<SearchResult> searchResults) throws IOException {
        List<SearchItem> searchItems = new ArrayList<>();

        if (searchResults != null) {

            List<Video> videoResults = fetchVideoDurations(searchResults);

            // convert search results to search items
            for (int i = 0; i < searchResults.size(); i++) {

                SearchResult searchResult = searchResults.get(i);
                SearchResultSnippet snippet = searchResult.getSnippet();
                String id = searchResult.getId().getVideoId();
                String title = snippet.getTitle();
                String description = snippet.getDescription();
                String defaultThumbnailURL = snippet.getThumbnails().getDefault().getUrl();
                String highResThumbnailURL = snippet.getThumbnails().getHigh().getUrl();
                String duration = videoResults.get(i).getContentDetails().getDuration();

                searchItems.add(new SearchItem(id, title, description, defaultThumbnailURL, highResThumbnailURL, duration));
            }
        }
        return searchItems;
    }

    /**
     * Fetches the video durations for all the video ids in the list
     * of search results.
     *
     * @param searchResults The search results containing the video ids to
     * retrieve the durations for.
     * @return The videos containing the video durations for all the video ids
     * in the list of search results.
     * @throws IOException
     */
    private static List<Video> fetchVideoDurations(List<SearchResult> searchResults) throws IOException {

        // build comma separated string of video ids
        StringBuilder sb = new StringBuilder();
        Iterator<SearchResult> resultIterator = searchResults.iterator();
        while (resultIterator.hasNext()) {
            SearchResult result = resultIterator.next();
            sb.append(result.getId().getVideoId());
            if (resultIterator.hasNext()) {
                sb.append(',');
            }
        }
        String idList = sb.toString();

        // get durations for video ids
        YouTube.Videos.List detailsSearch = YOUTUBE.videos().list("id,contentDetails");
        detailsSearch.setKey(API_KEY);
        detailsSearch.setId(idList);
        detailsSearch.setFields("items(id,contentDetails/duration)");

        VideoListResponse videoResponse = detailsSearch.execute();
        return videoResponse.getItems();
    }
}
