package su.rbws.rtplayer;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class Utils {

    // проверка на число
    public static boolean isDigit(String s) throws NumberFormatException {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // из пути к файлу выкусывает путь без имени фпайла
    public static String extractFolderName(String path) {
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

    // из пути к файлу выкусывает имя файла без пути
    public static String extractFileName(@NonNull String path) {
        int p = path.lastIndexOf('/');
        if (p >= 0)
            path = path.substring(p + 1);

        return path;
    }

    // из пути выкусывает последнее имя
    public static String extractLastFolderName(String path) {
        String result = path;
        if (result.charAt(result.length() - 1) == '/')
            result = result.substring(0, result.length() - 1);

        int p = result.lastIndexOf('/');
        if (p >= 0)
            result = result.substring(p + 1);

        return result;
    }


    // из пути к файлу выкусывает имя файла без пути и без разширения
    public static String extractFileNameNoExt(@NonNull String path) {
        int p = path.lastIndexOf('/');
        if (p >= 0)
            path = path.substring(p + 1);

        int t = path.lastIndexOf('.');
        if (t >= 0)
            path = path.substring(0, t);

        return path;
    }

    // из пути к файлу выкусывает hfcibhtybt файла без пути
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

    public static boolean fileExists(String path) {
        File f = new File(path);
        if (f.isDirectory())
            return false;

        return f.exists();
    }

    public static boolean directoryExists(String path) {
        File f = new File(path);
        if (!f.isDirectory())
            return false;

        return f.exists();
    }

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
                if (fileext.equals(".mp3"))
                    fileList.add(file.getAbsolutePath());
            }
        }

        Collections.sort(fileList);
        return true;
    }

    public static int getIndex(String filename, @NonNull ArrayList<String> list) {
        for (int i = 0; i < list.size(); ++i) {
            if (filename.equals(list.get(i)))
                return i;
        }
        return -1;
    }

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

    public static boolean ScanDirectory(String folder, ArrayList<String> filelist) {
        File f = new File(folder);
        if (!f.isDirectory())
            return false;

        File[] list = f.listFiles();
        if (list == null)
            return false;

        for (File file : list) {
            if (!file.isDirectory())
                filelist.add(file.getName());
        }

        return true;
    }

    public static String includePathDelimiter(String path) {
        String result = "/";
        if (!path.isEmpty()) {
            result = path;
            if (result.charAt(result.length() - 1) != '/')
                result = result + '/';
        }

        return result;
    }

    public static String excludePathDelimiter(String path) {
        String result = path;
        if (!result.isEmpty()) {
            if (result.charAt(result.length() - 1) == '/')
                result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    public static int parseInt(String value) {
        int result;
        try {
            result = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            result = 0;
        }
        return result;
    }

}