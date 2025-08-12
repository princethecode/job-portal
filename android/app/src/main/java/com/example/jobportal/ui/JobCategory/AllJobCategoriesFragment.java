package com.example.jobportal.ui.JobCategory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobportal.R;
import com.example.jobportal.models.JobCategory;

import java.util.ArrayList;
import java.util.List;

public class AllJobCategoriesFragment extends Fragment implements JobCategoryAdapter.OnCategoryClickListener {

    private RecyclerView recyclerView;
    private JobCategoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_job_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        // Initialize RecyclerView with 3 columns per row
        recyclerView = view.findViewById(R.id.categoriesRecyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        
        // Add spacing between grid items
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(3, spacingInPixels, true));

        // Load and display all categories
        List<JobCategory> allCategories = getAllJobCategories();
        adapter = new JobCategoryAdapter(allCategories, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCategoryClick(JobCategory category) {
        // Show toast with selected category
        Toast.makeText(requireContext(), "Selected category: " + category.getName(), Toast.LENGTH_SHORT).show();
        
        // Create a bundle to pass the category to JobsFragment
        Bundle bundle = new Bundle();
        bundle.putString("category", category.getName().toLowerCase());
        
        // Create and navigate to JobsFragment with the category filter
        Fragment jobsFragment = new com.example.jobportal.ui.jobs.JobsFragment();
        jobsFragment.setArguments(bundle);
        
        requireActivity().getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, jobsFragment)
            .addToBackStack(null)
            .commit();
    }

    /**
     * Creates a comprehensive list of all job categories
     * @return List of all job categories
     */
    private List<JobCategory> getAllJobCategories() {
        List<JobCategory> categories = new ArrayList<>();
        
        // Add all categories (including those shown in the home fragment plus additional ones)
        categories.add(new JobCategory("Delivery", R.drawable.delivery_guy));
        categories.add(new JobCategory("Housekeeping", R.drawable.housekeeping));
        categories.add(new JobCategory("Welder", R.drawable.welder));
        categories.add(new JobCategory("Labor/Helper", R.drawable.labor));
        categories.add(new JobCategory("Carpenter", R.drawable.carpenter));
        categories.add(new JobCategory("Driver", R.drawable.driver));
        categories.add(new JobCategory("Mason", R.drawable.mason));
        categories.add(new JobCategory("Electrician", R.drawable.electrician));
        categories.add(new JobCategory("designer", R.drawable.designer));
        categories.add(new JobCategory("scaffolding", R.drawable.scaffolding));
        categories.add(new JobCategory("technician", R.drawable.technician));
        categories.add(new JobCategory("warehouse", R.drawable.warehouse));
        categories.add(new JobCategory("Wind blade", R.drawable.welder));
        
        return categories;
    }
}
