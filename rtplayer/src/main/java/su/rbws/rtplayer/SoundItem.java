package su.rbws.rtplayer;

import androidx.annotation.NonNull;

import java.util.Comparator;

// элемент рекуклера, звук и т.д.
public class SoundItem implements Comparable<SoundItem> {

    public enum SourceItemType {
        fiParentDirectory, // выход на уроверь вверх
        fiDirectory, // директория
        fiFile, // файл

        fiRadioRoot,
        fiRadioFavorites,
        fiRadioCountry,
        fiRadioStation,
        fiRadioParentDirectory,
        fiRadioStationParentDirectory,
        fiRadioFavoriteStation
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public int compareTo(@NonNull SoundItem item) {
        int result = 4 * Integer.signum(compareFromState(state, item.state)) +
                     2 * Integer.signum(name.compareTo(item.name)) +
                         Integer.signum(location.compareTo(item.location));
        return result;
    }

    private int compareFromState(@NonNull SourceItemType value1, SourceItemType value2) {
        int result = 0;
        switch (value1) {
            case fiParentDirectory:
                if (value2 == SourceItemType.fiParentDirectory)
                    break;
                else
                    result = -1;
                break;
            case fiDirectory:
                if (value2 == SourceItemType.fiDirectory)
                    break;
                else
                if (value2 == SourceItemType.fiParentDirectory)
                    result = 1;
                else
                    result = -1;
                break;
            case fiFile:
                if (value2 == SourceItemType.fiFile)
                    break;
                else
                    result = 1;
                break;
        }

        return result;
    }

    private int compareToCountryName(SoundItem item) {
        int result = 0;

        if (item == null)
            return result;

        // RU - всегда вверху, неопознанные - всегда внизу

        if (this.location.equals("RU"))
            result = -1;
        else
        if (item.location.equals("RU"))
            result = 1;
        else
        if (this.name.isEmpty())
            result = 1;
        else
        if (item.name.isEmpty())
            result = -1;
        else
            result = name.compareTo(item.name);

        return result;
    }

    public SoundItem() {
    }

    public SoundItem(SoundItem value) {
        this.location = value.location;
        this.name = value.name;
        this.state = value.state;
        this.checked = value.checked;
        this.artist = value.artist;
        this.album = value.album;
        this.title = value.title;
        this.metadataAcquired = value.metadataAcquired;
    }

    public String location;
    public String name;
    public SourceItemType state; // 0 - выход на уроверь вверх, 1 - директория, 2 - файл,

    //public int value;

    public boolean checked = false;

    public String getFullName() {
        if (state == SourceItemType.fiParentDirectory ||
                state == SourceItemType.fiDirectory ||
                state == SourceItemType.fiFile)
            return FileUtils.excludePathDelimiter(this.location) + "/" + this.name;
        else
            return this.name;
    }

    // компаратор для сортировки для файлов по типу
    public static class RecyclerItemStateComparator implements Comparator<SoundItem> {
        public int compare(@NonNull SoundItem obj1, SoundItem obj2) {
            return obj1.compareTo(obj2);
        }
    }

    // компаратор для сортировки стран
    public static class RecyclerItemCountryNameComparator implements Comparator<SoundItem> {
        public int compare(@NonNull SoundItem obj1, SoundItem obj2) {
            return obj1.compareToCountryName(obj2);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // метаданные

    public String artist;
    public String album;
    public String title;

    public boolean metadataAcquired = false;
}

