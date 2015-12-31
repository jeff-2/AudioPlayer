package com.jmfoste2.audioplayer.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Used to represent a playlist of search items. Contains a unique
 * title, and a list of entries.
 */
public class Playlist implements Parcelable {

    private final String title;
    private final List<SearchItem> entries;
    private int currentEntryPosition;
    private boolean shuffleEnabled;

    /**
     * Constructs a playlist with the specified title and entries.
     *
     * @param title The title of the playlist
     * @param entries The entries in the playlist.
     */
    public Playlist(String title, List<SearchItem> entries) {
        this.title = title;
        this.entries = entries;
        currentEntryPosition = 0;
        shuffleEnabled = false;
    }

    public String getTitle() {
        return title;
    }

    public List<SearchItem> getEntries() {
        return entries;
    }

    public boolean isShuffleEnabled() {
        return shuffleEnabled;
    }

    public void setShuffleEnabled(boolean shuffleEnabled) {
        SearchItem currentItem = entries.get(currentEntryPosition);
        if (shuffleEnabled) {
            Collections.shuffle(entries, new Random(System.currentTimeMillis()));
        } else {
            Collections.sort(entries);
        }
        for (int i = 0; i < entries.size(); i++) {
            if (currentItem.equals(entries.get(i))) {
                currentEntryPosition = i;
                break;
            }
        }
        this.shuffleEnabled = shuffleEnabled;
    }

    /**
     * Returns the total duration of all the songs in the playlist
     * formatted as a string e.g. 12:03 for 3 songs.
     *
     * @return String representing the total formatted duration of the playlist.
     */
    public String getFormattedTotalDuration() {
        int totalDurationSeconds = 0;
        for (SearchItem entry : entries) {
            totalDurationSeconds += entry.getDurationInSeconds();
        }
        return SearchItem.getFormattedDuration(totalDurationSeconds);
    }

    /**
     * Indicates whether the playlist has a previous song. (Loops back around
     * to retrieve previous song if at beginning of playlist)
     *
     * @return Boolean indicating whether the playlist has a previous song.
     */
    public boolean hasPrev() {
        return entries.size() > 1;
    }

    /**
     * Indicates whether the playlist has a next song. (Loops around to retrieve
     * next song if at end of playlist)
     *
     * @return Boolean indicating whether the playlist has a next song.
     */
    public boolean hasNext() {
        return entries.size() > 1;
    }

    /**
     * Retrieves the previous song in the playlist. (Loops back around to
     * retrieve the previous song if at beginning of playlist)
     *
     * @return SearchItem representing the previous song, or null if there is none.
     */
    public SearchItem prev() {
        if (hasPrev()) {
            currentEntryPosition = (currentEntryPosition + entries.size() - 1) % entries.size();
            return entries.get(currentEntryPosition);
        }
        return null;
    }

    /**
     * Retrieves the next song in the playlist. (Loops back around to
     * retrieve the next song if at the end of playlist)
     *
     * @return SearchItem representing the next song, or null if there is none.
     */
    public SearchItem next() {
        if (hasNext()) {
            currentEntryPosition = (currentEntryPosition + 1) % entries.size();
            return entries.get(currentEntryPosition);
        }
        return null;
    }

    /**
     * Moves the current song in the playlist to the specified position
     * if it is a valid position in the playlist.
     *
     * @param position Position to move the current song in playlist to.
     */
    public void setCurrentEntryPosition(int position) {
        if (position >= 0 && position < entries.size()) {
            this.currentEntryPosition = position;
        }
    }

    /**
     * Retrieves the current song in the playlist.
     *
     * @return SearchItem representing the current song, or null if there is none.
     */
    public SearchItem current() {
        if (entries.size() > 0) {
            return entries.get(currentEntryPosition);
        }
        return null;
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "title='" + title + '\'' +
                ", entries=" + entries +
                ", currentEntryPosition=" + currentEntryPosition +
                ", shuffleEnabled=" + shuffleEnabled +
                '}';
    }

    /**
     * Creator used to instantiate Playlist instances
     * from parcels.
     */
    public static final Parcelable.Creator<Playlist> CREATOR
            = new Parcelable.Creator<Playlist>() {
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };

    /**
     * Creates a Playlist from the given parcel.
     *
     * @param in The parcel containing the Playlist fields.
     */
    private Playlist(Parcel in) {
        title = in.readString();
        entries = new ArrayList<>();
        in.readTypedList(entries, SearchItem.CREATOR);
        currentEntryPosition = in.readInt();
        shuffleEnabled = in.readInt() == 1 ? true : false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeTypedList(entries);
        dest.writeInt(currentEntryPosition);
        dest.writeInt(shuffleEnabled ? 1 : 0);
    }
}
