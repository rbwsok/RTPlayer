package su.rbws.rtplayer;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
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

    public static void setImageBackground(@NonNull AppCompatActivity activity, ConstraintLayout mainLayout) {
        activity.getWindow().setNavigationBarColor(activity.getColor(R.color.transparent));
        activity.getWindow().setStatusBarColor(activity.getColor(R.color.transparent));

        activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        int backgroundImage = RTApplication.getDataBase().getBackgroundImage();
        int backgroundImageResource = 0;

        switch (backgroundImage) {
            case 0:
                backgroundImageResource = R.drawable.background_classic_1_1920;
                break;
            case 1:
                backgroundImageResource = R.drawable.background_classic_2_1920;
                break;
            case 2:
                backgroundImageResource = R.drawable.background_style_1_1920;
                break;
            case 3:
                backgroundImageResource = R.drawable.background_style_2_1920;
                break;
            case 4:
                backgroundImageResource = R.drawable.background_stream_1_1920;
                break;
            case 5:
                backgroundImageResource = R.drawable.background_stream_2_1920;
                break;
        }
        Drawable drawable = AppCompatResources.getDrawable(RTApplication.getContext(), backgroundImageResource);
        mainLayout.setBackground(drawable);
        //mainLayout.setBackgroundColor(0xffff0000);
    }
}