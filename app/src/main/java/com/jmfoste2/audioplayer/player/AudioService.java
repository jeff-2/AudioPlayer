package com.jmfoste2.audioplayer.player;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.jmfoste2.audioplayer.model.Playlist;
import com.jmfoste2.audioplayer.model.SearchItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Foreground service for playing music. Exposes interface for controlling
 * the underlying media player.
 */
public class AudioService extends Service implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    public enum State {
        NONE, IDLE, INITIALIZED, PREPARING, PREPARED, STARTED, STOPPED, PAUSED, PLAYBACK_COMPLETED, ERROR, END;
    }


    private static final int NOTIFICATION_ID = 0xDEADBEEF;
    private static final String TAG = "AudioService";

    public static final String PLAYLIST = "playlist";


    private MediaPlayer mediaPlayer;
    private WifiManager.WifiLock wifiLock;

    private Playlist playlist;
    private boolean playWhenPrepared;
    private boolean playWhenAudioFocused;

    private State state = State.NONE;
    private List<OnStateChangedListener> stateChangedListeners = new ArrayList<>();


    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.d(TAG, "Focus change to:" + focusChange);
        switch(focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (playWhenAudioFocused) {
                    play();
                }
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                playWhenAudioFocused = (state == State.STARTED);
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                playWhenAudioFocused = false;
                mediaPlayer.setVolume(0.1f, 0.1f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                playWhenAudioFocused = (state == State.STARTED);
                pause();
                break;
            default:
                playWhenAudioFocused = false;
                Log.d(TAG, "Unhandled focus change:" + focusChange);
                break;
        }
    }

    private void requestAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        Log.d(TAG, "hasAudioFocus:" + (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED));
    }

    private void abandonAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(this);
    }

    /**
     * Initializes media player, or resets it if it already exists.
     */
    private void initializePlayer() {

        boolean isLooping = false;
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        } else {
            isLooping = mediaPlayer.isLooping();
            mediaPlayer.reset();
        }
        state = State.IDLE;
        onStateChanged();
        mediaPlayer.setLooping(isLooping);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mediaPlayer.setDataSource(playlist.current().getAudioURL());
            state = State.INITIALIZED;
            onStateChanged();
            mediaPlayer.prepareAsync();
            state = State.PREPARING;
            onStateChanged();
        } catch (IOException e) {
            Log.d(TAG, "IOException", e);
        }
    }

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (state != State.NONE) {
            return Service.START_STICKY;
        }

        startForeground();
        Log.d(TAG, "Started foreground");

        playlist = intent.getParcelableExtra(PLAYLIST);

        initializePlayer();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(broadcastReceiver, intentFilter);

        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        wifiLock.acquire();
        return Service.START_STICKY;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(TAG, "Error:" + what);
        state = State.ERROR;
        onStateChanged();
        initializePlayer();
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "Prepared");
        state = State.PREPARED;
        onStateChanged();
        if (playWhenPrepared) {
            mediaPlayer.start();
            state = State.STARTED;
            onStateChanged();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "Completed");
        state = State.PLAYBACK_COMPLETED;
        if (mediaPlayer.isLooping()) {
            onStateChanged();
            state = State.STARTED;
            onStateChanged();
        } else if (hasNext()) {
            advanceToNext();
            onStateChanged();
            initializePlayer();
        } else {
            onStateChanged();
        }
    }

    /**
     * Indicates whether there is a next song available.
     *
     * @return Boolean indicating if there is a next song available.
     */
    public boolean hasNext() {
        return playlist.hasNext();
    }

    private void advanceToNext() {
        playWhenPrepared = (state == State.PLAYBACK_COMPLETED) || (state == State.STARTED);
        playlist.next();
    }

    private void fallbackToPrev() {
        playWhenPrepared = state == State.STARTED;
        playlist.prev();
    }

    /**
     * Move on to the next song in the playlist, if one exists.
     */
    public void next() {
        if (playlist.hasNext()) {
            advanceToNext();
            initializePlayer();
        }
    }

    /**
     * Move back to the previous song in the playlist, if one exists.
     */
    public void prev() {
        if (playlist.hasPrev()) {
            fallbackToPrev();
            initializePlayer();
        }
    }

    private void onStateChanged() {
        for (OnStateChangedListener listener : stateChangedListeners) {
            listener.onStateChanged(state);
        }
    }

    /**
     * Starts the current song in the playlist
     */
    public void play() {
        requestAudioFocus();
        mediaPlayer.start();
        state = State.STARTED;
        onStateChanged();
    }

    /**
     * Pauses the current song
     */
    public void pause() {
        mediaPlayer.pause();
        state = State.PAUSED;
        onStateChanged();
    }

    /**
     * Sets the player to looping (e.g. repeat after completion)
     *
     * @param isLooping Whether to set the player to looping or not looping.
     */
    public void setLooping(boolean isLooping) {
        mediaPlayer.setLooping(isLooping);
    }

    /**
     * Indicates whether the playing is looping (e.g. repeat after completion)
     *
     * @return Boolean indicating whether it is looping
     */
    public boolean isLooping() {
        return mediaPlayer.isLooping();
    }

    /**
     * Indicates the current position in the song
     *
     * @return Integer representing current position in the song in milliseoncds.
     */
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * Moves to the specified position in the song in
     * milliseconds.
     *
     * @param msec The position in the song to seek to in milliseconds)
     */
    public void seekTo(int msec) {
        mediaPlayer.seekTo(msec);
    }

    public State getState() {
        return state;
    }

    public SearchItem current() {
        return playlist.current();
    }

    private void startForeground() {
        Notification notification = new Notification();
        notification.tickerText = "Ticker text";
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (wifiLock != null && wifiLock.isHeld()) {
           wifiLock.release();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            state = State.END;
            onStateChanged();
        }
        unregisterReceiver(broadcastReceiver);
        abandonAudioFocus();
        stopForeground(true);
    }

    public boolean isShuffleEnabled() {
        return playlist.isShuffleEnabled();
    }

    public void setShuffleEnabled(boolean shuffleEnabled) {
        playlist.setShuffleEnabled(shuffleEnabled);
    }

    public interface OnStateChangedListener {
        void onStateChanged(State state);
    }

    public void addOnStateChangedListener(OnStateChangedListener listener) {
        stateChangedListeners.add(listener);
    }

    public void removeOnStateChangedListener(OnStateChangedListener listener) {
        stateChangedListeners.remove(listener);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                if (state == State.STARTED || state == State.PLAYBACK_COMPLETED) {
                    Log.d(TAG, "Noisy audio -- pause");
                    pause();
                }
            }
        }
    };
}
