package su.rbws.rtplayer.service.soundplayer;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import su.rbws.rtplayer.FileUtils;
import su.rbws.rtplayer.RTApplication;

// список звуков
public class SoundList {
    // текущий воспроизводимый звук
    public static String currentPlayedSound = "";
    // трек поставленный на паузу
    public static String pausedCurrentPlayedSound = "";

    static Random randomSystem = ThreadLocalRandom.current();

    public SoundList() {
    }

    public static String getNextFileInFolder() {
        if (currentPlayedSound.isEmpty())
            return "";

        ArrayList<String> fileList = new ArrayList<>();

        String folder = FileUtils.extractFilePath(currentPlayedSound);
        if (!FileUtils.createFileList(folder, fileList))
            return "";

        int currentindex = -1;
        for (int i = 0; i < fileList.size(); ++i) {
             if (fileList.get(i).equals(currentPlayedSound)) {
                currentindex = i;
                break;
            }
        }

        if (currentindex == -1)
            return "";

        // переход на первый файл в папке
        if (currentindex + 1 >= fileList.size()) {
            return fileList.get(0);
        }

        return fileList.get(currentindex + 1);
    }

    public static String getPrevFileInFolder() {
        if (currentPlayedSound.isEmpty())
            return "";

        ArrayList<String> fileList = new ArrayList<>();

        String folder = FileUtils.extractFilePath(currentPlayedSound);
        if (!FileUtils.createFileList(folder, fileList))
            return "";

        int currentindex = -1;
        for (int i = 0; i < fileList.size(); ++i) {
            if (fileList.get(i).equals(currentPlayedSound)) {
                currentindex = i;
                break;
            }
        }

        if (currentindex == -1)
            return "";

        // переход на первый файл в папке
        if (currentindex - 1 < 0) {
            return fileList.get(fileList.size() - 1);
        }

        return fileList.get(currentindex - 1);
    }

    public static String getNextFileInAllFiles() {
        if (currentPlayedSound.isEmpty())
            return "";

        ArrayList<String> fileList = new ArrayList<>();

        String folder = FileUtils.extractFilePath(currentPlayedSound);
        if (!FileUtils.createFileList(folder, fileList))
            return "";

        int currentindex = fileList.indexOf(currentPlayedSound);
        if (currentindex < 0)
            return "";

        // файл в этой же папке
        if (currentindex + 1 < fileList.size()) {
            return fileList.get(currentindex + 1);
        }

        // воспроизведение в текущей папке закончено
        // 1. ищем вложенные папки
        // 2. если вложенных папок нет, то ищем папки в том же уровне

        ///////////////////////////////////////////////////////////
        // 1. ищем вложенные папки
        String res = findFirstFileInSubfolders(folder);
        if (!res.isEmpty())
            return res;

        ///////////////////////////////////////////////////////////
        // 2. перебираем папки в том же уровне от текущей до конца
        ArrayList<String> folders = new ArrayList<>();
        String scanFolder = FileUtils.removeLastFolder(folder);
        String baseFolder = FileUtils.excludePathDelimiter(RTApplication.getDataBase().getBaseDirectory());
        int index, i;

        while (!folder.equals(baseFolder)) {
            FileUtils.createFolderList(scanFolder, folders);
            index = folders.indexOf(folder);
            if (index >= 0) {
                i = index + 1;
                while (i < folders.size()) {
                    res = findFirstFileInFolder(folders.get(i));
                    if (!res.isEmpty())
                        return res;
                    i++;
                }
            }

            folder = scanFolder;
            scanFolder = FileUtils.removeLastFolder(folder);
        }

        res = findFirstFileInFolder(folder);
        if (!res.isEmpty())
            return res;

        res = findFirstFileInSubfolders(folder);
        if (!res.isEmpty())
            return res;

        return "";
    }

    // рекурсивный поиск файлов в поддиректориях
    private static String findFirstFileInFolder(String folder) {
        ArrayList<String> files = new ArrayList<>();

        if (!FileUtils.createFileList(folder, files))
            return "";

        if (!files.isEmpty())
            return files.get(0);

        return "";
    }

