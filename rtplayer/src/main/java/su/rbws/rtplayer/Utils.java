package su.rbws.rtplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

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

    public static int parseInt(String value) {
        int result;
        try {
            result = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            result = 0;
        }
        return result;
    }

    public static void setFullscreen(AppCompatActivity activity, ConstraintLayout mainLayout) {

        boolean isFullscreen = RTApplication.getDataBase().getBackgroundMode() == 1;
        int backgroundImage = RTApplication.getDataBase().getBackgroundImage();
        int backgroundImageResource = 0;

        switch (backgroundImage) {
            case 0:
                backgroundImageResource = R.drawable.background_classic_1920;
                break;
            case 1:
                backgroundImageResource = R.drawable.background_style_1920;
                break;
            case 2:
                backgroundImageResource = R.drawable.background_sport_1920;
                break;
            case 3:
                backgroundImageResource = R.drawable.background_style_2_1920;
                break;
            case 4:
                backgroundImageResource = R.drawable.background_add_1_1920;
                break;
        }

        Drawable drawable = null;
        if (!isFullscreen) {
            Bitmap src = BitmapFactory.decodeResource(activity.getResources(), backgroundImageResource);
            Bitmap dst = Bitmap.createBitmap(src, 285, 0, 1460, 720);
            drawable = new BitmapDrawable(activity.getResources(), dst);
        }
        else {
            Bitmap bmp1 = BitmapFactory.decodeResource(activity.getResources(), backgroundImageResource);
            Bitmap bmp2 = BitmapFactory.decodeResource(activity.getResources(), R.drawable.controlbuttons);
            Bitmap bmp = bmp1.copy(Bitmap.Config.ARGB_8888, true); // нужен mutable bitmap
            Canvas canvas = new Canvas(bmp);
            canvas.drawBitmap(bmp2, 0, bmp1.getHeight() - bmp2.getHeight(), null);
            drawable = new BitmapDrawable(activity.getResources(), bmp);
        }
        mainLayout.setBackground(drawable);

        activity.getWindow().setNavigationBarColor(activity.getColor(R.color.BackgroundColor));
        activity.getWindow().setStatusBarColor(activity.getColor(R.color.BackgroundColor));

        if (!isFullscreen) {
            activity.getWindow().getDecorView().setSystemUiVisibility(0);
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
        else
        {
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE
            );

           // activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

}