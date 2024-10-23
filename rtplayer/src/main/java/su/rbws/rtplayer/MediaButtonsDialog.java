package su.rbws.rtplayer;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import su.rbws.rtplayer.preference.PreferencesActivity;
import su.rbws.rtplayer.service.MediaButtonsMapper;

public class MediaButtonsDialog extends DialogFragment {

    public PreferencesActivity activity;

    @NonNull
    public static MediaButtonsDialog newInstance(PreferencesActivity activity) {
        MediaButtonsDialog dialog = new MediaButtonsDialog();
        dialog.activity = activity;

        return dialog;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    Button exitButton, defaultButton, addButton;
    TextView mediButtonInputTextView;
    RecyclerView buttonsRecyclerView;
    MediaButtonRecyclerAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_media_buttons, null);

        float value = this.getResources().getDisplayMetrics().density * 700;
        v.setMinimumWidth((int)value);

        buttonsRecyclerView = v.findViewById(R.id.media_button_recycler);
        buttonsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        adapter = new MediaButtonRecyclerAdapter(recyclerClick);
        adapter.recyclerView = buttonsRecyclerView;
        buttonsRecyclerView.setAdapter(adapter);
        adapter.update(RTApplication.getGlobalData().mediaButtonsMapper.mediaButtons);

        exitButton = v.findViewById(R.id.button_exit);
        exitButton.setOnClickListener(buttonClick);
        defaultButton = v.findViewById(R.id.button_default);
        defaultButton.setOnClickListener(buttonClick);
        addButton = v.findViewById(R.id.button_add);
        addButton.setOnClickListener(buttonClick);
        mediButtonInputTextView = v.findViewById(R.id.input_media_button);
        mediButtonInputTextView.setText("");

        currentMode = 0;

        Dialog dialog = getDialog();
        if (dialog != null)
            dialog.setOnKeyListener(keyPress);

        return v;
    }

    View.OnClickListener recyclerClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int itemPosition = adapter.recyclerView.getChildLayoutPosition(v);

            ArrayList<MediaButtonsMapper.MediaButton> mediaButtons = RTApplication.getGlobalData().mediaButtonsMapper.mediaButtons;
            MediaButtonsMapper.MediaButton mediaButton = mediaButtons.get(itemPosition);

            activity.showSelectMediaButtonActionsDialog(mediaButton);
        }
    };

    int currentMode = 0; // 0 - ничего, 1 - ожидание нажатия клавиши
    int currentKeyCode;

    View.OnClickListener buttonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == addButton) {
                mediButtonInputTextView.setText(getString(R.string.media_buttons_press_button));
                currentMode = 1;
            }

            if (v == defaultButton) {
                RTApplication.getGlobalData().mediaButtonsMapper.setDefault();
                updateRecyclerView();
            }

            if (v == exitButton) {
                dismiss();
            }
        }
    };

    DialogInterface.OnKeyListener keyPress = new DialogInterface.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, @NonNull KeyEvent event) {
            int action = event.getAction();

            if (action == KeyEvent.ACTION_DOWN) {
                if (currentMode == 1) {
                    currentKeyCode = keyCode;
                    mediButtonInputTextView.setText("");
                    currentMode = 0;

                    MediaButtonsMapper.MediaButton button;

                    button = RTApplication.getGlobalData().mediaButtonsMapper.newButton();
                    button.keyCode = currentKeyCode;
                    button.action = MediaButtonsMapper.MediaButtonActionType.mbaNone;
                    RTApplication.getGlobalData().mediaButtonsMapper.mediaButtons.add(button);
                }

            }

            return false;
        }
    };

    public void updateRecyclerView() {
        List<MediaButtonsMapper.MediaButton> items = RTApplication.getGlobalData().mediaButtonsMapper.mediaButtons;
        adapter.update(items);
    }

    // onDismiss вызывается при закрытии фрагмента
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        RTApplication.getDataBase().putAllPreferences();
    }

    // onCancel вызывается при нажатии на "назад"
    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
    }

/*    IDialogButtonInterface onClick;

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
*/

    public class MediaButtonRecyclerAdapter extends RecyclerView.Adapter<MediaButtonsDialog.MediaButtonRecyclerAdapter.ViewHolder> {
        private List<MediaButtonsMapper.MediaButton> items;
        public RecyclerView recyclerView;

        View.OnClickListener onClickListener;

        MediaButtonRecyclerAdapter(View.OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
            this.items = null;
        }

        @Override
        public int getItemViewType(int position) {
            return R.layout.recycler_item_media_button;
        }

        @NonNull
        @Override
        public MediaButtonRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
            view.setOnClickListener(onClickListener);
            return new MediaButtonRecyclerAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MediaButtonsDialog.MediaButtonRecyclerAdapter.ViewHolder holder, int position)
        {
            MediaButtonsMapper.MediaButton item = items.get(position);

            holder.keyCode.setText(Integer.toString(item.keyCode));
            holder.action.setText(RTApplication.getGlobalData().mediaButtonsMapper.getAction(item.action).description);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void update(List<MediaButtonsMapper.MediaButton> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder
        {
            public TextView keyCode, action;
            public ImageView crossImage;

            public ViewHolder(View view)
            {
                super(view);

                keyCode = view.findViewById(R.id.key_code);
                action = view.findViewById(R.id.action);

                View.OnClickListener crossClick = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RecyclerView.ViewHolder viewHolder = adapter.recyclerView.findContainingViewHolder(v);
                        if (viewHolder != null) {
                            int itemPosition = viewHolder.getLayoutPosition();

                            ArrayList<MediaButtonsMapper.MediaButton> mediaButtons = RTApplication.getGlobalData().mediaButtonsMapper.mediaButtons;
                            mediaButtons.remove(itemPosition);
                        }
                        updateRecyclerView();
                    }

                };

                crossImage = view.findViewById(R.id.cross);
                crossImage.setOnClickListener(crossClick);
            }
        }
    }
}