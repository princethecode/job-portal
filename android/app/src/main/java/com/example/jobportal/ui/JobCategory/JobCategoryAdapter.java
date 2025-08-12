package com.example.jobportal.ui.JobCategory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobportal.R;
import com.example.jobportal.models.JobCategory;

import java.util.List;


public class JobCategoryAdapter extends RecyclerView.Adapter<JobCategoryAdapter.CategoryViewHolder> {
    private final List<JobCategory> categories;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(JobCategory category);
    }

    public JobCategoryAdapter(List<JobCategory> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        JobCategory category = categories.get(position);
        holder.icon.setImageResource(category.getIconRes());
        holder.name.setText(category.getName());
        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.categoryIcon);
            name = itemView.findViewById(R.id.categoryName);
        }
    }
} 