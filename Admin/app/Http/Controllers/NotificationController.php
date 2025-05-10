<?php

namespace App\Http\Controllers;

use App\Models\Notification;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class NotificationController extends Controller
{
    /**
     * Display a listing of the notifications.
     *
     * @return \Illuminate\View\View
     */
    public function index()
    {
        $notifications = Notification::orderBy('created_at', 'desc')->paginate(10);
        return view('notifications.index', compact('notifications'));
    }

    /**
     * Show the form for creating a new notification.
     *
     * @return \Illuminate\View\View
     */
    public function create()
    {
        return view('notifications.create');
    }

    /**
     * Store a newly created notification in storage.
     *
     * @param  \Illuminate\Http\Request  $request
     * @return \Illuminate\Http\RedirectResponse
     */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'title' => 'required|string|max:255',
            'description' => 'required|string',
            'is_global' => 'boolean',
        ]);

        if ($validator->fails()) {
            return redirect()->back()
                ->withErrors($validator)
                ->withInput();
        }

        $notification = new Notification();
        $notification->title = $request->title;
        $notification->description = $request->description;
        $notification->is_global = $request->has('is_global') ? true : false;
        $notification->save();

        // If it's a global notification, we don't need to associate with specific users
        if ($notification->is_global) {
            return redirect()->route('admin.notifications.index')
                ->with('success', 'Global notification created successfully!');
        }

        return redirect()->route('admin.notifications.index')
            ->with('success', 'Notification created successfully!');
    }

    /**
     * Display the specified notification.
     *
     * @param  int  $id
     * @return \Illuminate\View\View
     */
    public function show($id)
    {
        $notification = Notification::findOrFail($id);
        return view('notifications.show', compact('notification'));
    }

    /**
     * Show the form for editing the specified notification.
     *
     * @param  int  $id
     * @return \Illuminate\View\View
     */
    public function edit($id)
    {
        $notification = Notification::findOrFail($id);
        return view('notifications.edit', compact('notification'));
    }

    /**
     * Update the specified notification in storage.
     *
     * @param  \Illuminate\Http\Request  $request
     * @param  int  $id
     * @return \Illuminate\Http\RedirectResponse
     */
    public function update(Request $request, $id)
    {
        $validator = Validator::make($request->all(), [
            'title' => 'required|string|max:255',
            'description' => 'required|string',
            'is_global' => 'boolean',
        ]);

        if ($validator->fails()) {
            return redirect()->back()
                ->withErrors($validator)
                ->withInput();
        }

        $notification = Notification::findOrFail($id);
        $notification->title = $request->title;
        $notification->description = $request->description;
        $notification->is_global = $request->has('is_global') ? true : false;
        $notification->save();

        return redirect()->route('admin.notifications.index')
            ->with('success', 'Notification updated successfully!');
    }

    /**
     * Remove the specified notification from storage.
     *
     * @param  int  $id
     * @return \Illuminate\Http\RedirectResponse
     */
    public function destroy($id)
    {
        $notification = Notification::findOrFail($id);
        $notification->delete();

        return redirect()->route('admin.notifications.index')
            ->with('success', 'Notification deleted successfully!');
    }
} 