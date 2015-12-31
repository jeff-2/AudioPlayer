package com.jmfoste2.audioplayer.player;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jmfoste2.audioplayer.R;
import com.jmfoste2.audioplayer.favorite.AddFavoriteTask;
import com.jmfoste2.audioplayer.favorite.FavoritesDbHelper;
import com.jmfoste2.audioplayer.favorite.RemoveFavoriteTask;
import com.jmfoste2.audioplayer.model.Playlist;
import com.jmfoste2.audioplayer.model.SearchItem;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A fullscreen activity with controls for playing audio from a
 * particular video indicated by the searchitem which is passed
 * as an intent to this activity.
 */
public class PlayerActivity extends Activity implements AudioService.OnStateChangedListener {

    public static final String PLAYLIST = "playlist";


    private static final String TAG = "PlayerActivity";

    // Used for tracking bind state with AudioService
    private AudioService service;
    private boolean bound;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final Handler handler = new Handler();
    private ScheduledFuture<?> scheduledFuture;

    private Playlist playlist;

    // PlayerControls and dynamically displayed info
    private TextView title;
    private SeekBar seekBar;
    private ImageView pausePlay;
    private TextView startText;
    private TextView endText;
    private ImageView repeat;
    private ImageView favorite;
    private ImageView next;
    private ImageView prev;
    private ImageView shuffle;

