package su.rbws.rtplayer.service.soundplayer;

import android.content.Context;

import su.rbws.rtplayer.RTApplication;

public class SoundPlayer {

    // подсистема воспроизведения звуков (MediaPlayer и т.д.)
    SoundSystemAbstract soundSystem;

    // список файлов
    public SoundList fileList;

    // команды плееру
    public enum SoundPlayerCommand {spcNone, spcPlay, spcPause, spcResume, spcStop, spcSetPosition,
        spcGetPosition, spcGetDuration, spcSetVolume, spcGetVolume, spcPlayNextSound, spcPlayPrevSound, spcGetIsPlayed}

    public SoundPlayer(Context context) {
        //SoundSystem = new SoundSystemMediaPlayer(context);
        soundSystem = new SoundSystemExoPlayer(context);
        fileList = new SoundList();
    }

    public long sendCommand(SoundPlayerCommand command) {
        return sendCommand(command, "", 0);
    }

    public long sendCommand(SoundPlayerCommand command, String param1) {
        return sendCommand(command, param1, 0);
    }

    public long sendCommand(SoundPlayerCommand command, long param2) {
        return sendCommand(command, "", param2);
    }

    public long sendCommand(SoundPlayerCommand command, String param1, long param2) {
        long result = 0;

        switch (command) {
            case spcNone:
                break;
            case spcPlay:
                soundSystem.play(param1);
                break;
            case spcPause:
                soundSystem.pause();
                break;
            case spcResume:
                soundSystem.resume();
                break;
            case spcStop:
                soundSystem.stop();
                break;
            case spcSetPosition:
                soundSystem.setPosition(param2);
                break;
            case spcGetPosition:
                result = soundSystem.getPosition();
                break;
            case spcGetDuration:
                result = soundSystem.getDuration();
                break;
            case spcSetVolume:
                soundSystem.setVolume(param2);
                break;
            case spcGetVolume:
                result = soundSystem.getVolume();
                break;
            case spcPlayNextSound:
                playNextSound();
                break;
            case spcPlayPrevSound:
                playPrevSound();
                break;
            case spcGetIsPlayed:
                if (soundSystem.getIsPlayed())
                    result = 1;
                break;
        }

        return result;
    }

    private void playPrevSound() {
        // включено перемешивание
        if (RTApplication.getDataBase().getShuffleMode() != 0) {
            SoundList.currentPlayedSound = SoundList.getRandomFile();
            soundSystem.play(SoundList.currentPlayedSound);
            return;
        }

        // перемешивания нет
        switch (RTApplication.getDataBase().getRepeatMode()) {
            case repNone:
                SoundList.currentPlayedSound = "";
                soundSystem.play("");
                return;
            case repOneSound:
                soundSystem.play(SoundList.currentPlayedSound);
                break;
            case repFolder:
                SoundList.currentPlayedSound = SoundList.getPrevFileInFolder();
                soundSystem.play(SoundList.currentPlayedSound);
                break;
            case repAll:
                SoundList.currentPlayedSound = SoundList.getPrevFileInAllFiles();
                soundSystem.play(SoundList.currentPlayedSound);
                break;
        }
    }

    private void playNextSound() {
        // включено перемешивание
        if (RTApplication.getDataBase().getShuffleMode() != 0) {
            SoundList.currentPlayedSound = SoundList.getRandomFile();
            soundSystem.play(SoundList.currentPlayedSound);
            return;
        }

        // перемешивания нет
        switch (RTApplication.getDataBase().getRepeatMode()) {
            case repNone:
                SoundList.currentPlayedSound = "";
                soundSystem.play("");
                return;
            case repOneSound:
                soundSystem.play(SoundList.currentPlayedSound);
                break;
            case repFolder:
                SoundList.currentPlayedSound = SoundList.getNextFileInFolder();
                soundSystem.play(SoundList.currentPlayedSound);
                break;
            case repAll:
                SoundList.currentPlayedSound = SoundList.getNextFileInAllFiles();
                soundSystem.play(SoundList.currentPlayedSound);
                break;
        }
    }
}
