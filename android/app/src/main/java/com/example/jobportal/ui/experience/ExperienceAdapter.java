package com.example.jobportal.ui.experience;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobportal.R;
import com.example.jobportal.models.Experience;

public class ExperienceAdapter extends ListAdapter<Experience, ExperienceAdapter.ExperienceViewHolder> {

    private final OnExperienceClickListener listener;

    public interface OnExperienceClickListener {
        void onExperienceClick(Experience experience);
        void onEditClick(Experience experience);
    }

    public ExperienceAdapter(OnExperienceClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Experience> DIFF_CALLBACK = new DiffUtil.ItemCallback<Experience>() {
        @Override
        public boolean areItemsTheSame(@NonNull Experience oldItem, @NonNull Experience newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Experience oldItem, @NonNull Experience newItem) {
            return oldItem.getJobTitle().equals(newItem.getJobTitle()) &&
                   oldItem.getCompanyName().equals(newItem.getCompanyName()) &&
                   oldItem.getStartDate().equals(newItem.getStartDate()) &&
                   oldItem.getEndDate().equals(newItem.getEndDate()) &&
                   oldItem.getDescription().equals(newItem.getDescription()) &&
                   oldItem.isCurrent() == newItem.isCurrent();
        }
    };

    @NonNull
    @Override
    public ExperienceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_experience, parent, false);
        return new ExperienceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExperienceViewHolder holder, int position) {
        Experience currentExperience = getItem(position);
        holder.bind(currentExperience, listener);
    }

    static class ExperienceViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvJobTitle;
        private final TextView tvCompanyName;
        private final TextView tvDateRange;
        private final TextView tvDescription;
        private final ImageButton btnEdit;

        public ExperienceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvCompanyName = itemView.findViewById(R.id.tvCompanyName);
            tvDateRange = itemView.findViewById(R.id.tvDateRange);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnEdit = itemView.findViewById(R.id.btnEditExperience);
        }

        public void bind(final Experience experience, final OnExperienceClickListener listener) {
            tvJobTitle.setText(experience.getJobTitle());
            tvCompanyName.setText(experience.getCompanyName());
            
            // Format date range
            String dateRange;
            if (experience.isCurrent()) {
                dateRange = experience.getStartDate() + " - Present";
            } else {
                dateRange = experience.getStartDate() + " - " + experience.getEndDate();
            }
            tvDateRange.setText(dateRange);
            
            tvDescription.setText(experience.getDescription());

            // Set click listeners
            itemView.setOnClickListener(v -> listener.onExperienceClick(experience));
            btnEdit.setOnClickListener(v -> listener.onEditClick(experience));
        }
    }
}
