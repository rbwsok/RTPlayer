package su.rbws.rtplayer.service.soundplayer;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import su.rbws.rtplayer.RTApplication;
import su.rbws.rtplayer.Utils;

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

        String folder = Utils.extractFolderName(currentPlayedSound);
        if (!Utils.createFileList(folder, fileList))
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

        String folder = Utils.extractFolderName(currentPlayedSound);
        if (!Utils.createFileList(folder, fileList))
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

        String folder = Utils.extractFolderName(currentPlayedSound);
        if (!Utils.createFileList(folder, fileList))
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

        // файл в этой же папке
        if (currentindex + 1 < fileList.size()) {
            return fileList.get(currentindex + 1);
        }

        // воспроизведение в текущей папке закончено
        // 1. ищем вложенные папки
        // 2. если вложенных папок нет, то ищем папки в том же уровне
        // 3. перебираем папки в том же уровне от первой до текущей

        ///////////////////////////////////////////////////////////
        // 1. ищем вложенные папки
        ArrayList<String> folders = new ArrayList<>();
        if (!Utils.createFolderList(folder, folders))
            return "";

        if (!folders.isEmpty()) {
            for (int i = 0; i < folders.size(); ++i) {
                Utils.createFileList(folders.get(i), fileList);

                if (!fileList.isEmpty())
                    return fileList.get(0);
            }
        }

        ///////////////////////////////////////////////////////////
        // 2. перебираем папки в том же уровне от текущей до конца
        String scanfolder = Utils.removeLastFolder(folder);
        folders.clear();
        if (!Utils.createFolderList(scanfolder, folders))
            return "";

        int index = Utils.getIndex(folder, folders);
        for (int i = index + 1; i < folders.size(); ++i) {
            Utils.createFileList(folders.get(i), fileList);

            if (!fileList.isEmpty())
                return fileList.get(0);
        }

        ///////////////////////////////////////////////////////////
        // 3. перебираем папки в том же уровне от первой до текущей
        for (int i = 0; i < index + 1; ++i) {
            Utils.createFileList(folders.get(i), fileList);

            if (!fileList.isEmpty())
                return fileList.get(0);
        }
        return "";
    }

    public static String getNextFileInAllFiles2() {
        if (currentPlayedSound.isEmpty())
            return "";

        ArrayList<String> fileList = new ArrayList<>();

        String folder = Utils.extractFolderName(currentPlayedSound);
        if (!Utils.createFileList(folder, fileList))
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

        // файл в этой же папке
        if (currentindex + 1 < fileList.size()) {
            return fileList.get(currentindex + 1);
        }

        // воспроизведение в текущей папке закончено
        // 1. ищем вложенные папки
        // 2. если вложенных папок нет, то ищем папки в том же уровне
        // 3. перебираем папки в том же уровне от первой до текущей

        ///////////////////////////////////////////////////////////
        // 1. ищем вложенные папки
        String res = scanForFiles(folder);
        if (!res.isEmpty())
            return res;

        ///////////////////////////////////////////////////////////
        // 2. перебираем папки в том же уровне от текущей до конца
        String scanFolder = Utils.removeLastFolder(folder);
        ArrayList<String> folders = new ArrayList<>();
        if (!Utils.createFolderList(scanFolder, folders))
            return "";

        int index = Utils.getIndex(folder, folders);
        for (int i = index + 1; i < folders.size(); ++i) {
            res = scanForFiles(folders.get(i));
            if (!res.isEmpty())
                return res;
        }

        scanAndFindNextFile(RTApplication.getDataBase().getBaseDirectory(), currentPlayedSound);

        ///////////////////////////////////////////////////////////
        // 3. перебираем папки в том же уровне от первой до текущей
        for (int i = 0; i < index + 1; ++i) {
            res = scanForFiles(folders.get(i));
            if (!res.isEmpty())
                return res;
        }

        ///////////////////////////////////////////////////////////
        // 4. идем на уровень вверх

        folder = scanFolder;

        while (true) {
            if (folder.equals(RTApplication.getDataBase().getBaseDirectory()))
                break;

            scanFolder = Utils.removeLastFolder(folder);
            ///////////////////////////////////////////////////////////
            // 5. файлы в папке
            Utils.createFileList(scanFolder, fileList);
            if (!fileList.isEmpty())
                return fileList.get(0);

            ///////////////////////////////////////////////////////////
            // 6. перебираем папки в том же уровне от текущей до конца
            folders.clear();
            if (!Utils.createFolderList(scanFolder, folders))
                return "";

            index = Utils.getIndex(folder, folders);
            for (int i = index + 1; i < folders.size(); ++i) {
                Utils.createFileList(folders.get(i), fileList);

                if (!fileList.isEmpty())
                    return fileList.get(0);
            }

            ///////////////////////////////////////////////////////////
            // 7. перебираем папки в том же уровне от первой до текущей
            for (int i = 0; i < index + 1; ++i) {
                Utils.createFileList(folders.get(i), fileList);

                if (!fileList.isEmpty())
                    return fileList.get(0);
            }

            ///////////////////////////////////////////////////////////
            // 8. идем на уровень вверх

            folder = scanFolder;
        }
        return "";
/*
        ///////////////////////////////////////////////////////////
        // 1. ищем вложенные папки
        ArrayList<String> list = new ArrayList<>();
        if (!Utils.CreateFolderList(folder, list))
            return "";

        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); ++i) {
                Utils.CreateFileList(list.get(i), filelist);

                if (!filelist.isEmpty())
                    return filelist.get(0);
            }
        }

        ///////////////////////////////////////////////////////////
        // 2. перебираем папки в том же уровне от текущей до конца
        String scanfolder = Utils.RemoveLastFolder(folder);
        list = new ArrayList<>();
        if (!Utils.CreateFolderList(scanfolder, list))
            return "";

        int index = Utils.GetIndex(folder, list);
        for (int i = index + 1; i < list.size(); ++i) {
            Utils.CreateFileList(list.get(i), filelist);

            if (!filelist.isEmpty())
                return filelist.get(0);
        }

        ///////////////////////////////////////////////////////////
        // 3. перебираем папки в том же уровне от первой до текущей
        list.clear();
        if (!Utils.CreateFolderList(scanfolder, list))
            return "";

        index = Utils.GetIndex(folder, list);
        for (int i = 0; i < index + 1; ++i) {
            Utils.CreateFileList(list.get(i), filelist);

            if (!filelist.isEmpty())
                return filelist.get(0);
        }
        return "";*/
    }

    // рекурсивный поиск файлов
    private static String scanForFiles(String folder) {
        ArrayList<String> folders = new ArrayList<>();
        ArrayList<String> files = new ArrayList<>();

        if (!Utils.createFolderList(folder, folders))
            return "";

        for (String f: folders) {
            Utils.createFileList(f, files);
            if (!files.isEmpty())
                return files.get(0);

            String res = scanForFiles(f);
            if (!res.isEmpty())
                return res;
        }

        return "";
    }

    // поиск следующего файла (через построение полного списка файлов)
    private static String scanAndFindNextFile(String folder, String filename) {
        ArrayList<String> folders = new ArrayList<>();
        ArrayList<String> files = new ArrayList<>();

        if (!Utils.createFolderList(folder, folders))
            return "";

        boolean find = false;
        int pos;

        for (String f: folders) {
            Utils.createFileList(f, files);

            if (find = true && files.size() > 0)
                return files.get(0);

            pos = files.indexOf(filename);
            if (pos >= 0) {
                // в той же папке
                if (pos < files.size() - 1)
                    return files.get(pos + 1);
                find = true;
            }

            String res = scanAndFindNextFile(f, filename);
            if (!res.isEmpty())
                return res;
        }

        return "";
    }

    public static String getPrevFileInAllFiles() {
        if (currentPlayedSound.isEmpty())
            return "";

        ArrayList<String> fileList = new ArrayList<>();

        String folder = Utils.extractFolderName(currentPlayedSound);
        if (!Utils.createFileList(folder, fileList))
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

        // файл в этой же папке
        if (currentindex - 1 >= 0) {
            return fileList.get(currentindex - 1);
        }

        // воспроизведение в текущей папке закончено
        // 1. ищем вложенные папки
        // 2. если вложенных папок нет, то ищем папки в том же уровне
        // 3. перебираем папки в том же уровне от последней до текущей

        ///////////////////////////////////////////////////////////
        // 1. ищем вложенные папки
        ArrayList<String> list = new ArrayList<>();
        if (!Utils.createFolderList(folder, list))
            return "";

        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); ++i) {
                Utils.createFileList(list.get(i), fileList);

                if (!fileList.isEmpty())
                    return fileList.get(fileList.size() - 1);
            }
        }

        ///////////////////////////////////////////////////////////
        // 3. перебираем папки в том же уровне от текущей до первой
        String scanFolder = Utils.removeLastFolder(folder);
        list = new ArrayList<>();
        if (!Utils.createFolderList(scanFolder, list))
            return "";

        int index = Utils.getIndex(folder, list);
        for (int i = index - 1; i >= 0; --i) {
            Utils.createFileList(list.get(i), fileList);

            if (!fileList.isEmpty())
                return fileList.get(fileList.size() - 1);
        }

        ///////////////////////////////////////////////////////////
        // 3. перебираем папки в том же уровне от последней до текущей
        list.clear();
        if (!Utils.createFolderList(scanFolder, list))
            return "";

        index = Utils.getIndex(folder, list);
        for (int i = list.size() - 1; i >= index; --i) {
            Utils.createFileList(list.get(i), fileList);

            if (!fileList.isEmpty())
                return fileList.get(fileList.size() - 1);
        }

        return "";
    }

    private static ArrayList<String> recentFileList = new ArrayList<>();

    public static String getRandomFile() {

        if (currentPlayedSound.isEmpty())
            return "";

        ArrayList<String> fileList = new ArrayList<>();

        String folder = Utils.extractFolderName(currentPlayedSound);
        if (!Utils.createFileList(folder, fileList))
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
        if (recentFileList.size() > maxDepthRecentList) {
            for (int i = 0; i < recentFileList.size() - maxDepthRecentList; ++i) {
                recentFileList.remove(0);
            }
        }

        String newName;
        int newIndex;
        while (true) {
            newIndex = randomSystem.nextInt(fileList.size()); // максимум не входит в диапазон
            newName = fileList.get(newIndex);
            if (!recentFileList.contains(newName))
                break;
        }

        return newName;
    }
}
