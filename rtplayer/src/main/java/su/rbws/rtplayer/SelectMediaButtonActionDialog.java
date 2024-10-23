package su.rbws.rtplayer;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import su.rbws.rtplayer.service.MediaButtonsMapper;

public class SelectMediaButtonActionDialog extends DialogFragment {

    MediaButtonsMapper.MediaButton mediaButton;

    @NonNull
    public static SelectMediaButtonActionDialog newInstance(MediaButtonsMapper.MediaButton mediaButton) {
        SelectMediaButtonActionDialog dialog = new SelectMediaButtonActionDialog();
        dialog.mediaButton = mediaButton;

        return dialog;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    RecyclerView actionsRecyclerView;
    public MediaButtonActionRecyclerAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_select_media_buttons_action, null);

        float value = this.getResources().getDisplayMetrics().density * 400;
        v.setMinimumWidth((int)value);

        actionsRecyclerView = v.findViewById(R.id.media_button_action_recycler);
        actionsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        adapter = new MediaButtonActionRecyclerAdapter(recyclerClick);
        adapter.recyclerView = actionsRecyclerView;
        actionsRecyclerView.setAdapter(adapter);
        adapter.update(RTApplication.getGlobalData().mediaButtonsMapper.mediaButtonActions);

        return v;
    }

    View.OnClickListener recyclerClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int itemPosition = adapter.recyclerView.getChildLayoutPosition(v);

            ArrayList<MediaButtonsMapper.MediaButtonAction> mediaButtonAction = RTApplication.getGlobalData().mediaButtonsMapper.mediaButtonActions;
            MediaButtonsMapper.MediaButtonAction item = mediaButtonAction.get(itemPosition);

            mediaButton.action = item.action;

            onClick.onDialogButtonClickListener(3, null);

            dismiss();
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

    public class MediaButtonActionRecyclerAdapter extends RecyclerView.Adapter<SelectMediaButtonActionDialog.MediaButtonActionRecyclerAdapter.ViewHolder> {
        private List<MediaButtonsMapper.MediaButtonAction> items;
        public RecyclerView recyclerView;

        View.OnClickListener onClickListener;

        MediaButtonActionRecyclerAdapter(View.OnClickListener onClickListener)
        {
            this.onClickListener = onClickListener;
            this.items = null;
        }

        @Override
        public int getItemViewType(int position) {
            return R.layout.recycler_item_select_media_button_action;
        }

        @NonNull
        @Override
        public MediaButtonActionRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
            view.setOnClickListener(onClickListener);
            return new MediaButtonActionRecyclerAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SelectMediaButtonActionDialog.MediaButtonActionRecyclerAdapter.ViewHolder holder, int position)
        {
            MediaButtonsMapper.MediaButtonAction item = items.get(position);

            holder.captionTextView.setText(RTApplication.getGlobalData().mediaButtonsMapper.getAction(item.action).description);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void update(List<MediaButtonsMapper.MediaButtonAction> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder
        {
            public TextView captionTextView;

            public ViewHolder(View view)
            {
                super(view);

                captionTextView = view.findViewById(R.id.caption);
            }
        }
    }
}
