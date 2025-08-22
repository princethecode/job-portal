package com.example.jobportal.recruiter.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobportal.R;
import com.example.jobportal.models.Application;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ApplicationsAdapter extends RecyclerView.Adapter<ApplicationsAdapter.ApplicationViewHolder> {
    
    private List<Application> applications;
    private OnApplicationClickListener listener;
    
    public interface OnApplicationClickListener {
        void onApplicationClick(Application application);
    }
    
    public ApplicationsAdapter() {
        this.applications = new ArrayList<>();
    }
    
    public void setApplications(List<Application> applications) {
        this.applications = applications != null ? applications : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setOnApplicationClickListener(OnApplicationClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recruiter_application, parent, false);
        return new ApplicationViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ApplicationViewHolder holder, int position) {
        Application application = applications.get(position);
        holder.bind(application);
    }
    
    @Override
    public int getItemCount() {
        return applications.size();
    }
    
    class ApplicationViewHolder extends RecyclerView.ViewHolder {
        private TextView tvApplicantName;
        private TextView tvJobTitle;
        private TextView tvApplicationDate;
        private TextView tvStatus;
        private TextView tvCompany;
        
        public ApplicationViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvApplicantName = itemView.findViewById(R.id.tv_applicant_name);
            tvJobTitle = itemView.findViewById(R.id.tv_job_title);
            tvApplicationDate = itemView.findViewById(R.id.tv_application_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvCompany = itemView.findViewById(R.id.tv_company);
            
            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onApplicationClick(applications.get(getAdapterPosition()));
                }
            });
        }
        
        public void bind(Application application) {
            // Set applicant name - use getName() method from User model
            if (application.getUser() != null) {
                tvApplicantName.setText(application.getUser().getName());
            } else {
                tvApplicantName.setText("Unknown Applicant");
            }
            
            // Set job title with debugging
            String jobTitle = application.getJobTitle();
            if (jobTitle != null && !jobTitle.isEmpty() && !"Unknown Job".equals(jobTitle)) {
                tvJobTitle.setText(jobTitle);
            } else {
                // Debug: Check if job object exists
                if (application.getJob() != null) {
                    tvJobTitle.setText("Job ID: " + application.getJob().getId());
                } else {
                    tvJobTitle.setText("No job data");
                }
            }
            
            // Set company name with debugging
            String company = application.getCompany();
            if (company != null && !company.isEmpty() && !"Unknown Company".equals(company)) {
                tvCompany.setText(company);
            } else {
                // Debug: Check what company data is available
                if (application.getJob() != null) {
                    String debugCompany = "Company: " + application.getJob().getCompany() + 
                                         ", CompanyName: " + application.getJob().getCompanyName();
                    tvCompany.setText(debugCompany);
                } else {
                    tvCompany.setText("No company data");
                }
            }
            
            // Set status
            tvStatus.setText(application.getStatus());
            
            // Set status color based on status
            setStatusColor(application.getStatus());
            
            // Set formatted application date
            tvApplicationDate.setText(formatDate(application.getApplicationDate()));
        }
        
        private void setStatusColor(String status) {
            int colorRes;
            if (status == null) {
                colorRes = R.color.gray;
            } else {
                switch (status.toLowerCase()) {
                    case "applied":
                        colorRes = R.color.orange;
                        break;
                    case "shortlisted":
                        colorRes = R.color.blue;
                        break;
                    case "accepted":
                    case "selected":
                        colorRes = R.color.green;
                        break;
                    case "rejected":
                        colorRes = R.color.red;
                        break;
                    default:
                        colorRes = R.color.gray;
                        break;
                }
            }
            
            tvStatus.setTextColor(itemView.getContext().getResources().getColor(colorRes, null));
        }
        
        private String formatDate(String dateStr) {
            if (dateStr == null || dateStr.isEmpty()) {
                return "Unknown date";
            }
            
            try {
                // Parse the ISO date format from server
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                
                Date date = inputFormat.parse(dateStr);
                return date != null ? outputFormat.format(date) : dateStr;
            } catch (ParseException e) {
                // If parsing fails, try to extract just the date part
                if (dateStr.contains(" ")) {
                    return dateStr.split(" ")[0];
                }
                return dateStr;
            }
        }
    }
}