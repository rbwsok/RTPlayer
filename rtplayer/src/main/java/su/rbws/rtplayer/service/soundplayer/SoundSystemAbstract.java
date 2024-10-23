package su.rbws.rtplayer.service.soundplayer;

import android.content.Context;

public abstract class SoundSystemAbstract {

    protected Context context;

    public static final String ACTION_SOUNDCOMPLETE = "su.rbws.rtplayer.SOUNDCOMPLETE";
    public static final String ACTION_PLAYFILE = "su.rbws.rtplayer.PLAYFILE";

    // конструктор
    public SoundSystemAbstract(Context context) {
        this.context = context;
    }

    // воспроизведение файла
    public abstract void play(String filename);
    // остановка воспроизведения
    public abstract void stop();
    // пауза
    public abstract void pause();
    // воспроизведенияч после паузы
    public abstract void resume();

    // установка текущей позиции воспроизведения (мс)
    public abstract void setPosition(long value);
    // получение текущей позиции воспроизведения (мс)
    public abstract long getPosition();
    // получение продолжительности файла (мс)
    public abstract long getDuration();

    // громкость
    public abstract void setVolume(long value);
    public abstract long getVolume();

    // статус
    public abstract boolean getIsPlayed();
}
