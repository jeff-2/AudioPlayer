package com.jmfoste2.audioplayer.search;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.jmfoste2.audioplayer.R;
import com.jmfoste2.audioplayer.model.Playlist;
import com.jmfoste2.audioplayer.model.SearchItem;
import com.jmfoste2.audioplayer.playlist.AddEntryToPlaylistTask;
import com.jmfoste2.audioplayer.playlist.AddPlaylistTask;
import com.jmfoste2.audioplayer.playlist.PlaylistsDbHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * An adapter for holding a collection of SearchItems for usage
 * in a ListView.
 */
public class SearchItemArrayAdapter extends ArrayAdapter<SearchItem> {

    private static final String TAG = "SearchItemArrayAdapter";

    private static final int CREATE_NEW_PLAYLIST = -1;

    private final List<Playlist> playlists;
    private final List<PopupMenu> popupMenus;

    /**
     * ViewHolder holds the data associated with this view.
     */
    private static class ViewHolder {
        public TextView title;
        public ImageView thumbnail;
        public TextView duration;
    }

    /**
     * Instantiates a new search item array adapter with the given context
     * and items.
     *
     * @param context The context
     * @param items The search items to display
     */
    public SearchItemArrayAdapter(Context context, List<SearchItem> items) {
        super(context, R.layout.search_item, items);
        playlists = null;
        popupMenus = null;
    }

    /**
     * Instantiates a new search item array adapter with the given context,
     * items and playlists.
     *
     * @param context The context
     * @param items The search items to display
     * @param playlists The existing playlists
     */
    public SearchItemArrayAdapter(Context context, List<SearchItem> items, List<Playlist> playlists) {
        super(context, R.layout.search_item, items);
        this.playlists = playlists;
        popupMenus = new ArrayList<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        ViewHolder viewHolder;

        // Utilize ViewHolder pattern so that the layout is only inflated when required
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.search_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) rowView.findViewById(R.id.video_title);
            viewHolder.thumbnail = (ImageView) rowView.findViewById(R.id.video_thumbnail);
            viewHolder.duration = (TextView) rowView.findViewById(R.id.video_duration);

            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        // show popup menus for adding to playlists only if existing playlists provided
        if (playlists == null) {
            rowView.findViewById(R.id.playlist).setVisibility(View.GONE);
        } else {
            setupPopupMenu(rowView, position);
        }

        SearchItem searchItem = getItem(position);
        viewHolder.title.setText(searchItem.getTitle());
        Picasso.with(getContext()).load(searchItem.getThumbnailURL()).into(viewHolder.thumbnail);
        viewHolder.duration.setText(searchItem.getFormattedDuration());

        return rowView;
    }

    /**
     * Adds popup menu to add to playlist button so songs can be
     * added to new playlists or existing playlists.
     *
     * @param rowView The view containing the add playlist button.
     * @param position The position of current item the array adapter.
     */
    private void setupPopupMenu(View rowView, int position) {
        final PopupMenu popupMenu = new PopupMenu(getContext(), rowView.findViewById(R.id.playlist));
        popupMenus.add(popupMenu);

        // add options for new playlist, and existing playlists
        popupMenu.getMenu().add(Menu.NONE, CREATE_NEW_PLAYLIST, Menu.NONE, "Create new playlist");
        for (int i = 0; i < playlists.size(); i++) {
            popupMenu.getMenu().add(Menu.NONE, i, Menu.NONE, playlists.get(i).getTitle());
        }

        final SearchItem currentItem = getItem(position);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case CREATE_NEW_PLAYLIST:
                        Log.d(TAG, "Create new playlist launch");
                        showPlaylistCreationDialog(currentItem);
                        break;
                    default:
                        Playlist playlist = playlists.get(item.getItemId());

                        AddEntryToPlaylistTask addEntryToPlaylistTask = new AddEntryToPlaylistTask(new PlaylistsDbHelper(getContext()), playlist);
                        addEntryToPlaylistTask.execute(currentItem);
                        Log.d(TAG, "Selected playlist: " + playlists.get(item.getItemId()).getTitle());
                        break;
                }
                return true;
            }
        });
        rowView.findViewById(R.id.playlist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });
    }

    /**
     * Indicates whether the specified playlist title is already used or not.
     *
     * @param playlistTitle The playlist title to check if it has been used.
     * @return Boolean indicating whether the specified playlist title is used.
     */
    private boolean playlistTitleIsUnique(String playlistTitle) {
        for (Playlist playlist : playlists) {
            if (playlist.getTitle().equals(playlistTitle)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Shows dialog with edit text to user to enter name of
     * playlist to be created. Creates new playlist with given
     * title, and adds the selected song to it. Otherwise if
     * the playlist is not unique, no new playlist is created and
     * a message is given to the user.
     *
     * @param selected The SearchItem representing the song to be added to the new playlist.
     */
    private void showPlaylistCreationDialog(final SearchItem selected) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("Create new playlist");
        alert.setMessage("Enter a title for the new playlist");
        final EditText editText = new EditText(getContext());
        alert.setView(editText);
        alert.setPositiveButton("Create Playlist", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "create playlist with title: " + editText.getText().toString());
                String playlistTitle = editText.getText().toString();
                if (!playlistTitle.isEmpty() && playlistTitleIsUnique(playlistTitle)) {
                    createPlaylist(playlistTitle, selected);
                } else {
                    Toast message = Toast.makeText(getContext(), "Cannot create playlist with title " +
                                    editText.getText() + ". It must be non-empty and different from existing playlists.",
                            Toast.LENGTH_SHORT);
                    message.show();
                }
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }

    /**
     * Creates a new playlist with the specified title and
     * the given song. Updates playlists and menu options with new playlist.
     *
     * @param playlistTitle The title of the new playlist.
     * @param selected SearchItem representing the song to add to the new playlist.
     */
    private void createPlaylist(String playlistTitle, SearchItem selected) {
        List<SearchItem> playlistEntries = new ArrayList<>();
        playlistEntries.add(selected);
        Playlist playlist = new Playlist(playlistTitle, playlistEntries);

        AddPlaylistTask addPlaylistTask = new AddPlaylistTask(new PlaylistsDbHelper(getContext()));
        addPlaylistTask.execute(playlist);

        playlists.add(playlist);
        for (PopupMenu popupMenu : popupMenus) {
            popupMenu.getMenu().add(Menu.NONE, playlists.size() - 1, Menu.NONE, playlistTitle);
        }
    }
}
