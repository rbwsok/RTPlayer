package su.rbws.rtplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class BootCompleteReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

            abortBroadcast();

            //Toast toast = Toast.makeText(context.getApplicationContext(), "Boot!!!!!", Toast.LENGTH_LONG);
            //toast.show();

            Log.i("rtplayer_tag", "boot onRecieve");

            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            context.startActivity(i);
        }
    }
}

