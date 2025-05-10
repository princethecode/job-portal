<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use App\Models\Notification;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class NotificationController extends Controller
{
    /**
     * Get all notifications for the authenticated user.
     * 
     * @return \Illuminate\Http\JsonResponse
     */
    public function index()
    {
        $user = Auth::user();
        
        // Get global notifications and user-specific notifications
        $notifications = Notification::where('is_global', true)
            ->orWhere('user_id', $user->id)
            ->orderBy('created_at', 'desc')
            ->get();
            
        return response()->json([
            'success' => true,
            'message' => 'Notifications retrieved successfully',
            'data' => $notifications
        ]);
    }
    
    /**
     * Mark a notification as read.
     * 
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function markAsRead($id)
    {
        $user = Auth::user();
        
        $notification = Notification::findOrFail($id);
        
        // Check if notification is for this user or is global
        if (!$notification->is_global && $notification->user_id !== $user->id) {
            return response()->json([
                'success' => false,
                'message' => 'Unauthorized'
            ], 403);
        }
        
        $notification->is_read = true;
        $notification->save();
        
        return response()->json([
            'success' => true,
            'message' => 'Notification marked as read'
        ]);
    }
} 