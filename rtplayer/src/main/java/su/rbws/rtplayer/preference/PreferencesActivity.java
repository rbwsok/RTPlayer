package su.rbws.rtplayer.preference;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.content.SharedPreferences;
import android.widget.LinearLayout;
import android.widget.TextView;

import su.rbws.rtplayer.IDialogButtonInterface;
import su.rbws.rtplayer.FTPDialog;
import su.rbws.rtplayer.MediaButtonsDialog;
import su.rbws.rtplayer.R;
import su.rbws.rtplayer.RTApplication;
import su.rbws.rtplayer.SelectMediaButtonActionDialog;
import su.rbws.rtplayer.Utils;
import su.rbws.rtplayer.service.MediaButtonsMapper;

// https://developer.alexanderklimov.ru/android/preferences_framework.php

public class PreferencesActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, IDialogButtonInterface {
    PreferencesFragment preferencesFragment;

    TextView captionTextView;

    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_preferences);

        //CreateItems();

        ImageView backImageView;
        FrameLayout preferenceFrameLayout;

        RTApplication.getDataBase().sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        View.OnClickListener imageBackClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferencesActivity.super.onBackPressed();
            }
        };
        backImageView = findViewById(R.id.BackUpImageView);
        backImageView.setOnClickListener(imageBackClick);

        preferenceFrameLayout = findViewById(R.id.PreferenceFrameLayout);
        preferencesFragment = new PreferencesFragment(this);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(preferenceFrameLayout.getId(), preferencesFragment);
        ft.commit();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getColor(R.color.BackgroundColor));
            getWindow().setStatusBarColor(getColor(R.color.BackgroundColor));
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        setFullscreen();

        linearLayout = findViewById(R.id.linearlayout);

        Rect rectangle = new Rect();
        Window window = this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);

        captionTextView = findViewById(R.id.caption_preferences);
        /*captionTextView.setText(getString(R.string.preferences) + " (" + getString(R.string.version) + " " + RTApplication.version + " " +
                getString(R.string.window_size) + " " + Integer.toString(rectangle.width()) + "x" + Integer.toString(rectangle.height()) +
                ")");*/
        captionTextView.setText(RTApplication.getContext().getString(R.string.preferences) + " (" + RTApplication.getContext().getString(R.string.version) + " " + RTApplication.version +
      //           " " + getString(R.string.window_size) + " " + Integer.toString(rectangle.width()) + "x" + Integer.toString(rectangle.height()) +
                ")");

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)linearLayout.getLayoutParams();
        params.topMargin = RTApplication.getDataBase().getTopSpace();
        params.leftMargin = RTApplication.getDataBase().getLeftSpace();
        params.rightMargin = RTApplication.getDataBase().getRightSpace();
        params.bottomMargin = RTApplication.getDataBase().getBottomSpace();
        linearLayout.setLayoutParams(params);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
    }

    private void setFullscreen() {
        ConstraintLayout mainLayout;
        mainLayout = findViewById(R.id.main);

        int backgroundMode = RTApplication.getDataBase().getBackgroundMode();
        if (backgroundMode == 0) {
            getWindow().getDecorView().setSystemUiVisibility(0);
            Drawable drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.background);
            mainLayout.setBackground(drawable);
        }
        else
        if (backgroundMode == 1) {
            // fullscreen
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);

            Drawable drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.background_1920);
            mainLayout.setBackground(drawable);
        }
    }

    @Override
    public void onSharedPreferenceChanged(@NonNull SharedPreferences sharedPreferences, String key) {
        String value = sharedPreferences.getString(key, "");
        RTApplication.getDataBase().set(key, value);

        preferencesFragment.updateSummary();

        if (key.equals(DataBasePreferences.PREFERENCE_NAME_TOP_SPACE)) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)linearLayout.getLayoutParams();
            params.topMargin = Utils.parseInt(sharedPreferences.getString(key, "0"));
            linearLayout.setLayoutParams(params);
        }

        if (key.equals(DataBasePreferences.PREFERENCE_NAME_LEFT_SPACE)) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)linearLayout.getLayoutParams();
            params.leftMargin = Utils.parseInt(sharedPreferences.getString(key, "0"));
            linearLayout.setLayoutParams(params);
        }

        if (key.equals(DataBasePreferences.PREFERENCE_NAME_RIGHT_SPACE)) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)linearLayout.getLayoutParams();
            params.rightMargin = Utils.parseInt(sharedPreferences.getString(key, "0"));
            linearLayout.setLayoutParams(params);
        }

        if (key.equals(DataBasePreferences.PREFERENCE_NAME_BOTTOM_SPACE)) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)linearLayout.getLayoutParams();
            params.bottomMargin = Utils.parseInt(sharedPreferences.getString(key, "0"));
            linearLayout.setLayoutParams(params);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        RTApplication.getDataBase().putAllPreferences();
    }

    public void showFTPDialog() {
        DialogFragment dialog = FTPDialog.newInstance();
        dialog.show(getSupportFragmentManager(), "msg");
    }

    MediaButtonsDialog mediaButtonsDialog;
    public void showMediaButtonsDialog() {
        mediaButtonsDialog = MediaButtonsDialog.newInstance(this);
        mediaButtonsDialog.show(getSupportFragmentManager(), "msg");
    }

    SelectMediaButtonActionDialog selectMediaButtonActionDialog;
    public void showSelectMediaButtonActionsDialog(MediaButtonsMapper.MediaButton mediaButton) {
        selectMediaButtonActionDialog = SelectMediaButtonActionDialog.newInstance(mediaButton);
        selectMediaButtonActionDialog.show(getSupportFragmentManager(), "msg");
    }

    @Override
    public void onDialogButtonClickListener(int action, Object param) {
        // возврат типа действия
        if (action == 3 && mediaButtonsDialog != null) {
            mediaButtonsDialog.updateRecyclerView();
        }
    }

}
