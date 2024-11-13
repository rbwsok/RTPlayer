package su.rbws.rtplayer;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SoundsRecyclerAdapter extends RecyclerView.Adapter<SoundsRecyclerAdapter.ViewHolder> implements IItemChange
{
    private List<FileItem> items;
    public RecyclerView recyclerView;

    View.OnClickListener onClickListener;

    MainActivity activity;

    SoundsRecyclerAdapter(View.OnClickListener onClickListener, MainActivity activity)
    {
        this.onClickListener = onClickListener;
        this.items = null;
        this.activity = activity;

        RTApplication.getGlobalData().metadataExtractor.objectChange = this;
    }

    @Override
    public int getItemViewType(int position) {
        int result = R.layout.recycler_item_sound;
        FileItem item = items.get(position);
        if (item.isFile()) {
            if (item.getFullName().equals(activity.serviceLink.getCurrentPlayedSound())) {
                result = R.layout.recycler_item_current_sound;
            }
        }

        return result;
    }

    @NonNull
    @Override
    public SoundsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        view.setOnClickListener(onClickListener);
        return new SoundsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SoundsRecyclerAdapter.ViewHolder holder, int position)
    {
        holder.recyclerAdapter = this;

        FileItem item = items.get(position);
        Drawable drawable = null;
        boolean playedFile = false;

        if (item.isFile()) {
            // запрос данных для отображения
            RTApplication.getGlobalData().metadataExtractor.getMetadata(item, position);

            holder.crossImage.setVisibility(View.VISIBLE);

            if (item.getFullName().equals(activity.serviceLink.getCurrentPlayedSound())) {
                playedFile = true;
            }

            if (playedFile)
                drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_current_play);
            else
                drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_play);

            if (item.metadataAcquired) {
                switch (RTApplication.getDataBase().getTitleFileMode()) {
                    case 0:
                        holder.songNameTextView.setText(item.name);
                        break;
                    case 1:
                        holder.songNameTextView.setText(item.title);
                        break;
                }

                String str;
                switch (RTApplication.getDataBase().getSubTitleFileMode()) {
                    case 0:
                        holder.infoTextView.setText(item.location);
                        break;
                    case 1:
                        str = "";
                        if (!item.artist.isEmpty())
                            str = item.artist;

                        if (!item.album.isEmpty()) {
                            if (str.isEmpty())
                                str = item.album;
                            else
                                str = str + " - " + item.album;
                        }

                        holder.infoTextView.setText(str);
                        break;
                    case 2:
                        str = "";
                        if (!item.title.isEmpty())
                            str = item.title;

                        if (!item.artist.isEmpty()) {
                            if (str.isEmpty())
                                str = item.artist;
                            else
                                str = str + " - " + item.artist;
                        }

                        if (!item.album.isEmpty()) {
                            if (str.isEmpty())
                                str = item.album;
                            else
                                str = str + " - " + item.album;
                        }
                        holder.infoTextView.setText(str);
                        break;
                }
            }
            else {
                holder.songNameTextView.setText(item.name);
                holder.infoTextView.setText(item.location);
            }
        }
        else
        if (item.isDirectory()) {
            drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_folder);
            holder.songNameTextView.setText(item.name);
            holder.infoTextView.setText(item.location);

            holder.crossImage.setVisibility(View.INVISIBLE);
        }
        else
        if (item.isParentDirectory()) {
            drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_backfolder);
            holder.songNameTextView.setText(item.name);
            holder.infoTextView.setText(item.location);

            holder.crossImage.setVisibility(View.INVISIBLE);
        }
        holder.captionImage.setImageDrawable(drawable);

        float mainSize = RTApplication.getDataBase().getTextSize();
        holder.songNameTextView.setTextSize(mainSize);

        float infoSize = (14.0f / 20.0f) * mainSize;

        if (playedFile)
            holder.infoTextView.setTextSize(infoSize + 1);
        else
            holder.infoTextView.setTextSize(infoSize);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public int getCurrentPosition() {
        int result = -1;
        FileItem item;
        for (int i = 0; i < items.size(); ++i) {
            item = items.get(i);
            if (item.getFullName().equals(activity.serviceLink.getCurrentPlayedSound())) {
                result = i;
                break;
            }
        }

        return result;
    }

    public void update(List<FileItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView songNameTextView, infoTextView;
        private final ImageView captionImage;
        private final ImageView crossImage;
        public SoundsRecyclerAdapter recyclerAdapter;

        public ViewHolder(View view)
        {
            super(view);

            songNameTextView = view.findViewById(R.id.name_text_view);
            infoTextView = view.findViewById(R.id.info_text_view);

            View.OnClickListener crossClick = v -> {
                RecyclerView.ViewHolder viewHolder = activity.adapter.recyclerView.findContainingViewHolder(v);
                if (viewHolder != null) {
                    int itemPosition = viewHolder.getLayoutPosition();
                    ArrayList<FileItem> fileList = RTApplication.getGlobalData().viewableFileList;
                    FileItem item = fileList.get(itemPosition);

                    activity.showDeleteFileDialog(item.getFullName());
                }
            };

            crossImage = view.findViewById(R.id.cross);
            crossImage.setOnClickListener(crossClick);

            captionImage = view.findViewById(R.id.caption_image);
        }
    }

    @Override
    public void onItemChanged(int position) {
        recyclerView.post(() -> {
            notifyDataSetChanged();
            //notifyItemChanged(position);
        });

    }
}

