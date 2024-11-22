package su.rbws.rtplayer;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.content.Context;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.Space;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import su.rbws.rtplayer.preference.PreferencesActivity;
import su.rbws.rtplayer.service.soundplayer.SoundSystemAbstract;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, IDialogButtonInterface {
    RecyclerView filesRecyclerView;
    ImageView shuffleLeft, shuffleRight, shuffleUp, shuffleDown;
    ImageView playPauseImageView, nextSoundImageView, prevSoundImageView;
    TextView positionTextView, durationSoundTextView;
    SeekBar soundSeekBar;
    Thread seekBarThread;
    SoundsRecyclerAdapter adapter;

    MediaServiceLink serviceLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // расположение activity на дисплее
        // -- 288 -- 1536 -- 96 --

        if (!PermissionUtils.hasPermissions(this))
            PermissionUtils.requestPermissions(this);

        serviceLink = new MediaServiceLink(this,
                new MediaServiceLink.IMediaServiceLinkInterface() {
                    @Override
                    public void onServiceConnect() {
                        if (serviceLink.isMediaServiceReady()) {
                            filesRecyclerView.setAdapter(adapter);

                            if (!applyNoShowPreferences()) {
                                RTApplication.getSoundSourceManager().createViewableRootList();
                                adapter.update(RTApplication.getSoundSourceManager().getViewableList());
                            }

                            filesRecyclerView.addOnScrollListener(scrollListener);

                            serviceLink.getMediaButtonsMapper();
                        }
                    }

                    @Override
                    public void onPlaybackStateChanged(PlaybackStateCompat state) {
                        updateUI(state);
                    }
                }
        );

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        setupUIElements();

        applyPreferences();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {

            v.setPadding(RTApplication.ScreenAuto.PADDING_LEFT,
                    RTApplication.ScreenAuto.PADDING_TOP,
                    RTApplication.ScreenAuto.PADDING_RIGHT,
                    RTApplication.ScreenAuto.PADDING_BOTTOM
                    );

            return insets;
        });
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent ) {
            String action = intent.getAction();
            if (action == null)
                return;
            // уведомление от службы об окончании воспроизведения трека
/*            if (intent.getAction().equals(SoundSystemAbstract.ACTION_SOUNDCOMPLETE)) {

            }*/

            // уведомление от службы о старте трека
            if (action.equals(SoundSystemAbstract.ACTION_PLAYFILE)) {
                if (serviceLink.isMediaServiceReady()) {
                    if (RTApplication.getSoundSourceManager().isFile(serviceLink.getCurrentPlayedSound())) {
                        // seek bar
                        long position, duration;
                        position = serviceLink.getPosition();
                        duration = serviceLink.getDuration();
                        positionTextView.setText(timeFormat(position));
                        durationSoundTextView.setText(timeFormat(duration));
                        soundSeekBar.setMin(0);
                        soundSeekBar.setMax((int) duration);
                        soundSeekBar.setProgress((int) position);

                        String lastFile = RTApplication.getPreferencesData().getLastPlayedFile();
                        if (!lastFile.isEmpty()) {
                            String newFile = serviceLink.getCurrentPlayedSound();
                            lastFile = FileUtils.extractFilePath(lastFile);
                            newFile = FileUtils.extractFilePath(newFile);

                            if (!lastFile.equals(newFile)) {
                                RTApplication.getSoundSourceManager().createViewableList(newFile);
                                adapter.update(RTApplication.getSoundSourceManager().getViewableList());
                            }
                        }

                        RTApplication.getPreferencesData().setLastPlayedFile(serviceLink.getCurrentPlayedSound());

                        // рекуклер
                        int selPosition = adapter.getCurrentPosition();
                        if (selPosition >= 0) {
                            LinearLayoutManager lm = (LinearLayoutManager) filesRecyclerView.getLayoutManager();
                            if (lm != null)
                                lm.scrollToPosition(selPosition);
                        }
                    }
                    else {
                        clearSeekBar();

                        RTApplication.getPreferencesData().setLastPlayedFile(serviceLink.getCurrentPlayedSound());
                        // рекуклер
                        int selPosition = adapter.getCurrentPosition();
                        if (selPosition >= 0) {
                            LinearLayoutManager lm = (LinearLayoutManager) filesRecyclerView.getLayoutManager();
                            if (lm != null)
                                lm.scrollToPosition(selPosition);
                        }
                    }
                }
            }
        }
    };

    boolean firstResume = true;

    @Override
    protected void onResume() {
        super.onResume();

        Log.i("rtplayer_tag", "onResume");

        applyPreferences();

        if (!RTApplication.getSoundSourceManager().getViewableList().isEmpty()) {
            if (firstResume)
                RTApplication.getSoundSourceManager().createViewableList(RTApplication.getPreferencesData().getBaseDirectory());
            adapter.update(RTApplication.getSoundSourceManager().getViewableList());
        }
        else {
            if (!firstResume)
                Toast.makeText(getApplicationContext(), "Папка с музыкой пустая или отсутствует", Toast.LENGTH_SHORT).show();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(SoundSystemAbstract.ACTION_SOUNDCOMPLETE); // Можно добавить больше действий
        filter.addAction(SoundSystemAbstract.ACTION_PLAYFILE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            registerReceiver(broadcastReceiver, filter, RECEIVER_EXPORTED);
        else
            registerReceiver(broadcastReceiver, filter);

        firstResume = false;
    }

    @Override
    protected void onPause() {
        RTApplication.getPreferencesData().putAllPreferences();
        super.onPause();

        Log.i("rtplayer_tag", "onPause");

        unregisterReceiver(broadcastReceiver);
    }

    int z;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 3) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (PermissionUtils.hasPermissions(this)) {
                    z = 1;
                } else {
                    z = 0;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                     @NonNull int[] grantResults) {
        if (requestCode == 3) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                z = 1;
            } else {
                z = 0;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void setupUIElements() {
        filesRecyclerView = findViewById(R.id.SelectFolderRecyclerView);
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapter = new SoundsRecyclerAdapter(this, this);
        adapter.recyclerView = filesRecyclerView;

        View.OnClickListener shuffleClick = v -> {
            int shuffle = RTApplication.getPreferencesData().getShuffleMode();
            shuffle = shuffle ^ 1;
            RTApplication.getPreferencesData().setShuffleMode(shuffle);

            showShuffleIcon();
        };

        ImageView optionsLeft, optionsRight, optionsUp, optionsDown;
        ImageView backLeftImageView, backRightImageView, backUpImageView, backDownImageView;
        ImageView backFolderLeft, backFolderRight, backFolderUp, backFolderDown;
        ImageView topFolderLeft, topFolderRight, topFolderUp, topFolderDown;

        shuffleLeft = findViewById(R.id.shuffle_left);
        shuffleLeft.setOnClickListener(shuffleClick);
        shuffleRight = findViewById(R.id.shuffle_right);
        shuffleRight.setOnClickListener(shuffleClick);
        shuffleUp = findViewById(R.id.shuffle_up);
        shuffleUp.setOnClickListener(shuffleClick);
        shuffleDown = findViewById(R.id.shuffle_down);
        shuffleDown.setOnClickListener(shuffleClick);

        View.OnClickListener optionsClick = v -> {
            Intent intent = new Intent(RTApplication.getContext(), PreferencesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        };

        optionsLeft = findViewById(R.id.options_left);
        optionsLeft.setOnClickListener(optionsClick);
        optionsRight = findViewById(R.id.options_right);
        optionsRight.setOnClickListener(optionsClick);
        optionsUp = findViewById(R.id.options_up);
        optionsUp.setOnClickListener(optionsClick);
        optionsDown = findViewById(R.id.options_down);
        optionsDown.setOnClickListener(optionsClick);

        View.OnClickListener imageExitClick = v -> {
            RTApplication.getPreferencesData().putAllPreferences();
            exitProgram();
        };

        backLeftImageView = findViewById(R.id.back_left);
        backLeftImageView.setOnClickListener(imageExitClick);
        backRightImageView = findViewById(R.id.back_right);
        backRightImageView.setOnClickListener(imageExitClick);
        backUpImageView = findViewById(R.id.back_up);
        backUpImageView.setOnClickListener(imageExitClick);
        backDownImageView = findViewById(R.id.back_down);
        backDownImageView.setOnClickListener(imageExitClick);

        View.OnClickListener imageBackFolderClick = v -> {
            List<SoundItem> FileList = RTApplication.getSoundSourceManager().getViewableList();
            if (!FileList.isEmpty()) {
                SoundItem item = FileList.get(0);
                boolean needUpdatePositionList = false;

                switch (item.state) {
                    case fiParentDirectory:
                        if (serviceLink.isMediaServiceReady())
                            RTApplication.getSoundSourceManager().createViewableList(item);
                        adapter.update(FileList);
                        needUpdatePositionList = true;
                        break;
                    case fiRadioParentDirectory:
                        RTApplication.getSoundSourceManager().createViewableRootList();
                        adapter.update(FileList);
                        needUpdatePositionList = true;
                        break;
                    case fiRadioStationParentDirectory:
                        if (!RTApplication.getSoundSourceManager().createViewableList(item)) {
                            Toast.makeText(getApplicationContext(), "Ошибка подключенияч к интернету", Toast.LENGTH_SHORT).show();
                        }
                        adapter.update(FileList);
                        needUpdatePositionList = true;
                        break;
                }

                if (needUpdatePositionList) {
                    LinearLayoutManager lm;
                    lm = (LinearLayoutManager)filesRecyclerView.getLayoutManager();
                    if (lm != null)
                        lm.scrollToPosition(0);
                }
            }
        };

        backFolderLeft = findViewById(R.id.back_folder_left);
        backFolderLeft.setOnClickListener(imageBackFolderClick);
        backFolderRight = findViewById(R.id.back_folder_right);
        backFolderRight.setOnClickListener(imageBackFolderClick);
        backFolderUp = findViewById(R.id.back_folder_up);
        backFolderUp.setOnClickListener(imageBackFolderClick);
        backFolderDown = findViewById(R.id.back_folder_down);
        backFolderDown.setOnClickListener(imageBackFolderClick);

        View.OnClickListener imageTopFolderClick = v -> {
            RTApplication.getSoundSourceManager().createViewableRootList();
            adapter.update(RTApplication.getSoundSourceManager().getViewableList());
        };

        topFolderLeft = findViewById(R.id.top_folder_left);
        topFolderLeft.setOnClickListener(imageTopFolderClick);
        topFolderRight = findViewById(R.id.top_folder_right);
        topFolderRight.setOnClickListener(imageTopFolderClick);
        topFolderUp = findViewById(R.id.top_folder_up);
        topFolderUp.setOnClickListener(imageTopFolderClick);
        topFolderDown = findViewById(R.id.top_folder_down);
        topFolderDown.setOnClickListener(imageTopFolderClick);

        View.OnClickListener imagePlayerClick = v -> {
            if (v == playPauseImageView) {
                if (serviceLink.isMediaServiceReady()) {
                    switch (serviceLink.getState()) {
                        case PlaybackStateCompat.STATE_NONE: {
                            break;
                        }
                        case PlaybackStateCompat.STATE_PAUSED: {
                            serviceLink.resume();
                            break;
                        }
                        case PlaybackStateCompat.STATE_PLAYING: {
                            serviceLink.pause();
                            break;
                        }
                    }
                }
            }
            else
            if (v == nextSoundImageView) {
                serviceLink.nextSound();
            }
            else
            if (v == prevSoundImageView) {
                serviceLink.prevSound();
            }
        };

        playPauseImageView = findViewById(R.id.PlayPauseImageView);
        playPauseImageView.setOnClickListener(imagePlayerClick);
        nextSoundImageView = findViewById(R.id.NextSoundImageView);
        nextSoundImageView.setOnClickListener(imagePlayerClick);
        prevSoundImageView = findViewById(R.id.PrevSoundImageView);
        prevSoundImageView.setOnClickListener(imagePlayerClick);

        positionTextView = findViewById(R.id.PositionTextView);
        durationSoundTextView = findViewById(R.id.DurationTextView);

        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (serviceLink.isMediaServiceReady() && fromUser)
                    serviceLink.setPosition(progress);

                positionTextView.setText(timeFormat(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (serviceLink.isMediaServiceReady()) {
                    int progress = seekBar.getProgress();
                    serviceLink.setPosition(progress);
                    positionTextView.setText(timeFormat(progress));
                }
            }
        };

        soundSeekBar = findViewById(R.id.SoundSeekBar);
        soundSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        soundSeekBar.setProgressDrawable(AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.seekbar));

        seekBarThread = new Thread(seekBarRunnable);
        seekBarThread.start();
    }

    public void exitProgram() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.finishAndRemoveTask();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            this.finishAffinity();
        } else {
            this.finish();
        }
        System.exit(0);
    }

    void showShuffleIcon() {
        Drawable drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_shuffle_off);
        int shuffle = RTApplication.getPreferencesData().getShuffleMode();
        if (shuffle != 0)
            drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_shuffle_on);
        shuffleLeft.setImageDrawable(drawable);
        shuffleRight.setImageDrawable(drawable);
        shuffleUp.setImageDrawable(drawable);
        shuffleDown.setImageDrawable(drawable);
    }

    void clearSeekBar() {
        positionTextView.setText(timeFormat(0));
        durationSoundTextView.setText(timeFormat(0));
        soundSeekBar.setMin(0);
        soundSeekBar.setMax(0);
        soundSeekBar.setProgress(0);
    }

    void updateUI(@NonNull PlaybackStateCompat state) {
        Drawable drawable = null;

        switch (state.getState()) {
            case PlaybackStateCompat.STATE_STOPPED:
                drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_play);
                clearSeekBar();
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_play);
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_pause);
                break;
            default:
        }

        playPauseImageView.setImageDrawable(drawable);
    }

    public void applyPreferences() {
        RTApplication.getPreferencesData().getAllPreferences();

        Utils.setImageBackground(this, this.findViewById(R.id.main));

        // обновление видимости панелей

        Toolbar toolbarLeft, toolbarRight, toolbarUp, toolbarDown;
        toolbarLeft = findViewById(R.id.toolbar_left);
        toolbarRight = findViewById(R.id.toolbar_right);
        toolbarUp = findViewById(R.id.toolbar_up);
        toolbarDown = findViewById(R.id.toolbar_down);

        try {
            switch (RTApplication.getPreferencesData().getToolbarPosition()) {
                case 1:
                    if (toolbarLeft.getVisibility() != View.VISIBLE)
                        toolbarLeft.setVisibility(View.VISIBLE);
                    if (toolbarRight.getVisibility() != View.GONE)
                        toolbarRight.setVisibility(View.GONE);
                    if (toolbarUp.getVisibility() != View.GONE)
                        toolbarUp.setVisibility(View.GONE);
                    if (toolbarDown.getVisibility() != View.GONE)
                        toolbarDown.setVisibility(View.GONE);
                    break;
                case 2:
                    if (toolbarLeft.getVisibility() != View.GONE)
                        toolbarLeft.setVisibility(View.GONE);
                    if (toolbarRight.getVisibility() != View.VISIBLE)
                        toolbarRight.setVisibility(View.VISIBLE);
                    if (toolbarUp.getVisibility() != View.GONE)
                        toolbarUp.setVisibility(View.GONE);
                    if (toolbarDown.getVisibility() != View.GONE)
                        toolbarDown.setVisibility(View.GONE);
                    break;
                case 3:
                    if (toolbarLeft.getVisibility() != View.GONE)
                        toolbarLeft.setVisibility(View.GONE);
                    if (toolbarRight.getVisibility() != View.GONE)
                        toolbarRight.setVisibility(View.GONE);
                    if (toolbarUp.getVisibility() != View.VISIBLE)
                        toolbarUp.setVisibility(View.VISIBLE);
                    if (toolbarDown.getVisibility() != View.GONE)
                        toolbarDown.setVisibility(View.GONE);
                    break;
                case 4:
                    if (toolbarLeft.getVisibility() != View.GONE)
                        toolbarLeft.setVisibility(View.GONE);
                    if (toolbarRight.getVisibility() != View.GONE)
                        toolbarRight.setVisibility(View.GONE);
                    if (toolbarUp.getVisibility() != View.GONE)
                        toolbarUp.setVisibility(View.GONE);
                    if (toolbarDown.getVisibility() != View.VISIBLE)
                        toolbarDown.setVisibility(View.VISIBLE);
                    break;
                default:
               }
            } catch (NumberFormatException e) {
                System.out.println("Invalid");
            }

        // обновление размеров шрифта
        if (filesRecyclerView.getAdapter() != null)
            filesRecyclerView.getAdapter().notifyDataSetChanged();

        Space topSpace, leftSpace, rightSpace, bottomSpace;

        topSpace = findViewById(R.id.top_space);
        leftSpace = findViewById(R.id.left_space);
        rightSpace = findViewById(R.id.right_space);
        bottomSpace = findViewById(R.id.bottom_space);

        ViewGroup.LayoutParams params = topSpace.getLayoutParams();
        params.height = RTApplication.getPreferencesData().getTopSpace();
        topSpace.setLayoutParams(params);

        params = leftSpace.getLayoutParams();
        params.width = RTApplication.ScreenAuto.INSETS_LEFT -
                RTApplication.ScreenAuto.PADDING_LEFT +
                RTApplication.getPreferencesData().getLeftSpace();
        leftSpace.setLayoutParams(params);

        params = rightSpace.getLayoutParams();
        params.width = RTApplication.getPreferencesData().getRightSpace();
        rightSpace.setLayoutParams(params);

        params = bottomSpace.getLayoutParams();
        params.height = RTApplication.getPreferencesData().getBottomSpace();
        bottomSpace.setLayoutParams(params);

        showShuffleIcon();
    }

    boolean firstStart = true;

    boolean applyNoShowPreferences() {
        boolean result = false;
        // текущий воспроизводимый файл
        if (RTApplication.getPreferencesData().getPlayOnStartMode() != 0 && firstStart) {
            String filename = RTApplication.getPreferencesData().getLastPlayedFile();
            if (!filename.isEmpty()) {
                if (RTApplication.getSoundSourceManager().isFile(filename)) {
                    RTApplication.getSoundSourceManager().createViewableList(FileUtils.extractFilePath(filename));
                } else {
                    RTApplication.getSoundSourceManager().createViewableFavoritesRadioStationsList();
                }

                if (serviceLink.isMediaServiceReady()) {
                    serviceLink.play(filename);
                }
                adapter.update(RTApplication.getSoundSourceManager().getViewableList());
                result = true;
            }
            firstStart = false;
        }
        return result;
    }

    @NonNull
    private String timeFormat(long value) {
        long totalSeconds = value / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    Runnable getSoundPlayerDataRunnable = () -> {
        long position;
        long duration;

        if (serviceLink.isMediaServiceReady() && soundSeekBar != null) {
            position = serviceLink.getPosition();
            duration = serviceLink.getDuration();
            if (position <= duration)
                soundSeekBar.setProgress((int) position);
        }
    };

    Runnable seekBarRunnable = () -> {

        while (true) {
            if (Thread.currentThread().isInterrupted())
                break;

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                break;
            }

            runOnUiThread(getSoundPlayerDataRunnable);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serviceLink.unbindService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        serviceLink.bindService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        RTApplication.getPreferencesData().putAllPreferences();
    }

    // клик на элементе рекуклера
    @Override
    public void onClick(View v) {
        int itemPosition = adapter.recyclerView.getChildLayoutPosition(v);

        List<SoundItem> FileList = RTApplication.getSoundSourceManager().getViewableList();
        if (FileList.size() == 0)
            return;

        SoundItem item = FileList.get(itemPosition);
        boolean needUpdatePositionList = false;

        switch (item.state) {
            case fiParentDirectory: // выход на уроверь вверх
                if (serviceLink.isMediaServiceReady())
                    RTApplication.getSoundSourceManager().createViewableList(item);
                adapter.update(FileList);
                needUpdatePositionList = true;
                break;
            case fiDirectory: // директория
                if (serviceLink.isMediaServiceReady())
                    RTApplication.getSoundSourceManager().createViewableList(item);
                adapter.update(FileList);
                needUpdatePositionList = true;
                break;
            case fiFile: // файл
                if (serviceLink.isMediaServiceReady()) {
                    serviceLink.play(item.getFullName());
                }
                adapter.update(FileList);
                break;
            case fiRadioRoot:
                if (!RTApplication.getSoundSourceManager().createViewableList(item)) {
                    Toast.makeText(getApplicationContext(), "Ошибка подключенияч к интернету", Toast.LENGTH_SHORT).show();
                }
                adapter.update(FileList);
                needUpdatePositionList = true;
                break;
            case fiRadioFavorites:
                if (!RTApplication.getSoundSourceManager().createViewableList(item)) {
                    Toast.makeText(getApplicationContext(), "Ошибка подключенияч к интернету", Toast.LENGTH_SHORT).show();
                }
                adapter.update(FileList);
                needUpdatePositionList = true;
                break;
            case fiRadioCountry:
                if (!RTApplication.getSoundSourceManager().createViewableList(item)) {
                    Toast.makeText(getApplicationContext(), "Ошибка подключенияч к интернету", Toast.LENGTH_SHORT).show();
                }
                adapter.update(FileList);
                needUpdatePositionList = true;
                break;
            case fiRadioStation:
            case fiRadioFavoriteStation:
                if (serviceLink.isMediaServiceReady()) {
                    serviceLink.play(item.location);
                }
                adapter.update(FileList);
                break;
            case fiRadioParentDirectory:
                RTApplication.getSoundSourceManager().createViewableRootList();
                adapter.update(FileList);
                needUpdatePositionList = true;
                break;
            case fiRadioStationParentDirectory:
                if (!RTApplication.getSoundSourceManager().createViewableList(item)) {
                    Toast.makeText(getApplicationContext(), "Ошибка подключенияч к интернету", Toast.LENGTH_SHORT).show();
                }
                adapter.update(FileList);
                needUpdatePositionList = true;
                break;
        }

        if (needUpdatePositionList) {
            LinearLayoutManager lm = (LinearLayoutManager)filesRecyclerView.getLayoutManager();
            if (lm != null)
                lm.scrollToPosition(0);
        }
    }

    RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            LinearLayoutManager lm = (LinearLayoutManager) filesRecyclerView.getLayoutManager();
            if (lm != null) {
                int visibleItemCount = lm.getChildCount();//смотрим сколько элементов на экране
                int totalItemCount = lm.getItemCount();//сколько всего элементов
                int firstVisibleItems = lm.findFirstVisibleItemPosition();//какая позиция первого элемента

                if (visibleItemCount + firstVisibleItems >= RTApplication.getSoundSourceManager().getViewableList().size() - 1) {
                    RTApplication.getSoundSourceManager().appendViewableItems();

                    recyclerView.post(() -> adapter.update(RTApplication.getSoundSourceManager().getViewableList()));
                }
            }
        }
    };


/*    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();

        if (action == KeyEvent.ACTION_DOWN) {
            Toast.makeText(getApplicationContext(), "код: " + Integer.toString(keyCode), Toast.LENGTH_SHORT).show();
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    RTApplication.mediaService.VolumeUp(1);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    RTApplication.mediaService.VolumeDown(1);
                }
                return true;
            default: {
                return super.dispatchKeyEvent(event);
            }
        }

    }*/

    public void showDeleteFileDialog(String filename) {
        DialogFragment dialog = DeleteFileDialog.newInstance(filename);
        dialog.show(getSupportFragmentManager(), "msg");
    }

    @Override
    public void onDialogButtonClickListener(int action, Object param) {
        String filename = "";
        if (param instanceof String)
            filename = (String)param;
        //Toast.makeText(getApplicationContext(), "Удаление " + filename, Toast.LENGTH_SHORT).show();
        RTApplication.getSoundSourceManager().removeItem(filename);
        adapter.update(RTApplication.getSoundSourceManager().getViewableList());

        if (filename.equals(serviceLink.getCurrentPlayedSound()))
            serviceLink.nextSound();
    }
}
