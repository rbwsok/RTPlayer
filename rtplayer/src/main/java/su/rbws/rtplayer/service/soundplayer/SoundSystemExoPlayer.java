package su.rbws.rtplayer.service.soundplayer;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import su.rbws.rtplayer.service.RTMediaService;

public class SoundSystemExoPlayer extends SoundSystemAbstract {
    ExoPlayer player;

    public SoundSystemExoPlayer(Context context) {
        super(context);

        player = new ExoPlayer.Builder(context).build();
        player.setWakeMode(PowerManager.PARTIAL_WAKE_LOCK);
        player.addListener(exoPlayerListener);
    }

    ExoPlayer.Listener exoPlayerListener = new ExoPlayer.Listener() {
        @Override
        public void onPlaybackStateChanged(int state) {
            switch (state) {
                case ExoPlayer.STATE_IDLE:
//                    break;
                case ExoPlayer.STATE_BUFFERING:
                    break;
                case ExoPlayer.STATE_READY:
                    context.sendBroadcast(new Intent(SoundSystemAbstract.ACTION_PLAYFILE));
                    break;
                case ExoPlayer.STATE_ENDED: {
                    if (context instanceof RTMediaService) {
                        ((RTMediaService) context).mediaServiceNext();
                    }
                    context.sendBroadcast(new Intent(SoundSystemAbstract.ACTION_SOUNDCOMPLETE));
                    break;
                }
            }
        }
    };

    @Override
    public void play(String filename) {

        if (player != null) {
            player.stop();

            try {
                MediaItem mediaItem = MediaItem.fromUri(filename);
                player.setMediaItem(mediaItem);
                player.prepare();
                player.play();

                player.setPlayWhenReady(true);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void stop() {
        if (player != null) {
            player.stop();
            player.setPlayWhenReady(false);
        }
    }

    @Override
    public void pause() {
        if (player != null) {
            player.pause();
            player.setPlayWhenReady(false);
         }
    }

    @Override
    public void resume() {
        if (player != null) {
            player.play();
            player.setPlayWhenReady(true);
        }
    }

    @Override
    public void setPosition(long value) {
        if (player != null) {
            player.seekTo((int) value);
        }
    }

    @Override
    public long getPosition() {
        long result = 0;

        if (player != null && player.getPlayWhenReady()) {
            result = player.getCurrentPosition();
        }

        if (result < 0)
            result = 0;

        return result;
    }

    @Override
    public long getDuration() {
        long result = 0;

        if (player != null && player.getPlayWhenReady()) {
            result = player.getDuration();
        }

        if (result < 0)
            result = 0;

        return result;
    }

    @Override
    public void setVolume(long value) {
        if (player != null) {
            player.setVolume(value / 100.0f);
        }
    }

    public long getVolume() {
        long result = 0;
        if (player != null) {
            float volume = player.getVolume();
            result = (long)(volume * 100.0f);
        }

        return result;
    }


    @Override
    public boolean getIsPlayed() {
        return player.isPlaying();
    }

}
