package su.rbws.rtplayer.preference;

import java.util.LinkedHashMap;
import java.util.Map;

// базовый класс для базы данных для хранения настроек

public class PreferencesAbstract {

    public enum RepeatMode {repNone, repOneSound, repFolder, repAll}

    public enum PreferenceValueType {
        ptNone, ptInt, ptString, ptMusicFolder, ptAdditionalMusicFolder, ptToolbarPosition, ptRepeatMode, ptPlayOnStartMode,
        ptTitleFileInfo, ptSubTitleFileInfo, ptInterruptAction, ptRemapKeysData, ptBackgroundMode, ptFavoriteList
    }

    public static class PreferenceItem {
        public String value;                        // значение
        public String name;                         // уникальное имя
        public String defaultValue;                 // значение по умолчанию
        public int entries;                         // список уникальных значений ключей для списков
        public int entryValues;                     // список значений для списков
        public PreferenceValueType preferenceType;  // тип
        public boolean isShow;                      // отображение в списке настроек
        public String title;                        // отображаемое название
    }

    public Map<String, PreferenceItem> data = new LinkedHashMap<>();

    public PreferencesAbstract() {

    }

    // поиск и получение итема
    public PreferenceItem get(String key) {
        PreferenceItem result = null;

        if (data.containsKey(key))
            result = data.get(key);

        return result;
    }

    // добавление нового итема
    public void add(PreferenceItem item) {
        data.put(item.name, item);
    }

    // установка значения
    public void set(String key, String value) {
        PreferenceItem item = get(key);
        item.value = value;
    }
}
