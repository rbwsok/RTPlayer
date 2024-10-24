package su.rbws.rtplayer.preference;

import android.content.SharedPreferences;
import android.os.Environment;

import androidx.preference.PreferenceManager;

import java.io.File;
import java.util.Map;

import su.rbws.rtplayer.R;
import su.rbws.rtplayer.RTApplication;
import su.rbws.rtplayer.Utils;

public class DataBasePreferences extends DataBaseAbstract {
    public SharedPreferences sharedPreferences;

    public final static String PREFERENCE_NAME_TOOLBAR_POSITION = "toolbar_position_preference";
    public final static String PREFERENCE_NAME_TEXT_SIZE = "text_size_preference";
    public final static String PREFERENCE_NAME_BASE_DIRCTORY = "base_music_directory_preference";
    public final static String PREFERENCE_NAME_ADDITIONAL_DIRECTORY = "additional_music_directory_preference";
    public final static String PREFERENCE_NAME_REPEAT_MODE = "repeat_mode_preference";
    public final static String PREFERENCE_NAME_LASET_PLAYED_FILE = "current_played_file";
    public final static String PREFERENCE_NAME_SHUFFLE = "shuffle_mode";
    public final static String PREFERENCE_NAME_PLAY_ON_START = "play_on_start_preference";
    public final static String PREFERENCE_NAME_TITLE_FILE = "title_file_preference";
    public final static String PREFERENCE_NAME_SUBLITLE_FILE = "subtitle_file_preference";
    public final static String PREFERENCE_NAME_TOP_SPACE = "top_space_preference";
    public final static String PREFERENCE_NAME_LEFT_SPACE = "left_space_preference";
    public final static String PREFERENCE_NAME_RIGHT_SPACE = "left_right_preference";
    public final static String PREFERENCE_NAME_BOTTOM_SPACE = "left_bottom_preference";
    public final static String PREFERENCE_NAME_DEFAULT = "default_preference";
    public final static String PREFERENCE_NAME_MAX_DEPTH_RECENT = "max_depth_recent";
    public final static String PREFERENCE_NAME_INTERRUPT_ACTION = "interrupt_action_preference";
    public final static String PREFERENCE_NAME_REMAP_KEYS = "remap_keys_preference";
    public final static String PREFERENCE_NAME_REMAP_KEYS_DATA = "remap_keys_data_preference";
    public final static String PREFERENCE_NAME_FTP_SERVER = "ftp_server_preference";
    public final static String PREFERENCE_NAME_FTP_SERVER_PORT = "ftp_server_port_preference";
    public final static String PREFERENCE_NAME_BACKGROUND_MODE = "background_preference";

