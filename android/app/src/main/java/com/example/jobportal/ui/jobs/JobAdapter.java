package com.example.jobportal.ui.jobs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobportal.R;
import com.example.jobportal.models.Job;

import java.util.List;

public class JobAdapter extends ListAdapter<Job, JobAdapter.JobViewHolder> {
    
    private final OnJobClickListener listener;
    
    public interface OnJobClickListener {
        void onJobClick(Job job);
    }
    
    public JobAdapter(OnJobClickListener listener) {
        super(new JobDiffCallback());
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = getItem(position);
        holder.bind(job, listener);
    }
    
    static class JobViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView companyTextView;
        private final TextView locationTextView;
        private final TextView salaryTextView;
        
        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.job_title);
            companyTextView = itemView.findViewById(R.id.job_company);
            locationTextView = itemView.findViewById(R.id.job_location);
            salaryTextView = itemView.findViewById(R.id.job_salary);
        }
        
        public void bind(Job job, OnJobClickListener listener) {
            titleTextView.setText(job.getTitle());
            companyTextView.setText(job.getCompany());
            locationTextView.setText(job.getLocation());
            salaryTextView.setText(job.getSalary());
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onJobClick(job);
                }
            });
        }
    }
    
    static class JobDiffCallback extends DiffUtil.ItemCallback<Job> {
        @Override
        public boolean areItemsTheSame(@NonNull Job oldItem, @NonNull Job newItem) {
            return oldItem.getId() == newItem.getId();
        }
        
        @Override
        public boolean areContentsTheSame(@NonNull Job oldItem, @NonNull Job newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                   oldItem.getCompany().equals(newItem.getCompany()) &&
                   oldItem.getLocation().equals(newItem.getLocation()) &&
                   oldItem.getSalary().equals(newItem.getSalary());
        }
    }
}