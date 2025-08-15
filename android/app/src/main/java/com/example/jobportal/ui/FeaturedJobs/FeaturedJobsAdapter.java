package com.example.jobportal.ui.FeaturedJobs;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.jobportal.BuildConfig;
import com.example.jobportal.R;
import com.example.jobportal.models.FeaturedJob;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FeaturedJobsAdapter extends RecyclerView.Adapter<FeaturedJobsAdapter.FeaturedJobViewHolder> {
    
    private List<FeaturedJob> jobs = new ArrayList<>();
    private final OnJobClickListener listener;
    
    public interface OnJobClickListener {
        /**
         * Called when a featured job is clicked
         * @param job The featured job that was clicked
         * @param position The position of the job in the adapter
         */
        void onJobClick(FeaturedJob job, int position);
    }
    
    public FeaturedJobsAdapter(OnJobClickListener listener) {
        this.listener = listener;
    }
    
    public void submitList(List<FeaturedJob> jobs) {
        this.jobs = jobs != null ? jobs : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public FeaturedJobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_featured_job, parent, false);
        return new FeaturedJobViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull FeaturedJobViewHolder holder, int position) {
        holder.bind(jobs.get(position), position);
    }
    
    @Override
    public int getItemCount() {
        return jobs.size();
    }
    
    // Currency formatting helper method
    private String formatSalaryWithCurrency(String salaryText) {
        if (salaryText == null || salaryText.isEmpty()) {
            return "$0";
        }
        
        // Remove any existing currency symbols or formatting
        String cleanSalary = salaryText.replaceAll("[^\\d.,]|^[.,]", "").trim();
        
        // If empty after cleaning, return default
        if (cleanSalary.isEmpty()) {
            return "$0";
        }
        
        // Handle different formats - K notation, ranges, etc.
        if (cleanSalary.contains("-")) {
            // Handle salary ranges
            String[] parts = cleanSalary.split("-");
            if (parts.length == 2) {
                String start = parts[0].trim();
                String end = parts[1].trim();
                return "$" + start + " - $" + end;
            }
        } else if (cleanSalary.toLowerCase().contains("k")) {
            // Handle K notation (thousands)
            return "$" + cleanSalary;
        }
        
        // Default formatting with dollar sign
        return "$" + cleanSalary;
    }
    
    class FeaturedJobViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final ImageView companyLogo;
        private final TextView jobTitle;
        private final TextView companyName;
        private final TextView location;
        private final TextView salary;
        private final TextView jobType;
        private final TextView postedDate;
        
        public FeaturedJobViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.featuredJobCard);
            companyLogo = itemView.findViewById(R.id.companyLogo);
            jobTitle = itemView.findViewById(R.id.jobTitle);
            companyName = itemView.findViewById(R.id.companyName);
            location = itemView.findViewById(R.id.location);
            salary = itemView.findViewById(R.id.salary);
            jobType = itemView.findViewById(R.id.jobType);
            postedDate = itemView.findViewById(R.id.postedDate);
        }
        
        void bind(FeaturedJob job, int position) {
            jobTitle.setText(job.getJobTitle());
            companyName.setText(job.getCompanyName());
            location.setText(job.getLocation());
            
            // Format salary with currency symbol
            String formattedSalary = formatSalaryWithCurrency(job.getSalary());
            salary.setText(formattedSalary);
            
            jobType.setText(job.getJobType());
            
            // Set posted date
            String formattedDate = "Posted " + getRelativeDateString(job.getPostedDate());
            postedDate.setText(formattedDate);
            
            // Load company logo from URL
            if (job.getCompanyLogo() != null && !TextUtils.isEmpty(job.getCompanyLogo())) {
                setCompanyLogo(job.getCompanyLogo());
            } else {
                // Fallback to drawable based on company name if no URL is available
                setCompanyLogoFallback(job.getCompanyName());
            }
            
            // Set click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onJobClick(job, position);
                }
            });
        }
        
        private void setCompanyLogo(String logoUrl) {
            // Load image from URL using Glide
            try {
                String baseUrl = "https://emps.co.in/";
                
                // Check if URL is already absolute or relative
                String fullUrl = logoUrl;
                if (!logoUrl.startsWith("http")) {
                    // If it's a relative URL, append to base URL
                    if (logoUrl.startsWith("/")) {
                        fullUrl = baseUrl + logoUrl.substring(1);
                    } else {
                        fullUrl = baseUrl + logoUrl;
                    }
                }
                
                Log.d("FeaturedJobsAdapter", "Loading logo from URL: " + fullUrl);
                
                RequestOptions options = new RequestOptions()
                        .placeholder(R.drawable.ic_company)
                        .error(R.drawable.ic_company)
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                
                Glide.with(companyLogo.getContext())
                        .load(fullUrl)
                        .apply(options)
                        .into(companyLogo);
            } catch (Exception e) {
                Log.e("FeaturedJobsAdapter", "Error loading logo: " + e.getMessage());
                companyLogo.setImageResource(R.drawable.ic_company);
            }
        }
        
        private void setCompanyLogoFallback(String company) {
            // Set logo based on company name - fallback method
            switch (company.toLowerCase()) {
                case "google":
                    companyLogo.setImageResource(R.drawable.logo_google);
                    break;
                case "spotify":
                    companyLogo.setImageResource(R.drawable.logo_spotify);
                    break;
                case "adobe":
                    companyLogo.setImageResource(R.drawable.logo_adobe);
                    break;
                case "airbnb":
                    companyLogo.setImageResource(R.drawable.logo_airbnb);
                    break;
                case "netflix":
                    companyLogo.setImageResource(R.drawable.logo_netflix);
                    break;
                default:
                    companyLogo.setImageResource(R.drawable.ic_company);
                    break;
            }
        }
        
        private String getRelativeDateString(String dateString) {
            if (dateString == null || dateString.isEmpty()) {
                return "recently";
            }

            try {
                Date postedDate = null;
                Date currentDate = new Date();
                
                // Try parsing with ISO 8601 format first (with microseconds)
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
                    postedDate = sdf.parse(dateString);
                } catch (ParseException e1) {
                    // Fallback: try without microseconds
                    try {
                        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                        postedDate = sdf2.parse(dateString);
                    } catch (ParseException e2) {
                        // Fallback: try the old format
                        SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        postedDate = sdf3.parse(dateString);
                    }
                }

                // Calculate the difference in milliseconds
                long diffInMillis = currentDate.getTime() - postedDate.getTime();
                long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
                long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
                long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);

                // Return a human-readable string
                if (diffInDays > 30) {
                    // If more than a month, show the actual date
                    SimpleDateFormat monthFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
                    return monthFormat.format(postedDate);
                } else if (diffInDays > 0) {
                    return diffInDays + (diffInDays == 1 ? " day ago" : " days ago");
                } else if (diffInHours > 0) {
                    return diffInHours + (diffInHours == 1 ? " hour ago" : " hours ago");
                } else if (diffInMinutes > 0) {
                    return diffInMinutes + (diffInMinutes == 1 ? " minute ago" : " minutes ago");
                } else {
                    return "just now";
                }
            } catch (ParseException e) {
                Log.e("FeaturedJobsAdapter", "Error parsing date: " + e.getMessage());
                return "recently"; // Fallback
            }
        }
    }
}
