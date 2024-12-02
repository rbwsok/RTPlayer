package su.rbws.rtplayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.media.MediaMetadataRetriever;

import androidx.annotation.NonNull;

// вытаскиватель метаинформации из файлов (исполнитель, альбом и т.д.)
public class MetadataExtractor {
    public static class MetadataItem {
        public String name;
        public String artist;
        public String album;
        public String title;

        public int position;

        public boolean metadataAcquired = false;
    }

    private final Map<String, MetadataItem> metadataItems;

    private final FileItemMediaMetadataRetriever metaRetriver;

    public IItemChange objectChange;

    public MetadataExtractor() {
        metadataItems = new ConcurrentHashMap<>();

        metaRetriver = new FileItemMediaMetadataRetriever();

        Thread workThread = new Thread(getMetadataRunnable);
        workThread.start();
    }

    public MetadataItem getOrAdd(String key, int position) {
        MetadataItem result = metadataItems.getOrDefault(key, null);
        if (result == null) {
            MetadataItem item = new MetadataItem();
            item.name = key;
            item.position = position;
            metadataItems.put(key, item);
        } else if (!result.metadataAcquired) {
            result = null;
        }

        return result;
    }

    public boolean getMetadata(@NonNull SoundItem item, int position) {
        if (item.metadataAcquired)
            return true;

        MetadataItem metadateItem = getOrAdd(item.getFullName(), position);
        if (metadateItem != null) {
            item.artist = metadateItem.artist;
            item.album = metadateItem.album;
            item.title = metadateItem.title;
            item.metadataAcquired = true;
            return true;
        }

        return false;
    }

    Runnable getMetadataRunnable = new Runnable() {
        @Override
        public void run() {

            int count;

            while (true) {
                if (Thread.currentThread().isInterrupted())
                    break;

                count = 0;
                for (Map.Entry<String, MetadataItem> entry : metadataItems.entrySet()) {
                    MetadataItem item = entry.getValue();
                    if (!item.metadataAcquired) {
                        getInternalMetadata(item);
                        count++;
                        try {
                            objectChange.onItemChanged(item.position);
                        } catch (Exception e) {
                            break;
                        }

                    }
                }

                if (count == 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }
    };

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

    public void getInternalMetadata(MetadataItem item) {
        try {
            metaRetriver.setDataSource(item.name);

            //Log.i("rtplayer_tag", "get metadata " + item.name);

            item.album = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            item.artist = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            item.title = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        } catch (Exception e) {
            item.album = "";
            item.artist = "";
            if (item.name != null)
                item.title = item.name;
            else
                item.title = "";
        }

        if (item.album == null)
            item.album = "";
        if (item.artist == null)
            item.artist = "";
        if (item.title == null) {
            if (item.name != null)
                item.title = FileUtils.extractFileName(item.name, false);
            else
                item.title = "";

            int t = item.title.indexOf('.');
            if (t >= 0) {
                if (Utils.isDigit(item.title.substring(0, t))) {
                    item.title = item.title.substring(t + 1).trim();
                }
            }
        }

        item.metadataAcquired = true;
    }

}