    private void CreateItems() {
        DataBaseAbstract.PreferenceItem item;

        // ftp серверitem
        item = new PreferenceItem();
        item.name = PREFERENCE_NAME_FTP_SERVER;
        item.defaultValue = "";
        item.preferenceType = PreferenceValueType.ptNone;
        item.isShow = true;
        item.title = RTApplication.getContext().getString(R.string.ftp_server);
        add(item);
        // порт ftp сервера
        item = new PreferenceItem();
        item.name = PREFERENCE_NAME_FTP_SERVER_PORT;
        item.defaultValue = "30000";
        item.preferenceType = PreferenceValueType.ptInt;
        item.isShow = false;
        add(item);

        // задний фон
        item = new PreferenceItem();
        item.name = PREFERENCE_NAME_BACKGROUND_MODE;
        item.defaultValue = "0";
        item.preferenceType = PreferenceValueType.ptBackgroundMode;
        item.isShow = true;
        item.title = RTApplication.getContext().getString(R.string.background_mode);
        item.entries = R.array.background_entries;
        item.entryValues = R.array.background_entry_values;
        add(item);

        // позиция тулбара 1 - слева, 2 - справа, 3 - вверху, 4 - внизу (по умолчанию = 1)
        item = new PreferenceItem();
        item.name = PREFERENCE_NAME_TOOLBAR_POSITION;
        item.defaultValue = "1";
        item.preferenceType = PreferenceValueType.ptToolbarPosition;
        item.isShow = true;
        item.title = RTApplication.getContext().getString(R.string.toolbar_position);
        item.entries = R.array.toolbar_position_entries;
        item.entryValues = R.array.toolbar_position_entry_values;
        add(item);
        // базовый размер текста (по умолчанию = 30)
        item = new PreferenceItem();
        item.name = PREFERENCE_NAME_TEXT_SIZE;
        item.defaultValue = "35";
        item.preferenceType = PreferenceValueType.ptInt;
        item.isShow = true;
        item.title = RTApplication.getContext().getString(R.string.text_size);
        add(item);
        // базовая папка. поиск музыки идет в ней.
        item = new PreferenceItem();
        item.name = PREFERENCE_NAME_BASE_DIRCTORY;
        item.defaultValue = "/";
        item.preferenceType = PreferenceValueType.ptMusicFolder;
        item.isShow = true;
        item.title = RTApplication.getContext().getString(R.string.music_folder);
        add(item);
        // дополнительная папка. поиск музыки идет в ней.
        item = new PreferenceItem();
        item.name = PREFERENCE_NAME_ADDITIONAL_DIRECTORY;
        item.defaultValue = "";
        item.preferenceType = PreferenceValueType.ptAdditionalMusicFolder;
        item.isShow = true;
        item.title = RTApplication.getContext().getString(R.string.additional_music_folder);
        add(item);
        // режим повтора (repNone, repOneSound, repFolder, repAll)
        item = new PreferenceItem();
        item.name = PREFERENCE_NAME_REPEAT_MODE;
        item.defaultValue = "4";
        item.preferenceType = PreferenceValueType.ptRepeatMode;
        item.isShow = true;
        item.title = RTApplication.getContext().getString(R.string.repeat_mode);
        item.entries = R.array.repeat_mode_entries;
        item.entryValues = R.array.repeat_mode_entry_values;
        add(item);
        // открытый файл
        item = new PreferenceItem();
        item.name = PREFERENCE_NAME_LASET_PLAYED_FILE;
        item.defaultValue = "";
        item.preferenceType = PreferenceValueType.ptString;
        item.isShow = false;
        add(item);
        // перемешивание
        item = new PreferenceItem();
        item.name = PREFERENCE_NAME_SHUFFLE;
        item.defaultValue = "0";
        item.preferenceType = PreferenceValueType.ptInt;
        item.isShow = false;
        add(item);
        // воспроизведение при запуске
        item = new PreferenceItem();
        item.name = PREFERENCE_NAME_PLAY_ON_START;
        item.defaultValue = "1";
        item.preferenceType = PreferenceValueType.ptPlayOnStartMode;
        item.isShow = true;
        item.title = RTApplication.getContext().getString(R.string.play_on_start);
        item.entries = R.array.play_on_start_entries;
        item.entryValues = R.array.play_on_start_entry_values;
        add(item);
        // отображение имени файла
        item = new PreferenceItem();
        item.name = PREFERENCE_NAME_TITLE_FILE;
        item.defaultValue = "1";
        item.preferenceType = PreferenceValueType.ptTitleFileInfo;
        item.isShow = true;
        item.title = RTApplication.getContext().getString(R.string.show_title_file);
        item.entries = R.array.title_file_entries;
        item.entryValues = R.array.title_file_entry_values;
        add(item);
        // отображение информации о файле
        item = new PreferenceItem();
        item.name = PREFERENCE_NAME_SUBLITLE_FILE;
        item.defaultValue = "1";
        item.preferenceType = PreferenceValueType.ptSubTitleFileInfo;
        item.isShow = true;
        item.title = RTApplication.getContext().getString(R.string.show_subtitle_file);
        item.entries = R.array.subtitle_file_entries;
        item.entryValues = R.array.subtitle_file_entry_values;
        add(item);
        // отступ сверху
        item = new PreferenceItem();
        item.name = PREFERENCE_NAME_TOP_SPACE;
        item.defaultValue = "30";
        item.preferenceType = PreferenceValueType.ptInt;
        item.isShow = true;
        item.title = RTApplication.getContext().getString(R.string.top_space);
        add(item);
        // отступ слева
        item = new PreferenceItem();
        item.name = PREFERENCE_NAME_LEFT_SPACE;
        item.defaultValue = "0";
        item.preferenceType = PreferenceValueType.ptInt;
        item.isShow = true;
        item.title = RTApplication.getContext().getString(R.string.left_space);
        add(item);
        // отступ справа
        item = new PreferenceItem();
        item.name = PREFERENCE_NAME_RIGHT_SPACE;
        item.defaultValue = "0";
        item.preferenceType = PreferenceValueType.ptInt;
        item.isShow = true;
        item.title = RTApplication.getContext().getString(R.string.right_space);
        add(item);
        // отступ снизу
        item = new PreferenceItem();
        item.name = PREFERENCE_NAME_BOTTOM_SPACE;
        item.defaultValue = "0";
        item.preferenceType = PreferenceValueType.ptInt;
        item.isShow = true;
        item.title = RTApplication.getContext().getString(R.string.bottom_space);
        add(item);
        // неповторяемые файлы
        item = new PreferenceItem();
        item.name = PREFERENCE_NAME_MAX_DEPTH_RECENT;
        item.defaultValue = "5";
        item.preferenceType = PreferenceValueType.ptInt;
        item.isShow = true;
        item.title = RTApplication.getContext().getString(R.string.max_depth_recent);
        add(item);
        // Действие при прерывании воспроизведения (задний ход, навигация и т.д.)
        item = new PreferenceItem();
        item.name = PREFERENCE_NAME_INTERRUPT_ACTION;
        item.defaultValue = "2";
        item.preferenceType = PreferenceValueType.ptInterruptAction;
        item.isShow = true;
        item.title = RTApplication.getContext().getString(R.string.interrupt_action);
        item.entries = R.array.interrupt_action_entries;
        item.entryValues = R.array.interrupt_action_entry_values;
        add(item);
        // установка по усмолчанию
        item = new PreferenceItem();
        item.name = PREFERENCE_NAME_DEFAULT;
        item.defaultValue = "";
        item.preferenceType = PreferenceValueType.ptNone;
        item.isShow = true;
        item.title = RTApplication.getContext().getString(R.string.set_defaults);
        add(item);

        // переопределение клавиш
        item = new PreferenceItem();
        item.name = PREFERENCE_NAME_REMAP_KEYS;
        item.defaultValue = "";
        item.preferenceType = PreferenceValueType.ptNone;
        item.isShow = true;
        item.title = RTApplication.getContext().getString(R.string.remap_keys);
        add(item);
        // данные переопределения клавиш
        item = new PreferenceItem();
        item.name = PREFERENCE_NAME_REMAP_KEYS_DATA;
        item.defaultValue = "";
        item.preferenceType = PreferenceValueType.ptRemapKeysData;
        item.isShow = false;
        add(item);
    }

