package com.jmfoste2.audioplayer.playlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jmfoste2.audioplayer.R;
import com.jmfoste2.audioplayer.model.Playlist;

import java.util.List;

/**
 * Adapter for displaying playlists in a ListView.
 */
public class PlaylistArrayAdapter extends ArrayAdapter<Playlist> {

    /**
     * ViewHolder holds the data associated with this view.
     */
    private static class ViewHolder {
        public TextView title;
        public TextView duration;
        public TextView numEntries;
    }

    /**
     * Creates a PlaylistArrayAdapter with the specified context
     * and items.
     *
     * @param context The context.
     * @param items The list of playlists to display.
     */
    public PlaylistArrayAdapter(Context context, List<Playlist> items) {
        super(context, R.layout.playlist, items);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        ViewHolder viewHolder;

        // Utilize ViewHolder pattern so that the layout is only inflated when required
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.playlist, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) rowView.findViewById(R.id.playlist_title);
            viewHolder.duration = (TextView) rowView.findViewById(R.id.playlist_duration);
            viewHolder.numEntries = (TextView) rowView.findViewById(R.id.playlist_num_entries);

            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        Playlist playlist = getItem(position);
        viewHolder.title.setText(playlist.getTitle());
        viewHolder.duration.setText(playlist.getFormattedTotalDuration());
        int numEntries = playlist.getEntries().size();
        viewHolder.numEntries.setText(numEntries + (numEntries == 1 ? " song" : " songs"));

        return rowView;
    }
}
