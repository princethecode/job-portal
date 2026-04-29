package com.emps.abroadjobs.recruiter.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.emps.abroadjobs.R;
import com.emps.abroadjobs.models.Job;
import com.emps.abroadjobs.network.ApiClient;
import com.emps.abroadjobs.network.ApiResponse;
import com.emps.abroadjobs.recruiter.JobEditActivity;
import com.emps.abroadjobs.recruiter.RecruiterJobDetailsActivity;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecentJobsAdapter extends RecyclerView.Adapter<RecentJobsAdapter.JobViewHolder> {
    
    private List<Job> jobs;
    private Context context;
    private OnJobActionListener actionListener;
    
    public interface OnJobActionListener {
        void onJobUpdated();
    }
    
    public RecentJobsAdapter(List<Job> jobs) {
        this.jobs = jobs;
    }
    
    public void setOnJobActionListener(OnJobActionListener listener) {
        this.actionListener = listener;
    }
    
    public void updateJobs(List<Job> newJobs) {
        this.jobs = newJobs;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
            .inflate(R.layout.item_recent_job_enhanced, parent, false);
        return new JobViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobs.get(position);
        holder.bind(job, context, actionListener);
    }
    
    @Override
    public int getItemCount() {
        return jobs != null ? jobs.size() : 0;
    }
    
    static class JobViewHolder extends RecyclerView.ViewHolder {
        private TextView tvJobTitle, tvCompany, tvLocation, tvJobType, tvStatus, tvApplicationsCount, tvSalary, tvApprovalStatus;
        private MaterialButton btnEdit, btnToggleStatus, btnViewDetails;
        
        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tv_job_title);
            tvCompany = itemView.findViewById(R.id.tv_company_name);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvJobType = itemView.findViewById(R.id.tv_job_type);
            tvStatus = itemView.findViewById(R.id.tv_job_status);
            tvApplicationsCount = itemView.findViewById(R.id.tv_applications_count);
            tvSalary = itemView.findViewById(R.id.tv_salary);
            tvApprovalStatus = itemView.findViewById(R.id.tv_approval_status);
            
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnToggleStatus = itemView.findViewById(R.id.btn_toggle_status);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
        }
        
        public void bind(Job job, Context context, OnJobActionListener actionListener) {
            tvJobTitle.setText(job.getTitle());
            tvCompany.setText(job.getCompanyName()); // Use getCompanyName() to match server's company_name field
            tvLocation.setText(job.getLocation());
            tvJobType.setText(job.getJobType());
            
            // Dynamic salary display
            String salary = job.getSalary();
            if (salary != null && !salary.isEmpty() && !salary.equalsIgnoreCase("null")) {
                tvSalary.setText(formatSalary(salary));
                tvSalary.setVisibility(View.VISIBLE);
            } else {
                tvSalary.setText("Salary not specified");
                tvSalary.setVisibility(View.VISIBLE);
            }
            
            // Set status with color
            if (job.isActive()) {
                tvStatus.setText("Active");
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                btnToggleStatus.setText("Deactivate");
                btnToggleStatus.setIconResource(R.drawable.ic_pause);
            } else {
                tvStatus.setText("Inactive");
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                btnToggleStatus.setText("Activate");
                btnToggleStatus.setIconResource(R.drawable.ic_play_arrow);
            }
            
            // Show approval status
            String approvalStatus = job.getApprovalStatus();
            if (approvalStatus != null) {
                switch (approvalStatus.toLowerCase()) {
                    case "approved":
                        tvApprovalStatus.setText("Approved");
                        tvApprovalStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                        tvApprovalStatus.setVisibility(View.VISIBLE);
                        break;
                    case "pending":
                        tvApprovalStatus.setText("Pending Approval");
                        tvApprovalStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                        tvApprovalStatus.setVisibility(View.VISIBLE);
                        break;
                    case "declined":
                        tvApprovalStatus.setText("Declined");
                        tvApprovalStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                        tvApprovalStatus.setVisibility(View.VISIBLE);
                        break;
                    default:
                        tvApprovalStatus.setVisibility(View.GONE);
                        break;
                }
            } else {
                tvApprovalStatus.setVisibility(View.GONE);
            }
            
            // Show applications count if available
            if (job.getApplicationsCount() > 0) {
                tvApplicationsCount.setText(job.getApplicationsCount() + " applications");
                tvApplicationsCount.setVisibility(View.VISIBLE);
            } else {
                tvApplicationsCount.setText("No applications yet");
                tvApplicationsCount.setVisibility(View.VISIBLE);
            }
            
            // Set up button click listeners
            btnViewDetails.setOnClickListener(v -> {
                Intent intent = new Intent(context, RecruiterJobDetailsActivity.class);
                intent.putExtra(RecruiterJobDetailsActivity.EXTRA_JOB_ID, Integer.parseInt(job.getId()));
                intent.putExtra(RecruiterJobDetailsActivity.EXTRA_JOB_TITLE, job.getTitle());
                context.startActivity(intent);
            });
            
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(context, JobEditActivity.class);
                intent.putExtra(JobEditActivity.EXTRA_JOB_ID, Integer.parseInt(job.getId()));
                intent.putExtra(JobEditActivity.EXTRA_JOB_DATA, job);
                context.startActivity(intent);
            });
            
            btnToggleStatus.setOnClickListener(v -> {
                showStatusChangeConfirmation(job, context, actionListener);
            });
        }
        
        private void showStatusChangeConfirmation(Job job, Context context, OnJobActionListener actionListener) {
            String action = job.isActive() ? "deactivate" : "activate";
            String message = job.isActive() ? 
                "Deactivating this job will stop it from receiving new applications." :
                "Activating this job will allow it to receive new applications.";
            
            new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Confirm Action")
                .setMessage(message)
                .setPositiveButton(action.substring(0, 1).toUpperCase() + action.substring(1), (dialog, which) -> {
                    toggleJobStatus(job, context, actionListener);
                })
                .setNegativeButton("Cancel", null)
                .show();
        }
        
        private void toggleJobStatus(Job job, Context context, OnJobActionListener actionListener) {
            btnToggleStatus.setEnabled(false);
            
            Call<ApiResponse<Void>> call;
            if (job.isActive()) {
                call = ApiClient.getRecruiterApiService().deactivateJob(Integer.parseInt(job.getId()));
            } else {
                call = ApiClient.getRecruiterApiService().activateJob(Integer.parseInt(job.getId()));
            }
            
            call.enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                    btnToggleStatus.setEnabled(true);
                    
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        String action = job.isActive() ? "deactivated" : "activated";
                        Toast.makeText(context, "Job " + action + " successfully", Toast.LENGTH_SHORT).show();
                        
                        // Update the job status locally
                        job.setActive(!job.isActive());
                        
                        // Update UI
                        if (job.isActive()) {
                            tvStatus.setText("Active");
                            tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                            btnToggleStatus.setText("Deactivate");
                            btnToggleStatus.setIconResource(R.drawable.ic_pause);
                        } else {
                            tvStatus.setText("Inactive");
                            tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                            btnToggleStatus.setText("Activate");
                            btnToggleStatus.setIconResource(R.drawable.ic_play_arrow);
                        }
                        
                        // Notify listener if available
                        if (actionListener != null) {
                            actionListener.onJobUpdated();
                        }
                    } else {
                        Toast.makeText(context, "Failed to update job status", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                    btnToggleStatus.setEnabled(true);
                    Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        private String formatSalary(String salary) {
            if (salary == null || salary.isEmpty() || salary.equalsIgnoreCase("null")) {
                return "Not specified";
            }
            
            // Remove any existing currency symbols and clean up
            String cleanSalary = salary.replaceAll("[^\\d.,k-]", "").trim();
            
            if (cleanSalary.isEmpty()) {
                return "Not specified";
            }
            
            // Handle different salary formats
            if (cleanSalary.toLowerCase().contains("k")) {
                return "$" + cleanSalary.toUpperCase();
            } else if (cleanSalary.contains("-")) {
                return "$" + cleanSalary;
            } else {
                // Try to parse as number and format
                try {
                    double amount = Double.parseDouble(cleanSalary.replace(",", ""));
                    if (amount >= 1000) {
                        return String.format("$%.0fK", amount / 1000);
                    } else {
                        return String.format("$%.0f", amount);
                    }
                } catch (NumberFormatException e) {
                    return "$" + cleanSalary;
                }
            }
        }
    }
}