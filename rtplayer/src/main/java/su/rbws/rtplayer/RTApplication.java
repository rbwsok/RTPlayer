package su.rbws.rtplayer;

import android.app.Application;
import android.content.Context;

import su.rbws.rtplayer.datasource.SoundSourceManager;
import su.rbws.rtplayer.preference.PreferencesData;
import su.rbws.rtplayer.service.MediaButtonsMapper;

public class RTApplication extends Application {

    private static Application app;

    public static Application getApplication() {
        return app;
    }

    public static Context getContext() {
        return getApplication().getApplicationContext();
    }

    private static PreferencesData preferencesDataBase;
    public static PreferencesData getPreferencesData() {
        return preferencesDataBase;
    }

    private static SoundSourceManager soundSourceManager;
    public static SoundSourceManager getSoundSourceManager() {
        return soundSourceManager;
    }

    private static CacheDataBase chacheDataBase;
    public static CacheDataBase getChacheDataBase() {
        return chacheDataBase;
    }

    public static MediaButtonsMapper mediaButtonsMapper;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        chacheDataBase = new CacheDataBase();
        soundSourceManager = new SoundSourceManager();
        preferencesDataBase = new PreferencesData();
    }

    // описание экрана ГУ
    public static class ScreenAuto {
        // разрешение экрана
        public final static int RESOLUTION_X = 1920;
        public final static int RESOLUTION_Y = 720;
        // ограничения экрана (панели)
        public final static int INSETS_LEFT = 288;
        public final static int INSETS_TOP = 0;
        public final static int INSETS_RIGHT = 96;
        public final static int INSETS_BOTTOM = 0;
        // вписывание окна приложения
        public final static int PADDING_LEFT = 180;
        public final static int PADDING_TOP = 0;
        public final static int PADDING_RIGHT = 96;
        public final static int PADDING_BOTTOM = 0;
        // рабочее разрешение экрана (без панелей)
        public final static int WORK_RESOLUTION_X = RESOLUTION_X - PADDING_LEFT - PADDING_RIGHT;
        public final static int WORK_RESOLUTION_Y = 720;
    }

    public static final String version = "1.0.2184";
}
