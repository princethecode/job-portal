package com.example.jobportal.ui.applications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobportal.R;
import com.example.jobportal.models.Application;
import java.util.ArrayList;
import java.util.List;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ApplicationViewHolder> {
    private List<Application> applications = new ArrayList<>();
    private OnApplicationClickListener listener;

    public interface OnApplicationClickListener {
        void onApplicationClick(Application application);
    }

    public ApplicationAdapter(OnApplicationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_application, parent, false);
        return new ApplicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicationViewHolder holder, int position) {
        Application application = applications.get(position);
        holder.jobTitleTextView.setText(application.getJobTitle());
        holder.companyTextView.setText(application.getCompany());
        holder.statusTextView.setText(application.getStatus());
        holder.dateTextView.setText(application.getApplicationDate());
        
        // Set status background based on status
        if (application.isAccepted()) {
            holder.statusTextView.setBackgroundResource(R.drawable.status_accepted_background);
        } else if (application.isRejected()) {
            holder.statusTextView.setBackgroundResource(R.drawable.status_rejected_background);
        } else if (application.isShortlisted()) {
            holder.statusTextView.setBackgroundResource(R.drawable.status_shortlisted_background);
        } else if (application.isReviewing()) {
            holder.statusTextView.setBackgroundResource(R.drawable.status_reviewing_background);
        } else {
            holder.statusTextView.setBackgroundResource(R.drawable.status_pending_background);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onApplicationClick(application);
            }
        });
    }

    @Override
    public int getItemCount() {
        return applications == null ? 0 : applications.size();
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
        notifyDataSetChanged();
    }

    static class ApplicationViewHolder extends RecyclerView.ViewHolder {
        TextView jobTitleTextView;
        TextView companyTextView;
        TextView statusTextView;
        TextView dateTextView;

        ApplicationViewHolder(@NonNull View itemView) {
            super(itemView);
            jobTitleTextView = itemView.findViewById(R.id.application_job_title);
            companyTextView = itemView.findViewById(R.id.application_company);
            statusTextView = itemView.findViewById(R.id.status);
            dateTextView = itemView.findViewById(R.id.applied_date);
        }
    }
} 