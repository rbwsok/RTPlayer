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

import java.util.List;

// адаптер для рекуклера на главной активити
public class SoundsRecyclerAdapter extends RecyclerView.Adapter<SoundsRecyclerAdapter.ViewHolder> implements IItemChange
{
    private List<SoundItem> items;
    public RecyclerView recyclerView;

    View.OnClickListener onClickListener;

    MainActivity activity;

    SoundsRecyclerAdapter(View.OnClickListener onClickListener, MainActivity activity)
    {
        this.onClickListener = onClickListener;
        this.items = null;
        this.activity = activity;

        RTApplication.getSoundSourceManager().metadataExtractor.objectChange = this;
    }

    @Override
    public int getItemViewType(int position) {
        int result = R.layout.recycler_item_sound;
        SoundItem item = items.get(position);
        switch (item.state) {
            case fiFile:
                if (item.getFullName().equals(activity.serviceLink.getCurrentPlayedSound())) {
                    result = R.layout.recycler_item_current_sound;
                }
                break;
            case fiRadioStation:
            case fiRadioFavoriteStation:
                if (item.location.equals(activity.serviceLink.getCurrentPlayedSound())) {
                    result = R.layout.recycler_item_current_sound;
                }
                break;
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

        SoundItem item = items.get(position);
        Drawable drawable = null;
        boolean playedFile = false;

        switch (item.state) {
            case fiFile:
                // запрос данных для отображения
                RTApplication.getSoundSourceManager().metadataExtractor.getMetadata(item, position);

                holder.crossImage.setImageDrawable(AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_cross));
                holder.crossImage.setVisibility(View.VISIBLE);

                if (item.getFullName().equals(activity.serviceLink.getCurrentPlayedSound())) {
                    playedFile = true;
                }

                if (playedFile)
                    drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_current_play);
                else
                    drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_play);

                if (item.metadataAcquired) {
                    switch (RTApplication.getPreferencesData().getTitleFileMode()) {
                        case 0:
                            holder.songNameTextView.setText(item.name);
                            break;
                        case 1:
                            holder.songNameTextView.setText(item.title);
                            break;
                    }

                    String str;
                    switch (RTApplication.getPreferencesData().getSubTitleFileMode()) {
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
                break;
            case fiDirectory:
                drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_folder);
                holder.songNameTextView.setText(item.name);
                holder.infoTextView.setText(item.location);
                holder.crossImage.setVisibility(View.INVISIBLE);
                break;
            case fiParentDirectory:
                drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_backfolder);
                holder.songNameTextView.setText(item.name);
                holder.infoTextView.setText(item.location);
                holder.crossImage.setVisibility(View.INVISIBLE);
                break;
            case fiRadioRoot:
                drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_internet_radio);
                holder.songNameTextView.setText(item.name);
                holder.infoTextView.setText(item.location);
                holder.crossImage.setVisibility(View.INVISIBLE);
                break;
            case fiRadioFavorites:
                drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_internet_radio);
                holder.songNameTextView.setText(item.name);
                holder.infoTextView.setText(item.location);
                holder.crossImage.setVisibility(View.INVISIBLE);
                break;
            case fiRadioCountry:
                drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_flag);
                holder.songNameTextView.setText(item.name);
                holder.infoTextView.setText(item.location);
                holder.crossImage.setVisibility(View.INVISIBLE);
                break;
            case fiRadioStation:
                if (item.location.equals(activity.serviceLink.getCurrentPlayedSound())) {
                    playedFile = true;
                }

                if (playedFile)
                    drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_current_play);
                else
                    drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_play);

                holder.songNameTextView.setText(item.name);
                holder.infoTextView.setText(item.location);

                if (item.checked)
                    holder.crossImage.setImageDrawable(AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_heart_on));
                else
                    holder.crossImage.setImageDrawable(AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_heart_off));
                holder.crossImage.setVisibility(View.VISIBLE);
                break;
            case fiRadioStationParentDirectory:
            case fiRadioParentDirectory:
                drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_backfolder);
                holder.songNameTextView.setText(item.name);
                holder.infoTextView.setText(item.location);
                holder.crossImage.setVisibility(View.INVISIBLE);
                break;
            case fiRadioFavoriteStation:
                if (item.location.equals(activity.serviceLink.getCurrentPlayedSound())) {
                    playedFile = true;
                }

                if (playedFile)
                    drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_current_play);
                else
                    drawable = AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_play);

                holder.songNameTextView.setText(item.name);
                holder.infoTextView.setText(item.location);

                holder.crossImage.setImageDrawable(AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_cross));
                holder.crossImage.setVisibility(View.VISIBLE);
                break;
        }

        holder.captionImage.setImageDrawable(drawable);

        float mainSize = RTApplication.getPreferencesData().getTextSize();
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
        SoundItem item;
        for (int i = 0; i < items.size(); ++i) {
            item = items.get(i);
            if (item.getFullName().equals(activity.serviceLink.getCurrentPlayedSound())) {
                result = i;
                break;
            }
        }

        return result;
    }

    public void update(List<SoundItem> items) {
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
                //RecyclerView.ViewHolder viewHolder = activity.adapter.recyclerView.findContainingViewHolder(v);
                SoundsRecyclerAdapter.ViewHolder viewHolder = (SoundsRecyclerAdapter.ViewHolder)activity.adapter.recyclerView.findContainingViewHolder(v);
                if (viewHolder != null) {
                    int itemPosition = viewHolder.getLayoutPosition();
                    List<SoundItem> fileList = RTApplication.getSoundSourceManager().getViewableList();
                    SoundItem item = fileList.get(itemPosition);

                    switch (item.state) {
                        case fiFile: // delete file
                            activity.showDeleteFileDialog(item.getFullName());
                            break;
                        case fiRadioStation: // favorites
                            item.checked = !item.checked;
                            RTApplication.getSoundSourceManager().changeFavoriteList(item);
                            if (item.checked)
                                viewHolder.crossImage.setImageDrawable(AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_heart_on));
                            else
                                viewHolder.crossImage.setImageDrawable(AppCompatResources.getDrawable(RTApplication.getContext(), R.drawable.ic_heart_off));
                            break;
                        case fiRadioFavoriteStation: // remove from favorites
                            RTApplication.getSoundSourceManager().changeFavoriteList(item);
                            update(RTApplication.getSoundSourceManager().getViewableList());
                            break;
                    }
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

