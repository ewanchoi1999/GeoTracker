package com.choi.geotracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewAdapter Class
 */
public class WorkoutViewAdapter extends ListAdapter<TrackData, WorkoutViewAdapter.WorkoutHolder> {
    private onItemClick itemClick;

    protected WorkoutViewAdapter() {
        super(DIFF_CALLBACK);
    }

    /**
     * Checks if items and contents in the workout are the same
     */
    private static final DiffUtil.ItemCallback<TrackData> DIFF_CALLBACK = new DiffUtil.ItemCallback<TrackData>() {
        @Override
        public boolean areItemsTheSame(@NonNull TrackData oldItem, @NonNull TrackData newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull TrackData oldItem, @NonNull TrackData newItem) {
            return oldItem.getDate().equals(newItem.getDate()) && oldItem.getDistance().equals(newItem.getDistance()) &&
                    oldItem.getWorkoutType().equals(newItem.getWorkoutType()) && oldItem.getAvgPace().equals(newItem.getAvgPace()) &&
                    oldItem.getTimeTaken().equals(newItem.getWorkoutType());
        }
    };

    /**
     * ViewHolder inflater
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return
     */
    @NonNull
    @Override
    public WorkoutHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);
        return new WorkoutHolder(itemView);
    }

    /**
     * Binds viewholder
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull WorkoutHolder holder, int position) {
        TrackData data = getItem(position);
        holder.trackDateTextView.setText(data.getDate());
        holder.trackDistanceTextView.setText(data.getDistance());
        holder.trackPaceTextView.setText(data.getAvgPace());
        holder.trackTimeTextView.setText(data.getTimeTaken());
        holder.trackTypeTextView.setText(data.getWorkoutType());
    }

    /**
     * Gets item position at users selection
     * @param position
     * @return
     */
    public TrackData getItemAtPosition(int position) {
        return getItem(position);
    }

    /**
     * Viewholder Class
     */
    class WorkoutHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView trackDateTextView;
        private TextView trackDistanceTextView;
        private TextView trackTimeTextView;
        private TextView trackPaceTextView;
        private TextView trackTypeTextView;

        public WorkoutHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            trackDateTextView = itemView.findViewById(R.id.trackDate);
            trackDistanceTextView = itemView.findViewById(R.id.trackDistance);
            trackTimeTextView = itemView.findViewById(R.id.trackTime);
            trackPaceTextView = itemView.findViewById(R.id.trackPace);
            trackTypeTextView = itemView.findViewById(R.id.trackType);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(itemClick != null && position != RecyclerView.NO_POSITION) {
                        itemClick.onWorkoutItemClick(getItem(position));
                    }
                }
            });
        }
    }

    /**
     * Handles item clicks
     */
    public interface onItemClick {
        void onWorkoutItemClick(TrackData data);
    }

    public void setOnItemClickListener(onItemClick itemClick) {
        this.itemClick = itemClick;
    }
}
