<?php

namespace App\Services;

use Kreait\Firebase\Factory;
use Kreait\Firebase\Messaging\CloudMessage;
use Kreait\Firebase\Messaging\Notification;
use Kreait\Firebase\Messaging\AndroidConfig;
use Kreait\Firebase\Messaging\ApnsConfig;
use Kreait\Firebase\Exception\MessagingException;
use Illuminate\Support\Facades\Log;

class FirebaseNotificationService
{
    protected $messaging;

    public function __construct()
    {
        $this->messaging = (new Factory)
            ->withServiceAccount(storage_path('app/firebase/firebase-adminsdk.json'))
            ->createMessaging();
    }

    /**
     * Send a notification to a specific user
     *
     * @param string $fcmToken
     * @param string $title
     * @param string $body
     * @param array $data
     * @return bool
     */
    public function sendNotification($fcmToken, $title, $body, $data = [])
    {
        try {
            $message = CloudMessage::withTarget('token', $fcmToken)
                ->withNotification(Notification::create($title, $body))
                ->withData($data)
                ->withAndroidConfig(AndroidConfig::fromArray([
                    'priority' => 'high',
                    'notification' => [
                        'sound' => 'default',
                        'click_action' => 'FLUTTER_NOTIFICATION_CLICK',
                    ],
                ]))
                ->withApnsConfig(ApnsConfig::fromArray([
                    'headers' => [
                        'apns-priority' => '10',
                    ],
                    'payload' => [
                        'aps' => [
                            'sound' => 'default',
                        ],
                    ],
                ]));

            $this->messaging->send($message);
            return true;
        } catch (MessagingException $e) {
            Log::error('Firebase notification error: ' . $e->getMessage());
            return false;
        }
    }

    /**
     * Send a notification to multiple users
     *
     * @param array $fcmTokens
     * @param string $title
     * @param string $body
     * @param array $data
     * @return array
     */
    public function sendMulticastNotification($fcmTokens, $title, $body, $data = [])
    {
        $results = [];
        foreach ($fcmTokens as $token) {
            $results[$token] = $this->sendNotification($token, $title, $body, $data);
        }
        return $results;
    }

    /**
     * Send a job application notification
     *
     * @param string $fcmToken
     * @param string $jobTitle
     * @param int $applicationId
     * @return bool
     */
    public function sendJobApplicationNotification($fcmToken, $jobTitle, $applicationId)
    {
        return $this->sendNotification(
            $fcmToken,
            'New Job Application',
            "You have received a new application for {$jobTitle}",
            [
                'type' => 'job_application',
                'application_id' => $applicationId,
                'job_title' => $jobTitle
            ]
        );
    }

    /**
     * Send an application status update notification
     *
     * @param string $fcmToken
     * @param string $jobTitle
     * @param string $status
     * @param int $applicationId
     * @return bool
     */
    public function sendApplicationStatusNotification($fcmToken, $jobTitle, $status, $applicationId)
    {
        return $this->sendNotification(
            $fcmToken,
            'Application Status Updated',
            "Your application for {$jobTitle} has been {$status}",
            [
                'type' => 'application_status',
                'application_id' => $applicationId,
                'status' => $status,
                'job_title' => $jobTitle
            ]
        );
    }

    /**
     * Send a new job notification
     *
     * @param string $fcmToken
     * @param string $jobTitle
     * @param int $jobId
     * @return bool
     */
    public function sendNewJobNotification($fcmToken, $jobTitle, $jobId)
    {
        return $this->sendNotification(
            $fcmToken,
            'New Job Available',
            "A new {$jobTitle} position is available",
            [
                'type' => 'new_job',
                'job_id' => $jobId,
                'job_title' => $jobTitle
            ]
        );
    }
} 