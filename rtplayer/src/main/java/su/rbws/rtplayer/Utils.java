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

    public static int getIndex(String filename, @NonNull ArrayList<String> list) {
        for (int i = 0; i < list.size(); ++i) {
            if (filename.equals(list.get(i)))
                return i;
        }
        return -1;
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