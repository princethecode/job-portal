package com.example.jobportal.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.jobportal.R;
import com.example.jobportal.models.Notification;

import java.util.List;

public class NotificationsFragment extends Fragment implements NotificationAdapter.OnNotificationClickListener {

    private NotificationsViewModel viewModel;
    private NotificationAdapter adapter;
    private RecyclerView recyclerView;
    private TextView textNotifications;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        
        // Setup UI components
        textNotifications = root.findViewById(R.id.text_notifications);
        recyclerView = root.findViewById(R.id.notifications_recycler_view);
        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh_layout);
        emptyView = root.findViewById(R.id.empty_view);
        
        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.loadNotifications();
        });
        
        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(this);
        recyclerView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Observe notifications
        viewModel.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
            updateUI(notifications);
        });
        
        // Observe loading state
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            swipeRefreshLayout.setRefreshing(isLoading);
        });
        
        // Observe errors
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Update UI based on notification data
     */
    private void updateUI(List<Notification> notifications) {
        adapter.setNotifications(notifications);
        
        if (notifications == null || notifications.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            textNotifications.setText("Notifications");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            textNotifications.setText("Notifications (" + notifications.size() + ")");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
                .get(NotificationsViewModel.class);
    }

    @Override
    public void onNotificationClick(Notification notification) {
        // Mark notification as read
        viewModel.markNotificationAsRead(notification.getId());
        
        // You could also navigate to a detail screen or show more info
        Toast.makeText(getContext(), notification.getTitle() + "\n" + notification.getDescription(), Toast.LENGTH_LONG).show();
    }
} 