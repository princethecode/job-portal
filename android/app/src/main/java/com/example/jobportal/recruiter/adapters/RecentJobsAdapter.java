package com.example.jobportal.recruiter.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobportal.R;
import com.example.jobportal.models.Job;
import java.util.List;

public class RecentJobsAdapter extends RecyclerView.Adapter<RecentJobsAdapter.JobViewHolder> {
    
    private List<Job> jobs;
    
    public RecentJobsAdapter(List<Job> jobs) {
        this.jobs = jobs;
    }
    
    public void updateJobs(List<Job> newJobs) {
        this.jobs = newJobs;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_recent_job, parent, false);
        return new JobViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobs.get(position);
        holder.bind(job);
    }
    
    @Override
    public int getItemCount() {
        return jobs != null ? jobs.size() : 0;
    }
    
    static class JobViewHolder extends RecyclerView.ViewHolder {
        private TextView tvJobTitle, tvCompany, tvLocation, tvJobType, tvStatus, tvApplicationsCount;
        
        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tv_job_title);
            tvCompany = itemView.findViewById(R.id.tv_company_name);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvJobType = itemView.findViewById(R.id.tv_job_type);
            tvStatus = itemView.findViewById(R.id.tv_job_status);
            tvApplicationsCount = itemView.findViewById(R.id.tv_applications_count);
        }
        
        public void bind(Job job) {
            tvJobTitle.setText(job.getTitle());
            tvCompany.setText(job.getCompanyName()); // Use getCompanyName() to match server's company_name field
            tvLocation.setText(job.getLocation());
            tvJobType.setText(job.getJobType());
            
            // Set status with color
            if (job.isActive()) {
                tvStatus.setText("Active");
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvStatus.setText("Inactive");
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            }
            
            // Show applications count if available
            if (job.getApplicationsCount() > 0) {
                tvApplicationsCount.setText(job.getApplicationsCount() + " applications");
                tvApplicationsCount.setVisibility(View.VISIBLE);
            } else {
                tvApplicationsCount.setVisibility(View.GONE);
            }
        }
    }
}