    // рекурсивный поиск файлов в поддиректориях
    @NonNull
    private static String findFirstFileInSubfolders(String folder) {
        ArrayList<String> folders = new ArrayList<>();
        String res;

        if (!FileUtils.createFolderList(folder, folders))
            return "";

        for (String f: folders) {
            res = findFirstFileInFolder(f);
            if (!res.isEmpty())
                return res;

            res = findFirstFileInSubfolders(f);
            if (!res.isEmpty())
                return res;
        }

        return "";
    }

    // рекурсивный поиск файлов в поддиректориях
    private static String findLastFileInFolder(String folder) {
        ArrayList<String> files = new ArrayList<>();

        if (!FileUtils.createFileList(folder, files))
            return "";

        if (!files.isEmpty())
            return files.get(files.size() - 1);

        return "";
    }

    // рекурсивный поиск файлов в поддиректориях
    @NonNull
    private static String findLastFileInSubfolders(String folder) {
        ArrayList<String> folders = new ArrayList<>();
        String res;

        if (!FileUtils.createFolderList(folder, folders))
            return "";

        String f;
        for (int i = folders.size() - 1; i >= 0; --i) {
            f = folders.get(i);

            res = findLastFileInSubfolders(f);
            if (!res.isEmpty())
                return res;

            res = findLastFileInFolder(f);
            if (!res.isEmpty())
                return res;
        }

        return "";
    }

    public static String getPrevFileInAllFiles() {
        if (currentPlayedSound.isEmpty())
            return "";

        ArrayList<String> fileList = new ArrayList<>();

        String folder = FileUtils.extractFilePath(currentPlayedSound);
        if (!FileUtils.createFileList(folder, fileList))
            return "";

        int currentindex = fileList.indexOf(currentPlayedSound);
        if (currentindex < 0)
            return "";

        // предыдущий файл в этой же папке
        if (currentindex - 1 >= 0) {
            return fileList.get(currentindex - 1);
        }

        // воспроизведение в текущей папке закончено
        ArrayList<String> folders = new ArrayList<>();
        String scanFolder = FileUtils.removeLastFolder(folder);
        String baseFolder = FileUtils.excludePathDelimiter(RTApplication.getDataBase().getBaseDirectory());
        int index, i;
        String res;

        while (!folder.equals(baseFolder)) {
            FileUtils.createFolderList(scanFolder, folders);
            index = folders.indexOf(folder);
            if (index >= 0) {
                i = index - 1;
                if (i >= 0) {
                    while (i >= 0) {
                        res = findLastFileInSubfolders(folders.get(i));
                        if (!res.isEmpty())
                            return res;

                        res = findLastFileInFolder(folders.get(i));
                        if (!res.isEmpty())
                            return res;
                        --i;
                    }
                }
                else {
                    res = findLastFileInFolder(scanFolder);
                    if (!res.isEmpty())
                        return res;
                }
            }

            folder = scanFolder;
            scanFolder = FileUtils.removeLastFolder(folder);
        }

        res = findLastFileInSubfolders(folder);
        if (!res.isEmpty())
            return res;

        res = findLastFileInFolder(folder);
        if (!res.isEmpty())
            return res;

        return "";
    }

    private static final ArrayList<String> recentFileList = new ArrayList<>();

    public static String getRandomFile() {

        if (currentPlayedSound.isEmpty())
            return "";

        ArrayList<String> fileList = new ArrayList<>();

        String folder = FileUtils.extractFilePath(currentPlayedSound);
        if (!FileUtils.createFileList(folder, fileList))
            return "";

        if (fileList.isEmpty())
            return "";

        if (fileList.size() == 1)
            return currentPlayedSound;

        recentFileList.add(currentPlayedSound);

        // максимальное количество песен, которые не будут повторяться
        int maxDepthRecentList = RTApplication.getDataBase().getMaxDepthRecent();

        if (fileList.size() <= maxDepthRecentList)
            maxDepthRecentList = fileList.size() - 1;

        // обрезаем список до нужного размера
        if (recentFileList.size() - maxDepthRecentList > 0)
            recentFileList.subList(0, recentFileList.size() - maxDepthRecentList).clear();

        String newName;
        int newIndex;
        do {
            newIndex = randomSystem.nextInt(fileList.size()); // максимум не входит в диапазон
            newName = fileList.get(newIndex);
        } while (recentFileList.contains(newName));

        return newName;
    }
}
