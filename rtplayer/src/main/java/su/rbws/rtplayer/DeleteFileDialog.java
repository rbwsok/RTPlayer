package su.rbws.rtplayer;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class DeleteFileDialog extends DialogFragment {

    @NonNull
    public static DeleteFileDialog newInstance(String filename) {
        DeleteFileDialog dialog = new DeleteFileDialog();

        Bundle args = new Bundle();
        args.putString("filename", filename);
        dialog.setArguments(args);

        return dialog;
    }

    String fullfilename;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fullfilename = getArguments().getString("filename");
    }

    Button buttonNo, buttonYes;
    TextView fileNameTextView, fileLocationTextView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_deletefile, null);

        float value = this.getResources().getDisplayMetrics().density * 700;
        v.setMinimumWidth((int)value);

        buttonNo = v.findViewById(R.id.buttonNo);
        buttonNo.setOnClickListener(buttonClick);
        buttonYes = v.findViewById(R.id.buttonYes);
        buttonYes.setOnClickListener(buttonClick);
        fileNameTextView = v.findViewById(R.id.FileNameTextView);
        fileNameTextView.setText(FileUtils.extractFileName(fullfilename));
        fileLocationTextView = v.findViewById(R.id.FileLocationTextView);
        fileLocationTextView.setText(FileUtils.extractFilePath(fullfilename));
        return v;
    }

    View.OnClickListener buttonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Закрываем диалоговое окно
            if (v == buttonYes) {
                onClick.onDialogButtonClickListener(1, fullfilename);
                dismiss();
            }

            if (v == buttonNo) {
                dismiss();
            }
        }
    };

    // onDismiss вызывается при закрытии фрагмента
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    // onCancel вызывается при нажатии на "назад"
    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
    }

    IDialogButtonInterface onClick;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            onClick = (IDialogButtonInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement onDialogClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onClick = null;
    }

}
