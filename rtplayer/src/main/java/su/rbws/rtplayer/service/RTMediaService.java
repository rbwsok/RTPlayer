package su.rbws.rtplayer.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.Binder;
import android.support.v4.media.session.MediaSessionCompat;

import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.media.session.MediaButtonReceiver;

import su.rbws.rtplayer.MainActivity;
import su.rbws.rtplayer.RTApplication;
import su.rbws.rtplayer.service.soundplayer.SoundList;
import su.rbws.rtplayer.service.soundplayer.SoundPlayer;

public class RTMediaService extends Service {
    private MediaSessionCompat mediaSession;

    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    boolean audioFocusRequested = false;

    final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
            .setActions(
                            PlaybackStateCompat.ACTION_PLAY
                            | PlaybackStateCompat.ACTION_STOP
                            | PlaybackStateCompat.ACTION_PAUSE
                            | PlaybackStateCompat.ACTION_PLAY_PAUSE
                            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                            | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);


    public SoundPlayer soundPlayer;

    public MediaButtonsMapper mediaButtonsMapper = new MediaButtonsMapper();

    public IBinder mediaServiceBinder = new MediaServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mediaServiceBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .setAcceptsDelayedFocusGain(true)
                .setWillPauseWhenDucked(true)
                .setAudioAttributes(audioAttributes)
                .build();

        mediaSession = new MediaSessionCompat(this, "PlayerService");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(mediaSessionCallback);

        Context appContext = getApplicationContext();

        Intent activityIntent = new Intent(appContext, MainActivity.class);
        mediaSession.setSessionActivity(PendingIntent.getActivity(appContext, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null, appContext, MediaButtonReceiver.class);
        mediaSession.setMediaButtonReceiver(PendingIntent.getBroadcast(appContext, 0, mediaButtonIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));

        if (soundPlayer == null)
            soundPlayer = new SoundPlayer(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        soundPlayer.sendCommand(SoundPlayer.SoundPlayerCommand.spcStop);
        mediaSession.release();
        super.onDestroy();
    }

    public void mediaServiceNext() {
        soundPlayer_NextSound();
        mediaSession.setPlaybackState(
                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
    }

    private final AudioManager.OnAudioFocusChangeListener audioFocusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    //Log.i("rtplayer_tag", "onAudioFocusChange " + Integer.toString(focusChange));
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_GAIN:
                        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
                        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                            if (playbackDelayed || resumeOnFocusGain) {
                                playbackDelayed = false;
                                resumeOnFocusGain = false;
                                playbackNow();
                            }
                            // Фокус предоставлен.
                            // Например, был входящий звонок и фокус у нас отняли.
                            // Звонок закончился, фокус выдали опять
                            // и мы продолжили воспроизведение.
                            soundPlayer_SetVolume(100);
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS:
                            resumeOnFocusGain = false;
                            playbackDelayed = false;
                            mediaSessionCallback.onPause();
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            resumeOnFocusGain = soundPlayer_IsPlayed();
                            playbackDelayed = false;
                            mediaSessionCallback.onPause();
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            playbackDelayed = false;
                            // Фокус отняли, потому что какому-то приложению надо
                            // коротко "крякнуть".
                            // Например, проиграть звук уведомления или навигатору сказать
                            // "Через 50 метров поворот направо".
                            // В этой ситуации нам разрешено не останавливать вопроизведение,
                            // но надо снизить громкость.
                            // Приложение не обязано именно снижать громкость,
                            // можно встать на паузу, что мы здесь и делаем.
//                            mediaSessionCallback.onPause();
                            switch (RTApplication.getDataBase().getInterruptAction()) {
                                case 0: // Поставить на паузу
                                    resumeOnFocusGain = soundPlayer_IsPlayed();
                                    mediaSessionCallback.onPause();
                                    break;
                                case 1: // Понизить громкость до 30%
                                    soundPlayer_SetVolume(30);
                                    break;
                                case 2: // Понизить громкость до 50%
                                    soundPlayer_SetVolume(50);
                                    break;
                                case 3: // Понизить громкость до 70%
                                    soundPlayer_SetVolume(70);
                                    break;
                                case 4: // Ничего не делать
                                    break;
                                default:
                                    break;
                            }
                            break;
                        default:
                            mediaSessionCallback.onPause();
                            break;
                    }
                }
            };

    boolean resumeOnFocusGain = false;
    boolean playbackDelayed = false;
    boolean playbackNowAuthorized = false;

    void playbackNow() {
        if (!soundPlayer_IsPlayed()) {
            // Указываем, что наше приложение теперь активный плеер и кнопки
            // на окне блокировки должны управлять именно нами
            mediaSession.setActive(true);

            // Запускаем воспроизведение
            soundPlayer_Play(SoundList.currentPlayedSound);

            // Сообщаем новое состояние
            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
        }
    }

    MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {
            // вызывается до обработки конкретного события
            boolean result; // true - обработано, false - не обработано
            int keyCode;
            int action;
            boolean mediaButtonProcessing = false;

            KeyEvent keyevent = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (keyevent != null) {
                keyCode = keyevent.getKeyCode();
                action = keyevent.getAction();

                if (action == KeyEvent.ACTION_DOWN) {

                    for (MediaButtonsMapper.MediaButton button : mediaButtonsMapper.mediaButtons) {
                        if (keyCode == button.keyCode) {
                            switch (button.action) {
                                case mbaNone:
                                    break;
                                case mbaNext:
                                    break;
                                case mbaPrev:
                                    break;
                                case mbaPlayPause:
                                    break;
                                case mbaMute:
                                    mediaButtonMute();
                                    break;
                            }
                        }
                    }
                }
            }

            if (!mediaButtonProcessing)
                result = super.onMediaButtonEvent(mediaButtonIntent);
            else
                result = true;

            return result;
        }
