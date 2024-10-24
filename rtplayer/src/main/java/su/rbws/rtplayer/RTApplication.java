package su.rbws.rtplayer;

import android.app.Application;
import android.content.Context;

import su.rbws.rtplayer.preference.DataBasePreferences;

public class RTApplication extends Application {

    private static Application app;

    public static Application getApplication() {
        return app;
    }

    public static Context getContext() {
        return getApplication().getApplicationContext();
    }

    private static DataBasePreferences preferencesDataBase;
    public static DataBasePreferences getDataBase() {
        return preferencesDataBase;
    }


    private static GlobalData globalData;
    public static GlobalData getGlobalData() {
        return globalData;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        preferencesDataBase = new DataBasePreferences();
        globalData = new GlobalData();
    }

    public static final String version = "1.0.1539";
}
