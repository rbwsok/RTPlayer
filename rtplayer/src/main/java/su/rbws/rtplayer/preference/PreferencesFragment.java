package su.rbws.rtplayer.preference;

import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import java.io.File;
import java.util.Map;

import su.rbws.rtplayer.R;
import su.rbws.rtplayer.RTApplication;
import su.rbws.rtplayer.Utils;
import su.rbws.rtplayer.FileUtils;

public class PreferencesFragment extends PreferenceFragmentCompat {

    AppCompatActivity ParentActivity;

    public PreferencesFragment(AppCompatActivity activity) {
        ParentActivity = activity;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private String getListValueFromEntries(@NonNull PreferencesAbstract.PreferenceItem item) {
        String keyvalue = item.value;

        if (keyvalue.isEmpty())
            keyvalue = item.defaultValue;

        String result = "";

        try {
            String[] entries_arr = RTApplication.getContext().getResources().getStringArray(item.entries);
            String[] entryval_arr = RTApplication.getContext().getResources().getStringArray(item.entryValues);

            for (int i = 0; i < entryval_arr.length; i++) {
                if (entryval_arr[i].equals(keyvalue)) {
                    result = entries_arr[i];
                    break;
                }
            }
        } catch (Exception e) {
        }

        return result;
    }

    private void createItems() {
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(ParentActivity);
        this.setPreferenceScreen(preferenceScreen);

        ListPreference listPreference;
        EditTextPreference editPreference;

        for (Map.Entry<String, PreferencesAbstract.PreferenceItem> entry : RTApplication.getPreferencesData().data.entrySet()) {
            PreferencesAbstract.PreferenceItem item = entry.getValue();
            if (item.value == null)
                item.value = item.defaultValue;

            if (!item.isShow)
                continue;

            switch (item.preferenceType) {
                case ptNone:
                case ptToolbarPosition:
                case ptRepeatMode:
                case ptPlayOnStartMode:
                case ptTitleFileInfo:
                case ptSubTitleFileInfo:
                case ptInterruptAction:
                case ptBackgroundMode:
                    listPreference = new ListPreference(ParentActivity);
                    listPreference.setKey(item.name);
                    listPreference.setTitle(item.title);
                    listPreference.setDefaultValue(item.defaultValue);
                    if (item.entries > 0 && item.entryValues > 0) {
                        listPreference.setEntries(item.entries);
                        listPreference.setEntryValues(item.entryValues);
                    }
                    preferenceScreen.addPreference(listPreference);
                    break;
                case ptInt:
                case ptMusicFolder:
                case ptAdditionalMusicFolder:
                    editPreference = new EditTextPreference(ParentActivity);
                    editPreference.setKey(item.name);
                    editPreference.setTitle(item.title);
                    editPreference.setDefaultValue(item.defaultValue);
                    preferenceScreen.addPreference(editPreference);
                    break;
            } // switch (item.preferenceType)
        } // for(Map.Entry<String, DataBaseAbstract.PreferenceItem> entry : RTApplication.getDataBase().data.entrySet())
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        createItems();

        updateSummary();
    }

    public void updateSummary() {
        ListPreference listPreference;
        EditTextPreference editPreference;
        String valueString;

        for (Map.Entry<String, PreferencesAbstract.PreferenceItem> entry : RTApplication.getPreferencesData().data.entrySet()) {
            PreferencesAbstract.PreferenceItem item = entry.getValue();
            if (item.value == null)
                item.value = item.defaultValue;

            if (!item.isShow)
                continue;

            switch (item.preferenceType) {
                case ptNone:
                case ptToolbarPosition:
                case ptRepeatMode:
                case ptPlayOnStartMode:
                case ptTitleFileInfo:
                case ptSubTitleFileInfo:
                case ptInterruptAction:
                case ptBackgroundMode:
                    if (item.entries > 0 && item.entryValues > 0) {
                        listPreference = findPreference(item.name);
                        valueString = getListValueFromEntries(item);
                        if (listPreference != null)
                            listPreference.setSummary(valueString);
                    }
                    break;
                case ptInt:
                    item.value = Integer.toString(Utils.parseInt(item.value));
                    editPreference = findPreference(item.name);
                    if (editPreference != null)
                        editPreference.setSummary(item.value);
                    break;
                case ptMusicFolder:
                    valueString = item.value;
                    if (valueString.isEmpty() || valueString.equals("/") ||
                        !FileUtils.directoryExists(valueString))
                        valueString = "";

                    if (valueString.isEmpty()) {
                        File extp = Environment.getExternalStorageDirectory();
                        valueString = extp.getAbsolutePath();
                    }

                    editPreference = findPreference(item.name);
                    if (editPreference != null)
                        editPreference.setSummary(valueString);
                    break;
                case ptAdditionalMusicFolder:
                    valueString = item.value;
                    if (!valueString.isEmpty() && !FileUtils.directoryExists(valueString))
                        valueString = "";

                    if (valueString.isEmpty())
                        valueString = RTApplication.getContext().getString(R.string.additional_folder_absent);

                    editPreference = findPreference(item.name);
                    if (editPreference != null)
                        editPreference.setSummary(valueString);
                    break;
            } // switch (item.preferenceType)
        } // for(Map.Entry<String, DataBaseAbstract.PreferenceItem> entry : RTApplication.getDataBase().data.entrySet())
    }

    @Override
    public void onDisplayPreferenceDialog(@NonNull Preference preference) {
        if (preference.getKey().equals(PreferencesData.PREFERENCE_NAME_DEFAULT)) {
            RTApplication.getPreferencesData().putAllDefaultPreferences();

            Toast.makeText(ParentActivity, getResources().getString(R.string.set_default_params), Toast.LENGTH_SHORT).show();
        }
        else
        if (preference.getKey().equals(PreferencesData.PREFERENCE_NAME_REMAP_KEYS)) {
            ((PreferencesActivity)ParentActivity).showMediaButtonsDialog();
        }
        else
        if (preference.getKey().equals(PreferencesData.PREFERENCE_NAME_FTP_SERVER)) {
            ((PreferencesActivity)ParentActivity).showFTPDialog();
        }
        else
            super.onDisplayPreferenceDialog(preference);
    }
}