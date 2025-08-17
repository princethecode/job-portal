package com.example.jobportal.ui.jobs;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobportal.R;
import com.example.jobportal.models.Job;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class    JobAdapter extends ListAdapter<Job, JobAdapter.JobViewHolder> {
    
    private final OnJobClickListener listener;
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
    private final SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    
    public interface OnJobClickListener {
        void onJobClick(Job job);
        void onJobDetailsClick(Job job);
        void onWhatsAppShareClick(Job job);
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
    
    class JobViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView companyTextView;
        private final TextView locationTextView;
        private final TextView salaryTextView;
        private final TextView postingDateTextView;
        private final TextView shareCountTextView;
        private final LinearLayout whatsappShareButton;
        private final Button jobDetailsButton;
        
        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.job_title);
            companyTextView = itemView.findViewById(R.id.job_company);
            locationTextView = itemView.findViewById(R.id.job_location);
            salaryTextView = itemView.findViewById(R.id.job_salary);
            postingDateTextView = itemView.findViewById(R.id.job_posting_date);
            shareCountTextView = itemView.findViewById(R.id.tv_share_count);
            whatsappShareButton = itemView.findViewById(R.id.btn_whatsapp_share);
            jobDetailsButton = itemView.findViewById(R.id.btn_job_details);
        }
        
        public void bind(Job job, OnJobClickListener listener) {
            titleTextView.setText(job.getTitle());
            companyTextView.setText(job.getCompany());
            locationTextView.setText(job.getLocation());
            salaryTextView.setText(job.getSalary());
            
            // Format and display posting date
            String postingDate = job.getPostingDate();
            if (postingDate != null && !postingDate.isEmpty()) {
                try {
                    Date date = inputFormat.parse(postingDate);
                    if (date != null) {
                        String formattedDate = outputFormat.format(date);
                        postingDateTextView.setText("Posted on: " + formattedDate);
                    } else {
                        postingDateTextView.setText("Posted on: " + postingDate);
                    }
                } catch (ParseException e) {
                    postingDateTextView.setText("Posted on: " + postingDate);
                }
            } else {
                postingDateTextView.setText("Posted on: N/A");
            }
            
            // Set share count from job data
            shareCountTextView.setText(String.valueOf(job.getShareCount()));
            
            // Set click listeners for buttons
            whatsappShareButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onWhatsAppShareClick(job);
                }
            });
            
            jobDetailsButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onJobDetailsClick(job);
                }
            });
            
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
            return oldItem.getId().equals(newItem.getId());
        }
        
        @Override
        public boolean areContentsTheSame(@NonNull Job oldItem, @NonNull Job newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                   oldItem.getCompany().equals(newItem.getCompany()) &&
                   oldItem.getLocation().equals(newItem.getLocation()) &&
                   oldItem.getSalary().equals(newItem.getSalary()) &&
                   oldItem.getPostingDate().equals(newItem.getPostingDate());
        }
    }
}