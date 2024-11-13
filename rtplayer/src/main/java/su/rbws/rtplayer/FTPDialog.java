package su.rbws.rtplayer;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.guichaguri.minimalftp.FTPServer;
import com.guichaguri.minimalftp.impl.NativeFileSystem;
import com.guichaguri.minimalftp.impl.NoOpAuthenticator;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class FTPDialog extends DialogFragment {

    public enum FTPError {ftpNone, ftpLocalAddress, ftpLocalPort, ftpErrorCustom}

    @NonNull
    @SuppressWarnings("UnnecessaryLocalVariable")
    public static FTPDialog newInstance() {
        FTPDialog dialog = new FTPDialog();

        return dialog;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    Button buttonStart, buttonExit;
    TextView StatusTextView, AddressLabel;
    TextView Address1TextView, Address2TextView, Address3TextView;
    EditText PortEditTextNumberSigned;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_ftp, null);

        float value = this.getResources().getDisplayMetrics().density * 500;
        v.setMinimumWidth((int)value);

        buttonStart = v.findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(buttonclick);
        buttonExit = v.findViewById(R.id.buttonExit);
        buttonExit.setOnClickListener(buttonclick);

        PortEditTextNumberSigned = v.findViewById(R.id.PortEditTextNumberSigned);
        PortEditTextNumberSigned.setText(Integer.toString(RTApplication.getDataBase().getFTPPort()));

        StatusTextView = v.findViewById(R.id.StatusTextView);
        Address1TextView = v.findViewById(R.id.Address1TextView);
        Address2TextView = v.findViewById(R.id.Address2TextView);
        Address3TextView = v.findViewById(R.id.Address3TextView);
        AddressLabel = v.findViewById(R.id.AddressLabel);

        ViewStateTexts();

        return v;
    }

    boolean ftpstarted = false;
    FTPError ftperror = FTPError.ftpNone;
    ArrayList<String> localaddresses = new ArrayList<>();

    String ftperrorcustom;

    public void ViewStateTexts() {
        StatusTextView.setText(RTApplication.getContext().getString(R.string.stopped));
        Address2TextView.setText("");
        Address3TextView.setText("");
        if (!ftpstarted) {
            buttonStart.setText(RTApplication.getContext().getString(R.string.start));
            switch (ftperror) {
                case ftpNone:
                    AddressLabel.setText(RTApplication.getContext().getString(R.string.addresses));
                    Address1TextView.setTextColor(RTApplication.getContext().getColor(R.color.FontColor));
                    Address1TextView.setText(RTApplication.getContext().getString(R.string.absent));
                    break;
                case ftpLocalAddress:
                    AddressLabel.setText(RTApplication.getContext().getString(R.string.error));
                    Address1TextView.setTextColor(RTApplication.getContext().getColor(R.color.red));
                    Address1TextView.setText(RTApplication.getContext().getString(R.string.ftp_error_1));
                    break;
                case ftpLocalPort:
                    AddressLabel.setText(RTApplication.getContext().getString(R.string.error));
                    Address1TextView.setTextColor(RTApplication.getContext().getColor(R.color.red));
                    Address1TextView.setText(RTApplication.getContext().getString(R.string.ftp_error_2));
                    break;
                case ftpErrorCustom:
                    AddressLabel.setText(RTApplication.getContext().getString(R.string.error));
                    Address1TextView.setTextColor(RTApplication.getContext().getColor(R.color.red));
                    Address1TextView.setText(ftperrorcustom);
                    break;
            }
        }
        else {
            Address1TextView.setTextColor(RTApplication.getContext().getColor(R.color.FontColor));
            AddressLabel.setText(RTApplication.getContext().getString(R.string.addresses));
            StatusTextView.setText(RTApplication.getContext().getString(R.string.started));

            if (!localaddresses.isEmpty())
                Address1TextView.setText(localaddresses.get(0));
            if (localaddresses.size() > 1)
                Address2TextView.setText(localaddresses.get(1));
            if (localaddresses.size() > 2)
                Address3TextView.setText(localaddresses.get(2));

            buttonStart.setText(RTApplication.getContext().getString(R.string.stop));
        }
    }

    View.OnClickListener buttonclick = v -> {
        // Закрываем диалоговое окно
        if (v == buttonStart) {
            ftperror = FTPError.ftpNone;
            if (!ftpstarted) {
                localaddresses.clear();
                getLocalIpAddresses(localaddresses);
                if (localaddresses.isEmpty()) {
                    ftperror = FTPError.ftpLocalAddress;
                } else {
                    int port = Utils.parseInt(PortEditTextNumberSigned.getText().toString());
                    if (port < 4000 || port > 65535) {
                        ftperror = FTPError.ftpLocalPort;
                    } else {
                        RTApplication.getDataBase().setFTPPort(port);
                        ftpstarted = true;
                        startFTP(port);
                    }

                }
            }
            else {
                ftpstarted = false;
                stopFTP();
            }
            ViewStateTexts();
        }

        if (v == buttonExit) {
            dismiss();
        }
    };

    // onDismiss вызывается при закрытии фрагмента
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        ftpstarted = false;
        stopFTP();
        RTApplication.getDataBase().putAllPreferences();
    }

    // onCancel вызывается при нажатии на "назад"
    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
    }

    FTPServer server;

    public void startFTP(int port) {
        String rootfolder = RTApplication.getDataBase().getBaseDirectory();
        File rootfile = new File(rootfolder);

        NativeFileSystem fs = new NativeFileSystem(rootfile);
        NoOpAuthenticator auth = new NoOpAuthenticator(fs);
        server = new FTPServer(auth);

        try {
            server.listen(port);
        }
        catch (IOException e) {
            ftpstarted = false;
            ftperror = FTPError.ftpErrorCustom;
            ftperrorcustom = e.toString();

            Log.e("rtplayer_tag", e.toString());

            ViewStateTexts();
        }
    }

    public void stopFTP() {
        if (server != null) {
            try {
                server.close();
            }
            catch (IOException e) {
            }
        }
    }

    public void getLocalIpAddresses(ArrayList<String> adresses) {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                String name = intf.getName();
                name = name.substring(0, name.length() - 1);
           /*     if (name.equals("wlan") || name.equals("eth"))*/ {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                            adresses.add(inetAddress.getHostAddress() + " (" + intf.getName() + ")");
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("rtplayer_tag", ex.toString());
        }
    }

}
