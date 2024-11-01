package su.rbws.rtplayer;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

import su.rbws.rtplayer.service.MediaButtonsMapper;

public class GlobalData {
    public MediaButtonsMapper mediaButtonsMapper;

    public GlobalData() {
        viewableFileList = new ArrayList<>();
    }

    // отображаемый список файлов
    public ArrayList<FileItem> viewableFileList;

    public void removeFromViewableFileList(String filename) {
        for (int i = 0; i < viewableFileList.size(); ++i) {
            if (filename.equals(viewableFileList.get(i).getFullName())) {
                viewableFileList.remove(i);
                return;
            }
        }
    }

    public void createViewableFileList(String path) {
        FileItem item;
        String baseDirectory, additionalDirectory, baseDirectoryWithOutLastFolder, additionalDirectoryWithOutLastFolder;

        boolean needRootDirectory = false;
        boolean needBackToParentDirectory = false;

        viewableFileList.clear();

        baseDirectory = Utils.excludePathDelimiter(RTApplication.getDataBase().getBaseDirectory());
        additionalDirectory = Utils.excludePathDelimiter(RTApplication.getDataBase().getAdditionalDirectory());

        baseDirectoryWithOutLastFolder = Utils.removeLastFolder(baseDirectory);
        additionalDirectoryWithOutLastFolder = Utils.removeLastFolder(additionalDirectory);

        if (additionalDirectory.isEmpty()) {
            // root состоит из содержимого baseDirectory
            if (path.isEmpty())
                path = baseDirectory;

            if (path.equals(baseDirectoryWithOutLastFolder)) {
                needRootDirectory = true;
            }

            if (!path.equals(baseDirectory)) {
                needBackToParentDirectory = true;
            }
        } else {
            // root состоит из двух виртуальных папок

            if (path.isEmpty() ||
                    path.equals(baseDirectoryWithOutLastFolder) ||
                    path.equals(additionalDirectoryWithOutLastFolder)) {
                needRootDirectory = true;
            }

            needBackToParentDirectory = true;
        }

        // виртуальный root
        if (needRootDirectory) {
            baseDirectory = RTApplication.getDataBase().getBaseDirectory();
            item = new FileItem();
            item.name = Utils.extractLastFolderName(baseDirectory);
            item.location = Utils.removeLastFolder(baseDirectory);
            item.state = FileItem.FileItemType.fiDirectory;
            viewableFileList.add(item);

            additionalDirectory = RTApplication.getDataBase().getAdditionalDirectory();
            if (!additionalDirectory.isEmpty()) {
                item = new FileItem();
                item.name = Utils.extractLastFolderName(additionalDirectory);
                item.location = Utils.removeLastFolder(additionalDirectory);
                item.state = FileItem.FileItemType.fiDirectory;
                viewableFileList.add(item);
            }

            return;
        }

        if (needBackToParentDirectory) {
            item = new FileItem();
            item.name = "[ .. ]";
            item.location = Utils.removeLastFolder(path);

            item.state = FileItem.FileItemType.fiParentDirectory;
            viewableFileList.add(item);
        }

        String fileext;
        File f = new File(path);
        if (f.isDirectory()) {
            File[] list = f.listFiles();
            if (list != null) {
                for (File file : list) {
                    if (file.isFile()) {
                        fileext = Utils.extractFileExt(file.getName());
                        if (!fileext.equalsIgnoreCase(".mp3")) {
                            continue;
                        }
                    }

                    item = new FileItem();
                    item.name = file.getName();
                    item.location = Utils.removeLastFolder(file.getAbsolutePath());
                    if (file.isFile())
                        item.state = FileItem.FileItemType.fiFile;
                    if (file.isDirectory())
                        item.state = FileItem.FileItemType.fiDirectory;

                    viewableFileList.add(item);
                }

/*                // читаем информацию из файлов
                int arraySize = viewableFileList.size();
                if (arraySize > 11)
                    arraySize = 11;

                for (int i = 0; i < arraySize; ++i) {
                    if (viewableFileList.get(i).isFile())
                        viewableFileList.get(i).getMetadata();
                }*/
            } // if (list != null) {
        } // if (f.isDirectory()) {

        viewableFileList.sort(new FileItem.FileItemComparator());
    }

    public void createViewableFileList() {
        createViewableFileList("");

        if (viewableFileList.isEmpty()) {
            File extp = Environment.getExternalStorageDirectory();
            createViewableFileList(extp.getAbsolutePath());
        }
    }

    MetadataExtractor metadataExtractor = new MetadataExtractor();
}