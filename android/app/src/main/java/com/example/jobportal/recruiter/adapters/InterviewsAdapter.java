package com.example.jobportal.recruiter.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobportal.R;
import com.example.jobportal.models.InterviewResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InterviewsAdapter extends RecyclerView.Adapter<InterviewsAdapter.InterviewViewHolder> {
    
    private List<InterviewResponse.Interview> interviews;
    private OnInterviewClickListener listener;
    
    // Date formatters for parsing and displaying dates
    private final SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
    private final SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private final SimpleDateFormat outputTimeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    
    public interface OnInterviewClickListener {
        void onInterviewClick(InterviewResponse.Interview interview);
    }
    
    public InterviewsAdapter(List<InterviewResponse.Interview> interviews, OnInterviewClickListener listener) {
        this.interviews = interviews;
        this.listener = listener;
    }
    
    public void updateInterviews(List<InterviewResponse.Interview> newInterviews) {
        this.interviews = newInterviews;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public InterviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_recruiter_interview, parent, false);
        return new InterviewViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull InterviewViewHolder holder, int position) {
        InterviewResponse.Interview interview = interviews.get(position);
        holder.bind(interview);
    }
    
    @Override
    public int getItemCount() {
        return interviews != null ? interviews.size() : 0;
    }
    
    class InterviewViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCandidateName, tvJobTitle, tvCompany, tvInterviewDate;
        private TextView tvInterviewTime, tvStatus, tvInterviewType, tvLocation;
        
        public InterviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCandidateName = itemView.findViewById(R.id.tv_candidate_name);
            tvJobTitle = itemView.findViewById(R.id.tv_job_title);
            tvCompany = itemView.findViewById(R.id.tv_company);
            tvInterviewDate = itemView.findViewById(R.id.tv_interview_date);
            tvInterviewTime = itemView.findViewById(R.id.tv_interview_time);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvInterviewType = itemView.findViewById(R.id.tv_interview_type);
            tvLocation = itemView.findViewById(R.id.tv_location);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onInterviewClick(interviews.get(position));
                    }
                }
            });
        }
        
        public void bind(InterviewResponse.Interview interview) {
            // Set candidate name
            if (interview.getUser() != null) {
                tvCandidateName.setText(interview.getUser().getName());
            } else {
                tvCandidateName.setText("Unknown Candidate");
            }
            
            // Set job details
            if (interview.getJob() != null) {
                tvJobTitle.setText(interview.getJob().getTitle());
                tvCompany.setText(interview.getJob().getCompany());
            } else {
                tvJobTitle.setText("Unknown Job");
                tvCompany.setText("Unknown Company");
            }
            
            // Format and set interview date
            String formattedDate = formatDate(interview.getInterviewDate());
            tvInterviewDate.setText(formattedDate);
            
            // Format and set interview time
            String formattedTime = formatTime(interview.getInterviewTime());
            tvInterviewTime.setText(formattedTime);
            
            // Set status with color
            String status = interview.getStatus();
            tvStatus.setText(capitalizeFirst(status));
            
            // Set status color based on value
            int statusColor;
            switch (status.toLowerCase()) {
                case "scheduled":
                    statusColor = itemView.getContext().getResources().getColor(android.R.color.holo_blue_dark);
                    break;
                case "completed":
                    statusColor = itemView.getContext().getResources().getColor(android.R.color.holo_green_dark);
                    break;
                case "cancelled":
                    statusColor = itemView.getContext().getResources().getColor(android.R.color.holo_red_dark);
                    break;
                default:
                    statusColor = itemView.getContext().getResources().getColor(android.R.color.darker_gray);
            }
            tvStatus.setTextColor(statusColor);
            
            // Set interview type
            tvInterviewType.setText(capitalizeFirst(interview.getInterviewType()));
            
            // Set location
            String location = interview.getLocation();
            if (location != null && !location.isEmpty()) {
                tvLocation.setText(location);
                tvLocation.setVisibility(View.VISIBLE);
            } else if ("online".equalsIgnoreCase(interview.getInterviewType())) {
                tvLocation.setText("Online Meeting");
                tvLocation.setVisibility(View.VISIBLE);
            } else {
                tvLocation.setVisibility(View.GONE);
            }
        }
        
        private String formatDate(String dateString) {
            if (dateString == null || dateString.isEmpty()) {
                return "Date TBD";
            }
            
            try {
                Date date = inputDateFormat.parse(dateString);
                return outputDateFormat.format(date);
            } catch (ParseException e) {
                return dateString; // Return original if parsing fails
            }
        }
        
        private String formatTime(String timeString) {
            if (timeString == null || timeString.isEmpty()) {
                return "Time TBD";
            }
            
            try {
                Date time = inputDateFormat.parse(timeString);
                return outputTimeFormat.format(time);
            } catch (ParseException e) {
                return timeString; // Return original if parsing fails
            }
        }
        
        private String capitalizeFirst(String text) {
            if (text == null || text.isEmpty()) {
                return text;
            }
            return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
        }
    }
}