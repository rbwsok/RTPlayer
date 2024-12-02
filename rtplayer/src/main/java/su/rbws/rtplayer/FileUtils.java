package su.rbws.rtplayer;

// Фаловые утилиты
// работа с путями, папками и файлами

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class FileUtils {
    // добавление разделителя в конец пути
    public static String includePathDelimiter(@NonNull String path) {
        String result = "/";
        if (!path.isEmpty()) {
            result = path;
            if (result.charAt(result.length() - 1) != '/')
                result = result + '/';
        }

        return result;
    }

    // удаление разделителя в конец пути
    @NonNull
    public static String excludePathDelimiter(String path) {
        String result = path;
        if (!result.isEmpty()) {
            if (result.charAt(result.length() - 1) == '/')
                result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    // проверка существование файла
    public static boolean fileExists(String path) {
        File f = new File(path);
        if (f.isDirectory())
            return false;

        return f.exists();
    }

    // проверка существование папки
    public static boolean directoryExists(String path) {
        File f = new File(path);
        if (!f.isDirectory())
            return false;

        return f.exists();
    }

    // из пути к файлу выкусывает путь без имени фпайла
    public static String extractFilePath(String path) {
        String result = path;
        if (!result.isEmpty()) {
            if (result.charAt(result.length() - 1) == '/')
                result = result.substring(0, result.length() - 1);

            int p = result.lastIndexOf('/');
            if (p >= 0)
                result = result.substring(0, p);
        }
        return result;
    }

    // из пути к файлу выкусывает имя файла без пути (и расширения)
    public static String extractFileName(@NonNull String path, boolean includeExtension) {
        int p = path.lastIndexOf('/');
        if (p >= 0)
            path = path.substring(p + 1);

        if (!includeExtension) {
            int t = path.lastIndexOf('.');
            if (t >= 0)
                path = path.substring(0, t);
        }

        return path;
    }

    public static String extractFileName(@NonNull String path) {
        return extractFileName(path, true);
    }

    // из пути выкусывает последнее имя
    public static String extractLastFolder(String path) {
        String result = path;
        if (result.charAt(result.length() - 1) == '/')
            result = result.substring(0, result.length() - 1);

        int p = result.lastIndexOf('/');
        if (p >= 0)
            result = result.substring(p + 1);

        return result;
    }

    // из пути к файлу выкусывает расширение файла без пути
    public static String extractFileExt(@NonNull String path) {
        String result = "";

        int t = path.lastIndexOf('.');
        if (t >= 0)
            result = path.substring(t);

        return result;
    }

    // из пути убирает последнюю папку
    @NonNull
    public static String removeLastFolder(String path) {
        String result = path;

        if (!result.isEmpty()) {
            if (result.charAt(result.length() - 1) == '/')
                result = result.substring(0, result.length() - 1);

            int p = result.lastIndexOf('/');
            if (p >= 0)
                result = result.substring(0, p);

            if (result.isEmpty())
                result = "/";
            else
            if (result.charAt(result.length() - 1) == '/')
                result = result.substring(0, result.length() - 1);
        }

        return excludePathDelimiter(result);
    }

    // возвращает список файлов в папке
    public static boolean createFileList(String path, ArrayList<String> fileList) {
        File f = new File(path);
        if (!f.isDirectory())
            return false;

        File[] list = f.listFiles();
        if (list == null)
            return false;

        fileList.clear();

        String fileext;
        for (File file : list) {
            if (!file.isDirectory()) {
                fileext = extractFileExt(file.getAbsolutePath());
                if (fileext.equalsIgnoreCase(".mp3") ||
                        fileext.equalsIgnoreCase(".ogg") ||
                        fileext.equalsIgnoreCase(".flac"))
                    fileList.add(file.getAbsolutePath());
            }
        }

        Collections.sort(fileList);
        return true;
    }

    // возвращает список подпапок в папке
    public static boolean createFolderList(String path, ArrayList<String> fileList) {
        File f = new File(path);
        if (!f.isDirectory())
            return false;

        File[] list = f.listFiles();
        if (list == null)
            return false;

        fileList.clear();

        for (File file : list) {
            if (file.isDirectory()) {
                fileList.add(file.getAbsolutePath());
            }
        }

        Collections.sort(fileList);
        return true;
    }
}
