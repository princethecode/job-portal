<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Log;

class FCMController extends Controller
{
    /**
     * Register FCM token for the authenticated user
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function registerToken(Request $request)
    {
        Log::info('FCM token registration attempt', [
            'user_id' => Auth::id(),
            'request_data' => $request->all(),
            'headers' => $request->headers->all()
        ]);

        $validator = Validator::make($request->all(), [
            'fcm_token' => 'required|string|min:1'
        ]);

        if ($validator->fails()) {
            Log::error('FCM token validation failed', [
                'errors' => $validator->errors()->toArray(),
                'request_data' => $request->all()
            ]);
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()->toArray()
            ], 422);
        }

        try {
            $user = Auth::user();
            if (!$user) {
                Log::error('User not authenticated during FCM token registration');
                return response()->json([
                    'success' => false,
                    'message' => 'User not authenticated',
                    'data' => null
                ], 401);
            }

            $token = $request->input('fcm_token');
            Log::info('Updating FCM token for user', [
                'user_id' => $user->id,
                'old_token' => $user->fcm_token,
                'new_token' => $token
            ]);

            $user->fcm_token = $token;
            $saved = $user->save();

            if (!$saved) {
                Log::error('Failed to save FCM token', [
                    'user_id' => $user->id,
                    'token' => $token
                ]);
                return response()->json([
                    'success' => false,
                    'message' => 'Failed to save FCM token',
                    'data' => null
                ], 500);
            }

            Log::info('FCM token registered successfully', [
                'user_id' => $user->id,
                'fcm_token' => $token
            ]);

            return response()->json([
                'success' => true,
                'message' => 'FCM token registered successfully',
                'data' => [
                    'user_id' => $user->id,
                    'fcm_token' => $token
                ]
            ]);
        } catch (\Exception $e) {
            Log::error('Failed to register FCM token', [
                'error' => $e->getMessage(),
                'trace' => $e->getTraceAsString(),
                'user_id' => Auth::id()
            ]);

            return response()->json([
                'success' => false,
                'message' => 'Failed to register FCM token: ' . $e->getMessage(),
                'data' => null
            ], 500);
        }
    }
} 