package su.rbws.rtplayer;

import android.media.MediaMetadataRetriever;

import androidx.annotation.NonNull;

import java.util.Comparator;

public class FileItem implements Comparable<FileItem> {

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public int compareTo(@NonNull FileItem item) {
        int result = 4 * Integer.signum(compare(state, item.state)) +
                     2 * Integer.signum(name.compareTo(item.name)) +
                         Integer.signum(location.compareTo(item.location));
        return result;
    }

    public int compare(@NonNull FileItemType value1, FileItemType value2) {
        int result = 0;
        switch (value1) {
            case fiParentDirectory:
                if (value2 == FileItemType.fiParentDirectory)
                    break;
                else
                    result = -1;
                break;
            case fiDirectory:
                if (value2 == FileItemType.fiDirectory)
                    break;
                else
                if (value2 == FileItemType.fiParentDirectory)
                    result = 1;
                else
                    result = -1;
                break;
            case fiFile:
                if (value2 == FileItemType.fiFile)
                    break;
                else
                    result = 1;
                break;
        }

        return result;
    }

    public enum FileItemType {
        fiParentDirectory, // выход на уроверь вверх
        fiDirectory, // директория
        fiFile // файл
    }

    public FileItem ()
    {
    }

    public String location;
    public String name;
    public FileItemType state; // 0 - выход на уроверь вверх, 1 - директория, 2 - файл,

    public String getFullName() {
        return Utils.excludePathDelimiter(this.location) + "/" + this.name;
    }

    public boolean isDirectory() { return state == FileItemType.fiDirectory; }

    public boolean isFile() {
        return state == FileItemType.fiFile;
    }

    public boolean isParentDirectory() { return state == FileItemType.fiParentDirectory; }

    // компаратор для сортировки
    public static class FileItemComparator implements Comparator<FileItem> {
        public int compare(@NonNull FileItem obj1, FileItem obj2) {
            return obj1.compareTo(obj2);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // метаданные

    private static class FileItemMediaMetadataRetriever extends MediaMetadataRetriever implements AutoCloseable {
        public FileItemMediaMetadataRetriever() {
            super();
        }

        @Override
        public void close() {
            try {
                release();
            } catch (Exception e) {

            }
        }

    }

    public String Artist;
    public String Album;
    public String Title;

    private boolean MetadataAcquired = false;

    private final static FileItemMediaMetadataRetriever metaRetriver = new FileItemMediaMetadataRetriever();

    public void getMetadata() {
        if (MetadataAcquired)
            return;

        MetadataAcquired = true;

        try {
            //
            metaRetriver.setDataSource(getFullName());

            Album = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            Artist = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            Title = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        } catch (Exception e) {
            Album = "";
            Artist = "";
            Title = this.name;
        }

        if (Album == null)
            Album = "";
        if (Artist == null)
            Artist = "";
        if (Title == null) {
            Title = Utils.extractFileNameNoExt(this.name);

            int t = Title.indexOf('.');
            if (t >= 0) {
                if (Utils.isDigit(Title.substring(0, t))) {
                    Title = Title.substring(t + 1).trim();
                }
            }
        }
    }
}

