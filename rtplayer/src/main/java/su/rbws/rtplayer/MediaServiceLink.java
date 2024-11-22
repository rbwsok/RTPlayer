package su.rbws.rtplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import su.rbws.rtplayer.service.RTMediaService;
import su.rbws.rtplayer.service.soundplayer.SoundList;

// класс, осуществляющий подключение к сервису. весь служебный код находится здесь
// также - этот класс - прослойка для общения с сервисом.
public class MediaServiceLink {
    // callback
    public interface IMediaServiceLinkInterface {
        void onServiceConnect();
        void onPlaybackStateChanged(PlaybackStateCompat state);
    }

    private final IMediaServiceLinkInterface callback;

    public final android.content.Context context;

    // конструктор
    public MediaServiceLink(android.content.Context context, MediaServiceLink.IMediaServiceLinkInterface callback) {
        this.context = context;
        this.callback = callback;
    }

    // вызывается в onStart
    public void bindService() {
        Intent intent = new Intent(context, RTMediaService.class);
        context.bindService(intent, mediaServiceConnection, Context.BIND_AUTO_CREATE);
    }

    // вызвается в onDestroy
    public void unbindService() {
        if (mediaServiceBound) {
            context.unbindService(mediaServiceConnection);
            mediaServiceBound = false;
        }
    }

    private RTMediaService.MediaServiceBinder binder;

    private final MediaControllerCompat.Callback mediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            if (state == null)
                return;

            callback.onPlaybackStateChanged(state);
        }
    };

    private final ServiceConnection mediaServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mediaServiceBound = false;
            binder = null;
            if (mediaController != null) {
                mediaController.unregisterCallback(mediaControllerCallback);
                mediaController = null;
            }
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (RTMediaService.MediaServiceBinder) service;
            mediaService = binder.getService();
            mediaServiceBound = true;

            try {
                mediaController = new MediaControllerCompat(context, binder.getMediaSessionToken());
                mediaController.registerCallback(mediaControllerCallback);

                callback.onServiceConnect();
            }
            catch (RemoteException e) {
                mediaController = null;
            }
        }
    };

    // сервис для проигрывания музыки
    private RTMediaService mediaService;
    // признак связанности сервиса с активностью
    private boolean mediaServiceBound = false;

    // готовность сервиса
    public boolean isMediaServiceReady() {
        return (mediaServiceBound) & (mediaService != null);
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    MediaControllerCompat mediaController;

    public String getCurrentPlayedSound() {
        String result = "";
        if (isMediaServiceReady())
            result = SoundList.currentPlayedSound;

        return result;
    }

    public void setCurrentPlayedSound(String filename) {
        if (isMediaServiceReady())
            SoundList.currentPlayedSound = filename;
    }

    public void setPosition(long position) {
        if (mediaController != null)
            mediaController.getTransportControls().seekTo(position);
    }

    public long getPosition() {
        long result = 0;
        if (isMediaServiceReady())
            result = mediaService.soundPlayer_GetPosition();
        return result;
    }

    public long getDuration() {
        long result = 0;
        if (isMediaServiceReady())
            result = mediaService.soundPlayer_GetDuration();
        return result;
    }

    public void prevSound() {
        if (isMediaServiceReady()) {
            if (mediaController != null)
                mediaController.getTransportControls().skipToPrevious();
        }
    }

    public void nextSound() {
        if (isMediaServiceReady()) {
            if (mediaController != null)
                mediaController.getTransportControls().skipToNext();
        }
    }

    public void stop() {
        if (isMediaServiceReady()) {
            if (mediaController != null)
                mediaController.getTransportControls().stop();
        }
    }

    public void play(String filename) {
        if (isMediaServiceReady()) {
            setCurrentPlayedSound(filename);
            if (mediaController != null)
                mediaController.getTransportControls().play();
        }
    }

    public void play() {
        if (isMediaServiceReady()) {
            if (mediaController != null)
                mediaController.getTransportControls().play();
        }
    }

    public void pause() {
        if (isMediaServiceReady()) {
            if (mediaController != null)
                mediaController.getTransportControls().pause();
        }
    }

    public int getState() {
        int result = PlaybackStateCompat.STATE_NONE;
        if (isMediaServiceReady()) {
            if (mediaController != null) {
                PlaybackStateCompat playbackState = mediaController.getPlaybackState();
                if (playbackState != null)
                    result = playbackState.getState();
            }
        }

        return result;
    }

    public void resume() {
        if (isMediaServiceReady())
            mediaService.soundPlayer_Resume();
    }

    public void volume(long value) {
        if (isMediaServiceReady())
            mediaService.soundPlayer_SetVolume(value);
    }

    public void getMediaButtonsMapper() {
        if (isMediaServiceReady()) {
            RTApplication.mediaButtonsMapper = mediaService.mediaButtonsMapper;
        }
    }
}