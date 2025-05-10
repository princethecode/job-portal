package com.example.jobportal.ui.notifications;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobportal.R;
import com.example.jobportal.models.Notification;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<Notification> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.titleTextView.setText(notification.getTitle());
        holder.messageTextView.setText(notification.getDescription());
        holder.dateTextView.setText(notification.getDate());
        
        // Apply visual styling based on read status
        CardView cardView = (CardView) holder.itemView;
        
        if (notification.isRead()) {
            // Read notification styling
            holder.titleTextView.setTypeface(null, Typeface.NORMAL);
            holder.titleTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
            cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.background_tertiary));
            // Reduce elevation for read notifications
            cardView.setCardElevation(2f);
            // Hide unread indicator
            holder.indicatorView.setVisibility(View.GONE);
        } else {
            // Unread notification styling
            holder.titleTextView.setTypeface(null, Typeface.BOLD);
            holder.titleTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
            cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.background_card));
            // Increase elevation for unread notifications
            cardView.setCardElevation(8f);
            // Show unread indicator
            holder.indicatorView.setVisibility(View.VISIBLE);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
                
                // Immediately update UI to show as read when clicked
                if (!notification.isRead()) {
                    holder.titleTextView.setTypeface(null, Typeface.NORMAL);
                    holder.titleTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
                    cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.background_tertiary));
                    cardView.setCardElevation(2f);
                    holder.indicatorView.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications == null ? 0 : notifications.size();
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView messageTextView;
        TextView dateTextView;
        View indicatorView;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.notification_title);
            messageTextView = itemView.findViewById(R.id.notification_message);
            dateTextView = itemView.findViewById(R.id.notification_date);
            indicatorView = itemView.findViewById(R.id.notification_indicator);
        }
    }
} 