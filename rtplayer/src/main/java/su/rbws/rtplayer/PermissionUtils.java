package su.rbws.rtplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

public class PermissionUtils {

    public final static int request_MANAGE_EXTERNAL_STORAGE = 3;
    public final static int request_WAKE_LOCK = 4;
    public final static int request_INTERNET = 5;

    public static boolean hasPermissions(Context context) {
        int permission;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        else {
            permission = ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED)
                return false;

            permission = ActivityCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED)
                return false;
        }

        permission = ActivityCompat.checkSelfPermission(context, android.Manifest.permission.WAKE_LOCK);
        if (permission != PackageManager.PERMISSION_GRANTED)
            return false;

        permission = ActivityCompat.checkSelfPermission(context, android.Manifest.permission.INTERNET);
        if (permission != PackageManager.PERMISSION_GRANTED)
            return false;

        return true;
    }

    public static void requestPermissions(Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", activity.getPackageName())));
                ActivityCompat.startActivityForResult(activity, intent, request_MANAGE_EXTERNAL_STORAGE, null);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                ActivityCompat.startActivityForResult(activity, intent, request_MANAGE_EXTERNAL_STORAGE, null);
            }
        } else {
            ActivityCompat.requestPermissions(activity, new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, request_MANAGE_EXTERNAL_STORAGE);
        }

        ActivityCompat.requestPermissions(activity, new String[] {android.Manifest.permission.WAKE_LOCK}, request_WAKE_LOCK);

        ActivityCompat.requestPermissions(activity, new String[] {android.Manifest.permission.INTERNET}, request_INTERNET);
    }

}
