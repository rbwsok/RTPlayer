package su.rbws.rtplayer;

import android.media.MediaMetadataRetriever;
import android.util.Log;

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

    public FileItem () {
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

    public String artist;
    public String album;
    public String title;

    public volatile boolean metadataAcquired = false;

    private IItemChange itemChange;
    private int itemPosition;

    // запуск получения данных с уведомлением в потоке
    public void getParallelMetadata(IItemChange itemChange, int itemPosition) {
        if (metadataAcquired)
            return;

        this.itemChange = itemChange;
        this.itemPosition = itemPosition;

        Thread workThread = new Thread(getMetadataRunnable);
        workThread.start();
    }

    Runnable getMetadataRunnable = new Runnable() {
        public void run() {
            getMetadata();
            itemChange.onItemChanged(itemPosition);
            itemChange = null;
        }
    };

    // непосредственное получение метаданных
    public void getMetadata() {
        try {
            FileItemMediaMetadataRetriever metaRetriver = new FileItemMediaMetadataRetriever();
            metaRetriver.setDataSource(getFullName());

            Log.i("rtplayer_tag", "get metadata " + this.name);

            album = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            artist = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            title = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        } catch (Exception e) {
            album = "";
            artist = "";
            title = this.name;
        }

        if (album == null)
            album = "";
        if (artist == null)
            artist = "";
        if (title == null) {
            title = Utils.extractFileNameNoExt(this.name);

            int t = title.indexOf('.');
            if (t >= 0) {
                if (Utils.isDigit(title.substring(0, t))) {
                    title = title.substring(t + 1).trim();
                }
            }
        }

        metadataAcquired = true;
    }
}

