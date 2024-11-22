package su.rbws.rtplayer.datasource;

import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import su.rbws.rtplayer.FileUtils;
import su.rbws.rtplayer.RTApplication;
import su.rbws.rtplayer.SoundItem;

// источник данных - локальная файловая система

public class SoundSourceLocalFileSystem extends SoundSourceAbstract {

    @Override
    public boolean isCanProcess(SoundItem item) {
        boolean result = false;
        switch (item.state) {
            case fiFile:
            case fiDirectory:
            case fiParentDirectory:
                result = true;
        }

        return result;
    }

    @Override
    public boolean isCanProcess(String name) {
        boolean result = true;

        Uri u = Uri.parse(name);
        String s = u.getScheme();
        if (s != null && (s.equals("http") || s.equals("https")))
            result = false;

        return result;
    }

    @Override
    public SoundItem createDefaultItem(String name) {
        SoundItem result = new SoundItem();

        name = FileUtils.excludePathDelimiter(name);
        result.location = FileUtils.extractFilePath(name);
        result.name = FileUtils.extractFileName(name);

        File f = new File(name);
        if (f.isFile())
            result.state = SoundItem.SourceItemType.fiFile;
        else
        if (f.isDirectory())
            result.state = SoundItem.SourceItemType.fiDirectory;

        return result;
    }

    private List<SoundItem> createViewableRootItems() {
        List<SoundItem> result = new ArrayList<>();

        SoundItem item;
        String baseDirectory, additionalDirectory;

        baseDirectory = RTApplication.getPreferencesData().getBaseDirectory();
        item = new SoundItem();
        item.name = FileUtils.extractLastFolder(baseDirectory);
        item.location = FileUtils.removeLastFolder(baseDirectory);
        item.state = SoundItem.SourceItemType.fiDirectory;
        result.add(item);

        additionalDirectory = RTApplication.getPreferencesData().getAdditionalDirectory();
        if (!additionalDirectory.isEmpty()) {
            item = new SoundItem();
            item.name = FileUtils.extractLastFolder(additionalDirectory);
            item.location = FileUtils.removeLastFolder(additionalDirectory);
            item.state = SoundItem.SourceItemType.fiDirectory;
            result.add(item);
        }

        return result;
    }

    @Override
    public List<SoundItem> createViewableItems(SoundItem parentItem) {
        if (parentItem == null)
            return createViewableRootItems();

        List<SoundItem> result = new ArrayList<>();

        SoundItem item;

        String path;
        switch (parentItem.state) {
            case fiDirectory:
                path = parentItem.getFullName();
                break;
            case fiParentDirectory:
                path = parentItem.location;

                String baseDirectory = FileUtils.excludePathDelimiter(RTApplication.getPreferencesData().getBaseDirectory());
                String additionalDirectory = FileUtils.excludePathDelimiter(RTApplication.getPreferencesData().getAdditionalDirectory());

                String baseDirectoryWithOutLastFolder = FileUtils.removeLastFolder(baseDirectory);
                String additionalDirectoryWithOutLastFolder = FileUtils.removeLastFolder(additionalDirectory);

                if (path.equals(baseDirectoryWithOutLastFolder) ||
                    path.equals(additionalDirectoryWithOutLastFolder)) {
                    return null;
                }

                break;
            default:
                return result;
        }

        item = new SoundItem();
        item.name = "[ .. ]";
        item.location = FileUtils.removeLastFolder(path);
        item.state = SoundItem.SourceItemType.fiParentDirectory;
        result.add(item);

        String fileext;
        File f = new File(path);
        if (f.isDirectory()) {
            File[] list = f.listFiles();
            if (list != null) {
                for (File file : list) {
                    if (file.isFile()) {
                        fileext = FileUtils.extractFileExt(file.getName());
                        if (!fileext.equalsIgnoreCase(".mp3")) {
                            continue;
                        }
                    }

                    item = new SoundItem();
                    item.name = file.getName();
                    item.location = FileUtils.removeLastFolder(file.getAbsolutePath());
                    if (file.isFile())
                        item.state = SoundItem.SourceItemType.fiFile;
                    if (file.isDirectory())
                        item.state = SoundItem.SourceItemType.fiDirectory;

                    result.add(item);
                }
            } // if (list != null) {
        } // if (f.isDirectory()) {

        result.sort(new SoundItem.RecyclerItemStateComparator());

        return result;
    }

    @Override
    public List<SoundItem> appendViewableItems(SoundItem parentItem) {
        return new ArrayList<>();
    }

    @Override
    public void removeItem(SoundItem item) {
        File file = new File(item.getFullName());
        if (file.exists()) {
            file.delete();
        }
    }
}

