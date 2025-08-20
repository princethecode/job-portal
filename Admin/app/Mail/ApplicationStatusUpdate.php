<?php

namespace App\Mail;

use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Mail\Mailable;
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;
use Illuminate\Queue\SerializesModels;
use App\Models\Application;

class ApplicationStatusUpdate extends Mailable
{
    use Queueable, SerializesModels;

    public $application;
    public $oldStatus;

    /**
     * Create a new message instance.
     */
    public function __construct(Application $application, $oldStatus = null)
    {
        $this->application = $application;
        $this->oldStatus = $oldStatus;
    }

    /**
     * Get the message envelope.
     */
    public function envelope(): Envelope
    {
        return new Envelope(
            subject: 'Application Status Update - ' . $this->application->job->title,
        );
    }

    /**
     * Get the message content definition.
     */
    public function content(): Content
    {
        return new Content(
            view: 'emails.application-status-update',
            with: [
                'application' => $this->application,
                'candidate' => $this->application->user,
                'job' => $this->application->job,
                'recruiter' => $this->application->job->recruiter,
                'oldStatus' => $this->oldStatus,
            ]
        );
    }

    /**
     * Get the attachments for the message.
     *
     * @return array<int, \Illuminate\Mail\Mailables\Attachment>
     */
    public function attachments(): array
    {
        return [];
    }
}