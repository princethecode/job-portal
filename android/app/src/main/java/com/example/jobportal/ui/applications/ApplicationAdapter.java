package com.example.jobportal.ui.applications;

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
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ApplicationViewHolder> {
    private List<Application> applications = new ArrayList<>();
    private OnApplicationClickListener listener;
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

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

        // Format and display the applied date, fallback to today's date if missing
        String appliedDate = application.getApplicationDate();
        if (appliedDate != null && !appliedDate.isEmpty() && !appliedDate.equals("null")) {
            try {
                Date date = inputFormat.parse(appliedDate);
                if (date != null) {
                    String formattedDate = outputFormat.format(date);
                    holder.dateTextView.setText(formattedDate);
                } else {
                    holder.dateTextView.setText(appliedDate);
                }
            } catch (ParseException e) {
                holder.dateTextView.setText(appliedDate);
            }
        } else {
            // Use today's date as fallback
            String today = outputFormat.format(new Date());
            holder.dateTextView.setText(today);
        }

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

        // View Details button: navigate to job details fragment
        holder.viewDetailsButton.setOnClickListener(v -> {
            // Use Fragment navigation to show job details
            androidx.fragment.app.FragmentActivity activity = (androidx.fragment.app.FragmentActivity) v.getContext();
            androidx.fragment.app.FragmentManager fragmentManager = activity.getSupportFragmentManager();
            String jobId = "";
if (application.getJob() != null && application.getJob().getId() != null) {
    String rawId = application.getJob().getId();
    // Remove decimal part if present (e.g., '43.0' -> '43')
    if (rawId.matches("\\d+\\.0")) {
        jobId = rawId.substring(0, rawId.indexOf('.'));
    } else {
        jobId = rawId;
    }
}
com.example.jobportal.ui.jobdetails.JobDetailsFragment fragment = com.example.jobportal.ui.jobdetails.JobDetailsFragment.newInstance(jobId);
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Withdraw button: show toast
        holder.withdrawButton.setOnClickListener(v -> {
            android.widget.Toast.makeText(v.getContext(), "Your application has been Withdraw", android.widget.Toast.LENGTH_SHORT).show();
        });

        // (Optional) Still allow whole card click for old behavior
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
    com.google.android.material.button.MaterialButton viewDetailsButton;
    com.google.android.material.button.MaterialButton withdrawButton;

    ApplicationViewHolder(@NonNull View itemView) {
        super(itemView);
        jobTitleTextView = itemView.findViewById(R.id.application_job_title);
        companyTextView = itemView.findViewById(R.id.application_company);
        statusTextView = itemView.findViewById(R.id.status);
        dateTextView = itemView.findViewById(R.id.applied_date);
        viewDetailsButton = itemView.findViewById(R.id.view_details_button);
        withdrawButton = itemView.findViewById(R.id.withdraw_button);
    }
}
} 