/*
    // голосовой ассистент - 292 KeyEvent.KEYCODE_VIDEO_APP_4
    // mode - 297 KeyEvent.KEYCODE_FEATURED_APP_1
    // telephone up - 286 KeyEvent.KEYCODE_THUMBS_UP
    // telephone down - 301 KeyEvent.KEYCODE_DEMO_APP_1
    // android auto (на панели) - 314 KeyEvent.KEYCODE_MACRO_2
    // музыка (на панели) - 289 KeyEvent.KEYCODE_VIDEO_APP_1

    mediaButtonIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(keyevent.getAction(), KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
    result = super.onMediaButtonEvent(mediaButtonIntent);
*/


        @Override
        public void onPlay() {
            // play/pause
            if (mediaSession.isActive() && mediaSession.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED &&
                    SoundList.pausedCurrentPlayedSound.equals(SoundList.currentPlayedSound)) {

                SoundList.pausedCurrentPlayedSound = "";
                soundPlayer_Resume();
                return;
            }

            SoundList.pausedCurrentPlayedSound = "";

            int audioFocusResult;

            if (!audioFocusRequested) {
                audioFocusResult = audioManager.requestAudioFocus(audioFocusRequest);

                switch (audioFocusResult) {
                    case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                        playbackNowAuthorized = false;
                        break;
                    case AudioManager.AUDIOFOCUS_REQUEST_GRANTED:
                        playbackNowAuthorized = true;
                        playbackNow();
                        soundPlayer_SetVolume(100);
                        break;
                    case AudioManager.AUDIOFOCUS_REQUEST_DELAYED:
                        playbackDelayed = true;
                        playbackNowAuthorized = false;
                        break;
                }

                audioFocusRequested = true;
            }
            else {
                soundPlayer_Stop();
                playbackNow();
            }
        }

        @Override
        public void onPause() {

            SoundList.pausedCurrentPlayedSound = SoundList.currentPlayedSound;

            // Останавливаем воспроизведение
            soundPlayer_Pause();

            // Сообщаем новое состояние
            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
        }

        @Override
        public void onStop() {
            // Останавливаем воспроизведение
            soundPlayer_Stop();
            //exoPlayer.setPlayWhenReady(false);

            // Все, больше мы не "главный" плеер, уходим со сцены
            mediaSession.setActive(false);

            audioManager.abandonAudioFocusRequest(audioFocusRequest);

            audioFocusRequested = false;

            // Сообщаем новое состояние
            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
        }

        @Override
        public void onSeekTo(long position) {
            soundPlayer_SetPosition(position);
        }

        @Override
        public void onSkipToNext() {
            mediaButtonSkipToNext();
        }

        @Override
        public void onSkipToPrevious() {
            mediaButtonSkipToPrevious();
        }

        public void mediaButtonSkipToNext() {
            soundPlayer_NextSound();
            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
        }

        public void mediaButtonSkipToPrevious() {
            soundPlayer_PrevSound();
            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
        }

        public void mediaButtonMute() {
            if (soundPlayer_GetVolume() == 0)
                soundPlayer_SetVolume(100);
            else
                soundPlayer_SetVolume(0);
        }
    };

    public class MediaServiceBinder extends Binder {
        public RTMediaService getService() {
            return RTMediaService.this;
        }

        public MediaSessionCompat.Token getMediaSessionToken() {
            return mediaSession.getSessionToken();
        }
    }

    public void soundPlayer_Play(String filename) {
        soundPlayer.sendCommand(SoundPlayer.SoundPlayerCommand.spcPlay, filename);
    }

    public void soundPlayer_Pause() {
        soundPlayer.sendCommand(SoundPlayer.SoundPlayerCommand.spcPause);
    }

    public void soundPlayer_Resume() {
        soundPlayer.sendCommand(SoundPlayer.SoundPlayerCommand.spcResume);
        mediaSession.setPlaybackState(
                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
    }

    public void soundPlayer_Stop() {
        soundPlayer.sendCommand(SoundPlayer.SoundPlayerCommand.spcStop);
    }

    public long soundPlayer_GetPosition() {
        return soundPlayer.sendCommand(SoundPlayer.SoundPlayerCommand.spcGetPosition);
    }

    public void soundPlayer_SetPosition(long value) {
        soundPlayer.sendCommand(SoundPlayer.SoundPlayerCommand.spcSetPosition, value);
    }

    public long soundPlayer_GetDuration() {
        return soundPlayer.sendCommand(SoundPlayer.SoundPlayerCommand.spcGetDuration);
    }

    public void soundPlayer_SetVolume(long value) {
        soundPlayer.sendCommand(SoundPlayer.SoundPlayerCommand.spcSetVolume, value);
    }

    public long soundPlayer_GetVolume() {
        return soundPlayer.sendCommand(SoundPlayer.SoundPlayerCommand.spcGetVolume);
    }

    public void soundPlayer_NextSound() {
        soundPlayer.sendCommand(SoundPlayer.SoundPlayerCommand.spcPlayNextSound);
    }

    public void soundPlayer_PrevSound() {
        soundPlayer.sendCommand(SoundPlayer.SoundPlayerCommand.spcPlayPrevSound);
    }

    public boolean soundPlayer_IsPlayed() {
        long res = soundPlayer.sendCommand(SoundPlayer.SoundPlayerCommand.spcGetIsPlayed);
        return (res != 0);
    }
}