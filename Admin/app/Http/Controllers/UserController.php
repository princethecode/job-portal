<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\User;
use App\Models\Application;
use Illuminate\Support\Facades\Storage;

class UserController extends Controller
{
    /**
     * Display a listing of users
     *
     * @return \Illuminate\View\View
     */
    public function index()
    {
        try {
            $users = User::orderBy('created_at', 'desc')->paginate(10);
            return view('users.index', compact('users'));
        } catch (\Exception $e) {
            return view('users.index', [
                'error' => 'Unable to fetch users. ' . $e->getMessage()
            ]);
        }
    }

    /**
     * Display the specified user
     *
     * @param int $id
     * @return \Illuminate\View\View
     */
    public function show($id)
    {
        try {
            $user = User::findOrFail($id);
            $applications = Application::where('user_id', $id)->get();
            
            return view('users.show', [
                'user' => $user,
                'applications' => $applications
            ]);
        } catch (\Exception $e) {
            return redirect()->route('admin.users.index')
                ->with('error', 'User not found or error occurred.');
        }
    }

    /**
     * Update user status (active/inactive)
     *
     * @param Request $request
     * @param int $id
     * @return \Illuminate\Http\RedirectResponse
     */
    public function updateStatus(Request $request, $id)
    {
        $request->validate([
            'is_active' => 'required|boolean',
        ]);

        try {
            $user = User::findOrFail($id);
            $user->is_active = $request->is_active;
            $user->save();

            return redirect()->back()
                ->with('success', 'User status updated successfully');
        } catch (\Exception $e) {
            return redirect()->back()
                ->with('error', 'Failed to update user status.');
        }
    }

    /**
     * Remove the specified user
     *
     * @param int $id
     * @return \Illuminate\Http\RedirectResponse
     */
    public function destroy($id)
    {
        try {
            $user = User::findOrFail($id);
            
            // Delete user's resume if exists
            if ($user->resume_path) {
                Storage::disk('public')->delete($user->resume_path);
            }
            
            $user->delete();

            return redirect()->route('admin.users.index')
                ->with('success', 'User deleted successfully');
        } catch (\Exception $e) {
            return redirect()->route('admin.users.index')
                ->with('error', 'Failed to delete user.');
        }
    }
}