    private ProgressDialog progressDialog;

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);

        handleIntent(getIntent());
    }

    /**
     * Handles the intent used to start the activity. Updates
     * the player and its controls and text.
     */
    private void handleIntent(Intent intent) {

        playlist = intent.getParcelableExtra(PLAYLIST);

        startText = (TextView) findViewById(R.id.startText);
        startText.setText(playlist.current().getFormattedStartDuration());

        endText = (TextView) findViewById(R.id.endText);
        endText.setText(playlist.current().getFormattedEndDuration());

        title = (TextView) findViewById(R.id.titleText);
        title.setText(playlist.current().getTitle());

        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(seekBarListener);
        seekBar.setMax(playlist.current().getDurationInSeconds() - 1);

        pausePlay = (ImageView) findViewById(R.id.pause_play);
        pausePlay.setOnClickListener(pausePlayListener);

        repeat = (ImageView) findViewById(R.id.repeat);
        repeat.setOnClickListener(repeatListener);

        favorite = (ImageView) findViewById(R.id.favorite);
        favorite.setOnClickListener(favoriteListener);

        shuffle = (ImageView) findViewById(R.id.shuffle);

        if (playlist.current().isFavorite()) {
            favorite.setImageResource(R.drawable.ic_favorite_selected_light);
        } else {
            favorite.setImageResource(R.drawable.ic_favorite_light);
        }

        next = (ImageView) findViewById(R.id.next);
        if (playlist.hasNext()) {
            next.setOnClickListener(nextListener);
        } else {
            next.setVisibility(View.GONE);
        }

        prev = (ImageView) findViewById(R.id.prev);
        if (playlist.hasPrev()) {
            prev.setOnClickListener(prevListener);
        } else {
            prev.setVisibility(View.GONE);
        }

        if (playlist.hasNext() || playlist.hasPrev()) {
            shuffle.setOnClickListener(shuffleListener);
        } else {
            shuffle.setVisibility(View.GONE);
        }
    }

    private void bindAndStartService() {
        Intent service = new Intent(getApplicationContext(), AudioService.class);
        service.putExtra(AudioService.PLAYLIST, playlist);
        startService(service);
        bindService(service, connection, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop our AudioService when this activity is destroyed
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        progressDialog.dismiss();
        Intent service = new Intent(getApplicationContext(), AudioService.class);
        stopService(service);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if (bound) {
            unbind();
        }
        progressDialog.hide();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        progressDialog.show();
        bindAndStartService();
    }

    private void setUnbound() {
        bound = false;
        service.removeOnStateChangedListener(this);
        service = null;
    }

    private void unbind() {
        setUnbound();
        stopSeekBarUpdates();
        unbindService(connection);
    }

    /**
     * Resets player position to beginning of song
     */
    private void setToBeginning() {
        stopSeekBarUpdates();
        pausePlay.setImageResource(R.drawable.ic_play_light);
        seekBar.setProgress(0);
    }

    @Override
    public void onStateChanged(AudioService.State state) {
        Log.d(TAG, "State:" + state);
        switch (state) {
            case PREPARED:
                progressDialog.hide();
                break;
            case ERROR:
                // TODO: nicer handling
                break;
            case PLAYBACK_COMPLETED:
                if (service.isLooping()) {
                    updatePlayerState();
                } else if (service.hasNext()) {
                    updateCurrentSong();
                } else {
                    setToBeginning();
                }
                break;
            case STARTED:
                startSeekBarUpdates();
                pausePlay.setImageResource(R.drawable.ic_pause_light);
                break;
            case PAUSED:
                stopSeekBarUpdates();
                pausePlay.setImageResource(R.drawable.ic_play_light);
                break;
            default:
                break;
        }
    }

    /**
     * Update the display of the player. Show current song info,
     * position, and relevant player controls in proper orientations.
     */
    private void updatePlayerState() {
        SearchItem current = service.current();
        String endDuration = current.getFormattedEndDuration();
        startText.setText(SearchItem.getFormattedDuration(service.getCurrentPosition() / 1000, endDuration.length()));
        endText.setText(endDuration);
        title.setText(current.getTitle());
        seekBar.setMax(current.getDurationInSeconds() - 1);

        if (service.getState() == AudioService.State.STARTED) {
            pausePlay.setImageResource(R.drawable.ic_pause_light);
        } else {
            pausePlay.setImageResource(R.drawable.ic_play_light);
        }

        if (service.current().isFavorite()) {
            favorite.setImageResource(R.drawable.ic_favorite_selected_light);
        } else {
            favorite.setImageResource(R.drawable.ic_favorite_light);
        }

        if (service.isLooping()) {
            repeat.setImageResource(R.drawable.ic_repeat_selected_light);
        } else {
            repeat.setImageResource(R.drawable.ic_repeat_light);
        }

        if (service.getState() == AudioService.State.PLAYBACK_COMPLETED) {
            seekBar.setProgress(0);
        } else {
            seekBar.setProgress(service.getCurrentPosition() / 1000);
        }

        if (service.isShuffleEnabled()) {
            shuffle.setImageResource(R.drawable.ic_shuffle_selected_light);
        } else {
            shuffle.setImageResource(R.drawable.ic_shuffle_light);
        }
    }

    private final ServiceConnection connection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder svc) {
            Log.d(TAG, "BOUND");
            AudioService.LocalBinder binder = (AudioService.LocalBinder) svc;
            bound = true;
            service = binder.getService();
            service.addOnStateChangedListener(PlayerActivity.this);

            switch (service.getState()) {
                case PREPARED:
                case PAUSED:
                case PLAYBACK_COMPLETED:
                    updatePlayerState();
                    progressDialog.hide();
                    break;
                case STARTED:
                    updatePlayerState();
                    startSeekBarUpdates();
                    progressDialog.hide();
                    break;
                default:
                    break;
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "UNBOUND");
            setUnbound();
        }
    };

    private void stopSeekBarUpdates() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }

    private void startSeekBarUpdates() {
        stopSeekBarUpdates();
        if (!executorService.isShutdown()) {
            scheduledFuture = executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (bound) {
                                seekBar.setProgress(service.getCurrentPosition()/1000);
                            }
                        }
                    });
                }
            }, 100, 1000, TimeUnit.MILLISECONDS);
        }
    }

    private final SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            startText.setText(SearchItem.getFormattedDuration(progress, endText.getText().length()));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            stopSeekBarUpdates();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (bound) {
                service.seekTo(seekBar.getProgress() * 1000);
                if (service.getState() == AudioService.State.STARTED) {
                    startSeekBarUpdates();
                }
            }
        }
    };

    private void updateCurrentSong() {
        stopSeekBarUpdates();
        updatePlayerState();
        progressDialog.show();
    }

    private final View.OnClickListener nextListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (bound) {
                service.next();
                updateCurrentSong();
            }
        }
    };

    private final View.OnClickListener prevListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (bound) {
                service.prev();
                updateCurrentSong();
            }
        }
    };

    private final View.OnClickListener favoriteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (bound) {
                SearchItem current = service.current();
                current.setFavorite(!current.isFavorite());
                if (current.isFavorite()) {
                    AddFavoriteTask addFavoriteTask = new AddFavoriteTask(new FavoritesDbHelper(PlayerActivity.this));
                    addFavoriteTask.execute(current);
                    favorite.setImageResource(R.drawable.ic_favorite_selected_light);
                } else {
                    RemoveFavoriteTask removeFavoriteTask = new RemoveFavoriteTask(new FavoritesDbHelper(PlayerActivity.this));
                    removeFavoriteTask.execute(current);
                    favorite.setImageResource(R.drawable.ic_favorite_light);
                }
            }
        }
    };

    private final View.OnClickListener repeatListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (bound) {
                Log.d(TAG, "Toggled repeat");
                boolean isLooping = !service.isLooping();
                if (isLooping) {
                    repeat.setImageResource(R.drawable.ic_repeat_selected_light);
                } else {
                    repeat.setImageResource(R.drawable.ic_repeat_light);
                }
                service.setLooping(isLooping);
            }
        }
    };

    private final View.OnClickListener pausePlayListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (bound) {
                Log.d(TAG, "Pressed pause/play in state:" + service.getState());
                switch(service.getState()) {
                    case PLAYBACK_COMPLETED:
                    case PREPARED:
                    case PAUSED:
                        pausePlay.setImageResource(R.drawable.ic_pause_light);
                        service.play();
                        break;
                    case STARTED:
                        pausePlay.setImageResource(R.drawable.ic_play_light);
                        service.pause();
                        break;
                    default:
                        break;
                }
            }
        }
    };

    private final View.OnClickListener shuffleListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (bound) {
                Log.d(TAG, "Toggled shuffle");
                boolean shuffleEnabled = !service.isShuffleEnabled();
                service.setShuffleEnabled(shuffleEnabled);
                if (shuffleEnabled) {
                    shuffle.setImageResource(R.drawable.ic_shuffle_selected_light);
                } else {
                    shuffle.setImageResource(R.drawable.ic_shuffle_light);
                }
            }
        }
    };
}
