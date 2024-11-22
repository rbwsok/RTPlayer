package su.rbws.rtplayer.preference;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.View;
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

        ImageView backImageView;
        FrameLayout preferenceFrameLayout;

        RTApplication.getPreferencesData().sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        View.OnClickListener imageBackClick = v -> PreferencesActivity.super.getOnBackPressedDispatcher().onBackPressed();

        backImageView = findViewById(R.id.BackUpImageView);
        backImageView.setOnClickListener(imageBackClick);

        preferenceFrameLayout = findViewById(R.id.PreferenceFrameLayout);
        preferencesFragment = new PreferencesFragment(this);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(preferenceFrameLayout.getId(), preferencesFragment);
        ft.commit();

        Utils.setImageBackground(this, this.findViewById(R.id.main));

        linearLayout = findViewById(R.id.linearlayout);

        captionTextView = findViewById(R.id.caption_preferences);
        captionTextView.setText(RTApplication.getContext().getString(R.string.preferences) + " (" + RTApplication.getContext().getString(R.string.version) + " " + RTApplication.version +
                      ")");

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)linearLayout.getLayoutParams();
        params.topMargin = RTApplication.getPreferencesData().getTopSpace();
        params.leftMargin = RTApplication.ScreenAuto.INSETS_LEFT -
                RTApplication.ScreenAuto.PADDING_LEFT +
                RTApplication.getPreferencesData().getLeftSpace();
        params.rightMargin = RTApplication.getPreferencesData().getRightSpace();
        params.bottomMargin = RTApplication.getPreferencesData().getBottomSpace();
        linearLayout.setLayoutParams(params);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(RTApplication.ScreenAuto.PADDING_LEFT,
                    RTApplication.ScreenAuto.PADDING_TOP,
                    RTApplication.ScreenAuto.PADDING_RIGHT,
                    RTApplication.ScreenAuto.PADDING_BOTTOM
            );

            return insets;

        });
    }

    @Override
    public void onSharedPreferenceChanged(@NonNull SharedPreferences sharedPreferences, String key) {
        String value = sharedPreferences.getString(key, "");
        RTApplication.getPreferencesData().set(key, value);

        preferencesFragment.updateSummary();

        if (key.equals(PreferencesData.PREFERENCE_NAME_TOP_SPACE)) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)linearLayout.getLayoutParams();
            params.topMargin = Utils.parseInt(sharedPreferences.getString(key, "0"));
            linearLayout.setLayoutParams(params);
        }

        if (key.equals(PreferencesData.PREFERENCE_NAME_LEFT_SPACE)) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)linearLayout.getLayoutParams();
            params.leftMargin = Utils.parseInt(sharedPreferences.getString(key, "0"));
            linearLayout.setLayoutParams(params);
        }

        if (key.equals(PreferencesData.PREFERENCE_NAME_RIGHT_SPACE)) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)linearLayout.getLayoutParams();
            params.rightMargin = Utils.parseInt(sharedPreferences.getString(key, "0"));
            linearLayout.setLayoutParams(params);
        }

        if (key.equals(PreferencesData.PREFERENCE_NAME_BOTTOM_SPACE)) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)linearLayout.getLayoutParams();
            params.bottomMargin = Utils.parseInt(sharedPreferences.getString(key, "0"));
            linearLayout.setLayoutParams(params);
        }

        if (key.equals(PreferencesData.PREFERENCE_NAME_BACKGROUND_IMAGE)) {
            Utils.setImageBackground(this, this.findViewById(R.id.main));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        RTApplication.getPreferencesData().putAllPreferences();
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