    public DataBasePreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RTApplication.getContext());

        CreateItems();

        getAllPreferences();
    }

    @Override
    public void set(String key, String value) {
        PreferenceItem item = get(key);
        item.value = value;

        SharedPreferences.Editor editor;

        editor = sharedPreferences.edit();
        editor.putString(item.name, item.value);
        editor.commit();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private int getValueAsInt(String name) {
        String value = getValueAsString(name);
        return Utils.parseInt(value);
    }

    private float getValueAsFloat(String name) {
        float result;
        String value = getValueAsString(name);
        result = Float.parseFloat(value);

        return result;
    }

    private String getValueAsString(String name) {
        String result = "";
        PreferenceItem item = get(name);
        if (item != null)
            result = item.value;

        return result;
    }

    public float getTextSize() {
        return getValueAsFloat(PREFERENCE_NAME_TEXT_SIZE);
    }

    public int getToolbarPosition() {
        return getValueAsInt(PREFERENCE_NAME_TOOLBAR_POSITION);
    }

    public String getBaseDirectory() {
        return getValueAsString(PREFERENCE_NAME_BASE_DIRCTORY);
    }

    public String getAdditionalDirectory() {
        return getValueAsString(PREFERENCE_NAME_ADDITIONAL_DIRECTORY);
    }

    public DataBaseAbstract.RepeatMode getRepeatMode() {
        DataBaseAbstract.RepeatMode result = RepeatMode.repNone;
        PreferenceItem item = get(PREFERENCE_NAME_REPEAT_MODE);
        if (item != null) {
            switch (Utils.parseInt(item.value)) {
                case 2:
                    result = RepeatMode.repOneSound;
                    break;
                case 3:
                    result = RepeatMode.repFolder;
                    break;
                case 4:
                    result = RepeatMode.repAll;
                    break;
            }
        }

        return result;
    }

    public String getLastPlayedFile() {
        return getValueAsString(PREFERENCE_NAME_LASET_PLAYED_FILE);
    }

    public void setLastPlayedFile(String value) {
        set(PREFERENCE_NAME_LASET_PLAYED_FILE, value);
    }

    public int getFTPPort() {
        return getValueAsInt(PREFERENCE_NAME_FTP_SERVER_PORT);
    }

    public void setFTPPort(int value) {
        set(PREFERENCE_NAME_FTP_SERVER_PORT, Integer.toString(value));
    }

    public int getShuffleMode() {
        return getValueAsInt(PREFERENCE_NAME_SHUFFLE);
    }

    public void setShuffleMode(int value) {
        set(PREFERENCE_NAME_SHUFFLE, Integer.toString(value));
    }

    public int getPlayOnStartMode() {
        return getValueAsInt(PREFERENCE_NAME_PLAY_ON_START);
    }

    public void setPlayOnStartMode(int value) {
        set(PREFERENCE_NAME_PLAY_ON_START, Integer.toString(value));
    }

    public int getTitleFileMode() {
        return getValueAsInt(PREFERENCE_NAME_TITLE_FILE);
    }

    public int getSubTitleFileMode() {
        return getValueAsInt(PREFERENCE_NAME_SUBLITLE_FILE);
    }

    public int getTopSpace() {
        return getValueAsInt(PREFERENCE_NAME_TOP_SPACE);
    }
    public int getLeftSpace() {
        return getValueAsInt(PREFERENCE_NAME_LEFT_SPACE);
    }
    public int getRightSpace() {
        return getValueAsInt(PREFERENCE_NAME_RIGHT_SPACE);
    }
    public int getBottomSpace() {
        return getValueAsInt(PREFERENCE_NAME_BOTTOM_SPACE);
    }

    public int getMaxDepthRecent() {
        return getValueAsInt(PREFERENCE_NAME_MAX_DEPTH_RECENT);
    }

    public int getInterruptAction() {
        return getValueAsInt(PREFERENCE_NAME_INTERRUPT_ACTION);
    }

    public int getBackgroundMode() {
        return getValueAsInt(PREFERENCE_NAME_BACKGROUND_MODE);
    }

    // из редактора preference в базу
    public void getAllPreferences() {
        SharedPreferences.Editor editor;

        for (Map.Entry<String, DataBaseAbstract.PreferenceItem> entry : data.entrySet()) {
            DataBaseAbstract.PreferenceItem item = entry.getValue();

            item.value = sharedPreferences.getString(item.name, "");

            // проверка значения на правильность
            switch (item.preferenceType) {
                case ptInt:
                case ptToolbarPosition:
                case ptPlayOnStartMode:
                case ptRepeatMode:
                case ptTitleFileInfo:
                case ptSubTitleFileInfo:
                case ptInterruptAction:
                case ptBackgroundMode:
                {
                    if (!Utils.isDigit(item.value)) {
                        item.value = item.defaultValue;
                        editor = sharedPreferences.edit();
                        editor.putString(item.name, item.value);
                        editor.commit();
                    }
                    break;
                }
                case ptString: {
                    break;
                }
/*                case ptRemapKeysData: {
                    if (rec.value.isEmpty()) {
                        if (mediaButtonsMapper != null) {
                            mediaButtonsMapper.setDefault();
                            rec.value = mediaButtonsMapper.serialization();
                        }
                        editor = sharedPreferences.edit();
                        editor.putString(rec.preferenceName, rec.value);
                        editor.commit();
                    }
                    MediaButtonsMapper.loadedSerializationXML = rec.value;
                    if (mediaButtonsMapper != null) {
                        mediaButtonsMapper.deserialization(MediaButtonsMapper.loadedSerializationXML);
                        MediaButtonsMapper.loadedSerializationXML = "";
                    }
                    break;
                }*/
                case ptMusicFolder: {
                    File extp = Environment.getExternalStorageDirectory();
                    String rootexternalpath = extp.getAbsolutePath();

                    if (item.value.isEmpty() ||
                            item.value.equals("/") ||
                            !Utils.directoryExists(item.value)) {
                        item.value = rootexternalpath;
                        editor = sharedPreferences.edit();
                        editor.putString(item.name, item.value);
                        editor.commit();
                    }
                    break;
                }
            }
        }
    }

    // из базы в редактор preference
    public void putAllPreferences() {
        SharedPreferences.Editor editor;

        for (Map.Entry<String, DataBaseAbstract.PreferenceItem> entry : data.entrySet()) {
            DataBaseAbstract.PreferenceItem item = entry.getValue();

            if (item.preferenceType == PreferenceValueType.ptRemapKeysData)
                item.value = RTApplication.getGlobalData().mediaButtonsMapper.serialization();

            editor = sharedPreferences.edit();
            editor.putString(item.name, item.value);
            editor.commit();
        }
    }

    // установка значений по умолчанию (и в базе и в редакторе)
    public void putAllDefaultPreferences() {
        for (Map.Entry<String, DataBaseAbstract.PreferenceItem> entry : data.entrySet()) {
            DataBaseAbstract.PreferenceItem item = entry.getValue();

            item.value = item.defaultValue;
        }

        putAllPreferences();
    }
}