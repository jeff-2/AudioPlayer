package com.jmfoste2.audioplayer.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

/**
 * Contains the data associated with the results of a search
 * for videos on youtube. The data stored is the id, title,
 * description, url for the thumbnail, and duration of the video.
 */
public class SearchItem implements Parcelable {

    private static final PeriodFormatter RAW_DURATION_FORMAT = ISOPeriodFormat.standard();
    private static final String BASE_AUDIO_URL = "https://murmuring-stream-1197.herokuapp.com/audio/";
    //"http://youtubeinmp3.com/fetch/?video=http://www.youtube.com/watch?v=";

    private final String videoId;
    private final String title;
    private final String description;
    private final String thumbnailURL;
    private final String duration;
    private boolean isFavorite;

    /**
     * Constructs a SearchItem with the specified id, title, description and thumbnail url.
     *
     * @param videoId The id of the video
     * @param title The title of the video
     * @param description The description of the video
     * @param thumbnailURL The url of the thumbnail for the video
     * @param duration The duration of the video
     */
    public SearchItem(String videoId, String title, String description, String thumbnailURL, String duration) {
        this.videoId = videoId;
        this.title = title;
        this.description = description;
        this.thumbnailURL = thumbnailURL;
        this.duration = duration;
        isFavorite = false;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public String getDuration() {
        return duration;
    }

    public String getAudioURL() {
        return BASE_AUDIO_URL + videoId;
    }

    public int getDurationInSeconds() {
        Period period = RAW_DURATION_FORMAT.parsePeriod(duration);
        return period.toStandardSeconds().getSeconds();
    }

    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    /**
     * Prints the field into the specified string builder followed by the
     * provided separator. If include zeros is set to true, then
     * fields containing zeros will be printed, and fields less
     * than 10 will have a leading zero before them.
     *
     * @param sb The string builder to print the field and separator into.
     * @param field The field to write into the string builder.
     * @param separator The separator to write after the field.
     * @param includeZeros Whether to print zeros into the string builder.
     * @return Boolean indicating whether the field was printed.
     */
    private static boolean print(StringBuilder sb, int field, String separator, boolean includeZeros) {
        if (includeZeros || field > 0) {
            if (includeZeros && field < 10) {
                sb.append('0');
            }
            sb.append(field);
            sb.append(separator);
            return true;
        }
        return false;
    }

    /**
     * Returns a formatted duration for the start of the video.
     * It will be the same length as the formatted duration of the video.
     * For example if the formatted duration is 15:27, then the
     * formatted start duration will be 00:00.
     *
     * @return The formatted start duration.
     */
    public String getFormattedStartDuration() {
        String formattedDuration = getFormattedDuration();
        return formattedDuration.replaceAll("\\d", "0");
    }

    /**
     * Returns the formatted duration for a period.
     * E.g. Period for PT9M15S will be formatted as 9:15
     *
     * @param period The period to get the formatted duration for.
     * @return The formatted duration for the period.
     */
    private static StringBuilder getFormattedDuration(Period period) {
        StringBuilder sb = new StringBuilder();
        boolean includeZeros = print(sb, period.getDays(), ":", false);
        includeZeros = print(sb, period.getHours(), ":", includeZeros);
        includeZeros = print(sb, period.getMinutes(), ":", includeZeros);
        print(sb, period.getSeconds(), "", includeZeros);

        return sb;
    }

    /**
     * Returns the formatted duration of the video.
     * For example if the duration is PT10M5S, then
     * the formatted duration will be 10:05.
     *
     * @return The formatted duration of the video.
     */
    public String getFormattedDuration() {
        Period period = RAW_DURATION_FORMAT.parsePeriod(duration);
        return getFormattedDuration(period).toString();
    }

    /**
     * Returns a formatted duration for the end of the video.
     * For example if the duration is 15s, then the
     * end duration will be 0:14.
     *
     * @return The formatted end duration of the video.
     */
    public String getFormattedEndDuration() {
        Period period = RAW_DURATION_FORMAT.parsePeriod(duration);
        // e.g. a duration 15 s will at at 0:14
        period = period.minusSeconds(1);
        return getFormattedDuration(period).toString();
    }

    /**
     * Retrieves the formatted duration for the specified number of
     * seconds, and pads it to be of the specified length.
     * For example if the duration in seconds is 3605, the
     * formatted string will be 1:00:05.
     *
     * @param seconds The number of seconds of the duration.
     * @param expectedLength The length of the formatted string to be returned.
     * @return The formatted duration of the specified seconds with specified length.
     */
    public static String getFormattedDuration(int seconds, int expectedLength) {
        Period period = Seconds.seconds(seconds).toPeriod().normalizedStandard();
        StringBuilder sb = getFormattedDuration(period);

        // we must pad this string to match expected length
        if (sb.length() < expectedLength) {
            sb = sb.reverse();
            int currentLength = sb.length();
            while (currentLength < expectedLength) {
                if ((currentLength + 1) % 3 == 0) {
                    sb.append(':');
                } else {
                    sb.append('0');
                }
                currentLength++;
            }
            sb = sb.reverse();
        }

        return sb.toString();
    }

    /**
     * Returns the formatted duration as a string representing
     * the provided number of seconds. E.g. 15 seconds -> 0:14
     *
     * @param seconds The number of seconds of the duration.
     * @return Formatted duration representing the number of seconds provided.
     */
    public static String getFormattedDuration(int seconds) {
        Period period = Seconds.seconds(seconds).toPeriod().normalizedStandard();
        StringBuilder sb = getFormattedDuration(period);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchItem that = (SearchItem) o;

        if (!videoId.equals(that.videoId)) return false;
        if (!title.equals(that.title)) return false;
        if (!description.equals(that.description)) return false;
        if (!duration.equals(that.duration)) return false;
        if (isFavorite != that.isFavorite) return false;
        return thumbnailURL.equals(that.thumbnailURL);
    }

    @Override
    public int hashCode() {
        int result = videoId.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + thumbnailURL.hashCode();
        result = 31 * result + duration.hashCode();
        result = 31 * result + (isFavorite ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SearchItem{" +
                "videoId='" + videoId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", thumbnailURL='" + thumbnailURL + '\'' +
                ", duration='" + duration + '\'' +
                ", isFavorite='" + isFavorite + '\'' +
                '}';
    }

    /**
     * Creator used to instantiate SearchItem instances
     * from parcels.
     */
    public static final Parcelable.Creator<SearchItem> CREATOR
            = new Parcelable.Creator<SearchItem>() {
        public SearchItem createFromParcel(Parcel in) {
            return new SearchItem(in);
        }

        public SearchItem[] newArray(int size) {
            return new SearchItem[size];
        }
    };

    /**
     * Creates a SearchItem from the given parcel.
     *
     * @param in The parcel containing the SearchItem fields.
     */
    private SearchItem(Parcel in) {
        videoId = in.readString();
        title = in.readString();
        description = in.readString();
        thumbnailURL = in.readString();
        duration = in.readString();
        isFavorite = in.readInt() == 1 ? true : false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(videoId);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(thumbnailURL);
        dest.writeString(duration);
        dest.writeInt(isFavorite ? 1 : 0);
    }
}
